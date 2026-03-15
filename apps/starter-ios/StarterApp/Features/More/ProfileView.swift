import PhotosUI
import SwiftUI
import UIKit

struct ProfileView: View {
    @Environment(AppState.self) var appState
    @Environment(ToastManager.self) var toastManager
    private let userService = UserService()
    private let notificationService = NotificationService()
    private let billingService = BillingService()
    private let mediaService = MediaService()
    private let pushRegistrationManager = PushRegistrationManager()
    @State private var displayName: String = ""
    @State private var isEditing = false
    @State private var showDeleteAccountAlert = false
    @State private var isSaving = false
    @State private var notificationPreferences: NotificationPreferences?
    @State private var entitlement: EntitlementState?
    @State private var mediaAssets: [MediaAsset] = []
    @State private var isLoadingSettings = false
    @State private var isUploadingMedia = false
    @State private var selectedPhoto: PhotosPickerItem?

    var body: some View {
        NavigationStack {
            List {
                // Avatar + name section
                Section {
                    HStack(spacing: AppTokens.Spacing.md) {
                        // Avatar circle with initials
                        ZStack {
                            Circle()
                                .fill(AppTokens.Color.primary)
                                .frame(width: 64, height: 64)
                            Text(initials)
                                .font(.title2.bold())
                                .foregroundStyle(.white)
                        }
                        VStack(alignment: .leading, spacing: 4) {
                            if isEditing {
                                TextField("Display name", text: $displayName)
                                    .font(.headline)
                                    .foregroundStyle(AppTokens.Color.textPrimary)
                            } else {
                                Text(displayName.isEmpty ? "Set your name" : displayName)
                                    .font(.headline)
                                    .foregroundStyle(
                                        displayName.isEmpty
                                            ? AppTokens.Color.textSecondary
                                            : AppTokens.Color.textPrimary
                                    )
                            }
                            Text(appState.currentUser?.email ?? "")
                                .font(.subheadline)
                                .foregroundStyle(AppTokens.Color.textSecondary)
                        }
                    }
                    .padding(.vertical, AppTokens.Spacing.sm)
                }

                Section("Notifications") {
                    Toggle(
                        "Push notifications",
                        isOn: Binding(
                            get: { notificationPreferences?.pushEnabled ?? false },
                            set: { value in Task { await updateNotificationSetting(pushEnabled: value) } }
                        )
                    )

                    Toggle(
                        "Email notifications",
                        isOn: Binding(
                            get: { notificationPreferences?.emailEnabled ?? false },
                            set: { value in Task { await updateNotificationSetting(emailEnabled: value) } }
                        )
                    )

                    if let notificationPreferences {
                        LabeledContent("Categories") {
                            Text(notificationPreferences.enabledCategories?.joined(separator: ", ") ?? "None")
                        }
                        LabeledContent("Quiet hours") {
                            Text(
                                notificationPreferences.quietHoursEnabled == true
                                    ? "\(notificationPreferences.quietHoursStart ?? "--") - \(notificationPreferences.quietHoursEnd ?? "--")"
                                    : "Off"
                            )
                        }
                    } else if isLoadingSettings {
                        ProgressView()
                    }
                }

                Section("Billing") {
                    if let entitlement {
                        LabeledContent("Tier") {
                            Text(entitlement.tier)
                        }
                        LabeledContent("Source") {
                            Text(entitlement.source)
                        }
                        LabeledContent("Features") {
                            Text(entitlement.features.joined(separator: ", "))
                                .multilineTextAlignment(.trailing)
                        }
                    } else if isLoadingSettings {
                        ProgressView()
                    }
                }

                Section("Media") {
                    PhotosPicker(
                        selection: $selectedPhoto,
                        matching: .images,
                        photoLibrary: .shared()
                    ) {
                        Text(isUploadingMedia ? "Uploading avatar…" : "Choose avatar photo")
                    }
                    .disabled(isUploadingMedia)

                    if mediaAssets.isEmpty, isLoadingSettings {
                        ProgressView()
                    } else {
                        ForEach(mediaAssets, id: \.id) { asset in
                            VStack(alignment: .leading, spacing: 4) {
                                Text(asset.fileName ?? asset.kind)
                                Text(asset.publicUrl ?? asset.storageKey)
                                    .font(.footnote)
                                    .foregroundStyle(AppTokens.Color.textSecondary)
                            }
                        }
                    }
                }

                // Account section
                Section("Account") {
                    Button("Sign out") {
                        HapticsHelper.impact(.medium)
                        Task {
                            await appState.logout()
                        }
                    }
                    .foregroundStyle(AppTokens.Color.warning)

                    Button("Delete account", role: .destructive) {
                        showDeleteAccountAlert = true
                    }
                }
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if isEditing {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Save") {
                            Task { await saveName() }
                        }
                        .disabled(isSaving)
                    }
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button("Cancel") {
                            isEditing = false
                            displayName = appState.currentUser?.displayName ?? ""
                        }
                    }
                } else {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Edit") { isEditing = true }
                    }
                }
            }
            .alert("Delete account?", isPresented: $showDeleteAccountAlert) {
                Button("Delete", role: .destructive) {
                    // TODO: call DELETE /user/me API then logout
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("This permanently deletes your account and all data. This cannot be undone.")
            }
            .onAppear {
                displayName = appState.currentUser?.displayName ?? ""
                Task { await loadAccountDetails() }
            }
            .onChange(of: selectedPhoto) { _, item in
                guard let item else { return }
                Task {
                    await uploadSelectedPhoto(item)
                    selectedPhoto = nil
                }
            }
        }
    }

    private var initials: String {
        let name = displayName.isEmpty ? appState.currentUser?.email ?? "" : displayName
        return name.split(separator: " ").prefix(2).compactMap { $0.first }.map(String.init).joined().uppercased()
    }

    private func saveName() async {
        isSaving = true
        defer { isSaving = false }

        do {
            let profile = try await userService.updateMe(displayName: displayName)
            appState.currentUser = AppUser(profile: profile)
            toastManager.show("Name updated", style: .success)
            isEditing = false
        } catch {
            toastManager.show(error.localizedDescription, style: .error)
        }
    }

    private func loadAccountDetails() async {
        guard !isLoadingSettings else { return }
        isLoadingSettings = true
        defer { isLoadingSettings = false }

        async let notificationTask = notificationService.fetchPreferences()
        async let billingTask = billingService.fetchEntitlements()
        async let mediaTask = mediaService.listAssets()

        do {
            notificationPreferences = try await notificationTask
            entitlement = try await billingTask
            mediaAssets = try await mediaTask
        } catch {
            toastManager.show(error.localizedDescription, style: .error)
        }
    }

    private func updateNotificationSetting(
        pushEnabled: Bool? = nil,
        emailEnabled: Bool? = nil
    ) async {
        do {
            notificationPreferences = try await notificationService.updatePreferences(
                pushEnabled: pushEnabled,
                emailEnabled: emailEnabled
            )

            if ProcessInfo.processInfo.arguments.contains("-UITestMode") ||
                ProcessInfo.processInfo.arguments.contains("-RuntimeFixtureMode") ||
                ProcessInfo.processInfo.environment["APP_RUNTIME_FIXTURE_MODE"] == "1" {
                if pushEnabled == true {
                    _ = try? await pushRegistrationManager.registerCurrentDevice(
                        pushToken: "fixture-ios-push-token"
                    )
                } else if pushEnabled == false {
                    try? await pushRegistrationManager.revokeCurrentDevice(
                        pushToken: "fixture-ios-push-token"
                    )
                }
            }

            toastManager.show("Preferences updated", style: .success)
        } catch {
            toastManager.show(error.localizedDescription, style: .error)
        }
    }

    private func uploadSelectedPhoto(_ item: PhotosPickerItem) async {
        guard !isUploadingMedia else { return }
        isUploadingMedia = true
        defer { isUploadingMedia = false }

        do {
            guard let data = try await item.loadTransferable(type: Data.self) else {
                toastManager.show("Could not load the selected photo", style: .error)
                return
            }
            let image = UIImage(data: data)
            let fileName = item.itemIdentifier.map { "\($0).jpeg" } ?? "profile-photo.jpeg"
            let preparation = try await mediaService.prepareUpload(
                kind: "avatar",
                mimeType: item.supportedContentTypes.first?.preferredMIMEType ?? "image/jpeg",
                fileName: fileName,
                sizeBytes: data.count,
                visibility: "public"
            )

            _ = try await mediaService.completeUpload(
                assetId: preparation.assetId,
                width: image.map { Int($0.size.width) },
                height: image.map { Int($0.size.height) }
            )
            mediaAssets = try await mediaService.listAssets()
            toastManager.show("Media asset synced", style: .success)
        } catch {
            toastManager.show(error.localizedDescription, style: .error)
        }
    }
}

#if os(iOS)
import BackgroundTasks
import Foundation

final class BackgroundSyncService {
    static let taskIdentifier = "com.appstarterkit.sync.background"

    private weak var syncEngine: SyncEngine?

    init(syncEngine: SyncEngine) {
        self.syncEngine = syncEngine
    }

    func registerTask() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: BackgroundSyncService.taskIdentifier,
            using: nil
        ) { [weak self] task in
            self?.handleSync(task: task as! BGAppRefreshTask)
        }
    }

    func scheduleNextSync() {
        let request = BGAppRefreshTaskRequest(identifier: BackgroundSyncService.taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        try? BGTaskScheduler.shared.submit(request)
    }

    private func handleSync(task: BGAppRefreshTask) {
        scheduleNextSync()
        let syncTask = Task { [weak self] in
            await self?.syncEngine?.sync()
            task.setTaskCompleted(success: true)
        }
        task.expirationHandler = {
            syncTask.cancel()
            task.setTaskCompleted(success: false)
        }
    }
}
#endif

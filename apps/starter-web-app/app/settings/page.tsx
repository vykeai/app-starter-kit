import { currentUser } from "../../lib/account";
import { currentNetworkContext } from "../../lib/api";
import { currentEntitlement } from "../../lib/billing";
import { currentNotificationPreferences } from "../../lib/notifications";

export default function SettingsPage() {
  return (
    <main className="app-shell settings-shell">
      <header className="settings-header">
        <div>
          <p className="eyebrow">Settings</p>
          <h1>Shared account, notification, and billing rules should feel first-class on web too.</h1>
        </div>
        <div className="header-card">
          <span>Session</span>
          <strong>{currentUser.loginMethod.replace("_", " ")}</strong>
          <small>{currentNetworkContext.correlationId}</small>
        </div>
      </header>

      <section className="settings-grid">
        <article className="inline-panel tall-panel">
          <span className="eyebrow">Account</span>
          <strong>{currentUser.displayName}</strong>
          <p>{currentUser.email}</p>
          <dl className="detail-list">
            <div>
              <dt>Theme</dt>
              <dd>{currentUser.preferences.theme}</dd>
            </div>
            <div>
              <dt>Request id</dt>
              <dd>{currentNetworkContext.requestId}</dd>
            </div>
            <div>
              <dt>Refresh</dt>
              <dd>{currentNetworkContext.refreshStrategy.replaceAll("_", " ")}</dd>
            </div>
          </dl>
        </article>

        <article className="inline-panel tall-panel">
          <span className="eyebrow">Notifications</span>
          <strong>
            {currentNotificationPreferences.pushEnabled ? "Push enabled" : "Push muted"}
          </strong>
          <p>
            {currentNotificationPreferences.emailEnabled ? "Email enabled" : "Email muted"}
          </p>
          <dl className="detail-list">
            <div>
              <dt>Categories</dt>
              <dd>{currentNotificationPreferences.enabledCategories.join(", ")}</dd>
            </div>
            <div>
              <dt>Quiet hours</dt>
              <dd>
                {currentNotificationPreferences.quietHoursEnabled
                  ? `${currentNotificationPreferences.quietHoursStart} - ${currentNotificationPreferences.quietHoursEnd}`
                  : "Off"}
              </dd>
            </div>
            <div>
              <dt>Urgent bypass</dt>
              <dd>{currentNotificationPreferences.urgentBreaksQuietHours ? "On" : "Off"}</dd>
            </div>
          </dl>
        </article>

        <article className="inline-panel tall-panel">
          <span className="eyebrow">Billing</span>
          <strong>{currentEntitlement.tier}</strong>
          <p>{currentEntitlement.source} purchase source</p>
          <dl className="detail-list">
            <div>
              <dt>Renews</dt>
              <dd>{currentEntitlement.renewsAt ?? "Never"}</dd>
            </div>
            <div>
              <dt>Features</dt>
              <dd>{currentEntitlement.features.join(", ")}</dd>
            </div>
          </dl>
        </article>
      </section>
    </main>
  );
}

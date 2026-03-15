import { SettingsConsole } from "../../components/settings-console";
import { currentUser } from "../../lib/account";
import { currentNetworkContext } from "../../lib/api";

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
      <SettingsConsole />
    </main>
  );
}

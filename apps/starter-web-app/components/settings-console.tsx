"use client";

import { useState } from "react";
import { currentUser } from "../lib/account";
import { currentEntitlement } from "../lib/billing";
import { currentNetworkContext } from "../lib/api";
import {
  currentNotificationPreferences,
  updateNotificationPreferences,
  type WebNotificationPreferences,
} from "../lib/notifications";
import { currentSession, refreshSession, type WebSession } from "../lib/session";

const CATEGORY_OPTIONS = ["activity", "transactional", "marketing"] as const;

function toggleCategory(
  current: WebNotificationPreferences,
  category: (typeof CATEGORY_OPTIONS)[number],
): WebNotificationPreferences {
  const nextCategories = current.enabledCategories.includes(category)
    ? current.enabledCategories.filter((value) => value !== category)
    : [...current.enabledCategories, category];

  return updateNotificationPreferences(current, {
    enabledCategories: nextCategories,
  });
}

export function SettingsConsole() {
  const [session, setSession] = useState<WebSession>(currentSession);
  const [preferences, setPreferences] = useState<WebNotificationPreferences>(
    currentNotificationPreferences,
  );

  const liveNetworkContext = {
    ...currentNetworkContext,
    requestId: session.requestId,
    correlationId: session.correlationId,
    authMode: session.authMode,
  };

  return (
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
            <dd>{liveNetworkContext.requestId}</dd>
          </div>
          <div>
            <dt>Refresh</dt>
            <dd>{liveNetworkContext.refreshStrategy.replaceAll("_", " ")}</dd>
          </div>
          <div>
            <dt>Access token</dt>
            <dd>{session.accessTokenState}</dd>
          </div>
        </dl>
        <button
          className="panel-action"
          type="button"
          onClick={() => setSession((value) => refreshSession(value))}
        >
          Simulate refresh rotation
        </button>
      </article>

      <article className="inline-panel tall-panel">
        <span className="eyebrow">Notifications</span>
        <strong>{preferences.pushEnabled ? "Push enabled" : "Push muted"}</strong>
        <p>{preferences.emailEnabled ? "Email enabled" : "Email muted"}</p>
        <div className="settings-controls">
          <label className="toggle-row">
            <span>Push notifications</span>
            <input
              checked={preferences.pushEnabled}
              type="checkbox"
              onChange={(event) =>
                setPreferences((value) =>
                  updateNotificationPreferences(value, { pushEnabled: event.target.checked }),
                )
              }
            />
          </label>
          <label className="toggle-row">
            <span>Email notifications</span>
            <input
              checked={preferences.emailEnabled}
              type="checkbox"
              onChange={(event) =>
                setPreferences((value) =>
                  updateNotificationPreferences(value, { emailEnabled: event.target.checked }),
                )
              }
            />
          </label>
          <label className="toggle-row">
            <span>Quiet hours</span>
            <input
              checked={preferences.quietHoursEnabled}
              type="checkbox"
              onChange={(event) =>
                setPreferences((value) =>
                  updateNotificationPreferences(value, {
                    quietHoursEnabled: event.target.checked,
                  }),
                )
              }
            />
          </label>
        </div>
        <div className="chip-row">
          {CATEGORY_OPTIONS.map((category) => (
            <button
              key={category}
              className={
                preferences.enabledCategories.includes(category)
                  ? "setting-chip selected"
                  : "setting-chip"
              }
              type="button"
              onClick={() => setPreferences((value) => toggleCategory(value, category))}
            >
              {category}
            </button>
          ))}
        </div>
        <dl className="detail-list">
          <div>
            <dt>Quiet hours</dt>
            <dd>
              {preferences.quietHoursEnabled
                ? `${preferences.quietHoursStart} - ${preferences.quietHoursEnd}`
                : "Off"}
            </dd>
          </div>
          <div>
            <dt>Urgent bypass</dt>
            <dd>{preferences.urgentBreaksQuietHours ? "On" : "Off"}</dd>
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
          <div>
            <dt>Correlation</dt>
            <dd>{liveNetworkContext.correlationId}</dd>
          </div>
        </dl>
      </article>
    </section>
  );
}

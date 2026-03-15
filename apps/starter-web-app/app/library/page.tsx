import { currentEntitlement } from "../../lib/billing";
import { currentUser } from "../../lib/account";
import { libraryShelves } from "../../lib/mock-library";

export default function LibraryPage() {
  return (
    <main className="app-shell library-shell">
      <header className="library-header">
        <div>
          <p className="eyebrow">Library</p>
          <h1>Prose-first, session-aware, and ready for auth wiring.</h1>
        </div>
        <div className="header-card">
          <span>Signed in</span>
          <strong>{currentUser.displayName}</strong>
          <small>{currentUser.email}</small>
        </div>
        <div className="header-card">
          <span>Entitlement</span>
          <strong>{currentEntitlement.tier}</strong>
          <small>{currentEntitlement.features.length} starter features</small>
        </div>
      </header>

      <section className="shelf-grid">
        {libraryShelves.map((shelf) => (
          <article key={shelf.title} className="shelf-card">
            <h2>{shelf.title}</h2>
            <ul>
              {shelf.items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </article>
        ))}
      </section>

      <section className="account-strip">
        <article className="inline-panel">
          <span className="eyebrow">Preferences</span>
          <strong>{currentUser.preferences.theme} theme</strong>
          <p>{currentUser.preferences.enabledCategories.join(", ")}</p>
        </article>
        <article className="inline-panel">
          <span className="eyebrow">Billing</span>
          <strong>{currentEntitlement.source} source</strong>
          <p>Renews {currentEntitlement.renewsAt ?? "never"}</p>
        </article>
      </section>

      <section className="route-strip">
        <a className="route-pill" href="/settings">
          Open account settings
        </a>
      </section>
    </main>
  );
}

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
          <span>Current mode</span>
          <strong>Fixture-backed</strong>
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
    </main>
  );
}

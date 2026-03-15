import { currentNetworkContext } from "../../lib/api";

export default function LoginPage() {
  return (
    <main className="app-shell auth-shell">
      <section className="auth-panel">
        <p className="eyebrow">Starter Web App</p>
        <h1>Keep the authenticated web surface aligned with native, not second-class.</h1>
        <p className="lede">
          This starter is the browser companion for the product itself: auth, library,
          account flows, and web-native product work.
        </p>
        <form className="auth-form">
          <label>
            Email
            <input type="email" placeholder="you@example.com" />
          </label>
          <button type="submit">Send magic link</button>
        </form>
        <div className="auth-meta">
          <span>Auth mode: {currentNetworkContext.authMode.replace("_", " ")}</span>
          <span>Request: {currentNetworkContext.requestId}</span>
          <span>Correlation: {currentNetworkContext.correlationId}</span>
          <span>Refresh: {currentNetworkContext.refreshStrategy.replaceAll("_", " ")}</span>
        </div>
        <a className="mock-link" href="/library">
          Continue in mock mode
        </a>
      </section>
    </main>
  );
}

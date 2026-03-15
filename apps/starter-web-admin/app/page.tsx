import { adminMetrics, adminQueues } from "../lib/mock-data";

export default function HomePage() {
  return (
    <main className="admin-shell">
      <section className="admin-hero">
        <div>
          <p className="eyebrow">Onlystack Admin</p>
          <h1>Operate the product without improvising dashboards in prod.</h1>
          <p className="lede">
            This starter gives you the skeleton for moderation, queue health,
            launch metrics, and intervention tooling.
          </p>
        </div>
        <div className="status-card">
          <span>Environment</span>
          <strong>Mock / staging-ready</strong>
          <small>Wire this to auth guards, role checks, and live analytics.</small>
        </div>
      </section>

      <section className="metric-grid">
        {adminMetrics.map((metric) => (
          <article key={metric.label} className="metric-card">
            <span>{metric.label}</span>
            <strong>{metric.value}</strong>
            <small>{metric.delta}</small>
          </article>
        ))}
      </section>

      <section className="queue-panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">Operations</p>
            <h2>Queues and intervention points</h2>
          </div>
          <button type="button">Connect live data</button>
        </div>
        <div className="queue-list">
          {adminQueues.map((queue) => (
            <article key={queue.name} className="queue-card">
              <div>
                <strong>{queue.name}</strong>
                <p>{queue.health}</p>
              </div>
              <span>{queue.backlog} waiting</span>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}

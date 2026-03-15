# Web Starter Strategy

`Onlystack` now carries three separate web starter surfaces because the source repos
show three genuinely different needs:

- `starter-web-public`: public marketing, docs, legal, campaign, app-link fallback
- `starter-web-admin`: operations, moderation, dashboards, internal tools
- `starter-web-app`: authenticated end-user product surface

## Pattern extraction

### Public web

Best signal came from:

- `fitkind/web-public`
- `sitches/web-public`

Those repos both treat public web as a lower-cost static-first surface. That is the
right default for `Onlystack`, so `starter-web-public` uses `Astro`.

### Admin

Best signal came from:

- `univiirse/web-admin`
- `goala/admin`

`univiirse` is the stronger template because the admin surface is closer to a real
product shell than the lighter Vite admin in `goala`. So `starter-web-admin` uses
`Next.js`.

### Authenticated web app

Best signal came from:

- `univiirse/vivii/web-app`
- `sitches/web-app`

Those surfaces are application shells, not marketing pages. They need auth, routing,
state, and product navigation. So `starter-web-app` also uses `Next.js`.

## Resulting stack

- `apps/starter-web-public`: `Astro`
- `apps/starter-web-admin`: `Next.js`
- `apps/starter-web-app`: `Next.js`

This is intentionally not one framework for everything. Public web and product web
have different cost and runtime profiles.

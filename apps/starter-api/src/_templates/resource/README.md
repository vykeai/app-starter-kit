# Resource Template

Copy this folder to `src/your-domain/` then:
1. Replace `Note` → `YourEntity` (PascalCase)
2. Replace `note` → `yourEntity` (camelCase)
3. Add your fields to the Prisma schema
4. Run `npx prisma migrate dev --name add_your_entity`
5. Import YourModule in app.module.ts

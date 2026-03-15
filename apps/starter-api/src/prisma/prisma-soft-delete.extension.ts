import { Prisma } from '@prisma/client';

// Prisma extension that automatically filters soft-deleted records
export const softDeleteExtension = Prisma.defineExtension({
  name: 'softDelete',
  query: {
    $allModels: {
      async findMany({ args, query }: { args: any; query: (args: any) => Promise<any> }) {
        args.where = { ...args.where, deletedAt: null };
        return query(args);
      },
      async findFirst({ args, query }: { args: any; query: (args: any) => Promise<any> }) {
        args.where = { ...args.where, deletedAt: null };
        return query(args);
      },
      async findUnique({ args, query }: { args: any; query: (args: any) => Promise<any> }) {
        return query(args); // findUnique doesn't filter — use findFirst for soft-delete-aware lookup
      },
      async count({ args, query }: { args: any; query: (args: any) => Promise<any> }) {
        args.where = { ...args.where, deletedAt: null };
        return query(args);
      },
    },
  },
});

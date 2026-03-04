import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';
import { softDeleteExtension } from './prisma-soft-delete.extension';

@Injectable()
export class PrismaService
  extends PrismaClient
  implements OnModuleInit, OnModuleDestroy
{
  async onModuleInit() {
    await this.$connect();
  }

  async onModuleDestroy() {
    await this.$disconnect();
  }

  // Returns an extended client with automatic soft-delete filtering.
  // Note: the return type is intentionally `any` because Prisma's extension
  // type inference is complex and varies by extension composition.
  get extended(): any {
    return this.$extends(softDeleteExtension);
  }
}

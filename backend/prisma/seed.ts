import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  await prisma.appVersion.upsert({
    where: { platform_version: { platform: 'ios', version: '1.0.0' } },
    update: {},
    create: { platform: 'ios', version: '1.0.0', isMinimumVersion: true, isLatestVersion: true },
  });
  await prisma.appVersion.upsert({
    where: { platform_version: { platform: 'android', version: '1.0.0' } },
    update: {},
    create: { platform: 'android', version: '1.0.0', isMinimumVersion: true, isLatestVersion: true },
  });
  console.log('Seed complete');
}

main().catch(console.error).finally(() => prisma.$disconnect());

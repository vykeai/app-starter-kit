import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('Seeding database...');

  // Admin user
  const admin = await prisma.user.upsert({
    where: { email: 'admin@example.com' },
    update: {},
    create: {
      email: 'admin@example.com',
      displayName: 'Admin',
      // role will be 'ADMIN' once the migration adding UserRole enum is applied
      ...({ role: 'ADMIN' } as Record<string, unknown>),
    } as any,
  });
  console.log('✓ Admin user:', admin.email);

  // Test user
  const testUser = await prisma.user.upsert({
    where: { email: 'test@example.com' },
    update: {},
    create: {
      email: 'test@example.com',
      displayName: 'Test User',
    },
  });
  console.log('✓ Test user:', testUser.email);

  await prisma.userPreferences.upsert({
    where: { userId: testUser.id },
    update: {},
    create: {
      userId: testUser.id,
      theme: 'system',
      pushActivityEnabled: true,
      pushTransactionalEnabled: true,
      pushSystemEnabled: true,
      pushMarketingEnabled: false,
      emailNotificationsEnabled: true,
    },
  });
  console.log('✓ Default user preferences seeded');

  // App versions
  await prisma.appVersion.upsert({
    where: { platform_version: { platform: 'ios', version: '1.0.0' } },
    update: {},
    create: {
      platform: 'ios',
      version: '1.0.0',
      isMinimumVersion: true,
      isLatestVersion: true,
    },
  });
  await prisma.appVersion.upsert({
    where: { platform_version: { platform: 'android', version: '1.0.0' } },
    update: {},
    create: {
      platform: 'android',
      version: '1.0.0',
      isMinimumVersion: true,
      isLatestVersion: true,
    },
  });
  console.log('✓ App versions seeded');

  console.log('Seeding complete!');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());

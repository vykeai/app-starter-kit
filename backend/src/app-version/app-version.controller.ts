import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags, ApiQuery } from '@nestjs/swagger';
import { PrismaService } from '../prisma/prisma.service';

@ApiTags('app')
@Controller('app')
export class AppVersionController {
  constructor(private readonly prisma: PrismaService) {}

  @Get('version-check')
  @ApiQuery({ name: 'platform', required: true, example: 'ios' })
  @ApiQuery({ name: 'version', required: true, example: '1.0.0' })
  async versionCheck(
    @Query('platform') platform: string,
    @Query('version') version: string,
  ) {
    const minimumVersionRecord = await this.prisma.appVersion.findFirst({
      where: { platform, isMinimumVersion: true },
      orderBy: { createdAt: 'desc' },
    });

    const latestVersionRecord = await this.prisma.appVersion.findFirst({
      where: { platform, isLatestVersion: true },
      orderBy: { createdAt: 'desc' },
    });

    if (!minimumVersionRecord && !latestVersionRecord) {
      return {
        isUpdateRequired: false,
        isUpdateRecommended: false,
        minimumVersion: version,
        latestVersion: version,
      };
    }

    const minimumVersion = minimumVersionRecord?.version ?? version;
    const latestVersion = latestVersionRecord?.version ?? version;

    return {
      isUpdateRequired: this.isVersionLessThan(version, minimumVersion),
      isUpdateRecommended: this.isVersionLessThan(version, latestVersion),
      minimumVersion,
      latestVersion,
    };
  }

  private isVersionLessThan(a: string, b: string): boolean {
    const partsA = a.split('.').map(Number);
    const partsB = b.split('.').map(Number);
    for (let i = 0; i < Math.max(partsA.length, partsB.length); i++) {
      const numA = partsA[i] ?? 0;
      const numB = partsB[i] ?? 0;
      if (numA < numB) return true;
      if (numA > numB) return false;
    }
    return false;
  }
}

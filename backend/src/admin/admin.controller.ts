import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
} from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AdminGuard } from '../common/guards/admin.guard';
import { PrismaService } from '../prisma/prisma.service';
import { PaginationDto } from '../common/dto/pagination.dto';

@ApiTags('admin')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard, AdminGuard)
@Controller('admin')
export class AdminController {
  constructor(private readonly prisma: PrismaService) {}

  @Get('users')
  @ApiOperation({ summary: 'List all users (admin only)' })
  @ApiResponse({ status: 200, description: 'Paginated list of all users' })
  @ApiResponse({ status: 403, description: 'Admin access required' })
  async listUsers(@Query() pagination: PaginationDto) {
    const limit = pagination.limit ?? 50;
    const offset = pagination.offset ?? 0;

    const [users, totalCount] = await Promise.all([
      this.prisma.user.findMany({
        take: limit,
        skip: offset,
        orderBy: { createdAt: 'desc' },
        select: {
          id: true,
          email: true,
          displayName: true,
          createdAt: true,
          updatedAt: true,
          deletedAt: true,
        } as any,
      }),
      this.prisma.user.count(),
    ]);

    return {
      items: users,
      pagination: {
        totalCount,
        limit,
        hasMore: offset + users.length < totalCount,
        nextCursor: null,
        offset,
      },
    };
  }

  @Get('stats')
  @ApiOperation({ summary: 'Get system stats (admin only)' })
  @ApiResponse({ status: 200, description: 'System statistics' })
  @ApiResponse({ status: 403, description: 'Admin access required' })
  async getStats() {
    const [totalUsers, activeUsers, adminUsers] = await Promise.all([
      this.prisma.user.count(),
      this.prisma.user.count({ where: { deletedAt: null } }),
      this.prisma.user.count({ where: { deletedAt: null, ...({ role: 'ADMIN' } as any) } }),
    ]);

    return {
      users: {
        total: totalUsers,
        active: activeUsers,
        admins: adminUsers,
      },
    };
  }
}

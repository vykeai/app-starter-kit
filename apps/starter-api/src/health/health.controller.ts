import { Controller, Get } from '@nestjs/common';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { SkipThrottle } from '@nestjs/throttler';

@ApiTags('health')
@Controller('health')
export class HealthController {
  @Get()
  @SkipThrottle()
  @ApiOperation({ summary: 'Health check — returns server status and timestamp' })
  @ApiResponse({ status: 200, description: 'Server is healthy' })
  check() {
    return { status: 'ok', timestamp: new Date().toISOString() };
  }
}

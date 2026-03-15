import { Body, Controller, Get, Post, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { AnalyticsService } from './analytics.service';
import { TrackEventDto } from './dtos/track-event.dto';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { CurrentUser } from '../user/decorators/current-user.decorator';

@ApiTags('analytics')
@Controller('analytics')
export class AnalyticsController {
  constructor(private readonly analyticsService: AnalyticsService) {}

  @Post('events')
  @ApiOperation({ summary: 'Track an analytics event' })
  @ApiResponse({ status: 201, description: 'Event accepted' })
  trackAnonymous(@Body() dto: TrackEventDto) {
    return this.analyticsService.track(null, dto);
  }

  @Post('events/authenticated')
  @UseGuards(JwtAuthGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: 'Track an authenticated analytics event' })
  trackAuthenticated(
    @CurrentUser() user: { userId: string },
    @Body() dto: TrackEventDto,
  ) {
    return this.analyticsService.track(user.userId, dto);
  }

  @Get('summary')
  @ApiOperation({ summary: 'Return a lightweight analytics summary' })
  summary() {
    return this.analyticsService.summary();
  }
}

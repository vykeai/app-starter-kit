import { Injectable } from '@nestjs/common';
import { TrackEventDto } from './dtos/track-event.dto';

type EventRecord = {
  name: string;
  category: string | null;
  userId: string | null;
  properties: Record<string, string | number | boolean | null> | null;
  createdAt: string;
};

@Injectable()
export class AnalyticsService {
  private readonly events: EventRecord[] = [];

  track(userId: string | null, dto: TrackEventDto) {
    this.events.push({
      name: dto.name,
      category: dto.category ?? null,
      userId,
      properties: dto.properties ?? null,
      createdAt: new Date().toISOString(),
    });

    return { accepted: true };
  }

  summary() {
    const counts = new Map<string, number>();
    for (const event of this.events) {
      counts.set(event.name, (counts.get(event.name) ?? 0) + 1);
    }

    return {
      totalEvents: this.events.length,
      uniqueEvents: counts.size,
      topEvents: Array.from(counts.entries())
        .sort((left, right) => right[1] - left[1])
        .slice(0, 10)
        .map(([name, count]) => ({ name, count })),
      lastEventAt: this.events.at(-1)?.createdAt ?? null,
    };
  }
}

import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsInt, Min, Max, IsString } from 'class-validator';
import { Type } from 'class-transformer';

export class PaginationDto {
  @ApiPropertyOptional({ description: 'Cursor for next page', example: 'eyJpZCI6IjEyMyJ9' })
  @IsOptional()
  @IsString()
  cursor?: string;

  @ApiPropertyOptional({ description: 'Number of items to skip (offset-based)', example: 0 })
  @IsOptional()
  @IsInt()
  @Min(0)
  @Type(() => Number)
  offset?: number;

  @ApiPropertyOptional({ description: 'Number of items per page', example: 50, default: 50 })
  @IsOptional()
  @IsInt()
  @Min(1)
  @Max(250)
  @Type(() => Number)
  limit?: number = 50;
}

export interface PaginatedResult<T> {
  items: T[];
  pagination: {
    totalCount: number;
    limit: number;
    hasMore: boolean;
    nextCursor: string | null;
    offset: number;
  };
}

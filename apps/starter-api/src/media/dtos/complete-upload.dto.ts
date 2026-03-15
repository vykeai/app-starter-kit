import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class CompleteUploadDto {
  @ApiProperty({ example: 'asset_123' })
  @IsString()
  assetId!: string;

  @ApiPropertyOptional({ example: 1080 })
  @IsOptional()
  @IsNumber()
  @Min(1)
  width?: number;

  @ApiPropertyOptional({ example: 1350 })
  @IsOptional()
  @IsNumber()
  @Min(1)
  height?: number;
}

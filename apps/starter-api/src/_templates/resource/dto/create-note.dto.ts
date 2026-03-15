import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, MinLength, MaxLength } from 'class-validator';

export class CreateNoteDto {
  @ApiProperty({ description: 'Note title', example: 'My Note' })
  @IsString()
  @MinLength(1)
  @MaxLength(255)
  title: string;

  @ApiPropertyOptional({ description: 'Note content' })
  @IsOptional()
  @IsString()
  @MaxLength(10000)
  content?: string;
}

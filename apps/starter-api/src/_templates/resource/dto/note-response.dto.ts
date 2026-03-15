import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
export class NoteResponseDto {
  @ApiProperty() id: string;
  @ApiProperty() userId: string;
  @ApiProperty() title: string;
  @ApiPropertyOptional() content: string | null;
  @ApiProperty() createdAt: Date;
  @ApiProperty() updatedAt: Date;
}

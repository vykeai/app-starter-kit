import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsIn, IsOptional, IsString } from 'class-validator';

export class VerifyNativePurchaseDto {
  @ApiProperty({ example: 'ios', enum: ['ios', 'android'] })
  @IsString()
  @IsIn(['ios', 'android'])
  platform!: 'ios' | 'android';

  @ApiProperty({ example: 'tracker_monthly' })
  @IsString()
  productId!: string;

  @ApiProperty({ example: 'store-token-123' })
  @IsString()
  purchaseToken!: string;

  @ApiPropertyOptional({ example: 'app-store-transaction-id' })
  @IsOptional()
  @IsString()
  transactionId?: string;
}

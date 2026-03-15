import { Injectable } from '@nestjs/common';
import { VerifyNativePurchaseDto } from './dtos/verify-native-purchase.dto';

type EntitlementState = {
  tier: 'free' | 'tracker';
  source: 'starter' | 'ios' | 'android';
  features: string[];
  renewsAt: string | null;
};

@Injectable()
export class BillingService {
  private readonly entitlements = new Map<string, EntitlementState>();

  getEntitlements(userId: string) {
    return (
      this.entitlements.get(userId) ?? {
        tier: 'free',
        source: 'starter',
        features: ['basic_logging', 'exercise_library'],
        renewsAt: null,
      }
    );
  }

  verifyNativePurchase(userId: string, dto: VerifyNativePurchaseDto) {
    const nextState: EntitlementState = {
      tier: 'tracker',
      source: dto.platform,
      features: [
        'basic_logging',
        'exercise_library',
        'unlimited_templates',
        'advanced_analytics',
        'export_data',
      ],
      renewsAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
    };

    this.entitlements.set(userId, nextState);

    return {
      verified: true,
      productId: dto.productId,
      entitlement: nextState,
    };
  }
}

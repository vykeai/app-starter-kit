import { Injectable, NotFoundException } from '@nestjs/common';
import { randomUUID } from 'crypto';
import { ConfigService } from '@nestjs/config';
import { PrepareUploadDto } from './dtos/prepare-upload.dto';
import { CompleteUploadDto } from './dtos/complete-upload.dto';

type MediaAssetRecord = {
  id: string;
  ownerId: string;
  kind: string;
  storageKey: string;
  uploadUrl: string;
  publicUrl: string | null;
  mimeType: string;
  fileName: string | null;
  sizeBytes: number | null;
  width: number | null;
  height: number | null;
  visibility: 'private' | 'public';
  status: 'pending' | 'ready';
  createdAt: string;
};

@Injectable()
export class MediaService {
  private readonly assetStore = new Map<string, MediaAssetRecord>();

  constructor(private readonly config: ConfigService) {}

  prepareUpload(userId: string, dto: PrepareUploadDto) {
    const assetId = `asset_${randomUUID()}`;
    const storageKey = this.buildStorageKey(userId, dto.kind, assetId, dto.fileName);
    const uploadUrl = this.buildUploadUrl(storageKey);
    const publicUrl =
      (dto.visibility ?? 'private') === 'public'
        ? this.buildPublicUrl(storageKey)
        : null;

    const record: MediaAssetRecord = {
      id: assetId,
      ownerId: userId,
      kind: dto.kind,
      storageKey,
      uploadUrl,
      publicUrl,
      mimeType: dto.mimeType,
      fileName: dto.fileName ?? null,
      sizeBytes: dto.sizeBytes ?? null,
      width: null,
      height: null,
      visibility: dto.visibility ?? 'private',
      status: 'pending',
      createdAt: new Date().toISOString(),
    };

    this.assetStore.set(assetId, record);

    return {
      assetId: record.id,
      storageKey: record.storageKey,
      uploadUrl: record.uploadUrl,
      publicUrl: record.publicUrl,
      uploadMethod: 'PUT',
      headers: {
        'Content-Type': record.mimeType,
      },
      expiresInSeconds: 900,
    };
  }

  completeUpload(userId: string, dto: CompleteUploadDto) {
    const record = this.assetStore.get(dto.assetId);
    if (!record || record.ownerId !== userId) {
      throw new NotFoundException('Media asset not found');
    }

    record.status = 'ready';
    record.width = dto.width ?? null;
    record.height = dto.height ?? null;

    return this.toResponse(record);
  }

  listAssets(userId: string) {
    return Array.from(this.assetStore.values())
      .filter((asset) => asset.ownerId === userId)
      .map((asset) => this.toResponse(asset));
  }

  deleteAsset(userId: string, assetId: string) {
    const record = this.assetStore.get(assetId);
    if (!record || record.ownerId !== userId) {
      throw new NotFoundException('Media asset not found');
    }

    this.assetStore.delete(assetId);
    return { message: 'Media asset deleted' };
  }

  private toResponse(record: MediaAssetRecord) {
    return {
      id: record.id,
      ownerId: record.ownerId,
      kind: record.kind,
      status: record.status,
      storageKey: record.storageKey,
      publicUrl: record.publicUrl,
      mimeType: record.mimeType,
      fileName: record.fileName,
      sizeBytes: record.sizeBytes,
      width: record.width,
      height: record.height,
      visibility: record.visibility,
      createdAt: record.createdAt,
    };
  }

  private buildStorageKey(
    userId: string,
    kind: string,
    assetId: string,
    fileName?: string,
  ) {
    const safeFileName = (fileName ?? 'upload.bin')
      .replace(/[^a-zA-Z0-9._-]/g, '-')
      .replace(/-+/g, '-');

    return `${userId}/${kind}/${assetId}/${safeFileName}`;
  }

  private buildUploadUrl(storageKey: string) {
    const bucketName = this.config.get<string>('R2_BUCKET_NAME');
    const accountId = this.config.get<string>('R2_ACCOUNT_ID');

    if (bucketName && accountId) {
      return `https://${accountId}.r2.cloudflarestorage.com/${bucketName}/${encodeURIComponent(storageKey)}`;
    }

    const fallbackBaseUrl = this.config.get<string>(
      'R2_PUBLIC_BASE_URL',
      'https://media.onlystack.dev',
    );
    return `${fallbackBaseUrl.replace(/\/$/, '')}/upload/${encodeURIComponent(storageKey)}`;
  }

  private buildPublicUrl(storageKey: string) {
    const publicBaseUrl = this.config.get<string>('R2_PUBLIC_BASE_URL');
    if (publicBaseUrl) {
      return `${publicBaseUrl.replace(/\/$/, '')}/${encodeURIComponent(storageKey)}`;
    }

    const accountId = this.config.get<string>('R2_ACCOUNT_ID');
    const bucketName = this.config.get<string>('R2_BUCKET_NAME');
    if (accountId && bucketName) {
      return `https://pub-${accountId}.r2.dev/${encodeURIComponent(storageKey)}`;
    }

    return `https://media.onlystack.dev/${encodeURIComponent(storageKey)}`;
  }
}

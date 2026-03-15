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
    const storageKey = `${userId}/${dto.kind}/${assetId}/${dto.fileName ?? 'upload.bin'}`;
    const bucketBaseUrl = this.config.get<string>(
      'MEDIA_PUBLIC_BASE_URL',
      'https://media.onlystack.dev',
    );

    const record: MediaAssetRecord = {
      id: assetId,
      ownerId: userId,
      kind: dto.kind,
      storageKey,
      uploadUrl: `${bucketBaseUrl}/upload/${encodeURIComponent(storageKey)}`,
      publicUrl:
        (dto.visibility ?? 'private') === 'public'
          ? `${bucketBaseUrl}/${encodeURIComponent(storageKey)}`
          : null,
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
}

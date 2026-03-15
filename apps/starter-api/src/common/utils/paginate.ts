import { PaginationDto, PaginatedResult } from '../dto/pagination.dto';

export function encodeCursor(id: string): string {
  return Buffer.from(JSON.stringify({ id })).toString('base64url');
}

export function decodeCursor(cursor: string): string {
  return JSON.parse(Buffer.from(cursor, 'base64url').toString()).id as string;
}

// Generic helper — call from any service
export async function paginateQuery<T extends { id: string }>(
  findMany: (args: { take: number; skip?: number; cursor?: { id: string } }) => Promise<T[]>,
  count: () => Promise<number>,
  dto: PaginationDto,
): Promise<PaginatedResult<T>> {
  const limit = dto.limit ?? 50;
  const cursorId = dto.cursor ? decodeCursor(dto.cursor) : undefined;
  const offset = dto.offset ?? 0;

  const [items, totalCount] = await Promise.all([
    findMany({
      take: limit + 1,
      ...(cursorId ? { cursor: { id: cursorId }, skip: 1 } : { skip: offset }),
    }),
    count(),
  ]);

  const hasMore = items.length > limit;
  if (hasMore) items.pop();

  const nextCursor = hasMore ? encodeCursor(items[items.length - 1].id) : null;

  return {
    items,
    pagination: { totalCount, limit, hasMore, nextCursor, offset },
  };
}

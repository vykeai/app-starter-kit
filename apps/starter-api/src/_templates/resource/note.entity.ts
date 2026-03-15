// Copy this folder to src/your-domain/ and replace Note→YourEntity, note→yourEntity
export class NoteEntity {
  id: string;
  userId: string;
  title: string;
  content: string | null;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;
}

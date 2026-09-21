export interface ImageGenerationResponse {
  id: number;
  prompt: string;
  imageUrl: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}
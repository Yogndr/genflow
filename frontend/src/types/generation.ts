export type GenerationType =
  | "GENERATE"
  | "REWRITE"
  | "EXPAND"
  | "SHORTEN";

export interface GenerationResponse {
  id: number;
  type: GenerationType;
  input: string;
  output: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}
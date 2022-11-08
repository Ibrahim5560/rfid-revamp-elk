export interface ITasks {
  id?: number;
  nameEn?: string;
  nameAr?: string;
  status?: number | null;
  code?: string | null;
}

export const defaultValue: Readonly<ITasks> = {};

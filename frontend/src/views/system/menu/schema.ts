export interface RoleRow {
  id: number;
  name: string;
  permissions?: string[];
}

export type OpenType = 'internal' | 'iframe' | 'external';

export interface TagView {
  name: string;
  path: string;
  title?: string;
  query?: Record<string, any>;
  params?: Record<string, any>;
  affix?: boolean;
}

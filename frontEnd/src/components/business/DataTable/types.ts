import type { Component, VNode } from "vue";

export interface SearchItem {
  prop: string | string[];
  label: string;
  type?: "input" | "select" | "date" | "daterange" | "custom";
  placeholder?: string;
  options?: Array<{ label: string; value: any }>;
  render?: (model: Record<string, any>) => VNode | Component | any;
  components?: Array<{
    prop: string;
    type?: "input" | "select" | "date" | "daterange" | "custom";
    placeholder?: string;
    options?: Array<{ label: string; value: any }>;
    render?: (model: Record<string, any>) => VNode | Component | any;
    style?: Record<string, any>;
  }>;
}

export interface Column {
  prop: string;
  label: string;
  width?: string | number;
  minWidth?: string | number;
  align?: "left" | "center" | "right";
  fixed?: boolean | "left" | "right";
  sortable?: boolean;
  emptyText?: string;
  showOverflowTooltip?: boolean;
  formatter?: (row: any, column: Column) => string;
  render?: (row: any, column: Column, index: number) => VNode | Component | any;
}

export interface Operation {
  label: string;
  type?: "primary" | "success" | "warning" | "danger" | "info";
  handler: (row: any, index: number) => void;
}

export interface DataTableProps {
  searchConfig?: SearchItem[];
  searchCols?:
    | number
    | { xs?: number; sm?: number; md?: number; lg?: number; xl?: number };
  columns: Column[];
  loadData: (params: any) => Promise<{ data: any[]; total: number }>;
  border?: boolean;
  stripe?: boolean;
  height?: string | number;
  maxHeight?: string | number;
  showSelection?: boolean;
  showIndex?: boolean;
  showToolbar?: boolean;
  toolbarLeft?: () => VNode | Component | any;
  toolbarRight?: () => VNode | Component | any;
  showOperation?: boolean;
  operationWidth?: string | number;
  operationFixed?: boolean | "left" | "right";
  operations?: Operation[];
  showPagination?: boolean;
  pageSizes?: number[];
  initialPage?: number;
  initialPageSize?: number;
}

import type { ImportResult, ImportError } from "@/api/order/types";

export interface ImportDialogProps {
  modelValue: boolean;
  title?: string;
  width?: string;
}

export interface ImportDialogEmits {
  (e: "update:modelValue", value: boolean): void;
  (e: "success", result: ImportResult): void;
}

export type { ImportResult, ImportError };

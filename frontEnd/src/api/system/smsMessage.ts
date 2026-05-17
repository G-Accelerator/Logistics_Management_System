import request from "../../utils/request";

export type SmsMessageTemplateType = "OVERSPEED" | "OFFLINE" | "REGULAR";

export interface SmsMessageLog {
  id: number;
  templateType: SmsMessageTemplateType;
  templateTypeLabel: string;
  triggerSource: string;
  recipientPhone: string;
  plateNumber?: string;
  orderNo?: string;
  vehicleId?: number;
  content: string;
  sentAt: string;
}

export interface SmsMessageQuery {
  page: number;
  pageSize: number;
  templateType?: SmsMessageTemplateType | "";
  triggerSource?: string;
  orderNo?: string;
  plateNumber?: string;
  recipientPhone?: string;
  startTime?: string;
  endTime?: string;
}

export interface PageResult<T> {
  data: T[];
  total: number;
}

export function getSmsMessages(
  params: SmsMessageQuery,
): Promise<PageResult<SmsMessageLog>> {
  return request.get("/sms-messages", { params });
}

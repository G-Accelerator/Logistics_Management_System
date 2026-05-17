import request from "../../utils/request";

export type SmsTemplateType = "OVERSPEED" | "OFFLINE" | "REGULAR";

export interface SmsTemplate {
  type: SmsTemplateType;
  content: string;
  updateTime?: string;
}

export function getSmsTemplates(): Promise<SmsTemplate[]> {
  return request.get("/system/sms-templates");
}

export function updateSmsTemplate(
  type: SmsTemplateType,
  content: string,
): Promise<SmsTemplate> {
  return request.put(`/system/sms-templates/${type}`, { content });
}

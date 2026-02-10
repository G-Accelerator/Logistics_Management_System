import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getEnabledExpressCompanies } from "../../api/system/expressCompany";
import type { ExpressCompany } from "../../api/system/expressCompany";

export const useExpressCompanyStore = defineStore("expressCompany", () => {
  const companies = ref<ExpressCompany[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // 快递公司选项（用于 select 组件）
  const options = computed(() =>
    companies.value.map((c) => ({
      label: c.name,
      value: c.code,
    })),
  );

  // 快递公司映射表（用于显示快递公司名称）
  const companyMap = computed(() => {
    const map: Record<string, string> = {};
    companies.value.forEach((c) => {
      map[c.code] = c.name;
    });
    return map;
  });

  /**
   * 加载快递公司列表
   */
  const load = async () => {
    loading.value = true;
    error.value = null;
    try {
      const response = await getEnabledExpressCompanies();

      // 尝试多种方式获取数据
      let data: ExpressCompany[] = [];

      if (Array.isArray(response)) {
        data = response;
      } else if (
        (response as any).data &&
        Array.isArray((response as any).data)
      ) {
        data = (response as any).data;
      } else if (
        (response as any).list &&
        Array.isArray((response as any).list)
      ) {
        data = (response as any).list;
      } else if (
        (response as any).records &&
        Array.isArray((response as any).records)
      ) {
        data = (response as any).records;
      }

      companies.value = data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : "加载快递公司失败";
      console.error("加载快递公司失败", err);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 根据代码获取快递公司名称
   */
  const getCompanyName = (code: string): string => {
    return companyMap.value[code] || code;
  };

  /**
   * 刷新数据（清除缓存并重新加载）
   */
  const refresh = async () => {
    companies.value = [];
    await load();
  };

  return {
    companies,
    options,
    companyMap,
    loading,
    error,
    load,
    getCompanyName,
    refresh,
  };
});

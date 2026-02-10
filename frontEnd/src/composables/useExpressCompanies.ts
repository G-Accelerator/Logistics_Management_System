import { ref, computed } from "vue";
import { getEnabledExpressCompanies } from "../api/system/expressCompany";
import type { ExpressCompany } from "../api/system/expressCompany";

// 缓存快递公司数据
let cachedCompanies: ExpressCompany[] | null = null;
let loadingPromise: Promise<ExpressCompany[]> | null = null;

/**
 * 获取快递公司列表
 * @returns 快递公司列表
 */
export async function getExpressCompanies(): Promise<ExpressCompany[]> {
  // 如果已缓存，直接返回
  if (cachedCompanies) {
    return cachedCompanies;
  }

  // 如果正在加载，等待加载完成
  if (loadingPromise) {
    return loadingPromise;
  }

  // 开始加载
  loadingPromise = (async () => {
    const response = await getEnabledExpressCompanies();
    return (response as any).data || response;
  })();

  try {
    cachedCompanies = await loadingPromise;
    return cachedCompanies;
  } finally {
    loadingPromise = null;
  }
}

/**
 * 获取快递公司选项（用于 select 组件）
 * @returns 快递公司选项数组
 */
export async function getExpressCompanyOptions(): Promise<
  Array<{ label: string; value: string }>
> {
  const companies = await getExpressCompanies();
  return companies.map((c) => ({
    label: c.name,
    value: c.code,
  }));
}

/**
 * 获取快递公司映射表（用于显示快递公司名称）
 * @returns 快递公司代码到名称的映射
 */
export async function getExpressCompanyMap(): Promise<Record<string, string>> {
  const companies = await getExpressCompanies();
  const map: Record<string, string> = {};
  companies.forEach((c) => {
    map[c.code] = c.name;
  });
  return map;
}

/**
 * 根据代码获取快递公司名称
 * @param code 快递公司代码
 * @returns 快递公司名称
 */
export async function getExpressCompanyName(code: string): Promise<string> {
  const companies = await getExpressCompanies();
  const company = companies.find((c) => c.code === code);
  return company?.name || code;
}

/**
 * 清除缓存（用于手动刷新数据）
 */
export function clearExpressCompaniesCache(): void {
  cachedCompanies = null;
}

/**
 * Vue 组件中使用的 composable
 * @returns 快递公司相关的响应式数据和方法
 */
export function useExpressCompanies() {
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
      companies.value = await getExpressCompanies();
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

  return {
    companies,
    options,
    companyMap,
    loading,
    error,
    load,
    getCompanyName,
  };
}

<template>
  <page-container title="创建订单" description="创建新的物流订单">
    <!-- 订单表单 -->
    <el-card shadow="never" class="order-form-card">
      <el-form
        ref="formRef"
        :model="orderForm"
        :rules="rules"
        label-position="top"
      >
        <!-- 货物信息区块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Box /></el-icon>货物信息
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="5">
              <el-form-item label="货物名称" prop="cargoName">
                <el-input
                  v-model="orderForm.cargoName"
                  placeholder="请输入货物名称"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="4">
              <el-form-item label="货物类型" prop="cargoType">
                <el-select
                  v-model="orderForm.cargoType"
                  placeholder="请选择"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in cargoTypes"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="4">
              <el-form-item label="快递公司" prop="expressCompany">
                <ExpressCompanySelect v-model="orderForm.expressCompany" />
              </el-form-item>
            </el-col>
            <el-col :xs="8" :sm="4" :md="3">
              <el-form-item label="重量(kg)" prop="cargoWeight">
                <el-input-number
                  v-model="orderForm.cargoWeight"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="8" :sm="4" :md="3">
              <el-form-item label="体积(m³)">
                <el-input-number
                  v-model="orderForm.cargoVolume"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="8" :sm="4" :md="3">
              <el-form-item label="数量(件)">
                <el-input-number
                  v-model="orderForm.cargoQuantity"
                  :min="1"
                  :controls="false"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="备注">
                <el-input
                  v-model="orderForm.remark"
                  placeholder="订单备注信息"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 发货信息区块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Location /></el-icon>发货信息
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="24" :md="12">
              <el-form-item label="起始地址" prop="origin">
                <el-autocomplete
                  v-model="orderForm.origin"
                  :fetch-suggestions="searchAddress"
                  placeholder="请输入起始地址"
                  style="width: 100%"
                  :trigger-on-focus="false"
                  @select="(item: any) => handleAddressSelect(item, 'origin')"
                >
                  <template #prefix
                    ><el-icon><Location /></el-icon
                  ></template>
                  <template #suffix>
                    <el-tooltip content="使用当前位置" placement="top">
                      <el-icon
                        class="location-btn"
                        @click="useCurrentLocation('origin')"
                        ><Aim
                      /></el-icon>
                    </el-tooltip>
                  </template>
                </el-autocomplete>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="发货人" prop="senderName">
                <el-input
                  v-model="orderForm.senderName"
                  placeholder="发货人姓名"
                  prefix-icon="User"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="联系电话" prop="senderPhone">
                <el-input
                  v-model="orderForm.senderPhone"
                  placeholder="发货人电话"
                  prefix-icon="Phone"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 收货信息区块 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Position /></el-icon>收货信息
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="24" :md="12">
              <el-form-item label="目的地址" prop="destination">
                <el-autocomplete
                  v-model="orderForm.destination"
                  :fetch-suggestions="searchAddress"
                  placeholder="请输入目的地址"
                  style="width: 100%"
                  :trigger-on-focus="false"
                  @select="
                    (item: any) => handleAddressSelect(item, 'destination')
                  "
                >
                  <template #prefix
                    ><el-icon><Position /></el-icon
                  ></template>
                  <template #suffix>
                    <el-tooltip content="使用当前位置" placement="top">
                      <el-icon
                        class="location-btn"
                        @click="useCurrentLocation('destination')"
                        ><Aim
                      /></el-icon>
                    </el-tooltip>
                  </template>
                </el-autocomplete>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="收货人" prop="receiverName">
                <el-input
                  v-model="orderForm.receiverName"
                  placeholder="收货人姓名"
                  prefix-icon="User"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="联系电话" prop="receiverPhone">
                <el-input
                  v-model="orderForm.receiverPhone"
                  placeholder="收货人电话"
                  prefix-icon="Phone"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-form>
    </el-card>

    <!-- 底部操作按钮 -->
    <div class="bottom-actions">
      <el-button
        type="success"
        size="large"
        @click="submitOrder"
        :loading="submitting"
      >
        <el-icon><Check /></el-icon>提交订单
      </el-button>
      <el-button size="large" @click="resetForm" :disabled="submitting"
        >重置</el-button
      >
    </div>
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from "element-plus";
import { Location, Position, Aim, Box, Check } from "@element-plus/icons-vue";
import PageContainer from "../../../components/layout/PageContainer/index.vue";
import ExpressCompanySelect from "../../../components/business/ExpressCompanySelect/index.vue";
import { createOrder } from "../../../api/order";
import { useExpressCompanyStore } from "../../../store/expressCompany";

const formRef = ref<FormInstance>();
const router = useRouter();
const submitting = ref(false);

const orderForm = reactive({
  origin: "北京市朝阳区望京SOHO",
  destination: "上海市浦东新区陆家嘴",
  senderName: "张三",
  senderPhone: "13800138001",
  receiverName: "李四",
  receiverPhone: "13900139002",
  cargoName: "电子产品",
  cargoType: "normal",
  cargoWeight: 500 as number | null,
  cargoVolume: 10 as number | null,
  cargoQuantity: 20 as number | null,
  remark: "轻拿轻放",
  expressCompany: "",
  originLng: null as number | null,
  originLat: null as number | null,
  destLng: null as number | null,
  destLat: null as number | null,
});

const cargoTypes = [
  { label: "普通货物", value: "normal" },
  { label: "易碎品", value: "fragile" },
  { label: "冷链货物", value: "cold" },
  { label: "危险品", value: "dangerous" },
];

const rules: FormRules = {
  origin: [{ required: true, message: "请输入起始地址", trigger: "blur" }],
  destination: [{ required: true, message: "请输入目的地址", trigger: "blur" }],
  senderName: [
    { required: true, message: "请输入发货人姓名", trigger: "blur" },
  ],
  senderPhone: [
    { required: true, message: "请输入发货人电话", trigger: "blur" },
  ],
  receiverName: [
    { required: true, message: "请输入收货人姓名", trigger: "blur" },
  ],
  receiverPhone: [
    { required: true, message: "请输入收货人电话", trigger: "blur" },
  ],
  cargoName: [{ required: true, message: "请输入货物名称", trigger: "blur" }],
  cargoType: [{ required: true, message: "请选择货物类型", trigger: "change" }],
  expressCompany: [
    { required: true, message: "请选择快递公司", trigger: "change" },
  ],
  cargoWeight: [{ required: true, message: "请输入货物重量", trigger: "blur" }],
};

const searchAddress = (query: string, cb: (r: any[]) => void) => {
  if (!query) {
    cb([]);
    return;
  }
  const ac = new (AMap as any).AutoComplete({ city: "全国" });
  ac.search(query, (s: string, r: any) => {
    cb(
      s === "complete" && r.tips
        ? r.tips
            .filter((t: any) => t.location)
            .map((t: any) => ({
              value: t.name + (t.district ? ` (${t.district})` : ""),
              name: t.name,
              lng: t.location.lng,
              lat: t.location.lat,
            }))
        : [],
    );
  });
};

const handleAddressSelect = (
  item: { value: string; lng: number; lat: number },
  type: "origin" | "destination",
) => {
  if (type === "origin") {
    orderForm.originLng = item.lng;
    orderForm.originLat = item.lat;
  } else {
    orderForm.destLng = item.lng;
    orderForm.destLat = item.lat;
  }
};

const useCurrentLocation = (type: "origin" | "destination") => {
  const geo = new (AMap as any).Geolocation({
    enableHighAccuracy: true,
    timeout: 10000,
  });
  ElMessage.info("正在获取当前位置...");
  geo.getCurrentPosition(async (s: string, r: any) => {
    if (s === "complete") {
      const { lng, lat } = r.position;
      let address = r.formattedAddress || "";
      if (!address) {
        address = await new Promise<string>((resolve) => {
          const geocoder = new (AMap as any).Geocoder();
          geocoder.getAddress([lng, lat], (status: string, result: any) => {
            resolve(
              status === "complete" && result.regeocode
                ? result.regeocode.formattedAddress || ""
                : "",
            );
          });
        });
      }
      if (type === "origin") {
        orderForm.origin = address || `${lng.toFixed(6)}, ${lat.toFixed(6)}`;
        orderForm.originLng = lng;
        orderForm.originLat = lat;
      } else {
        orderForm.destination =
          address || `${lng.toFixed(6)}, ${lat.toFixed(6)}`;
        orderForm.destLng = lng;
        orderForm.destLat = lat;
      }
      ElMessage.success(address ? "已获取当前位置" : "已获取坐标位置");
    } else {
      ElMessage.error("获取位置失败");
    }
  });
};

const submitOrder = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const result = await createOrder({
          cargoName: orderForm.cargoName,
          cargoType: orderForm.cargoType,
          cargoWeight: orderForm.cargoWeight ?? undefined,
          cargoVolume: orderForm.cargoVolume ?? undefined,
          cargoQuantity: orderForm.cargoQuantity ?? undefined,
          remark: orderForm.remark || undefined,
          expressCompany: orderForm.expressCompany,
          origin: orderForm.origin,
          destination: orderForm.destination,
          senderName: orderForm.senderName,
          receiverName: orderForm.receiverName,
          senderPhone: orderForm.senderPhone,
          receiverPhone: orderForm.receiverPhone,
          originLng: orderForm.originLng ?? undefined,
          originLat: orderForm.originLat ?? undefined,
          destLng: orderForm.destLng ?? undefined,
          destLat: orderForm.destLat ?? undefined,
        });

        ElMessageBox.confirm(`订单编号：${result.orderNo}`, "订单创建成功", {
          confirmButtonText: "查看订单列表",
          cancelButtonText: "继续新建",
          type: "success",
          closeOnClickModal: false,
        })
          .then(() => {
            resetForm();
            router.push("/order/list");
          })
          .catch(() => {
            resetForm();
          });
      } catch {
        ElMessage.error("创建订单失败");
      } finally {
        submitting.value = false;
      }
    }
  });
};

const resetForm = () => {
  formRef.value?.resetFields();
};

onMounted(async () => {
  // 初始化地图插件
  (AMap as any).plugin(
    ["AMap.Geocoder", "AMap.AutoComplete", "AMap.Geolocation"],
    () => {},
  );

  // 初始化快递公司，选中第一个
  const store = useExpressCompanyStore();
  // 如果数据还没加载，等待加载完成
  if (store.companies.length === 0) {
    await store.load();
  }
  const companies = store.companies as any;
  if (companies && companies.length > 0) {
    orderForm.expressCompany = companies[0].code;
  }
});
</script>

<style scoped>
.order-form-card {
  margin-bottom: 16px;
}
.form-section {
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.form-section:last-of-type {
  border-bottom: none;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}
.section-title .el-icon {
  color: var(--el-color-primary);
}
.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
  margin-top: 16px;
}
.location-btn {
  cursor: pointer;
  transition: color 0.2s;
}
.location-btn:hover {
  color: var(--el-color-primary);
}
</style>

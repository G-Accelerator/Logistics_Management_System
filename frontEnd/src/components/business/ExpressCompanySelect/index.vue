<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled || store.loading"
    :loading="store.loading"
    :clearable="clearable"
    :filterable="filterable"
    @update:model-value="$emit('update:modelValue', $event)"
    v-bind="$attrs"
  >
    <el-option
      v-for="option in store.options"
      :key="option.value"
      :label="option.label"
      :value="option.value"
    />
  </el-select>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useExpressCompanyStore } from "../../../store/expressCompany";

interface Props {
  modelValue?: string;
  placeholder?: string;
  disabled?: boolean;
  clearable?: boolean;
  filterable?: boolean;
}

withDefaults(defineProps<Props>(), {
  modelValue: "",
  placeholder: "请选择快递公司",
  disabled: false,
  clearable: true,
  filterable: true,
});

defineEmits<{
  (e: "update:modelValue", value: string): void;
}>();

const store = useExpressCompanyStore();

onMounted(async () => {
  await store.load();
});
</script>

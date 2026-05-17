<template>
  <page-container title="个人中心" description="管理您的账号信息">
    <template v-if="isAdminAccount">
      <el-card shadow="never" class="profile-card">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          label-width="100px"
          class="profile-form"
          @submit.prevent="saveProfile"
        >
          <el-form-item
            label="昵称"
            prop="nickname"
            :rules="[
              { required: true, message: '请输入昵称', trigger: 'blur' },
              { max: 64, message: '昵称不超过 64 字', trigger: 'blur' },
            ]"
          >
            <el-input
              v-model="profileForm.nickname"
              maxlength="64"
              show-word-limit
              style="max-width: 360px"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="profileSaving"
              @click="saveProfile"
            >
              保存昵称
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="profile-card">
        <template #header>
          <span class="card-title">登录用户名</span>
        </template>
        <el-form
          ref="usernameFormRef"
          :model="usernameForm"
          label-width="100px"
          class="profile-form"
          @submit.prevent="saveUsername"
        >
          <el-form-item label="当前用户名">
            <span class="readonly-text">{{ userStore.userInfo?.username }}</span>
          </el-form-item>
          <el-form-item
            label="新用户名"
            prop="newUsername"
            :rules="[
              { required: true, message: '请输入新用户名', trigger: 'blur' },
              {
                min: 3,
                max: 32,
                message: '长度 3–32 个字符',
                trigger: 'blur',
              },
              {
                pattern: /^[a-zA-Z0-9_]+$/,
                message: '仅支持字母、数字和下划线',
                trigger: 'blur',
              },
            ]"
          >
            <el-input
              v-model="usernameForm.newUsername"
              maxlength="32"
              style="max-width: 360px"
            />
          </el-form-item>
          <el-form-item
            label="当前密码"
            prop="password"
            :rules="[
              { required: true, message: '请输入当前密码', trigger: 'blur' },
            ]"
          >
            <el-input
              v-model="usernameForm.password"
              type="password"
              show-password
              autocomplete="current-password"
              style="max-width: 360px"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="usernameSaving"
              @click="saveUsername"
            >
              修改用户名
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="never" class="profile-card">
        <template #header>
          <span class="card-title">登录密码</span>
        </template>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          label-width="100px"
          class="profile-form"
          @submit.prevent="savePassword"
        >
          <el-form-item
            label="原密码"
            prop="oldPassword"
            :rules="[
              { required: true, message: '请输入原密码', trigger: 'blur' },
            ]"
          >
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
              autocomplete="current-password"
              style="max-width: 360px"
            />
          </el-form-item>
          <el-form-item
            label="新密码"
            prop="newPassword"
            :rules="[
              { required: true, message: '请输入新密码', trigger: 'blur' },
              { min: 6, message: '至少 6 位', trigger: 'blur' },
            ]"
          >
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              autocomplete="new-password"
              style="max-width: 360px"
            />
          </el-form-item>
          <el-form-item
            label="确认新密码"
            prop="confirmPassword"
            :rules="[
              { required: true, message: '请再次输入新密码', trigger: 'blur' },
              { validator: validateConfirmPassword, trigger: 'blur' },
            ]"
          >
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              autocomplete="new-password"
              style="max-width: 360px"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="passwordSaving"
              @click="savePassword"
            >
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>

    <el-card v-else shadow="never" class="profile-card">
      <el-result icon="info" title="买家/卖家账号">
        <template #sub-title>
          <p>当前为手机号登录，无需设置用户名密码。</p>
          <p v-if="userStore.userInfo?.phone">
            绑定手机：{{ userStore.userInfo.phone }}
          </p>
        </template>
      </el-result>
    </el-card>
  </page-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, type FormInstance } from "element-plus";
import PageContainer from "../../components/layout/PageContainer/index.vue";
import { useUserStore } from "../../store/user";
import * as authApi from "../../api/auth";

defineOptions({ name: "Profile" });

const userStore = useUserStore();

const isAdminAccount = computed(
  () =>
    !localStorage.getItem("buyerPhone") &&
    !localStorage.getItem("sellerPhone") &&
    userStore.userInfo?.role === "admin",
);

const profileFormRef = ref<FormInstance>();
const usernameFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();

const profileForm = reactive({ nickname: "" });
const usernameForm = reactive({ newUsername: "", password: "" });
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const profileSaving = ref(false);
const usernameSaving = ref(false);
const passwordSaving = ref(false);

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (e?: Error) => void,
) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error("两次输入的新密码不一致"));
  } else {
    callback();
  }
};

const syncForms = () => {
  profileForm.nickname = userStore.userInfo?.nickname || "";
  usernameForm.newUsername = userStore.userInfo?.username || "";
};

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo();
    } catch {
      /* ignore */
    }
  }
  syncForms();
});

const saveProfile = async () => {
  const valid = await profileFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  profileSaving.value = true;
  try {
    const info = await authApi.updateProfile({
      nickname: profileForm.nickname.trim(),
    });
    userStore.userInfo = info;
    ElMessage.success("昵称已保存");
  } finally {
    profileSaving.value = false;
  }
};

const saveUsername = async () => {
  const valid = await usernameFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  if (usernameForm.newUsername === userStore.userInfo?.username) {
    ElMessage.warning("新用户名与当前相同");
    return;
  }
  usernameSaving.value = true;
  try {
    const info = await authApi.changeUsername({
      newUsername: usernameForm.newUsername.trim(),
      password: usernameForm.password,
    });
    userStore.userInfo = info;
    usernameForm.password = "";
    ElMessage.success("用户名已更新");
  } finally {
    usernameSaving.value = false;
  }
};

const savePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  passwordSaving.value = true;
  try {
    await authApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
    passwordFormRef.value?.clearValidate();
    ElMessage.success("密码已修改，请使用新密码下次登录");
  } finally {
    passwordSaving.value = false;
  }
};
</script>

<style scoped>
.profile-card {
  margin-bottom: 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.profile-form {
  max-width: 520px;
}

.readonly-text {
  color: var(--text-secondary);
}
</style>

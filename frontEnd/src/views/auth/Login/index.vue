<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>物流管理系统</h2>
        <p>Logistics Management System</p>
      </div>

      <!-- 登录方式切换 -->
      <div class="login-tabs">
        <span
          :class="['tab-item', { active: loginType === 'account' }]"
          @click="loginType = 'account'"
        >
          账号登录
        </span>
        <span
          :class="['tab-item', { active: loginType === 'phone' }]"
          @click="loginType = 'phone'"
        >
          买家查询
        </span>
      </div>

      <!-- 账号密码登录 -->
      <el-form
        v-if="loginType === 'account'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 手机号登录（买家） -->
      <el-form
        v-else
        ref="phoneFormRef"
        :model="phoneForm"
        :rules="phoneRules"
        class="login-form"
        @keyup.enter="handlePhoneLogin"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="phoneForm.phone"
            placeholder="请输入收货手机号"
            prefix-icon="Phone"
            size="large"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handlePhoneLogin"
          >
            查询订单
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-tips">
        <p v-if="loginType === 'account'">默认账号：admin / 123456</p>
        <p v-else>输入收货手机号查询您的订单</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useUserStore } from "../../../store/user";
import type { LoginForm } from "../../../store/user";

const router = useRouter();
const userStore = useUserStore();

const loginFormRef = ref<FormInstance>();
const phoneFormRef = ref<FormInstance>();
const loading = ref(false);
const loginType = ref<"account" | "phone">("account");

const loginForm = reactive<LoginForm>({
  username: "admin",
  password: "123456",
});

const phoneForm = reactive({
  phone: "",
});

const loginRules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" },
  ],
};

const phoneRules: FormRules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号",
      trigger: "blur",
    },
  ],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await userStore.login(loginForm.username, loginForm.password);
        ElMessage.success("登录成功");
        router.push("/dashboard");
      } catch (error) {
        ElMessage.error("登录失败，请检查用户名和密码");
      } finally {
        loading.value = false;
      }
    }
  });
};

const handlePhoneLogin = async () => {
  if (!phoneFormRef.value) return;

  await phoneFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await userStore.loginByPhone(phoneForm.phone);
        ElMessage.success("验证成功");
        router.push("/buyer/orders");
      } catch (error) {
        ElMessage.error("验证失败，请检查手机号");
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.1) 1px,
    transparent 1px
  );
  background-size: 50px 50px;
  animation: moveBackground 20s linear infinite;
}

@keyframes moveBackground {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

.login-box {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: var(--text-tertiary);
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
}

.login-tips {
  margin-top: 20px;
  text-align: center;
}
.login-tips p {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.login-tabs {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 24px;
}

.tab-item {
  font-size: 15px;
  color: var(--text-secondary);
  cursor: pointer;
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #6366f1;
}

.tab-item.active {
  color: #6366f1;
  border-bottom-color: #6366f1;
  font-weight: 500;
}
</style>

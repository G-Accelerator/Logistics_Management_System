<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="logo-area">
          <div class="logo-icon">
            <svg
              viewBox="0 0 48 48"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M8 16L24 8L40 16V32L24 40L8 32V16Z"
                stroke="currentColor"
                stroke-width="2"
                fill="rgba(255,255,255,0.1)"
              />
              <path
                d="M24 8V40"
                stroke="currentColor"
                stroke-width="2"
                stroke-dasharray="4 2"
              />
              <circle cx="24" cy="24" r="4" fill="currentColor" />
              <circle cx="12" cy="20" r="2" fill="currentColor" opacity="0.6" />
              <circle cx="36" cy="20" r="2" fill="currentColor" opacity="0.6" />
              <circle cx="12" cy="28" r="2" fill="currentColor" opacity="0.6" />
              <circle cx="36" cy="28" r="2" fill="currentColor" opacity="0.6" />
            </svg>
          </div>
          <h1 class="system-name">物流轨迹追踪系统</h1>
          <p class="system-slogan">Logistics Tracking System</p>
        </div>

        <!-- 物流轨迹动画 -->
        <div class="track-animation">
          <svg viewBox="0 0 400 200" class="track-svg">
            <defs>
              <linearGradient
                id="trackGradient"
                x1="0%"
                y1="0%"
                x2="100%"
                y2="0%"
              >
                <stop
                  offset="0%"
                  style="stop-color: #38bdf8; stop-opacity: 0.3"
                />
                <stop
                  offset="50%"
                  style="stop-color: #22d3ee; stop-opacity: 1"
                />
                <stop
                  offset="100%"
                  style="stop-color: #38bdf8; stop-opacity: 0.3"
                />
              </linearGradient>
            </defs>
            <path
              class="track-path"
              d="M20 100 Q100 40 200 100 T380 100"
              stroke="url(#trackGradient)"
              stroke-width="3"
              fill="none"
            />
            <circle class="track-dot" cx="20" cy="100" r="6" fill="#38bdf8" />
            <circle
              class="track-dot dot-2"
              cx="200"
              cy="100"
              r="6"
              fill="#22d3ee"
            />
            <circle
              class="track-dot dot-3"
              cx="380"
              cy="100"
              r="6"
              fill="#38bdf8"
            />
            <circle class="moving-dot" r="8" fill="#ffffff">
              <animateMotion dur="4s" repeatCount="indefinite">
                <mpath href="#movePath" />
              </animateMotion>
            </circle>
            <path
              id="movePath"
              d="M20 100 Q100 40 200 100 T380 100"
              fill="none"
            />
          </svg>
        </div>

        <!-- 功能亮点 -->
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">📍</div>
            <div class="feature-text">
              <h4>实时追踪</h4>
              <p>全程可视化物流轨迹</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔔</div>
            <div class="feature-text">
              <h4>智能提醒</h4>
              <p>关键节点自动通知</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">
              <h4>数据分析</h4>
              <p>物流效率一目了然</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-section">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户继续操作</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <span
            :class="['tab-item', { active: loginType === 'account' }]"
            @click="loginType = 'account'"
          >
            管理员
          </span>
          <span
            :class="['tab-item', { active: loginType === 'buyer' }]"
            @click="loginType = 'buyer'"
          >
            收货人
          </span>
          <span
            :class="['tab-item', { active: loginType === 'seller' }]"
            @click="loginType = 'seller'"
          >
            发货人
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
              登录系统
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 手机号登录 -->
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
              :placeholder="
                loginType === 'buyer' ? '请输入收货手机号' : '请输入发货手机号'
              "
              prefix-icon="Phone"
              size="large"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item prop="code">
            <div class="code-input">
              <el-input
                v-model="phoneForm.code"
                placeholder="请输入验证码"
                prefix-icon="Message"
                size="large"
                maxlength="6"
              />
              <el-button
                size="large"
                :loading="sendingCode"
                :disabled="countdown > 0"
                class="code-btn"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : "获取验证码" }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handlePhoneLogin"
            >
              {{ loginType === "buyer" ? "查询我的订单" : "查询发货记录" }}
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="loginType !== 'account'" class="login-tips">
          <p v-if="loginType === 'buyer'">
            <span class="tip-icon">📱</span> 输入收货手机号查询您的订单
          </p>
          <p v-else>
            <span class="tip-icon">📱</span> 输入发货手机号查询发货记录
          </p>
        </div>
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
import { sendVerifyCode } from "../../../api/auth";
import type { LoginForm } from "../../../store/user";

const router = useRouter();
const userStore = useUserStore();

const loginFormRef = ref<FormInstance>();
const phoneFormRef = ref<FormInstance>();
const loading = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);
const loginType = ref<"account" | "buyer" | "seller">("account");

const loginForm = reactive<LoginForm>({
  username: "",
  password: "",
});

const phoneForm = reactive({
  phone: "",
  code: "",
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
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { len: 6, message: "验证码为6位数字", trigger: "blur" },
  ],
};

const handleSendCode = async () => {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning("请先输入正确的手机号");
    return;
  }

  sendingCode.value = true;
  try {
    await sendVerifyCode(phoneForm.phone.trim());
    ElMessage.success("验证码已发送，请注意查收");
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) clearInterval(timer);
    }, 1000);
  } catch (error) {
    ElMessage.error("发送验证码失败");
  } finally {
    sendingCode.value = false;
  }
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
        const role = loginType.value === "buyer" ? "buyer" : "seller";
        await userStore.loginByPhone(
          phoneForm.phone.trim(),
          phoneForm.code.trim(),
          role,
        );
        ElMessage.success("验证成功");
        router.push(
          loginType.value === "buyer" ? "/buyer/orders" : "/seller/shipment",
        );
      } catch {
        // 错误文案由 request 拦截器展示（如验证码错误或已过期）
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-secondary);
}

/* 左侧品牌区 */
.brand-section {
  flex: 1.2;
  background: var(--gradient-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(
      circle at 20% 80%,
      rgba(14, 165, 233, 0.15) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 80% 20%,
      rgba(6, 182, 212, 0.15) 0%,
      transparent 50%
    );
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #ffffff;
  max-width: 480px;
}

.logo-area {
  margin-bottom: 48px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  color: #38bdf8;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.system-name {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.system-slogan {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  letter-spacing: 4px;
  text-transform: uppercase;
}

/* 轨迹动画 */
.track-animation {
  margin: 48px 0;
  padding: 20px 0;
}

.track-svg {
  width: 100%;
  max-width: 400px;
  height: 120px;
}

.track-path {
  stroke-dasharray: 8 4;
  animation: dashMove 20s linear infinite;
}

@keyframes dashMove {
  to {
    stroke-dashoffset: -240;
  }
}

.track-dot {
  animation: pulse 2s ease-in-out infinite;
}

.dot-2 {
  animation-delay: 0.5s;
}
.dot-3 {
  animation-delay: 1s;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.moving-dot {
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.8));
}

/* 功能亮点 */
.features {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 48px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
}

.feature-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(10px);
}

.feature-text h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
}

.feature-text p {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

/* 右侧登录区 */
.login-section {
  flex: 0.8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #ffffff;
}

.login-box {
  width: 100%;
  max-width: 380px;
}

.login-header {
  margin-bottom: 32px;
}

.login-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: var(--text-tertiary);
}

/* 登录标签 */
.login-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  background: var(--bg-tertiary);
  padding: 4px;
  border-radius: var(--radius-md);
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 10px 16px;
  border-radius: 6px;
  transition: all var(--transition-normal);
}

.tab-item:hover {
  color: var(--primary-color);
}

.tab-item.active {
  background: #ffffff;
  color: var(--primary-color);
  font-weight: 500;
  box-shadow: var(--shadow-sm);
}

/* 表单 */
.login-form {
  margin-top: 8px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: none;
  border: 1px solid var(--border-color);
  transition: all var(--transition-normal);
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: var(--primary-light);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.login-button {
  width: 100%;
  height: 44px;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 500;
  transition: all var(--transition-normal);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-primary);
}

.code-input {
  display: flex;
  gap: 12px;
  width: 100%;
}

.code-input .el-input {
  flex: 1;
}

.code-btn {
  width: 120px;
  flex-shrink: 0;
  background: transparent;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
  border-radius: var(--radius-md);
}

.code-btn:hover {
  background: rgba(14, 165, 233, 0.1);
}

.code-btn:disabled {
  background: transparent;
  border-color: var(--border-color);
  color: var(--text-tertiary);
}

/* 提示 */
.login-tips {
  margin-top: 24px;
  text-align: center;
}

.login-tips p {
  margin: 0;
  font-size: 13px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tip-icon {
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .login-page {
    flex-direction: column;
  }

  .brand-section {
    flex: none;
    padding: 48px 24px;
  }

  .features {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }

  .login-section {
    flex: 1;
    padding: 32px 24px;
  }
}

@media (max-width: 640px) {
  .system-name {
    font-size: 24px;
  }

  .track-animation {
    margin: 32px 0;
  }

  .login-box {
    max-width: 100%;
  }
}
</style>

<template>
  <div class="login-body">
    <div class="bg"></div>
    <div class="login-panel">
      <el-form class="login-register" :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>

        <div class="login-title">一度网盘</div>

        <!--input输入-->
        <el-form-item prop="email">
          <el-input size="large" clearable placeholder="请输入邮箱" v-model.trim="formData.email" maxLength="150">
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 登录密码 -->
        <el-form-item prop="password" v-if="opType == 1">
          <el-input type="password" size="large" placeholder="请输入密码" v-model="formData.password" show-password>
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- 注册或忘记密码 -->
        <div v-if="opType == 0 || opType == 2">
          <el-form-item prop="emailCode">
            <div class="send-email-panel">
              <el-input size="large" placeholder="请输入邮箱验证码" v-model="formData.emailCode">
                <template #prefix>
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <el-button class="send-mail-btn" type="primary" size="large" @click="getEmailCode">获取验证码
              </el-button>
            </div>
            <el-popover placement="left" :width="500" trigger="click">
              <div>
                <p>1、在垃圾箱中查找邮箱验证码</p>
                <p>2、在邮箱中头像->设置->反垃圾->白名单->设置邮件地址白名单</p>
                <P>3、将邮箱添加到白名单不知道怎么设置？</P>
              </div>
              <template #reference>
                <span class="a-link" :style="{ 'font-size': '14px' }">
                  未收到邮箱验证码？</span>
              </template>
            </el-popover>
          </el-form-item>

          <!-- 昵称 -->
          <el-form-item prop="nickName" v-if="opType == 0">
            <el-input size="large" clearable placeholder="请输入昵称" v-model="formData.nickName" maxlength="20">
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- 输入密码 -->
          <el-form-item prop="registerPassword">
            <el-input type="password" size="large" placeholder="请输入密码" v-model="formData.registerPassword" show-password>
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- 重新输入密码 -->
          <el-form-item prop="reRegisterPassword">
            <el-input type="password" size="large" placeholder="请再次输入密码" v-model="formData.reRegisterPassword"
              show-password>
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <!-- 验证码 -->
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" placeholder="请输入验证码" v-model="formData.checkCode" @keyup.enter="doSubmit">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl" class="check-code" @click="changeCheckCode(0)" />
          </div>
        </el-form-item>

        <!-- 跳转链接 -->
        <el-form-item v-if="opType == 1">
          <div class="rememberme-panel">
            <el-checkbox v-model="formData.rememberMe">记住我</el-checkbox>
          </div>
          <div class="no-account">
            <a href="javascript:void(0)" class="a-link" @click="showPanel(2)"> 忘记密码？</a>
            <a href="javascript:void(0)" class="a-link" @click="showPanel(0)"> 没有账号? </a>
          </div>
        </el-form-item>
        <el-form-item v-if="opType == 0">
          <a href="javascript:void(0)" class="a-link" @click="showPanel(1)"> 已有账号？</a>
        </el-form-item>
        <el-form-item v-if="opType == 2">
          <a href="javascript:void(0)" class="a-link" @click="showPanel(1)"> 去登录? </a>
        </el-form-item>

        <!-- 确定按钮 -->
        <el-form-item>
          <el-button type="primary" class="op-btn" @click="doSubmit" size="large">
            <span v-if="opType == 0">注册</span>
            <span v-if="opType == 1">登录</span>
            <span v-if="opType == 2">重置密码</span>
          </el-button>
        </el-form-item>

      </el-form>

    </div>

    <!--发送邮箱验证码-->
    <Dialog :show="dialogConfig4SendMailCode.show" :title="dialogConfig4SendMailCode.title"
      :buttons="dialogConfig4SendMailCode.buttons" width="500px" :showCancel="false"
      @close="dialogConfig4SendMailCode.show = false">
      <el-form :model="formData4SendMailCode" :rules="rules" ref="formData4SendMailCodeRef" label-width="80px">
        <el-form-item label="邮箱">
          {{ formData.email }}
        </el-form-item>

        <el-form-item label="验证码" prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" placeholder="请输入验证码" v-model="formData4SendMailCode.checkCode">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl4SendMailCode" class="check-code" @click="changeCheckCode(1)" />
          </div>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue"
import { useRouter, useRoute } from "vue-router";
import md5 from "js-md5";

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const api = {
  checkCode: "/api/checkCode",
  sendEmailCode: "/sendEmailCode",
  register: "/register",
  login: "/login",
  resetPwd: "/resetPwd",
  /*   qqlogin: "/qqlogin", */
}


//0:注册 1:登录 2:重置密码
const opType = ref();
const showPanel = (type) => {
  opType.value = type;
  resetForm();
};

onMounted(() => {
  showPanel(1);
});


//验证码(两个界面的验证码独立)
const checkCodeUrl = ref(api.checkCode);
const checkCodeUrl4SendMailCode = ref(api.checkCode);
const changeCheckCode = (type) => {
  if (type == 0) {
    checkCodeUrl.value = api.checkCode
      + "?type=" + type + "&time=" + new Date().getTime();
  } else {
    checkCodeUrl4SendMailCode.value = api.checkCode
      + "?type=" + type + "&time=" + new Date().getTime();
  }
};

/* 发送邮箱验证码弹窗 */
const formData4SendMailCode = ref({});
const formData4SendMailCodeRef = ref();
const dialogConfig4SendMailCode = reactive(
  {
    show: false,
    title: "发送邮箱验证码",
    buttons: [
      {
        type: "primary",
        text: "发送验证码",
        click: () => {
          sendEmailCode();
        },
      },
    ],
  }
);

//获取邮箱验证码
const getEmailCode = () => {
  formDataRef.value.validateField("email", (valid) => {
    if (!valid) {
      return;
    }
    dialogConfig4SendMailCode.show = true;/* 判断邮箱格式是否正确 */

    nextTick(() => {
      changeCheckCode(1);/* 将验证码换掉 */
      formData4SendMailCodeRef.value.resetFields();/* 重置表单值 */
      formData4SendMailCode.value = {
        email: formData.value.email,/* 将登录界面的邮箱传到验证界面 */
      };
    });
  })
}

/* 发送邮件，要后端联调 */
const sendEmailCode = () => {
  formData4SendMailCodeRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    const params = Object.assign({}, formData4SendMailCode.value);
    params.type = opType.value == 0 ? 0 : 1;
    let result = await proxy.Request({
      url: api.sendEmailCode,
      params: params,
      errorCallback: () => {
        changeCheckCode(1);
      },
    });
    if (!result) {
      return;
    }
    proxy.Message.success("验证码发送成功，请登录邮箱查看");
    dialogConfig4SendMailCode.show = false;
  });
};

//登录，注册 弹出配置
const dialogConfig = reactive({
  show: false,
  title: "标题",
});

/* 检查密码的再次输入 */
const checkRePassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

const formData = ref({});
const formDataRef = ref();

/* 检查各种输入的规则 */
const rules = {
  email: [
    { required: true, message: "请输入邮箱" },
    { validator: proxy.Verify.email, message: "请输入正确的邮箱" },
  ],
  password: [{ required: true, message: "请输入密码" }],
  emailCode: [{ required: true, message: "请输入邮箱验证码" }],
  nickName: [{ required: true, message: "请输入昵称" }],
  registerPassword: [
    { required: true, message: "请输入密码" },
    {
      validator: proxy.Verify.password,
      message: "密码只能是数字，字母，特殊字符 8-18位",
    },
  ],
  reRegisterPassword: [
    { required: true, message: "请再次输入密码" },
    {
      validator: checkRePassword,
      message: "两次输入的密码不一致",
    },
  ],
  checkCode: [{ required: true, message: "请输入图片验证码" }],
};


/* 重置表单 */
const resetForm = () => {
  dialogConfig.show = true;
  if (opType.value == 0) {
    dialogConfig.title = "注册";
  } else if (opType.value == 1) {
    dialogConfig.title = "登录";
  } else if (opType.value == 2) {
    dialogConfig.title = "重置密码";
  }
  nextTick(() => {
    changeCheckCode(0);
    formDataRef.value.resetFields();
    formData.value = {};

    //登录
    if (opType.value == 1) {
      const cookieLoginInfo = proxy.VueCookies.get("loginInfo");
      if (cookieLoginInfo) {
        formData.value = cookieLoginInfo;
      }
    }
  });
};

// 登录、注册、重置密码  提交表单
/**
 * 提交表单数据，根据操作类型（注册、登录、重置密码）执行相应的逻辑。
 * 该函数首先验证表单数据的有效性，然后根据操作类型打包参数，并发起网络请求。
 * 根据请求结果，执行相应的成功或失败逻辑。
 */
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);

    /* 根据操作类型（注册、登录）调整参数 */
    if (opType.value == 0 || opType.value == 2) {
      // 注册或重置密码时，将密码字段重新命名并移除重复密码字段
      params.password = params.registerPassword;
      delete params.registerPassword;
      delete params.reRegisterPassword;
    }

    /* 根据操作类型确定是否对密码进行加密 */
    if (opType.value == 1) {
      // 登录时，尝试从cookie获取登录信息，并根据输入密码与cookie中的密码判断是否需要加密
      let cookieLoginInfo = proxy.VueCookies.get("loginInfo");
      let cookiePassword =
        cookieLoginInfo == null ? null : cookieLoginInfo.password;
      if (params.password !== cookiePassword) {
        params.password = md5(params.password);
      }
    }

    /* 根据操作类型发起相应的请求 */
    let url = null;
    if (opType.value == 0) {
      url = api.register;
    } else if (opType.value == 1) {
      url = api.login;
    } else if (opType.value == 2) {
      url = api.resetPwd;
    }

    /* 发起网络请求 */
   /**
     * 异步请求操作，封装了基本的请求逻辑。
     * @param {string} url - 请求的URL。
     * @param {object} params - 请求的参数。
     * @returns {Promise<any>} 返回一个Promise对象，成功时resolve包含请求结果，失败时reject。
     */
    let result = await proxy.Request({
      url: url,
      params: params,
      errorCallback: () => {
        // 当请求失败时，调用changeCheckCode函数重置验证码
        changeCheckCode(0);
      },
    });

    // 若请求结果不存在，则直接返回
    if (!result) {
      return;
    }

    /* 根据操作类型执行相应的成功逻辑 */
    if (opType.value == 0) {
      // 注册成功
      proxy.Message.success("注册成功,请登录");
      showPanel(1);
    } else if (opType.value == 1) {
      // 登录成功，选择是否记住我，并存储登录信息到cookie，重定向到原页面或登录页
      if (params.rememberMe) {
        const loginInfo = {
          email: params.email,
          password: params.password,
          rememberMe: params.rememberMe,
        };
        proxy.VueCookies.set("loginInfo", loginInfo, "7d");
      } else {
        proxy.VueCookies.remove("loginInfo");
      }
      dialogConfig.show = false;
      proxy.Message.success("登录成功");
      proxy.VueCookies.set("userInfo", result.data, 0);
      const redirectUrl = route.query.redirectUrl || "/";
      router.push(redirectUrl);
    } else if (opType.value == 2) {
      // 重置密码成功
      proxy.Message.success("重置密码成功,请登录");
      showPanel(1);
    }
  });
};

</script>

<style lang="scss" scoped>
/* 登录主体 */
.login-body {
  height: calc(100vh);
  background-size: cover;
  background: url("../assets/bg.png");
  display: flex;

  /* 登录图片 */
  .bg {
    flex: 1;
    background-size: cover;
    background-position: center;
    background-size: 800px;
    background-repeat: no-repeat;
    background-image: url("../assets/bg1.png");
  }

  /* 登录框 */
  .login-panel {
    width: 430px;
    margin-right: 15%;
    margin-top: calc((100vh - 500px) / 2);

    /* 登录注册 */
    .login-register {
      padding: 25px;
      background: #fff;
      border-radius: 5px;

      /* 标题 */
      .login-title {
        text-align: center;
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 20px;
      }

      /* 邮箱验证码 */
      .send-email-panel {
        display: flex;
        width: 100%;
        justify-content: space-between;

        /* 邮箱验证码的按钮 */
        .send-mail-btn {
          margin-left: 5px;
        }
      }

      /* 记住我选项 */
      .rememberme-panel {
        width: 100%;
      }

      /* 忘记密码？没有账号？*/
      .no-account {
        width: 100%;
        display: flex;
        justify-content: space-between;
      }

      /* 确定按钮 */
      .op-btn {
        width: 100%;
      }
    }
  }

  /* 图片验证码 */
  .check-code-panel {
    width: 100%;
    display: flex;

    .check-code {
      margin-left: 5px;
      cursor: pointer;
    }
  }

  .login-btn-qq {
    margin-top: 20px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      cursor: pointer;
      margin-left: 10px;
      width: 20px;
    }
  }
}
</style>
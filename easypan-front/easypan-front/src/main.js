
import { createApp } from 'vue'
import App from './App.vue'
/* import App from './views/Test.vue' */
import router from './router'
//引入element plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
//图标 图标在附件中
import './assets/icon/iconfont.css'
import './assets/base.scss'
//引入cookies
import VueCookies from 'vue-cookies'

import Request from '@/utils/Request'
import Message from '@/utils/Message'
import Confirm from '@/utils/Confirm'
import Verify from '@/utils/verify'
import Utils from '@/utils/Utils'

//自定义组件
import Icon from "@/components/Icon.vue"
import Table from '@/components/Table.vue'
import Dialog from '@/components/Dialog.vue'
import Avatar from '@/components/Avatar.vue'
import NoData from '@/components/NoData.vue'
import FolderSelect from '@/components/FolderSelect.vue'
import Navigation from '@/components/Navigation.vue'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)

app.component("Icon", Icon);
app.component("Table", Table);
app.component("Dialog", Dialog);
app.component("Avatar", Avatar);
app.component("NoData", NoData);
app.component("FolderSelect", FolderSelect);
app.component("Navigation", Navigation);

//配置全局变量
app.config.globalProperties.Request = Request;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Confirm = Confirm;
app.config.globalProperties.Verify=Verify;
app.config.globalProperties.Utils = Utils;
app.config.globalProperties.VueCookies = VueCookies;
app.config.globalProperties.globalInfo = {
    avatarUrl: "/api/getAvatar/",
    imageUrl: "/api/file/getImage/"
}

app.mount('#app')

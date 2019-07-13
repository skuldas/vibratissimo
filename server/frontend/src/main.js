import Vue from 'vue'
import App from './App.vue'

Vue.config.productionTip = false

import BootstrapVue from 'bootstrap-vue'

Vue.use(BootstrapVue)

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

import VueNativeSock from 'vue-native-websocket'
Vue.use(VueNativeSock, "wss://"+window.location.hostname+":"+window.location.port+"/vibro/socket/page")

import VueSlider from 'vue-slider-component'
import 'vue-slider-component/theme/default.css'

Vue.component('VueSlider', VueSlider)

new Vue({
  render: h => h(App),
}).$mount('#app')

// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import iView from 'iview'
import '!style-loader!css-loader!less-loader!./theme/index.less'
import Web3 from 'web3'
Vue.prototype.Web3 = Web3

Vue.use(iView)

/* eslint-disable no-new */
new Vue({
    el: '#app',
    router,
    template: '<App/>',
    components: { App }
})

<template>
  <div class="basic-table vm-margin">
    <VmTable title="我的数字藏品版权" :columns="dataColumns" :data="dataTable"></VmTable>
    
    <!-- 发布版权弹窗 -->
    <Modal v-model="publishModal" title="发布版权到交易市场" width="500">
      <div v-if="selectedCopyright" class="publish-form">
        <div class="copyright-info">
          <h3>{{ selectedCopyright.title }}</h3>
          <p><strong>描述：</strong> {{ selectedCopyright.description }}</p>
          <p><strong>类别：</strong> {{ selectedCopyright.category }}</p>
          <p><strong>创建时间：</strong> {{ formatDateTime(selectedCopyright.createdTime) }}</p>
          
          <div class="copyright-image" v-if="selectedCopyright.imgUrl">
            <img :src="'/api/uploads/' + selectedCopyright.imgUrl" alt="版权图片" style="max-width: 100%; max-height: 200px;">
          </div>
          
          <div class="price-input">
            <p><strong>请输入发布价格：</strong></p>
            <InputNumber 
              v-model="publishPrice" 
              :min="0.01" 
              :step="0.1" 
              style="width: 200px"
              placeholder="请输入价格">
            </InputNumber>
          </div>
        </div>
      </div>
      <div slot="footer">
        <Button @click="publishModal = false" :disabled="publishing">取消</Button>
        <Button 
          type="primary" 
          @click="confirmPublish" 
          :disabled="!publishPrice || publishing"
          :loading="publishing">
          {{ publishing ? '交易处理中...' : '确认发布' }}
        </Button>
      </div>
    </Modal>
  </div>
</template>

<script>
  import VmTable from '@/components/vm-table'
  import axios from 'axios'
  import Web3 from 'web3'
  import { abi } from '../contracts/CopyrightNFT.json'
  import { contractAddress } from '../contracts/config'

  export default {
    name: 'BasicTable',
    components: {
      VmTable
    },
    data () {
      return {
        dataColumns: [
          {
            id: '2.140710',
            title: 'ID',
            key: 'id',
            sortable: true
          },
          {
            id: '2.140711',
            title: '标题',
            key: 'title',
            sortable: true
          },
          {
            id: '2.140712',
            title: '状态',
            key: 'status',
            sortable: true
          },
          {
            id: '2.140713',
            title: '类别',
            key: 'category',
            sortable: true
          },
          {
            id: '2.140714',
            title: '所有者',
            key: 'username',
            sortable: true
          },
          {
            id: '2.140715',
            title: '创建时间',
            key: 'createdTime',
            sortable: true,
            render: (h, params) => {
              return h('div', this.formatDateTime(params.row.createdTime))
            }
          },
          {
            id: '2.140716',
            title: '操作',
            key: 'action',
            width: 150,
            align: 'center',
            render: (h, params) => {
              return h('div', [
                h('Button', {
                  props: {
                    type: 'primary',
                    size: 'small',
                    disabled: params.row.status !== 'APPROVED'
          },
                  on: {
                    click: () => {
                      this.openPublishModal(params.row)
                    }
                  }
                }, '发布')
              ])
            }
          }
        ],
        dataTable: [],
        publishModal: false,
        selectedCopyright: null,
        publishPrice: null,
        publishing: false,
        web3: null,
        contract: null,
        currentUser: null,
        refreshInterval: null
      }
    },
    async created() {
      await this.initWeb3()
      // 从sessionStorage获取用户信息
      const userStr = sessionStorage.getItem('user')
      if (userStr) {
        try {
          this.currentUser = JSON.parse(userStr)
        } catch (e) {
          console.error('Failed to parse user info:', e)
        }
      }
    },
    mounted() {
      this.loadUserCopyrights()
      // 设置定时刷新，每30秒更新一次数据
      this.refreshInterval = setInterval(() => {
        this.refreshData()
      }, 30000)
    },
    activated() {
      this.loadUserCopyrights()
    },
    beforeDestroy() {
      // 组件销毁前清除定时器
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
      }
    },
    methods: {
      async initWeb3() {
        if (window.ethereum) {
          try {
            // 请求用户授权
            await window.ethereum.request({ method: 'eth_requestAccounts' })
            this.web3 = new Web3(window.ethereum)
            // 初始化合约
            this.contract = new this.web3.eth.Contract(abi, contractAddress)
          } catch (error) {
            console.error('Error initializing web3:', error)
          }
        } else {
          console.warn('MetaMask not detected')
        }
      },
      loadUserCopyrights() {
        if (!this.currentUser) {
        //   this.$Message.error('请先登录')
          return
        }
        // 使用用户ID查询其版权（优先），包含用户名信息
        if (this.currentUser.id) {
          axios.get(`/api/jdbc/copyright/user-id/${this.currentUser.id}/with-username`)
            .then(response => {
              if (response.data && response.data.length > 0) {
                this.dataTable = response.data
              } else {
                // 如果通过用户ID没有找到版权，尝试使用钱包地址
                this.tryFetchByAddress()
              }
            })
            .catch(error => {
              console.error('通过用户ID加载版权失败:', error)
              // 发生错误时尝试使用钱包地址
              this.tryFetchByAddress()
            })
        } else {
          // 如果没有用户ID，使用钱包地址
          this.tryFetchByAddress()
        }
      },
      tryFetchByAddress() {
        if (this.currentUser && this.currentUser.address) {
          axios.get(`/api/jdbc/copyright/user/${this.currentUser.address}/with-username`)
            .then(response => {
              this.dataTable = response.data
            })
            .catch(error => {
              console.error('通过钱包地址加载版权失败:', error)
              this.$Message.error('加载版权信息失败')
            })
        }
      },
      formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '';
        const date = new Date(dateTimeStr);
        return date.toLocaleString();
      },
      openPublishModal(copyright) {
        if (copyright.status !== 'APPROVED') {
          this.$Message.warning('只有审核通过的版权才能发布到交易市场');
          return;
        }
        this.selectedCopyright = copyright;
        this.publishPrice = null;
        this.publishModal = true;
      },
      async confirmPublish() {
        if (!this.publishPrice || this.publishPrice <= 0) {
          this.$Message.warning('请输入有效的价格')
          return
        }
        // 确认MetaMask钱包已连接
        if (!window.ethereum) {
          this.$Message.error('请安装MetaMask钱包插件后再发布版权')
          return
        }
        try {
          // 请求MetaMask授权
          if (!this.web3) {
            await this.initWeb3()
          }
          if (!this.web3) {
            this.$Message.error('MetaMask钱包连接失败，请确认已授权')
            return
          }
          // 获取钱包账户，验证是否授权成功
          const accounts = await this.web3.eth.getAccounts()
          if (!accounts || accounts.length === 0) {
            this.$Message.error('未检测到MetaMask钱包账户，请授权后再试')
            return
          }
          const account = accounts[0]
          this.publishing = true
          // 区块链交易结果标志
          let blockchainSuccess = false
          // 准备上架元数据
          const metadata = {
            id: this.selectedCopyright.id,
            title: this.selectedCopyright.title,
            description: this.selectedCopyright.description,
            price: this.publishPrice,
            owner: account,
            status: 'LISTED'
          }
          try {
            // 尝试区块链交易
            if (this.web3 && this.contract) {
              console.log('尝试区块链记录...')
              const methodNames = Object.keys(this.contract.methods)
              console.log('合约可用方法:', methodNames)
              const supportedMethods = ['mint', 'listToken', 'listItem', 'list']
              const method = supportedMethods.find(m => methodNames.includes(m))
              if (method) {
                console.log(`找到可用方法: ${method}，正在调用...`)
                let result;
                if (method === 'mint') {
                  // mint方法需要两个参数: 接收者地址和元数据
                  result = await this.contract.methods[method](account, JSON.stringify(metadata))
                    .send({ from: account })
                } else {
                  // 其他方法按原样调用
                  result = await this.contract.methods[method](JSON.stringify(metadata))
                    .send({ from: account })
                }
                console.log('区块链交易哈希:', result.transactionHash)
                blockchainSuccess = true
              } else {
                console.warn('未找到适用的合约方法，将跳过区块链记录')
                this.$Message.warning('未找到适合的区块链合约方法，但仍将继续处理')
              }
            }
          } catch (blockchainError) {
            console.error('区块链交易失败:', blockchainError)
            this.$Message.error('区块链交易失败: ' + blockchainError.message)
            this.publishing = false
            return // 如果MetaMask交易失败，中止整个流程
          }
          // 区块链交易成功后更新数据库
          axios.post(`/api/jdbc/copyright/${this.selectedCopyright.id}/list`, null, {
            params: {
              price: this.publishPrice
            }
          })
          .then(response => {
            this.$Message.success(blockchainSuccess
              ? '版权已成功上架交易市场并写入区块链！'
              : '版权已成功上架交易市场！')
            this.publishModal = false
            this.loadUserCopyrights()
          })
          .catch(error => {
            this.$Message.error('版权上架失败')
            console.error('API调用失败:', error)
          })
        } catch (error) {
          this.$Message.error('操作失败: ' + error.message)
          console.error('Unexpected error:', error)
        } finally {
          this.publishing = false
        }
      },
      refreshData() {
        console.log('刷新用户版权信息数据...')
        this.loadUserCopyrights()
      }
    }
  }
</script>

<style scoped>
  .basic-table {
    width: 100%;
  }
  
  .copyright-info {
    margin-bottom: 20px;
  }
  
  .copyright-info h3 {
    margin-bottom: 15px;
    color: #17233d;
  }
  
  .copyright-info p {
    margin-bottom: 10px;
  }
  
  .copyright-image {
    margin: 15px 0;
    text-align: center;
  }
  
  .price-input {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px solid #e8eaec;
  }
</style>

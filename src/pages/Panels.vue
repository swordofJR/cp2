<template>
   <div class="panel-form vm-margin">
    <Form :model="formItem" :label-width="100">
      <FormItem label="版权全称" required>
        <Input v-model="formItem.title" placeholder="请输入版权完整名称" style="width: 400px"></Input>
      </FormItem>
      
      <FormItem label="版权信息描述" required>
        <Input v-model="formItem.description" type="textarea" :rows="4" style="width: 400px"></Input>
      </FormItem>

      <FormItem label="版权图片" required>
        <Upload 
          ref="upload"
          :before-upload="handleBeforeUpload"
          :format="['jpg','jpeg','png']"
          :max-size="2048"
          :on-format-error="handleFormatError"
          :on-exceeded-size="handleSizeError"
          :show-upload-list="false"
          accept="image/*">
          <div class="upload-area">
            <Icon type="ios-cloud-done" size="52" style="color: #1890ff"></Icon>
            <p class="upload-text">点击上传图片</p>
            <div v-if="previewImage" class="preview-image">
              <img :src="previewImage" alt="预览">
              <p class="file-name">{{ fileName }}</p>
            </div>
          </div>
        </Upload>
      </FormItem>

      <FormItem label="版权类别" required>
        <Select v-model="formItem.category" style="width: 400px">
          <Option v-for="(cat, index) in categories" 
                 :value="cat" 
                 :key="index">{{ cat }}</Option>
        </Select>
      </FormItem>

      <!-- <FormItem label="交易起始价(ETH)" required>
        <InputNumber 
          v-model="formItem.startPrice" 
          :min="0.01" 
          :step="0.1"
          style="width: 400px"
          placeholder="输入起始价格"></InputNumber>
      </FormItem>

      <FormItem label="数字藏品铸造时间" required>
        <DatePicker 
          v-model="formItem.startTime" 
          type="datetime" 
          style="width: 400px"
          placeholder="请选择日期"></DatePicker>
      </FormItem>

      <FormItem label="交易持续时间" required>
        <InputNumber 
          v-model="formItem.duration" 
          :min="1" 
          :max="30"
          style="width: 400px"
          placeholder="输入天数"></InputNumber>
        <span class="ml-10">天</span>
      </FormItem> -->

      <FormItem>
        <Button 
          type="primary" 
          @click="submit"
          :loading="loading"
          :disabled="!formValid"
          style="width: 400px">
          提交版权信息
        </Button>
      </FormItem>
    </Form>

    <!-- Success Dialog -->
    <Modal
      v-model="showSuccessDialog"
      title="提交成功"
      @on-ok="handleSuccessOk"
      @on-cancel="handleSuccessOk">
      <p>您的版权信息已成功提交！</p>
      <p>区块链交易哈希: {{ transactionHash }}</p>
    </Modal>
  </div>
</template>

<script>
import Web3 from 'web3';
import axios from 'axios';
import { abi } from '../contracts/CopyrightNFT.json';
import { contractAddress } from '../contracts/config';

export default {
  data() {
    return {
      loading: false,
      showSuccessDialog: false,
      transactionHash: '',
      categories: ['艺术品', '收藏品', '电子产品', '宠物', '游戏', '生活', '旅行'],
      previewImage: '',
      fileName: '',
      fileObject: null,
      web3: null,
      contract: null,
      formItem: {
        title: '',
        description: '',
        imageData: null,
        category: '艺术品',
      }
    }
  },
  computed: {
    formValid() {
      return this.formItem.title &&
             this.formItem.description &&
             this.formItem.imageData &&
             this.fileObject
    }
  },
  async created() {
    await this.initWeb3();
    // 检查用户权限
    this.checkUserPermission();
  },
  methods: {
    async initWeb3() {
      if (window.ethereum) {
        try {
          // 请求用户授权
          await window.ethereum.request({ method: 'eth_requestAccounts' });
          this.web3 = new Web3(window.ethereum);
          // 初始化合约
          this.contract = new this.web3.eth.Contract(abi, contractAddress);
          console.log('MetaMask连接成功，合约初始化完成');
          // 获取当前账户
          const accounts = await this.web3.eth.getAccounts();
          if (accounts && accounts.length > 0) {
            console.log('当前连接的钱包地址:', accounts[0]);
          } else {
            console.warn('未检测到连接的钱包地址');
          }
          // 监听账户变化
          window.ethereum.on('accountsChanged', (accounts) => {
            console.log('钱包账户已变更:', accounts);
            if (accounts.length === 0) {
              this.$Message.warning('钱包已断开连接，请重新连接');
            }
          });
          // 监听网络变化
          window.ethereum.on('chainChanged', (chainId) => {
            console.log('区块链网络已变更:', chainId);
            this.$Message.info('区块链网络已变更，页面将刷新');
            window.location.reload();
          });
        } catch (error) {
          console.error('初始化Web3失败:', error);
          this.$Message.error('初始化区块链失败，请检查MetaMask是否正确安装并授权');
        }
      } else {
        console.warn('未检测到MetaMask插件');
        this.$Message.warning('请安装MetaMask钱包插件以启用完整功能');
      }
    },
    checkUserPermission() {
      const userStr = sessionStorage.getItem('user');
      if (!userStr) {
        this.$Message.error('请先登录');
        this.$router.push('/login');
        return;
      }
      try {
        const user = JSON.parse(userStr);
        if (user.role !== 'admin' && user.role !== 'user') {
          this.$Message.error('您没有上传版权的权限');
          this.$router.push('/');
        }
      } catch (e) {
        console.error('解析用户信息失败:', e);
        this.$router.push('/login');
      }
    },
    async submit() {
      if (!this.formValid) {
        this.$Message.warning('请完善所有必填信息');
        return;
      }
      this.loading = true;
      let blockchainSuccess = false;
      try {
        // 1. 准备数据
        const accounts = await this.web3.eth.getAccounts();
        const account = accounts[0];
        const metadata = {
          name: this.formItem.title,
          description: this.formItem.description,
          image: this.formItem.imageData,
          category: this.formItem.category
        };
        // 2. 尝试区块链操作
        try {
          if (this.web3 && this.contract) {
            console.log('尝试上链...');
            const methodNames = Object.keys(this.contract.methods);
            console.log('合约可用方法:', methodNames);
            if (methodNames.includes('mint')) {
              const result = await this.contract.methods
                .mint(account, JSON.stringify(metadata))
                .send({ from: account });
              console.log('Transaction hash:', result.transactionHash);
              this.transactionHash = result.transactionHash;
              blockchainSuccess = true;
            } else {
              console.warn('找不到mint方法，跳过区块链记录');
            }
          }
        } catch (blockchainError) {
          console.error('区块链交易失败:', blockchainError);
          // 区块链失败不影响后续数据库操作
        }
        // 3. 无论区块链成功与否，都上传到后端数据库
        await this.uploadToBackend(account);
        this.showSuccessDialog = true;
        this.$Message.success(blockchainSuccess
          ? '版权信息已成功上链并保存到数据库！'
          : '版权信息已保存！');
      } catch (error) {
        console.error('提交失败:', error);
        this.$Message.error('提交失败：' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    },
    async uploadToBackend(ownerAddress) {
      try {
        // 创建表单数据
        const formData = new FormData();
        formData.append('file', this.fileObject);
        formData.append('title', this.formItem.title);
        formData.append('description', this.formItem.description);
        formData.append('category', this.formItem.category);
        formData.append('ownerAddress', ownerAddress);
        // 从sessionStorage获取用户信息
        const userStr = sessionStorage.getItem('user');
        if (userStr) {
          try {
            const user = JSON.parse(userStr);
            if (user.id) {
              formData.append('userId', user.id);
              console.log('添加用户ID到版权信息:', user.id);
            }
          } catch (e) {
            console.error('解析用户信息失败:', e);
          }
        }
        // 发送到后端
        const response = await axios.post('/api/jdbc/copyright/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        console.log('Backend response:', response.data);
        return response.data;
      } catch (error) {
        console.error('Error uploading to backend:', error);
        throw error;
      }
    },
    handleBeforeUpload(file) {
      // 保存文件对象，用于后续上传到后端
      this.fileObject = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewImage = e.target.result;
        this.fileName = file.name;
        this.formItem.imageData = e.target.result;
      };
      reader.readAsDataURL(file);
      return false;
    },
    handleFormatError(file) {
      this.$Message.error('文件格式不正确，仅支持 JPG/PNG 格式');
    },
    handleSizeError(file) {
      this.$Message.error('文件大小超过2MB限制');
    },
    handleSuccessOk() {
      this.showSuccessDialog = false;
      this.formItem = {
        title: '',
        description: '',
        imageData: null,
        category: '艺术品',
      };
      this.previewImage = '';
      this.fileName = '';
      this.fileObject = null;
      this.transactionHash = '';
      // 刷新版权列表
      this.$router.push('/basic-table');
    }
  }
}
</script>

<style scoped>
.panel-form {
  text-align: left;
  position: relative;
  padding: 40px;
  background-image: url('../assets/img/bg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  min-height: 100vh;
}

.panel-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.5);
  z-index: 1;
}

.panel-form > * {
  position: relative;
  z-index: 2;
}

.upload-area {
  padding: 20px;
  text-align: left;
  border: 1px dashed #e8e8e8;
  border-radius: 4px;
  cursor: pointer;
  transition: all .3s ease;
  background-color: rgba(250, 250, 250, 0.9);
  width: 400px;
}

.upload-text {
  margin-top: 10px;
  text-align: left;
}

.upload-area:hover {
  border-color: #1890ff;
  background-color: #f0f7ff;
}

.preview-image {
  margin-top: 20px;
  text-align: left;
}

.preview-image img {
  max-width: 200px;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.file-name {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
  text-align: left;
}

.ml-10 {
  margin-left: 10px;
}

.ivu-form-item {
  margin-bottom: 24px;
}

.ivu-form-item-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.ivu-input-wrapper, .ivu-select, .ivu-date-picker, .ivu-input-number {
  text-align: left;
}
</style>

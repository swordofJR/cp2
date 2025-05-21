<template>
  <div class="widget">
    <!-- 用户积分信息 -->
    <Row :gutter="16" class="user-credit-section">
      <Col :span="24">
        <Card class="credit-card">
          <p slot="title">
            <Icon type="ios-analytics"></Icon>
            用户积分信息
          </p>
          <div class="credit-info">
            <h3>当前积分: <span class="credit-value">{{ userCredit }}</span></h3>
            <Alert v-if="userCredit < 60" type="error" show-icon>
              您的积分不足60分，无法参与数字藏品交易。每完成一笔交易可获得5积分。
            </Alert>
            <Alert v-else type="success" show-icon>
              您的积分良好，可以正常参与数字藏品交易。每完成一笔交易可获得5积分。
            </Alert>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- <Row class="vm-margin">
      <VmStateGroup :data="dataStateGroup">
      </VmStateGroup>
    </Row>
    <Row :gutter="16">
      <Col :lg="12">
        <VmCircle class="demo-circle" title="交易完成率" usedName="交易总次数" :usedValue="10" restName="成功交易次数" :restValue="5">
        </VmCircle>
      </Col>
      <Col :lg="12">
        <VmCircle class="demo-circle" title="信用值" usedName="当前信用值" :usedValue="70" restName="最高信用值" :restValue="100">
        </VmCircle>
      </Col>
    </Row> -->
    <Row :gutter="16">
      <Col :lg="12">
        <VmMessageCarousel :data="dataMessageCarouse1">
        </VmMessageCarousel>
      </Col>
      <Col :lg="12">
        <VmMessageCarousel :data="dataMessageCarouse1" :autoplay="false">
        </VmMessageCarousel>
      </Col>
    </Row>
    <Row>
      <h3 class="section-title">我的数字藏品</h3>
    </Row>
    <Row :gutter="16" v-if="userCopyrights.length > 0">
      <Col :lg="6" :md="12" v-for="item in userCopyrights" :key="item.id">
        <VmCard 
          :title="item.title" 
          :img="item.img" 
          :desc="item.description" 
          :detailUrl="item.detailUrl"
          :price="item.price"
          :category="item.category"
          :status="item.status"
          @click.native="showDetails(item)">
        </VmCard>  
      </Col>
    </Row>
    <Row v-else>
      <Col :span="24">
        <Alert type="info">您还没有数字藏品，可以在"上传版权"页面创建新的数字藏品</Alert>
      </Col>
    </Row>
    
    <!-- 数字藏品详情弹窗 -->
    <Modal v-model="detailsModal" title="数字藏品详情" width="700">
      <div v-if="selectedCopyright" class="details-container">
        <div class="copyright-image-container">
          <div class="image-wrapper">
            <img :src="selectedCopyright.img" 
                 class="details-image"
                 :class="{'zoomed': isImageZoomed}"
                 @click="toggleImageZoom" />
            <div class="zoom-hint" v-if="!isImageZoomed">点击图片放大</div>
            <div class="zoom-hint" v-else>点击图片缩小</div>
          </div>
        </div>
        <div class="copyright-info">
          <p><strong>标题：</strong> {{ selectedCopyright.title }}</p>
          <p><strong>描述：</strong> {{ selectedCopyright.description }}</p>
          <p><strong>类别：</strong> {{ selectedCopyright.category }}</p>
          <p><strong>状态：</strong> {{ selectedCopyright.status }}</p>
          <p v-if="selectedCopyright.price"><strong>价格：</strong> {{ selectedCopyright.price }} ETH</p>
          <p v-if="selectedCopyright.createdTime"><strong>创建时间：</strong> {{ formatDate(selectedCopyright.createdTime) }}</p>
        </div>
      </div>
      <div slot="footer">
        <Button type="primary" @click="detailsModal = false">关闭</Button>
      </div>
    </Modal>
    
    <Row class="section-space">
      <h3 class="section-title">示例藏品</h3>
    </Row>
    <Row :gutter="16">
      <Col :lg="6" :md="12" v-for="item in dataCards1" :key="item.id">
        <VmCard :title="item.title" :img="item.img" :desc="item.desc" :detailUrl="item.detailUrl"></VmCard>  
      </Col>
    </Row>
    <Row :gutter="16">
      <Col :lg="12" v-for="item in dataCards2" :key="item.id">
        <VmCard type="horizantal" :title="item.title" :img="item.img" :desc="item.desc" :detailUrl="item.detailUrl"></VmCard>  
      </Col>
    </Row>
  </div>
</template>

<script>
  import VmCard from '@/components/vm-card'
  import VmStateGroup from '@/components/vm-state-group'
  import VmMessageCarousel from '@/components/vm-message-carousel'
  import VmCircle from '@/components/vm-circle'
  import axios from 'axios'
  
  export default {
    name: 'Widget',
    components: {
      VmCard,
      VmStateGroup,
      VmMessageCarousel,
      VmCircle
    },
    data: function () {
      return {
        userCopyrights: [],
        currentUser: null,
        userCredit: 80, // 默认显示80积分
        detailsModal: false,
        selectedCopyright: null,
        isImageZoomed: false,
        dataCards1: [
          {
            id: '1234567981',
            title: '我的竞品1',
            img: require('@/assets/img/img-1.jpg'),
            desc: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s',
            detailUrl: '#'
          },
          {
            id: '1234567982',
            title: '我的竞品2',
            img: require('@/assets/img/img-2.jpg'),
            desc: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s',
            detailUrl: '#'
          },
          {
            id: '1234567983',
            title: '我的竞品3',
            img: require('@/assets/img/img-3.jpg'),
            desc: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s',
            detailUrl: '#'
          },
          {
            id: '1234567984',
            title: '我的竞品4',
            img: require('@/assets/img/img-4.jpg'),
            desc: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s',
            detailUrl: '#'
          }
        ],
        dataCards2: [
          {
            id: '6541231',
            title: '我的竞品5',
            img: require('@/assets/img/img-l-1.jpg'),
            desc: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s',
            detailUrl: '#'
          },
          {
            id: '6541232',
            title: '我的竞品6',
            img: require('@/assets/img/img-l-2.jpg'),
            desc: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s',
            detailUrl: '#'
          }
        ],
        // dataStateGroup: [
        //   {
        //     title: '浏览次数',
        //     icon: 'fa fa-eye',
        //     value: '666',
        //     color: '#41b883'
        //   },
        //   {
        //     title: '新用户',
        //     icon: 'fa fa-user',
        //     value: '666',
        //     color: '#428bca'
        //   },
        //   {
        //     title: '评价',
        //     icon: 'fa fa-pencil',
        //     value: '666',
        //     color: '#f60000'
        //   },
        //   {
        //     title: '询问',
        //     icon: 'fa fa-download',
        //     value: '666',
        //     color: '#ffa000'
        //   },
        //   {
        //     title: '照片',
        //     icon: 'fa fa-photo',
        //     value: '666',
        //     color: '#656565'
        //   }
        // ],
        dataMessageCarouse1: [
          {
            title: '温馨提示',
            content: '用户积分是您参与数字藏品交易的重要凭证，保持良好的交易记录可以提高您的积分。',
            photo: require('@/assets/img/photo.jpg'),
            name: 'JR'
          },
          {
            title: '温馨提示',
            content: '积分低于60分的用户将无法参与数字藏品交易，请保持良好的交易记录。每完成一笔交易，买家和卖家都将获得5积分的奖励。',
            photo: require('@/assets/img/photo.jpg'),
            name: 'JR'
          }
        ]
      }
    },
    created() {
      // 从sessionStorage获取用户信息
      const userStr = sessionStorage.getItem('user')
      if (userStr) {
        try {
          this.currentUser = JSON.parse(userStr)
          // 如果用户信息中已有积分信息，则直接使用
          if (this.currentUser && this.currentUser.credit !== undefined) {
            this.userCredit = this.currentUser.credit
          }
          this.loadUserCopyrights()
          this.loadUserCredit() // 加载用户积分
        } catch (e) {
          console.error('Failed to parse user info:', e)
        }
      }
    },
    methods: {
      showDetails(copyright) {
        this.selectedCopyright = copyright
        this.detailsModal = true
      },
      toggleImageZoom() {
        this.isImageZoomed = !this.isImageZoomed
      },
      formatDate(dateStr) {
        if (!dateStr) return '未知';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      },
      loadUserCredit() {
        if (!this.currentUser || !this.currentUser.id) {
          return
        }
        // 从服务器获取最新的用户积分
        axios.get(`/api/jdbc/user/${this.currentUser.id}/credit`)
          .then(response => {
            if (response.data && response.data.credit !== undefined) {
              this.userCredit = response.data.credit
              // 更新sessionStorage中的用户积分
              if (this.currentUser) {
                this.currentUser.credit = this.userCredit
                sessionStorage.setItem('user', JSON.stringify(this.currentUser))
              }
            }
          })
          .catch(error => {
            console.error('加载用户积分失败:', error)
          })
      },
      loadUserCopyrights() {
        if (!this.currentUser) {
          return
        }
        // 先清空当前藏品列表
        this.userCopyrights = []
        // 使用用户ID查询买家的藏品（状态为PENDING）
        if (this.currentUser.id) {
          axios.get(`/api/jdbc/copyright/user-id/${this.currentUser.id}/with-username`, {
            params: {
              status: 'PENDING'
            }
          })
          .then(response => {
            if (response.data && response.data.length > 0) {
              // 处理买家藏品数据
              const buyerCopyrights = this.processUserCopyrights(response.data)
              this.userCopyrights = [...this.userCopyrights, ...buyerCopyrights]
            }
            // 继续加载卖家藏品（状态为SOLD）
            this.loadSellerCopyrights()
          })
          .catch(error => {
            console.error('通过用户ID加载买家藏品失败:', error)
            // 发生错误时仍然尝试加载卖家藏品
            this.loadSellerCopyrights()
          })
        } else {
          // 如果没有用户ID，尝试使用钱包地址加载
          this.loadCopyrightsByAddress()
        }
      },
      loadSellerCopyrights() {
        // 使用用户ID查询卖家的藏品（状态为SOLD）
        if (this.currentUser && this.currentUser.id) {
          axios.get(`/api/jdbc/copyright/user-id/${this.currentUser.id}/with-username`, {
            params: {
              status: 'SOLD'
            }
          })
          .then(response => {
            if (response.data && response.data.length > 0) {
              // 处理卖家藏品数据
              const sellerCopyrights = this.processUserCopyrights(response.data)
              this.userCopyrights = [...this.userCopyrights, ...sellerCopyrights]
            }
          })
          .catch(error => {
            console.error('通过用户ID加载卖家藏品失败:', error)
          })
        }
      },
      loadCopyrightsByAddress() {
        // 如果没有用户ID，使用钱包地址分别加载买家和卖家藏品
        if (this.currentUser && this.currentUser.address) {
          // 加载买家藏品（PENDING）
          axios.get(`/api/jdbc/copyright/user/${this.currentUser.address}/with-username`, {
            params: {
              status: 'PENDING'
            }
          })
          .then(response => {
            if (response.data && response.data.length > 0) {
              const buyerCopyrights = this.processUserCopyrights(response.data)
              this.userCopyrights = [...this.userCopyrights, ...buyerCopyrights]
            }
            // 加载卖家藏品（SOLD）
            this.loadSellerCopyrightsByAddress()
          })
          .catch(error => {
            console.error('通过钱包地址加载买家藏品失败:', error)
            this.loadSellerCopyrightsByAddress()
          })
        }
      },
      loadSellerCopyrightsByAddress() {
        // 使用钱包地址加载卖家藏品（SOLD）
        if (this.currentUser && this.currentUser.address) {
          axios.get(`/api/jdbc/copyright/user/${this.currentUser.address}/with-username`, {
            params: {
              status: 'SOLD'
            }
          })
          .then(response => {
            if (response.data && response.data.length > 0) {
              const sellerCopyrights = this.processUserCopyrights(response.data)
              this.userCopyrights = [...this.userCopyrights, ...sellerCopyrights]
            }
          })
          .catch(error => {
            console.error('通过钱包地址加载卖家藏品失败:', error)
          })
        }
      },
      processUserCopyrights(data) {
        // 处理数据，添加图片URL，返回处理后的数组
        return data.map(item => {
          return {
            id: item.id,
            title: item.title,
            img: item.imgUrl ? `/api/uploads/${item.imgUrl}` : require('@/assets/img/bg.jpg'),
            description: item.description,
            price: item.price,
            category: item.category,
            status: item.status,
            createdTime: item.createdTime,
            detailUrl: '#'
          }
        })
      }
    }
  }
</script>

<style>
.user-credit-section {
  margin-bottom: 20px;
}
.credit-card {
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.1);
}
.credit-info {
  padding: 10px 0;
}
.credit-value {
  font-size: 22px;
  font-weight: bold;
  color: #41b883;
}
.section-space {
  margin-top: 30px;
}
.section-title {
  margin: 20px 0;
  padding-left: 10px;
  border-left: 3px solid #41b883;
}

/* 详情弹窗样式 */
.details-container {
  display: flex;
  flex-direction: column;
}

.copyright-image-container {
  width: 100%;
  text-align: center;
  margin-bottom: 20px;
}

.image-wrapper {
  position: relative;
  display: inline-block;
}

.details-image {
  max-width: 100%;
  max-height: 350px;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.details-image.zoomed {
  transform: scale(1.8);
  transform-origin: center;
}

.zoom-hint {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.5);
  color: #fff;
  padding: 5px 10px;
  border-radius: 5px;
  font-size: 12px;
  opacity: 0.8;
}

.copyright-info {
  margin-top: 10px;
}

.copyright-info p {
  margin-bottom: 10px;
  line-height: 1.6;
}
</style>

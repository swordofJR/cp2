<template>
  <div :class="[type === 'horizantal' ? 'vm-card-horizantal' : 'vm-card-vertical' , 'vm-panel']">
    <div class="card-img">
      <img :src="img" alt="" @error="handleImgError">
      <div v-if="editable && type == 'vertical'" class="control">
        <span class="edit">
          <i class="fa fa-shopping-bag" @click="openBuyModal"></i>
        </span>
      </div>
    </div>
    <div class="card-desc panel-body">
      <h2>{{ title }}</h2>
      <p>{{ desc }}</p>
      <div class="card-price" v-if="price">
        <span class="price-label">价格：</span>
        <span class="price-value">{{ price }} ETH</span>
      </div>
      <div class="card-owner" v-if="username">
        <span class="owner-label">所有者：</span>
        <span class="owner-value">{{ username }}</span>
      </div>
      <a :href="detailUrl">
        <!-- more > -->
      </a>
      <div class="card-actions" v-if="isAdmin && status === 'LISTED'">
        <Button type="error" size="small" @click="handleDelist">下架商品</Button>
      </div>
    </div>

    <!-- 购买确认弹窗 -->
    <Modal
      v-model="showBuyModal"
      title="确认购买"
      @on-ok="handleBuy"
      @on-cancel="cancelBuy">
      <div class="buy-modal-content">
        <div class="modal-item">
          <span class="label">藏品名称：</span>
          <span class="value">{{ title }}</span>
        </div>
        <div class="modal-item">
          <span class="label">拥有者：</span>
          <span class="value">{{ username || '未知用户' }}</span>
        </div>
        <div class="modal-item">
          <span class="label">钱包地址：</span>
          <span class="value">{{ ownerAddress || 'Unknown' }}</span>
        </div>
        <div class="modal-item">
          <span class="label">描述：</span>
          <span class="value">{{ desc }}</span>
        </div>
        <div class="modal-item">
          <span class="label">类别：</span>
          <span class="value">{{ category || 'Unknown' }}</span>
        </div>
        <div class="modal-item">
          <span class="label">价格：</span>
          <span class="value price-value">{{ price }} ETH</span>
        </div>
        <div class="modal-item">
          <span class="label">状态：</span>
          <span class="value status-available">{{ getStatusText(status) }}</span>
        </div>
      </div>
    </Modal>
  </div>
</template>
<script>
  export default {
    name: 'VmCard',
    props: {
      type: {
        type: String,
        default: 'vertical'
      },
      editable: {
        type: Boolean,
        default: false
      },
      id: {
        type: [Number, String],
        default: ''
      },
      title: {
        type: String,
        default: 'Title'
      },
      img: {
        type: String,
        default: require('@/assets/img/img-1.jpg')
      },
      desc: {
        type: String,
        default: 'Lorem Ipsum is simply dummy text of the printing and typesetting industry,Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s'
      },
      detailUrl: {
        type: String,
        default: '#'
      },
      editUrl: {
        type: String,
        default: '#'
      },
      price: {
        type: [Number, String],
        default: null
      },
      ownerAddress: {
        type: String,
        default: ''
      },
      category: {
        type: String,
        default: ''
      },
      status: {
        type: String,
        default: 'LISTED'
      },
      isAdmin: {
        type: Boolean,
        default: false
      }
    //   username: {
    //     type: String,
    //     default: '未知用户'
    //   }
    },
    data: function () {
      return {
        showBuyModal: false
      }
    },
    methods: {
      openBuyModal() {
        this.showBuyModal = true;
      },
      handleBuy() {
        this.$emit('edit', {
          title: this.title,
          desc: this.desc,
          price: this.price,
          ownerAddress: this.ownerAddress,
          category: this.category,
          status: this.status,
          username: this.username
        });
        this.showBuyModal = false;
      },
      cancelBuy() {
        this.showBuyModal = false;
      },
      handleDelist() {
        this.$emit('delist', {
          id: this.id,
          title: this.title
        });
      },
      handleImgError() {
        // Implement the logic to handle image loading failure
        // For example, you can set a default image
        this.img = require('@/assets/img/img-1.jpg');
      },
      getStatusText(status) {
        const statusMap = {
          'PENDING': '待审核',
          'APPROVED': '审核通过',
          'LISTED': '已发布',
          'REJECTED': '被驳回',
          'SOLD': '已出售'
        }
        return statusMap[status] || status
      }
    }
  }
</script>

<style lang="less" scoped>
.buy-modal-content {
  .modal-item {
    margin-bottom: 15px;
    display: flex;
    align-items: flex-start;
    
    .label {
      width: 80px;
      color: #666;
      font-weight: bold;
    }
    
    .value {
      flex: 1;
      color: #333;
    }
    
    .status-available {
      color: #19be6b;
    }
  }
}

.card-price {
  margin-top: 12px;
  font-weight: bold;
  
  .price-label {
    color: #666;
  }
  
  .price-value {
    color: #ff9900;
    font-size: 16px;
  }
}

.card-owner {
  margin-top: 8px;
  
  .owner-label {
    color: #666;
    font-weight: bold;
  }
  
  .owner-value {
    color: #2d8cf0;
  }
}

.card-actions {
  margin-top: 12px;
  text-align: right;
}
</style>

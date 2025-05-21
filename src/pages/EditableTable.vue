<template>
  <div>
    <VmTable title="审核版权申请" 
             type="review" 
           :columns="dataColumns" 
           :data="dataTable"
             v-on:details-ok="showDetails"
             v-on:approve-ok="approveCopyright"
             v-on:reject-ok="rejectCopyright"
           class="vm-margin">
  </VmTable>
    
    <!-- 版权详情弹窗 -->
    <Modal v-model="detailsModal" title="版权详情" width="700">
      <div v-if="selectedCopyright" class="details-container">
        <div class="copyright-image-container">
          <div class="image-wrapper">
            <img v-if="selectedCopyright.imgUrl" 
                 :src="'/api/uploads/' + selectedCopyright.imgUrl" 
                 class="details-image"
                 :class="{'zoomed': isImageZoomed}"
                 @click="toggleImageZoom"
                 onerror="this.src=require('../assets/img/bg.jpg')" />
            <img v-else 
                 :src="require('../assets/img/bg.jpg')" 
                 class="details-image"
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
          <p><strong>拥有者地址：</strong> {{ selectedCopyright.ownerAddress }}</p>
          <p><strong>拥有者ID：</strong> {{ selectedCopyright.userId }}</p>
          <p><strong>创建时间：</strong> {{ formatDate(selectedCopyright.createdTime) }}</p>
        </div>
      </div>
      <div slot="footer">
        <Button type="primary" @click="detailsModal = false">关闭</Button>
      </div>
    </Modal>
    
    <!-- 驳回原因弹窗 -->
    <Modal v-model="rejectModal" title="驳回原因">
      <Input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因..."></Input>
      <div slot="footer">
        <Button @click="rejectModal = false">取消</Button>
        <Button type="error" @click="confirmReject">确认驳回</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
  import VmTable from '@/components/vm-table'
  import axios from 'axios'
  
  export default {
    name: 'EditableTable',
    components: {
      VmTable
    },
    methods: {
      loadPendingCopyrights() {
        axios.get('/api/jdbc/copyright/pending')
          .then(response => {
            this.dataTable = response.data
          })
          .catch(error => {
            console.error('Failed to load pending copyrights:', error)
          })
      },
      showDetails(copyright) {
        this.selectedCopyright = copyright
        this.detailsModal = true
      },
      approveCopyright(copyright) {
        axios.post(`/api/jdbc/copyright/${copyright.id}/review`, null, {
          params: {
            status: 'APPROVED'
          }
        })
        .then(response => {
          this.$Message.success('版权已通过审核')
          this.loadPendingCopyrights()
        })
        .catch(error => {
          this.$Message.error('审核操作失败')
          console.error('Approval failed:', error)
        })
      },
      rejectCopyright(copyright) {
        this.copyrightToReject = copyright
        this.rejectModal = true
      },
      confirmReject() {
        if (!this.rejectReason) {
          this.$Message.warning('请输入驳回原因')
          return
        }
        axios.post(`/api/jdbc/copyright/${this.copyrightToReject.id}/review`, null, {
          params: {
            status: 'REJECTED',
            reason: this.rejectReason
          }
        })
        .then(response => {
          this.$Message.success('版权已驳回')
          this.rejectModal = false
          this.rejectReason = ''
          this.copyrightToReject = null
          this.loadPendingCopyrights()
        })
        .catch(error => {
          this.$Message.error('驳回操作失败')
          console.error('Rejection failed:', error)
        })
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
      }
    },
    data () {
      return {
        dataColumns: [
          {
            id: '20156541',
            title: 'ID',
            key: 'id'
          },
          {
            id: '20156542',
            title: '标题',
            key: 'title'
          },
          {
            id: '20156543',
            title: '状态',
            key: 'status'
          },
          {
            id: '20156544',
            title: '类别',
            key: 'category'
          }
        ],
        dataTable: [],
        selectedCopyright: null,
        detailsModal: false,
        rejectModal: false,
        rejectReason: '',
        copyrightToReject: null,
        isImageZoomed: false
      }
    },
    mounted() {
      this.loadPendingCopyrights()
    },
    activated() {
      // 当组件被激活时也重新加载数据
      this.loadPendingCopyrights()
    }
  }
</script>

<style scoped>
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

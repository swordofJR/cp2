<template>
  <div class="report-form vm-margin">
    <Card title="提交举报信息" style="width: 100%">
      <Form :model="formItem" :label-width="100">
        <FormItem label="被举报用户ID" required>
          <Input v-model="formItem.reportedUserId" placeholder="请输入被举报用户ID" style="width: 400px"></Input>
        </FormItem>
        
        <FormItem label="版权ID" required>
          <Input v-model="formItem.copyrightId" placeholder="请输入涉及的版权ID" style="width: 400px"></Input>
        </FormItem>
        
        <FormItem label="版权名称" required>
          <Input v-model="formItem.copyrightTitle" placeholder="请输入版权名称" style="width: 400px"></Input>
        </FormItem>
        
        <FormItem label="举报原因" required>
          <Input v-model="formItem.reason" type="textarea" :rows="4" style="width: 400px" placeholder="请详细描述举报原因"></Input>
        </FormItem>

        <FormItem label="证据图片">
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
              <p class="upload-text">点击上传证据图片(可选)</p>
              <div v-if="previewImage" class="preview-image">
                <img :src="previewImage" alt="预览">
                <p class="file-name">{{ fileName }}</p>
              </div>
            </div>
          </Upload>
        </FormItem>

        <FormItem>
          <Button 
            type="primary" 
            @click="submit"
            :loading="loading"
            :disabled="!formValid"
            style="width: 400px">
            提交举报
          </Button>
        </FormItem>
      </Form>
    </Card>

    <!-- 我的举报列表 -->
    <Card title="我的举报记录" style="width: 100%; margin-top: 20px;">
      <div slot="extra">
        <Button type="primary" size="small" icon="md-refresh" @click="loadReportList">刷新</Button>
      </div>
      <Table :columns="reportColumns" :data="reportList" stripe no-data-text="您还没有提交过举报"></Table>
    </Card>

    <!-- 成功对话框 -->
    <Modal
      v-model="showSuccessDialog"
      title="提交成功"
      @on-ok="handleSuccessOk"
      @on-cancel="handleSuccessOk">
      <p>您的举报信息已成功提交！</p>
      <p>举报ID: {{ submittedReportId }}</p>
      <p>状态: 等待管理员审核</p>
    </Modal>

    <!-- 举报详情对话框 -->
    <Modal
      v-model="showDetailDialog"
      title="举报详情"
      width="700">
      <div v-if="selectedReport" class="report-detail">
        <div class="detail-section">
          <p><strong>举报ID:</strong> {{ selectedReport.id }}</p>
          <p><strong>被举报用户:</strong> {{ selectedReport.reportedUsername }} (ID: {{ selectedReport.reportedUserId }})</p>
          <p><strong>版权名称:</strong> {{ selectedReport.copyrightTitle }} (ID: {{ selectedReport.copyrightId }})</p>
          <p><strong>举报原因:</strong> {{ selectedReport.reason }}</p>
          <p><strong>状态:</strong> 
            <Tag v-if="selectedReport.status === 'PENDING'" color="blue">待审核</Tag>
            <Tag v-else-if="selectedReport.status === 'APPROVED'" color="success">已通过</Tag>
            <Tag v-else-if="selectedReport.status === 'REJECTED'" color="error">已驳回</Tag>
          </p>
          <p v-if="selectedReport.status === 'APPROVED'"><strong>奖励积分:</strong> 10分</p>
          <p v-if="selectedReport.status === 'REJECTED'"><strong>驳回原因:</strong> {{ selectedReport.rejectReason }}</p>
          <p><strong>提交时间:</strong> {{ formatDate(selectedReport.createdTime) }}</p>
        </div>
        
        <div v-if="selectedReport.evidenceImg" class="evidence-image">
          <h3>证据图片:</h3>
          <div class="image-wrapper">
            <img :src="'/api/uploads/evidence/' + selectedReport.evidenceImg" 
                 class="details-image"
                 :class="{'zoomed': isImageZoomed}"
                 @click="toggleImageZoom"
                 onerror="this.src=require('../assets/img/bg.jpg')" />
            <div class="zoom-hint" v-if="!isImageZoomed">点击图片放大</div>
            <div class="zoom-hint" v-else>点击图片缩小</div>
          </div>
        </div>
      </div>
      <div slot="footer">
        <Button type="primary" @click="showDetailDialog = false">关闭</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ReportForm',
  data() {
    return {
      loading: false,
      showSuccessDialog: false,
      showDetailDialog: false,
      submittedReportId: '',
      previewImage: '',
      fileName: '',
      fileObject: null,
      isImageZoomed: false,
      selectedReport: null,
      reportList: [],
      formItem: {
        reportedUserId: '',
        copyrightId: '',
        copyrightTitle: '',
        reason: ''
      },
      reportColumns: [
        {
          title: 'ID',
          key: 'id'
        },
        {
          title: '被举报用户',
          key: 'reportedUsername'
        },
        {
          title: '版权名称',
          key: 'copyrightTitle'
        },
        {
          title: '状态',
          key: 'status',
          render: (h, params) => {
            let color = 'blue';
            let text = '待审核';
            if (params.row.status === 'APPROVED') {
              color = 'blue';
              text = '已通过';
            } else if (params.row.status === 'REJECTED') {
              color = 'blue';
              text = '已驳回';
            }
            return h('Tag', {
              props: {
                color: color
              }
            }, text);
          }
        },
        {
          title: '提交时间',
          key: 'createdTime',
          render: (h, params) => {
            return h('span', this.formatDate(params.row.createdTime));
          }
        },
        {
          title: '详情',
          key: 'action',
          width: 100,
          render: (h, params) => {
            return h('Button', {
              props: {
                type: 'primary',
                size: 'small'
              },
              on: {
                click: () => {
                  this.viewReportDetail(params.row);
                }
              }
            }, '查看详情');
          }
        }
      ]
    }
  },
  computed: {
    formValid() {
      return this.formItem.reportedUserId &&
             this.formItem.copyrightId &&
             this.formItem.copyrightTitle &&
             this.formItem.reason
    },
    currentUser() {
      try {
        const userStr = sessionStorage.getItem('user');
        if (!userStr) return null;
        return JSON.parse(userStr);
      } catch (e) {
        console.error('解析用户信息失败:', e);
        return null;
      }
    }
  },
  created() {
    this.checkUserPermission();
    this.loadReportList();
  },
  mounted() {
    this.loadReportList();
  },
  activated() {
    // 当组件被激活时也重新加载数据
    this.loadReportList();
  },
  methods: {
    checkUserPermission() {
      if (!this.currentUser) {
        this.$Message.error('请先登录');
        this.$router.push('/login');
      }
    },
    handleBeforeUpload(file) {
      // 处理文件上传前的预览
      const reader = new FileReader();
      reader.onload = (e) => {
        this.previewImage = e.target.result;
        this.fileName = file.name;
      };
      reader.readAsDataURL(file);
      this.fileObject = file;
      return false; // 阻止自动上传
    },
    handleFormatError() {
      this.$Message.error('图片格式不正确，请上传 jpg 或 png 格式的图片');
    },
    handleSizeError() {
      this.$Message.error('图片大小不能超过 2MB');
    },
    loadReportList() {
      if (!this.currentUser || !this.currentUser.id) return;
      this.loading = true;
      axios.get(`/api/reports/user/${this.currentUser.id}`)
        .then(response => {
          this.reportList = Array.isArray(response.data) ? response.data : [];
          console.log('加载用户举报列表成功，数量:', this.reportList.length);
        })
        .catch(error => {
          console.error('加载举报列表失败:', error);
          this.$Message.error('加载举报列表失败');
          this.reportList = [];
        })
        .finally(() => {
          this.loading = false;
        });
    },
    viewReportDetail(report) {
      this.selectedReport = report;
      this.showDetailDialog = true;
    },
    toggleImageZoom() {
      this.isImageZoomed = !this.isImageZoomed;
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
    async submit() {
      if (!this.formValid) {
        this.$Message.warning('请完善所有必填信息');
        return;
      }
      if (!this.currentUser || !this.currentUser.id) {
        this.$Message.error('请先登录');
        return;
      }
      this.loading = true;
      try {
        // 创建FormData对象
        const formData = new FormData();
        formData.append('reporterId', this.currentUser.id);
        formData.append('reportedUserId', this.formItem.reportedUserId);
        formData.append('copyrightId', this.formItem.copyrightId);
        formData.append('copyrightTitle', this.formItem.copyrightTitle);
        formData.append('reason', this.formItem.reason);
        // 如果有证据图片，也添加到表单中
        if (this.fileObject) {
          formData.append('evidence', this.fileObject);
        }
        // 提交表单
        const response = await axios.post('/api/reports/submit', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        // 处理成功响应
        this.submittedReportId = response.data.id;
        this.showSuccessDialog = true;
        // 重置表单
        this.formItem = {
          reportedUserId: '',
          copyrightId: '',
          copyrightTitle: '',
          reason: ''
        };
        this.previewImage = '';
        this.fileName = '';
        this.fileObject = null;
        // 重新加载举报列表
        this.loadReportList();
      } catch (error) {
        console.error('提交举报失败:', error);
        if (error.response && error.response.data && error.response.data.message) {
          this.$Message.error(error.response.data.message);
        } else {
          this.$Message.error('提交举报失败，请稍后重试');
        }
      } finally {
        this.loading = false;
      }
    },
    handleSuccessOk() {
      this.showSuccessDialog = false;
    }
  }
}
</script>

<style scoped>
.report-form {
  padding: 20px;
}

.upload-area {
  padding: 20px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  text-align: center;
  width: 400px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #1890ff;
}

.upload-text {
  margin-top: 8px;
  color: #666;
}

.preview-image {
  margin-top: 16px;
}

.preview-image img {
  max-width: 100%;
  max-height: 200px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.file-name {
  margin-top: 8px;
  color: #666;
  font-size: 12px;
}

.report-detail {
  display: flex;
  flex-direction: column;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section p {
  margin-bottom: 10px;
  line-height: 1.6;
}

.evidence-image {
  margin-top: 10px;
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
</style> 
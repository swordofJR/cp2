<template>
  <div>
    <Card title="待审核举报" class="vm-margin">
      <div slot="extra">
        <Button type="primary" size="small" icon="md-refresh" @click="loadReports">刷新</Button>
      </div>
      <Table :columns="reportColumns" :data="pendingReports" stripe :loading="loading.pending" no-data-text="没有待审核的举报"></Table>
    </Card>
    
    <Card title="所有举报记录" class="vm-margin">
      <div slot="extra">
        <Button type="primary" size="small" icon="md-refresh" @click="loadReports">刷新</Button>
      </div>
      <Table :columns="historyColumns" :data="allReports" stripe :loading="loading.all" no-data-text="没有举报记录"></Table>
    </Card>
    
    <!-- 详情弹窗 -->
    <Modal v-model="detailModal" title="举报详情" width="700">
      <div v-if="selectedReport" class="report-detail">
        <div class="detail-section">
          <p><strong>举报ID:</strong> {{ selectedReport.id }}</p>
          <p><strong>举报人:</strong> {{ selectedReport.reporterUsername }} (ID: {{ selectedReport.reporterId }})</p>
          <p><strong>被举报用户:</strong> {{ selectedReport.reportedUsername }} (ID: {{ selectedReport.reportedUserId }})</p>
          <p><strong>版权名称:</strong> {{ selectedReport.copyrightTitle }} (ID: {{ selectedReport.copyrightId }})</p>
          <p><strong>举报原因:</strong> {{ selectedReport.reason }}</p>
          <p><strong>状态:</strong> 
            <Tag v-if="selectedReport.status === 'PENDING'" color="blue">待审核</Tag>
            <Tag v-else-if="selectedReport.status === 'APPROVED'" color="success">已通过</Tag>
            <Tag v-else-if="selectedReport.status === 'REJECTED'" color="error">已驳回</Tag>
          </p>
          <p v-if="selectedReport.status === 'APPROVED'"><strong>处理结果:</strong> 被举报用户扣除20积分，举报人奖励10积分</p>
          <p v-if="selectedReport.status === 'REJECTED'"><strong>驳回原因:</strong> {{ selectedReport.rejectReason }}</p>
          <p><strong>提交时间:</strong> {{ formatDate(selectedReport.createdTime) }}</p>
          <p v-if="selectedReport.status !== 'PENDING'"><strong>审核时间:</strong> {{ formatDate(selectedReport.updatedTime) }}</p>
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
        <template v-if="selectedReport && selectedReport.status === 'PENDING'">
          <Button type="success" style="margin-right: 5px" @click="approveReport(selectedReport)">通过举报</Button>
          <Button type="error" style="margin-right: 10px" @click="showRejectModal(selectedReport)">驳回举报</Button>
        </template>
        <Button type="primary" @click="detailModal = false">关闭</Button>
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
import axios from 'axios';

export default {
  name: 'ReportManagement',
  data() {
    return {
      pendingReports: [],
      allReports: [],
      detailModal: false,
      rejectModal: false,
      rejectReason: '',
      selectedReport: null,
      reportToReject: null,
      isImageZoomed: false,
      loading: {
        pending: false,
        all: false
      },
      reportColumns: [
        {
          title: 'ID',
          key: 'id',
          width: 80
        },
        {
          title: '举报人',
          key: 'reporterUsername',
          width: 120
        },
        {
          title: '被举报用户',
          key: 'reportedUsername',
          width: 120
        },
        {
          title: '版权名称',
          key: 'copyrightTitle'
        },
        {
          title: '提交时间',
          key: 'createdTime',
          width: 160,
          render: (h, params) => {
            return h('span', this.formatDate(params.row.createdTime));
          }
        },
        {
          title: '状态',
          key: 'status',
          width: 100,
          render: (h, params) => {
            return h('Tag', {
              props: {
                color: 'blue'
              }
            }, '待审核');
          }
        },
        {
          title: '操作',
          key: 'action',
          width: 200,
          render: (h, params) => {
            return h('div', [
              h('Button', {
                props: {
                  type: 'primary',
                  size: 'small'
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.showReportDetail(params.row);
                  }
                }
              }, '查看详情'),
              h('Button', {
                props: {
                  type: 'success',
                  size: 'small'
                },
                style: {
                  marginRight: '5px'
                },
                on: {
                  click: () => {
                    this.approveReport(params.row);
                  }
                }
              }, '通过'),
              h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                on: {
                  click: () => {
                    this.showRejectModal(params.row);
                  }
                }
              }, '驳回')
            ]);
          }
        }
      ],
      historyColumns: [
        {
          title: 'ID',
          key: 'id',
          width: 80
        },
        {
          title: '举报人',
          key: 'reporterUsername',
          width: 120
        },
        {
          title: '被举报用户',
          key: 'reportedUsername',
          width: 120
        },
        {
          title: '版权名称',
          key: 'copyrightTitle'
        },
        {
          title: '提交时间',
          key: 'createdTime',
          width: 160,
          render: (h, params) => {
            return h('span', this.formatDate(params.row.createdTime));
          }
        },
        {
          title: '状态',
          key: 'status',
          width: 100,
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
          title: '操作',
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
                  this.showReportDetail(params.row);
                }
              }
            }, '查看详情');
          }
        }
      ]
    }
  },
  computed: {
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
    this.checkAdminPermission();
    this.loadReports();
  },
  mounted() {
    this.loadReports();
  },
  activated() {
    // 当组件被激活时也重新加载数据
    this.loadReports();
  },
  methods: {
    checkAdminPermission() {
      if (!this.currentUser) {
        this.$Message.error('请先登录');
        this.$router.push('/login');
        return;
      }
      if (this.currentUser.role !== 'admin') {
        this.$Message.error('您没有管理员权限');
        this.$router.push('/');
      }
    },
    loadReports() {
      // 加载待审核举报
      this.loading.pending = true;
      axios.get('/api/reports/pending')
        .then(response => {
          this.pendingReports = Array.isArray(response.data) ? response.data : [];
          console.log('加载待审核举报成功，数量:', this.pendingReports.length);
        })
        .catch(error => {
          console.error('加载待审核举报失败:', error);
          this.$Message.error('加载待审核举报失败');
          this.pendingReports = [];
        })
        .finally(() => {
          this.loading.pending = false;
        });
      // 加载所有举报
      this.loading.all = true;
      axios.get('/api/reports/all')
        .then(response => {
          this.allReports = Array.isArray(response.data) ? response.data : [];
          console.log('加载所有举报成功，数量:', this.allReports.length);
        })
        .catch(error => {
          console.error('加载所有举报失败:', error);
          this.$Message.error('加载所有举报失败');
          this.allReports = [];
        })
        .finally(() => {
          this.loading.all = false;
        });
    },
    showReportDetail(report) {
      this.selectedReport = report;
      this.detailModal = true;
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
    approveReport(report) {
      // 显示确认对话框
      this.$Modal.confirm({
        title: '确认审核',
        content: '您确定要通过此举报吗？通过后将扣除被举报用户20积分，奖励举报人10积分。',
        onOk: () => {
          // 审核通过
          axios.post(`/api/reports/${report.id}/review`, null, {
            params: {
              status: 'APPROVED'
            }
          })
          .then(response => {
            this.$Message.success('举报已通过，已扣除被举报用户20积分，奖励举报人10积分');
            this.loadReports(); // 重新加载数据
            // 如果是在详情弹窗中操作，关闭弹窗
            if (this.detailModal) {
              this.detailModal = false;
            }
          })
          .catch(error => {
            console.error('审核失败:', error);
            if (error.response && error.response.data && error.response.data.message) {
              this.$Message.error(error.response.data.message);
            } else {
              this.$Message.error('审核操作失败');
            }
          });
        }
      });
    },
    showRejectModal(report) {
      this.reportToReject = report;
      this.rejectReason = '';
      this.rejectModal = true;
    },
    confirmReject() {
      if (!this.rejectReason) {
        this.$Message.warning('请输入驳回原因');
        return;
      }
      // 驳回举报
      axios.post(`/api/reports/${this.reportToReject.id}/review`, null, {
        params: {
          status: 'REJECTED',
          rejectReason: this.rejectReason
        }
      })
      .then(response => {
        this.$Message.success('举报已驳回');
        this.rejectModal = false;
        this.loadReports(); // 重新加载数据
      })
      .catch(error => {
        console.error('驳回失败:', error);
        if (error.response && error.response.data && error.response.data.message) {
          this.$Message.error(error.response.data.message);
        } else {
          this.$Message.error('驳回操作失败');
        }
      });
    }
  }
}
</script>

<style scoped>
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
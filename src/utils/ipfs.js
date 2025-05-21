import { create } from 'ipfs-http-client'

// 配置Infura认证
const auth = 'Basic ' + Buffer.from(
  process.env.VUE_APP_INFURA_PROJECT_ID + ':' + process.env.VUE_APP_INFURA_PROJECT_SECRET
).toString('base64')

export const IPFSClient = {
  client: create({
    host: 'ipfs.infura.io',
    port: 5001,
    protocol: 'https',
    headers: {
      authorization: auth
    }
  }),

  async upload(file) {
    try {
      const added = await this.client.add(file)
      return `https://ipfs.infura.io/ipfs/${added.path}`
    } catch (error) {
      console.error('IPFS upload error:', error)
      throw new Error('文件上传失败，请重试')
    }
  }
}
# 拍卖溯源系统智能合约使用文档

本文档提供了拍卖溯源系统智能合约的测试、部署和交互指南。

## 合约结构

系统由三个主要合约组成：

1. **EcommerceStore.sol**: 拍卖商店合约，负责商品上架、竞价、揭示和拍卖结束等核心功能
2. **Escrow.sol**: 托管合约，负责保管资金并在适当条件下释放给卖家或退回给买家
3. **Migrations.sol**: Truffle迁移合约，用于合约部署和版本控制

## 环境要求

- Node.js (v12.0.0以上)
- Truffle (v5.0.0以上)
- Ganache 或其他以太坊本地开发链
- MetaMask（用于前端连接）

## 安装依赖

```bash
# 安装全局Truffle (如果尚未安装)
npm install -g truffle

# 安装项目依赖
npm install

# 安装测试依赖
npm install @openzeppelin/test-helpers
```

## 测试合约

系统提供了全面的测试脚本，您可以运行以下命令来执行所有测试：

```bash
# 启动本地开发链（如果使用Ganache CLI）
npx ganache-cli

# 在另一个终端运行测试
truffle test
```

测试文件位于`test/auction_system_test.js`，包含了以下方面的测试:

- EcommerceStore合约的基本功能测试
  - 商品添加
  - 竞价和揭示
  - 拍卖结束和托管合约创建
  - 资金释放和退款
- Escrow合约的功能测试
  - 资金托管
  - 多方释放资金机制
- Migrations合约的权限测试

## 部署合约

### 本地开发链部署

```bash
# 确保Ganache已启动
truffle migrate --reset
```

### 测试网部署 (如Sepolia)

1. 在项目根目录创建`.env`文件，并添加以下内容:

```
INFURA_API_KEY=<你的Infura API密钥>
MNEMONIC=<你的助记词>
```

2. 执行部署命令:

```bash
truffle migrate --network sepolia
```

部署脚本位于`migrations/`目录:
- `1_initial_migration.js`: 部署Migrations合约
- `2_deploy_ecommerce_store.js`: 部署EcommerceStore合约

## 合约交互

系统提供了交互脚本，可以用于测试合约的各种功能:

```bash
truffle exec scripts/interact.js
```

交互脚本包含以下功能:
- 添加商品
- 对商品竞价
- 揭示竞价
- 结束拍卖
- 释放资金给卖家
- 退款给买家

## 前端集成

前端连接智能合约需要以下配置:

1. 确保部署后得到的合约地址保存在`.env`文件中:

```
VUE_APP_CONTRACT_ADDRESS=<EcommerceStore合约地址>
```

2. 在前端代码中，使用以下方式连接合约:

```javascript
import { ethers } from 'ethers';
import AuctionABI from '../../build/contracts/EcommerceStore.json';

// 连接MetaMask
const provider = new ethers.providers.Web3Provider(window.ethereum);
await provider.send('eth_requestAccounts', []);
const signer = provider.getSigner();

// 创建合约实例
const contract = new ethers.Contract(
  process.env.VUE_APP_CONTRACT_ADDRESS,
  AuctionABI.abi,
  signer
);

// 调用合约方法
const tx = await contract.addProductToStore(...);
```

## 合约功能说明

### EcommerceStore合约

- `addProductToStore`: 添加商品到拍卖商店
- `getProduct`: 获取商品详情
- `bid`: 对商品进行密封竞价
- `revealBid`: 揭示竞价
- `finalizeAuction`: 结束拍卖并创建托管合约
- `releaseAmountToSeller`: 释放资金给卖家
- `refundAmountToBuyer`: 退款给买家

### Escrow合约

- `releaseAmountToSeller`: 释放资金给卖家（需要至少2人同意）
- `refundAmountToBuyer`: 退款给买家（需要至少2人同意）

## 常见问题

### Q: 合约调用失败，显示"transaction underpriced"
A: 增加gasPrice或gas limit，确保足够的gas用于执行交易。

### Q: 无法完成竞价揭示
A: 确保竞价金额和密钥与竞价时使用的完全一致，且在拍卖时间内。

### Q: 释放资金失败
A: 托管合约需要至少2人同意（买家、卖家或仲裁者中的任意2人）才能释放资金。 
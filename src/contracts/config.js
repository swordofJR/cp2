// 合约地址配置
export const contractAddress = '0x5FbDB2315678afecb367f032d93F642f64180aa3'; // 本地Ganache部署的合约地址

// 网络配置
export const networkConfig = {
  chainId: '0x539', // Ganache默认链ID
  chainName: 'Ganache Local',
  rpcUrls: ['http://127.0.0.1:7545'],
  nativeCurrency: {
    name: 'Ether',
    symbol: 'ETH',
    decimals: 18
  }
}
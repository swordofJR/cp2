const EcommerceStore = artifacts.require("EcommerceStore");
const Escrow = artifacts.require("Escrow");
const Web3 = require('web3');

// 将Unix时间戳转换为可读时间格式
function formatTimestamp(timestamp) {
  return new Date(timestamp * 1000).toLocaleString();
}

// 获取和显示商品信息
async function getProductInfo(ecommerceStore, productId) {
  const product = await ecommerceStore.getProduct(productId);
  
  console.log('\n商品详情:');
  console.log('------------------------');
  console.log(`商品ID: ${product[0].toString()}`);
  console.log(`名称: ${product[1]}`);
  console.log(`类别: ${product[2]}`);
  console.log(`图片链接: ${product[3]}`);
  console.log(`描述链接: ${product[4]}`);
  console.log(`拍卖开始时间: ${formatTimestamp(product[5].toString())}`);
  console.log(`拍卖结束时间: ${formatTimestamp(product[6].toString())}`);
  console.log(`起拍价格: ${Web3.utils.fromWei(product[7].toString())} ETH`);
  
  // 获取状态文本
  let status;
  switch(product[8].toString()) {
    case '0': status = '拍卖中'; break;
    case '1': status = '已售出'; break;
    case '2': status = '未售出'; break;
    default: status = '未知';
  }
  console.log(`商品状态: ${status}`);
  
  // 获取条件文本
  let condition = product[9].toString() === '0' ? '全新' : '二手';
  console.log(`商品状况: ${condition}`);
  
  // 获取出价信息
  if (product[8].toString() !== '2') { // 如果不是'未售出'
    try {
      const bidInfo = await ecommerceStore.highestBidderInfo(productId);
      console.log(`\n竞价信息:`);
      console.log(`最高出价者: ${bidInfo[0]}`);
      console.log(`最高出价金额: ${Web3.utils.fromWei(bidInfo[1].toString())} ETH`);
      console.log(`第二高出价金额: ${Web3.utils.fromWei(bidInfo[2].toString())} ETH`);
    } catch (error) {
      console.log('无法获取出价信息');
    }
  }
  
  // 获取托管信息
  if (product[8].toString() === '1') { // 如果是'已售出'
    try {
      const escrowAddress = await ecommerceStore.escrowAddressForProduct(productId);
      const escrowInfo = await ecommerceStore.escrowInfo(productId);
      
      console.log(`\n托管信息:`);
      console.log(`托管合约地址: ${escrowAddress}`);
      console.log(`买家: ${escrowInfo[0]}`);
      console.log(`卖家: ${escrowInfo[1]}`);
      console.log(`仲裁者: ${escrowInfo[2]}`);
      console.log(`资金是否已释放: ${escrowInfo[3]}`);
      console.log(`释放资金的用户数: ${escrowInfo[4].toString()}`);
      console.log(`退款的用户数: ${escrowInfo[5].toString()}`);
    } catch (error) {
      console.log('无法获取托管信息');
    }
  }
}

// 添加新商品
async function addProduct(ecommerceStore, options, account) {
  const {
    name,
    category,
    imageLink,
    descLink,
    startPrice,
    duration,
    condition
  } = options;
  
  // 计算拍卖开始和结束时间
  const now = Math.floor(Date.now() / 1000);
  const startTime = now + 60; // 1分钟后开始
  const endTime = now + (duration * 86400); // duration天后结束
  
  console.log('\n添加新商品:');
  console.log('------------------------');
  console.log(`名称: ${name}`);
  console.log(`类别: ${category}`);
  console.log(`图片链接: ${imageLink}`);
  console.log(`描述链接: ${descLink}`);
  console.log(`起拍价格: ${startPrice} ETH`);
  console.log(`拍卖开始时间: ${formatTimestamp(startTime)}`);
  console.log(`拍卖结束时间: ${formatTimestamp(endTime)}`);
  console.log(`商品状况: ${condition === 0 ? '全新' : '二手'}`);
  
  try {
    const tx = await ecommerceStore.addProductToStore(
      name,
      category,
      imageLink,
      descLink,
      startTime,
      endTime,
      startPrice.toString(),
      condition,
      { from: account }
    );
    
    const productId = tx.logs[0].args._productId.toNumber();
    console.log(`\n商品添加成功! 商品ID: ${productId}`);
    return productId;
  } catch (error) {
    console.error('添加商品失败:', error.message);
    return null;
  }
}

// 对商品进行竞价
async function bidOnProduct(ecommerceStore, productId, amount, secret, account) {
  console.log('\n对商品竞价:');
  console.log('------------------------');
  console.log(`商品ID: ${productId}`);
  console.log(`竞价金额: ${amount} ETH`);
  console.log(`密钥: ${secret}`);
  
  try {
    // 生成密封竞价
    const bid = await ecommerceStore.keccak(amount.toString(), secret);
    console.log(`生成的密封竞价: ${bid}`);
    
    // 提交竞价
    const tx = await ecommerceStore.bid(
      productId,
      bid,
      { 
        from: account,
        value: Web3.utils.toWei(amount.toString(), 'ether')
      }
    );
    
    console.log(`竞价提交成功! 交易哈希: ${tx.tx}`);
    return true;
  } catch (error) {
    console.error('竞价失败:', error.message);
    return false;
  }
}

// 揭示竞价
async function revealBid(ecommerceStore, productId, amount, secret, account) {
  console.log('\n揭示竞价:');
  console.log('------------------------');
  console.log(`商品ID: ${productId}`);
  console.log(`竞价金额: ${amount} ETH`);
  console.log(`密钥: ${secret}`);
  
  try {
    const tx = await ecommerceStore.revealBid(
      productId,
      amount.toString(),
      secret,
      { from: account }
    );
    
    console.log(`竞价揭示成功! 交易哈希: ${tx.tx}`);
    return true;
  } catch (error) {
    console.error('揭示竞价失败:', error.message);
    return false;
  }
}

// 结束拍卖
async function finalizeAuction(ecommerceStore, productId, account) {
  console.log('\n结束拍卖:');
  console.log('------------------------');
  console.log(`商品ID: ${productId}`);
  
  try {
    const tx = await ecommerceStore.finalizeAuction(
      productId,
      { from: account }
    );
    
    console.log(`拍卖结束成功! 交易哈希: ${tx.tx}`);
    
    // 获取托管合约地址
    const escrowAddress = await ecommerceStore.escrowAddressForProduct(productId);
    console.log(`托管合约地址: ${escrowAddress}`);
    
    return true;
  } catch (error) {
    console.error('结束拍卖失败:', error.message);
    return false;
  }
}

// 释放资金给卖家
async function releaseToSeller(ecommerceStore, productId, account) {
  console.log('\n释放资金给卖家:');
  console.log('------------------------');
  console.log(`商品ID: ${productId}`);
  
  try {
    const tx = await ecommerceStore.releaseAmountToSeller(
      productId,
      { from: account }
    );
    
    console.log(`释放资金成功! 交易哈希: ${tx.tx}`);
    return true;
  } catch (error) {
    console.error('释放资金失败:', error.message);
    return false;
  }
}

// 退款给买家
async function refundToBuyer(ecommerceStore, productId, account) {
  console.log('\n退款给买家:');
  console.log('------------------------');
  console.log(`商品ID: ${productId}`);
  
  try {
    const tx = await ecommerceStore.refundAmountToBuyer(
      productId,
      { from: account }
    );
    
    console.log(`退款成功! 交易哈希: ${tx.tx}`);
    return true;
  } catch (error) {
    console.error('退款失败:', error.message);
    return false;
  }
}

// 执行示例流程
module.exports = async function(callback) {
  try {
    const accounts = await web3.eth.getAccounts();
    const owner = accounts[0];
    const seller = accounts[1];
    const buyer1 = accounts[2];
    const buyer2 = accounts[3];
    const arbiter = accounts[4];
    
    console.log('------ 拍卖系统交互脚本 ------');
    console.log('可用账户:');
    console.log(`所有者: ${owner}`);
    console.log(`卖家: ${seller}`);
    console.log(`买家1: ${buyer1}`);
    console.log(`买家2: ${buyer2}`);
    console.log(`仲裁者: ${arbiter}`);
    
    // 获取EcommerceStore实例
    const ecommerceStore = await EcommerceStore.deployed();
    console.log(`\nEcommerceStore合约地址: ${ecommerceStore.address}`);
    
    // 添加测试商品
    const productId = await addProduct(
      ecommerceStore,
      {
        name: '测试艺术品',
        category: '艺术品',
        imageLink: 'https://ipfs.io/ipfs/QmHashTest1',
        descLink: 'https://ipfs.io/ipfs/QmHashTest2',
        startPrice: 0.1,
        duration: 1, // 1天
        condition: 0 // 全新
      },
      seller
    );
    
    if (productId) {
      // 显示商品信息
      await getProductInfo(ecommerceStore, productId);
      
      // 等待一分钟，让拍卖开始
      console.log('\n等待拍卖开始...');
      await new Promise(resolve => setTimeout(resolve, 60000));
      
      // 买家1出价
      const bidSuccess1 = await bidOnProduct(
        ecommerceStore, 
        productId, 
        0.2, 
        'secret1', 
        buyer1
      );
      
      // 买家2出价
      const bidSuccess2 = await bidOnProduct(
        ecommerceStore, 
        productId, 
        0.3, 
        'secret2', 
        buyer2
      );
      
      // 揭示出价
      await revealBid(ecommerceStore, productId, 0.2, 'secret1', buyer1);
      await revealBid(ecommerceStore, productId, 0.3, 'secret2', buyer2);
      
      // 显示商品信息
      await getProductInfo(ecommerceStore, productId);
      
      // 可以选择性地等待拍卖结束或者手动修改合约状态进行测试
      // 注意：实际使用时这一步不应该跳过，而是应当等待拍卖真正结束
      console.log('\n等待拍卖结束...(实际场景下应该等待拍卖时间结束)');
      
      // 模拟等待拍卖结束
      // await new Promise(resolve => setTimeout(resolve, 24 * 60 * 60 * 1000)); // 24小时
      
      // 由于我们不能真正等待，这里只是演示操作
      console.log('\n--- 以下操作仅作为演示，实际场景需要等待拍卖结束 ---');
      
      // 结束拍卖
      await finalizeAuction(ecommerceStore, productId, arbiter);
      
      // 查看更新后的商品信息
      await getProductInfo(ecommerceStore, productId);
      
      // 释放资金给卖家
      await releaseToSeller(ecommerceStore, productId, buyer2); // 买家同意
      await releaseToSeller(ecommerceStore, productId, arbiter); // 仲裁者同意
      
      // 查看最终的商品和托管信息
      await getProductInfo(ecommerceStore, productId);
    }

    console.log('\n------ 脚本执行完成 ------');
    callback();
  } catch (error) {
    console.error(error);
    callback(error);
  }
}; 
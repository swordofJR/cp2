const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();

// 创建竞品模型
const ProductSchema = new mongoose.Schema({
  title: { type: String, required: true },
  description: { type: String, required: true },
  imageHash: { type: String, required: true },
  metadataHash: { type: String, required: true },
  category: { type: Number, required: true },
  startPrice: { type: Number, required: true },
  startTime: { type: Date, required: true },
  duration: { type: Number, required: true },
  productId: { type: Number },
  txHash: { type: String },
  address: { type: String },
  createdAt: { type: Date, default: Date.now }
});

// 如果模型不存在，则创建
const Product = mongoose.models.Product || mongoose.model('Product', ProductSchema);

// POST /api/products - 创建新的竞品记录
router.post('/products', async (req, res) => {
  try {
    const {
      title,
      description,
      imageHash,
      metadataHash,
      category,
      startPrice,
      startTime,
      duration
    } = req.body;

    // 验证必要字段
    if (!title || !description || !imageHash || !metadataHash) {
      return res.status(400).json({ error: '缺少必要字段' });
    }

    // 创建新的竞品记录
    const product = new Product({
      title,
      description,
      imageHash,
      metadataHash,
      category,
      startPrice,
      startTime,
      duration,
      address: req.headers['x-user-address'] || '未知地址' // 从请求头获取用户地址
    });

    await product.save();
    
    res.status(201).json({ 
      success: true, 
      message: '竞品信息已保存到数据库',
      product: {
        id: product._id,
        title: product.title
      }
    });
  } catch (error) {
    console.error('保存竞品失败:', error);
    res.status(500).json({ error: '保存竞品失败', message: error.message });
  }
});

// GET /api/products - 获取所有竞品列表
router.get('/products', async (req, res) => {
  try {
    const products = await Product.find().sort({ createdAt: -1 });
    res.json(products);
  } catch (error) {
    console.error('获取竞品列表失败:', error);
    res.status(500).json({ error: '获取竞品列表失败', message: error.message });
  }
});

// GET /api/products/:id - 获取单个竞品详情
router.get('/products/:id', async (req, res) => {
  try {
    const product = await Product.findById(req.params.id);
    if (!product) {
      return res.status(404).json({ error: '竞品不存在' });
    }
    res.json(product);
  } catch (error) {
    console.error('获取竞品详情失败:', error);
    res.status(500).json({ error: '获取竞品详情失败', message: error.message });
  }
});

// PATCH /api/products/:id/transaction - 更新区块链交易信息
router.patch('/products/:id/transaction', async (req, res) => {
  try {
    const { txHash, productId } = req.body;
    
    if (!txHash) {
      return res.status(400).json({ error: '缺少交易哈希' });
    }
    
    const product = await Product.findByIdAndUpdate(
      req.params.id,
      { 
        txHash,
        productId: productId || undefined,
        updatedAt: Date.now()
      },
      { new: true }
    );
    
    if (!product) {
      return res.status(404).json({ error: '竞品不存在' });
    }
    
    res.json({ 
      success: true, 
      message: '交易信息已更新',
      product
    });
  } catch (error) {
    console.error('更新交易信息失败:', error);
    res.status(500).json({ error: '更新交易信息失败', message: error.message });
  }
});

module.exports = router; 
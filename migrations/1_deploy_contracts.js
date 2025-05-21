const CopyrightToken = artifacts.require("CopyrightToken");
const CopyrightMarketplace = artifacts.require("CopyrightMarketplace");

module.exports = async function(deployer) {
  // 部署 CopyrightToken
  await deployer.deploy(CopyrightToken);
  const copyrightToken = await CopyrightToken.deployed();

  // 部署 CopyrightMarketplace，并传入 CopyrightToken 的地址
  await deployer.deploy(CopyrightMarketplace, copyrightToken.address);
}; 
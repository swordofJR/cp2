require("@nomicfoundation/hardhat-toolbox");

/** @type import('hardhat/config').HardhatUserConfig */
module.exports = {
    solidity: {
        version: "0.8.28",
        settings: {
            optimizer: {
                enabled: true,
                runs: 200
            },
            evmVersion: "london" // 使用london而非paris/shanghai（不包含EIP-3860限制）
        }
    },
};

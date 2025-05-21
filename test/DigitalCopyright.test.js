const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("DigitalCopyright", function () {
  let DigitalCopyright;
  let copyright;
  let owner;
  let addr1;
  let addr2;

  beforeEach(async function () {
    [owner, addr1, addr2] = await ethers.getSigners();

    DigitalCopyright = await ethers.getContractFactory("DigitalCopyright");
    copyright = await DigitalCopyright.deploy();
    await copyright.waitForDeployment();
  });

  describe("Deployment", function () {
    it("Should deploy successfully", async function () {
      const address = await copyright.getAddress();
      expect(address).to.not.equal(ethers.ZeroAddress);
    });
  });

  describe("Copyright Registration", function () {
    it("Should register a new copyright", async function () {
      const title = "Test Work";
      const description = "Test Description";
      const ipfsHash = "QmTestHash";

      const tx = await copyright.registerCopyright(title, description, ipfsHash);
      await tx.wait();
      
      const copyrightId = 1; // First copyright ID
      const copyrightInfo = await copyright.getCopyright(copyrightId);

      expect(copyrightInfo.owner).to.equal(owner.address);
      expect(copyrightInfo.title).to.equal(title);
      expect(copyrightInfo.description).to.equal(description);
      expect(copyrightInfo.ipfsHash).to.equal(ipfsHash);
      expect(copyrightInfo.isRegistered).to.be.true;
    });

    it("Should not register with empty IPFS hash", async function () {
      const title = "Test Work";
      const description = "Test Description";
      const ipfsHash = "";

      await expect(
        copyright.registerCopyright(title, description, ipfsHash)
      ).to.be.revertedWithCustomError(copyright, "InvalidIPFSHash");
    });
  });

  describe("Copyright Transfer", function () {
    it("Should transfer copyright ownership", async function () {
      // First register a copyright
      const title = "Test Work";
      const description = "Test Description";
      const ipfsHash = "QmTestHash";
      
      await copyright.registerCopyright(title, description, ipfsHash);
      
      // Transfer ownership
      const copyrightId = 1;
      const tx = await copyright.transferCopyright(copyrightId, addr1.address);
      await tx.wait();
      
      const copyrightInfo = await copyright.getCopyright(copyrightId);
      expect(copyrightInfo.owner).to.equal(addr1.address);
    });

    it("Should not transfer non-existent copyright", async function () {
      await expect(
        copyright.transferCopyright(999, addr1.address)
      ).to.be.revertedWithCustomError(copyright, "CopyrightNotRegistered");
    });

    it("Should not transfer copyright by non-owner", async function () {
      // First register a copyright
      const title = "Test Work";
      const description = "Test Description";
      const ipfsHash = "QmTestHash";
      
      await copyright.registerCopyright(title, description, ipfsHash);
      
      // Try to transfer as non-owner
      const copyrightId = 1;
      await expect(
        copyright.connect(addr1).transferCopyright(copyrightId, addr2.address)
      ).to.be.revertedWithCustomError(copyright, "NotCopyrightOwner");
    });
  });

  describe("Ownership Verification", function () {
    it("Should verify ownership correctly", async function () {
      // Register a copyright
      const title = "Test Work";
      const description = "Test Description";
      const ipfsHash = "QmTestHash";
      
      await copyright.registerCopyright(title, description, ipfsHash);
      
      const copyrightId = 1;
      expect(await copyright.verifyOwnership(copyrightId, owner.address)).to.be.true;
      expect(await copyright.verifyOwnership(copyrightId, addr1.address)).to.be.false;
    });

    it("Should not verify ownership of non-existent copyright", async function () {
      expect(await copyright.verifyOwnership(999, owner.address)).to.be.false;
    });
  });
}); 
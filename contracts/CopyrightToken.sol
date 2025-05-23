// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC721/IERC721.sol";

contract CopyrightToken is ERC721URIStorage, Ownable {
  uint256 private _tokenIds;

  enum CopyrightStatus {
    Pending,
    Approved,
    Listed,
    Rejected,
    Sold
  }

  struct CopyrightInfo {
    string title;
    string description;
    string imageUrl;
    string category;
    uint256 registrationDate;
    CopyrightStatus status;
    string ipfsHash;
    uint256 price;
  }

  mapping(uint256 => CopyrightInfo) private _copyrightInfo;
  mapping(address => uint256[]) private _userCopyrights;

  event CopyrightRegistered(
    uint256 indexed tokenId,
    string title,
    string category,
    address owner,
    string ipfsHash
  );
  event CopyrightStatusChanged(
    uint256 indexed tokenId,
    CopyrightStatus newStatus
  );

  constructor() ERC721("CopyrightToken", "CPT") Ownable {}

  function registerCopyright(
    string memory title,
    string memory description,
    string memory imageUrl,
    string memory category,
    string memory ipfsHash
  ) public returns (uint256) {
    require(bytes(title).length > 0, "Title cannot be empty");
    require(bytes(description).length > 0, "Description cannot be empty");
    require(bytes(imageUrl).length > 0, "Image URL cannot be empty");
    require(bytes(category).length > 0, "Category cannot be empty");
    require(bytes(ipfsHash).length > 0, "IPFS hash cannot be empty");

    uint256 newTokenId = _tokenIds;
    _tokenIds += 1;

    _safeMint(msg.sender, newTokenId);
    _setTokenURI(newTokenId, ipfsHash);

    _copyrightInfo[newTokenId] = CopyrightInfo({
      title: title,
      description: description,
      imageUrl: imageUrl,
      category: category,
      registrationDate: block.timestamp,
      status: CopyrightStatus.Pending,
      ipfsHash: ipfsHash,
      price: 0.01 ether
    });

    _userCopyrights[msg.sender].push(newTokenId);

    emit CopyrightRegistered(newTokenId, title, category, msg.sender, ipfsHash);
    return newTokenId;
  }

  function updateCopyrightStatus(
    uint256 tokenId,
    CopyrightStatus newStatus
  ) public onlyOwner {
    require(tokenId != 0, "Token does not exist");
    _copyrightInfo[tokenId].status = newStatus;
    emit CopyrightStatusChanged(tokenId, newStatus);
  }

  function getCopyrightInfo(
    uint256 tokenId
  )
    public
    view
    returns (
      string memory title,
      string memory description,
      string memory imageUrl,
      string memory category,
      uint256 registrationDate,
      CopyrightStatus status,
      string memory ipfsHash
    )
  {
    require(tokenId != 0, "Token does not exist");
    CopyrightInfo memory info = _copyrightInfo[tokenId];
    return (
      info.title,
      info.description,
      info.imageUrl,
      info.category,
      info.registrationDate,
      info.status,
      info.ipfsHash
    );
  }

  function getUserCopyrights(
    address user
  ) public view returns (uint256[] memory) {
    return _userCopyrights[user];
  }

  function transferFrom(
    address from,
    address to,
    uint256 tokenId
  ) public virtual override(ERC721, IERC721) {
    super.transferFrom(from, to, tokenId);

    uint256[] storage fromList = _userCopyrights[from];
    for (uint256 i = 0; i < fromList.length; i++) {
      if (fromList[i] == tokenId) {
        fromList[i] = fromList[fromList.length - 1];
        fromList.pop();
        _userCopyrights[to].push(tokenId);
        break;
      }
    }
  }

  function safeTransferFrom(
    address from,
    address to,
    uint256 tokenId,
    bytes memory data
  ) public virtual override(ERC721, IERC721) {
    super.safeTransferFrom(from, to, tokenId, data);

    uint256[] storage fromList = _userCopyrights[from];
    for (uint256 i = 0; i < fromList.length; i++) {
      if (fromList[i] == tokenId) {
        fromList[i] = fromList[fromList.length - 1];
        fromList.pop();
        _userCopyrights[to].push(tokenId);
        break;
      }
    }
  }
}

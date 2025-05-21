// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/access/Ownable.sol";
import "./CopyrightToken.sol";

contract CopyrightMarketplace is Ownable {
  CopyrightToken private _copyrightToken;

  struct Listing {
    uint256 tokenId;
    address seller;
    uint256 price;
    bool isActive;
  }

  mapping(uint256 => Listing) private _listings;
  uint256[] private _activeListings;

  event CopyrightListed(
    uint256 indexed tokenId,
    address indexed seller,
    uint256 price
  );
  event CopyrightSold(
    uint256 indexed tokenId,
    address indexed seller,
    address indexed buyer,
    uint256 price
  );
  event ListingCancelled(uint256 indexed tokenId);

  constructor(address copyrightTokenAddress) Ownable() {
    _copyrightToken = CopyrightToken(copyrightTokenAddress);
  }

  function listCopyright(uint256 tokenId, uint256 price) public {
    require(
      _copyrightToken.ownerOf(tokenId) == msg.sender,
      "Not the owner of the copyright"
    );
    require(price > 0, "Price must be greater than 0");
    require(!_listings[tokenId].isActive, "Copyright already listed");

    _listings[tokenId] = Listing({
      tokenId: tokenId,
      seller: msg.sender,
      price: price,
      isActive: true
    });

    _activeListings.push(tokenId);
    emit CopyrightListed(tokenId, msg.sender, price);
  }

  function buyCopyright(uint256 tokenId) public payable {
    Listing storage listing = _listings[tokenId];
    require(listing.isActive, "Copyright not listed for sale");
    require(msg.value >= listing.price, "Insufficient payment");
    require(msg.sender != listing.seller, "Cannot buy your own copyright");

    address seller = listing.seller;
    uint256 price = listing.price;

    _copyrightToken.transferFrom(seller, msg.sender, tokenId);

    (bool success, ) = seller.call{value: price}("");
    require(success, "Payment transfer failed");

    if (msg.value > price) {
      (success, ) = msg.sender.call{value: msg.value - price}("");
      require(success, "Refund failed");
    }

    listing.isActive = false;
    _removeListing(tokenId);

    emit CopyrightSold(tokenId, seller, msg.sender, price);
  }

  function cancelListing(uint256 tokenId) public {
    Listing storage listing = _listings[tokenId];
    require(listing.isActive, "Copyright not listed");
    require(listing.seller == msg.sender, "Only seller can cancel listing");

    listing.isActive = false;
    _removeListing(tokenId);

    emit ListingCancelled(tokenId);
  }

  function getActiveListings() public view returns (uint256[] memory) {
    return _activeListings;
  }

  function getListing(
    uint256 tokenId
  ) public view returns (address seller, uint256 price, bool isActive) {
    Listing memory listing = _listings[tokenId];
    return (listing.seller, listing.price, listing.isActive);
  }

  function _removeListing(uint256 tokenId) private {
    for (uint256 i = 0; i < _activeListings.length; i++) {
      if (_activeListings[i] == tokenId) {
        _activeListings[i] = _activeListings[_activeListings.length - 1];
        _activeListings.pop();
        break;
      }
    }
  }
}

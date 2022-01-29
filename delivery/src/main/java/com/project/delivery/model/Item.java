package com.project.delivery.model;

public class Item {

    public Long restId, itemId, qty, price;

    public Item(Long restId, Long itemId, Long price) {
        this.restId = restId;
        this.itemId = itemId;
        this.price = price;
    }

    /*

    public RestaurantInventory(Long restId, Long itemId, Long qty, Long price) {
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
        this.price = price;
    }

    */
    public Long getRestId() {
        return restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
    
}

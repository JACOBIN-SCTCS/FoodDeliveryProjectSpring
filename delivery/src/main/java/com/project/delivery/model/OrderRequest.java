package com.project.delivery.model;

public class OrderRequest {

    public Long restId, itemId, qty;

  
    public OrderRequest(Long restId, Long itemId, Long qty) {
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
    }

    public void setRestId(Long restId)
    {
        this.restId = restId;
    }
    public Long getRestId()
    {
        return this.restId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }
    public Long getItemId()
    {
        return this.itemId;
    }


    public void setQty(Long qty)
    {
        this.qty = qty;
    }
    public Long getQty()
    {
        return this.qty;
    }


    
}

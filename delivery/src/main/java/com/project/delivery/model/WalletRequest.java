package com.project.delivery.model;
public class WalletRequest {
    
    Long  custId;
    Long amount;

    public WalletRequest(Long custId, Long amount)
    {
        this.custId = custId;
        this.amount = amount;
    }

    public Long getCustId()
    {
        return this.custId;
    }
    public Long getAmount()
    {
        return this.amount;
    }
    public void setCustId(Long custId)
    {
        this.amount = custId;
    }
    public void setAmount(Long amount)
    {
        this.amount = amount;
    }

}

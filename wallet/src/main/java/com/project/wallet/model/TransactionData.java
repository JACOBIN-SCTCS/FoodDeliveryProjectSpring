package com.project.wallet.model;

public class TransactionData
{
    long custId;
    long amount;
    
    public TransactionData(long custId, long amount)
    {
        this.custId = custId;
        this.amount = amount;
    }

    public long getCustId()
    {
        return this.custId;
    }

    public long getAmount()
    {
        return this.amount;
    }

    public void setCustId(long custId)
    {
        this.custId = custId;
    }

    public void setAmount(long amount)
    {
        this.amount = amount;
    }

}

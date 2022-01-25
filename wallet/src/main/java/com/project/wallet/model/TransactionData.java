package com.project.wallet.model;

public class TransactionData
{
    int custId;
    int amount;
    
    public TransactionData(int custId, int amount)
    {
        this.custId = custId;
        this.amount = amount;
    }

    public int getCustId()
    {
        return this.custId;
    }

    public int getAmount()
    {
        return this.amount;
    }

    public void setCustId(int custId)
    {
        this.custId = custId;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

}

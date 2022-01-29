package com.project.wallet.model;

public class CustomerWallet
{
    long custId;
    long balance;

    public CustomerWallet(long custId, long balance)
    {
        this.custId = custId;
        this.balance = balance;
    }

    public long getCustId()
    {
        return this.custId;
    }

    public long getBalance()
    {
        return this.balance;
    }
}

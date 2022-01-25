package com.project.wallet.model;

public class CustomerWallet
{
    int custId;
    int balance;

    public CustomerWallet(int custId, int balance)
    {
        this.custId = custId;
        this.balance = balance;
    }

    public int getCustId()
    {
        return this.custId;
    }

    public int getBalance()
    {
        return this.balance;
    }
}

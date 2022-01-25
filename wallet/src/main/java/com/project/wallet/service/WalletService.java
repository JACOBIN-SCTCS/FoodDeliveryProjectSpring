package com.project.wallet.service;
import java.util.*;

import com.project.wallet.model.CustomerWallet;
import com.project.wallet.model.TransactionData;

public class WalletService 
{

    HashMap<Integer,Integer> wallet;
    HashMap<Integer,Integer> initialData;

    public WalletService()
    {
        wallet = new HashMap<>();
        initialData = new HashMap<>();
        wallet.put(301, 2000);
        wallet.put(302, 2000);
        wallet.put(303, 2000);

        initialData.putAll(wallet);
    }

    public boolean addBalance(int custId, int amount)
    {
        if(wallet.containsKey(custId))
            wallet.put(custId, wallet.get(custId) + amount);
        System.out.println(wallet.get(custId));
        return true;
    }

    public boolean deductBalance(int custId, int amount)
    {
        if(wallet.containsKey(custId))
        {
            if(wallet.get(custId) < amount)
            {
                return false;
            }
            else
            {
                wallet.put(custId, wallet.get(custId)-amount);
                return true;
            }
        }
        else
        {
            return false;
        } 
    }

    public CustomerWallet getData(int custId)
    {
        if(!wallet.containsKey(custId))
        {
            return null;
        }
        else
        {
            TransactionData txndata = new TransactionData(custId, wallet.get(custId));
            CustomerWallet customerData = new CustomerWallet(txndata.getCustId(), txndata.getAmount());
            return customerData;
        }
    }

    public boolean reInitialize()
    {
        wallet = new HashMap<>();
        wallet.putAll(initialData);
        return true;

    }

}

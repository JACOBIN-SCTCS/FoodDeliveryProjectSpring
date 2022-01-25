package com.project.wallet.service;
import java.io.File;
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
        
        int wallet_amount = 0;
        List<Integer> customers = new ArrayList<Integer>(); 

        try{
            File datafile = new File("/Users/depressedcoder/code/fooddelivery/initialData.txt");
            Scanner myReader = new Scanner(datafile);
            int count = 0 ;
            String fourstar = new String("****");
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.equals(fourstar))
                {
                    count+=1;
                }
                if(count==2)
                {
                    while(myReader.hasNextLine())
                    {
                        data = myReader.nextLine();
                        if(data.equals(fourstar))
                        {
                            String walletAmountString = myReader.nextLine();
                            wallet_amount = Integer.parseInt(walletAmountString);
                            break;
                        }
                        else
                        {
                            customers.add(Integer.parseInt(data));
                        }
                    }
                }
            }
            myReader.close();
        }
        catch(Exception e)
        {
            System.out.println("Error Opening File");
            e.printStackTrace();
        }
        
        if(customers.size()>0)
        {
            for(int i=0;i<customers.size();++i)
            {
                wallet.put(customers.get(i), wallet_amount);
            }
            initialData.putAll(wallet);
        }
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

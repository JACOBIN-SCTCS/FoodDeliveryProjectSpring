package com.project.delivery.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.project.delivery.entities.AgentEntity;
import com.project.delivery.entities.CurrentState;
import com.project.delivery.entities.RestaurantEntity;
import com.project.delivery.model.Item;
import com.project.delivery.repositories.AgentsRepository;
import com.project.delivery.repositories.OrderHistoryRepository;
import com.project.delivery.repositories.RestaurantRepository;

import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.type.TrueFalseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DBInitializer 
{
    @Autowired
    AgentsRepository agentsRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    OrderHistoryRepository orderHistoryRepository;
    
    @PersistenceContext 
    private EntityManager em ;

    final int SIGNED_OUT    = 0;
    List<Long> agents; 

    @Transactional
    public boolean initAllTables() throws Exception
    {
        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/initialData.txt");
        Scanner sc = new Scanner(file);
        agents = new ArrayList<Long>();
        CurrentState appState = em.find(CurrentState.class, 1, LockModeType.PESSIMISTIC_WRITE);
        if(appState!=null)
        {
            sc.close();
            return true;    
        }
        int count = 0;

        while (sc.hasNextLine()) {

            String str = sc.nextLine();
            System.out.println(str);
            String[] splited = str.split("\\s+");

            if (splited[0].indexOf('*') > -1) {
                count += 1;
                continue;
            }

            if (count == 0) {
                Long restId = Long.parseLong(splited[0]);
                int restNum = Integer.parseInt(splited[1]);

                for (int i = 0; i < restNum; i++) {

                    String str2 = sc.nextLine();
                    System.out.println(str2);
                    String[] splited2 = str2.split("\\s+");
                    
                    Long itemId, price, qty;

                    itemId = Long.parseLong(splited2[0]);
                    price  = Long.parseLong(splited2[1]);
                    qty    = Long.parseLong(splited2[2]);
                    
                    Item item = new Item(restId, itemId, price);
                    RestaurantEntity entity = new RestaurantEntity(restId,itemId,price);
                    this.restaurantRepository.save(entity);
                    //itemList.add(item);
                    
                    
                }
            
            }
            else if (count == 1) {
                //agentStatus.put(Long.parseLong(str), SIGNED_OUT);
                AgentEntity entity = new AgentEntity(Long.parseLong(str),SIGNED_OUT);
                agents.add(Long.parseLong(str));
                this.agentsRepository.save(entity);
                //this.em.persist();
            }
            else if (count >= 2) {
                break;
            }
        }
        sc.close(); 
        this.em.persist(new CurrentState(1,1000l));
        return true;
    }

    @Transactional 
    public boolean reinitTables()
    {
        CurrentState appState = em.find(CurrentState.class, 1, LockModeType.PESSIMISTIC_WRITE);
        orderHistoryRepository.deleteAll();
        agentsRepository.deleteAll();
        for (int i=0;i<agents.size();++i)
        {
            AgentEntity entity = new AgentEntity(agents.get(i),SIGNED_OUT);
            this.agentsRepository.save(entity);

        }
        appState.setValue(1000l);
        this.em.merge(appState);
        this.em.flush();
        return true;
    }
 
}

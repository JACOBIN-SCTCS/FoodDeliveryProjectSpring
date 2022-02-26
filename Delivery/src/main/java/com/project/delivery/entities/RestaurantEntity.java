package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class RestaurantEntity 
{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgen")
    @SequenceGenerator(name = "idgen", initialValue = 1, allocationSize = 1)    
    int id;

    @Column(name = "restId")
    int restId;
    @Column(name = "itemId")
    int itemId;
    @Column(name = "price")
    int price;

    public RestaurantEntity()
    {

    }
    public RestaurantEntity(int restId, int itemId, int price)
    {
        this.restId = restId;
        this.itemId = itemId;
        this.price = price;
    }
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRestId() {
        return this.restId;
    }

    public void setRestId(int restId) {
        this.restId = restId;
    }

    public int getItemId() {
        return this.itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }


    
}

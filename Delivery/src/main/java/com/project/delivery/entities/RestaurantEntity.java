package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class RestaurantEntity 
{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idgen")
    @SequenceGenerator(name = "idgen", initialValue = 1, allocationSize = 1)    
    int id;

    @Column(name = "restId")
    Long restId;
    @Column(name = "itemId")
    Long itemId;
    @Column(name = "price")
    Long price;

    public RestaurantEntity()
    {

    }
    public RestaurantEntity(Long restId, Long itemId, Long price)
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

    public Long getRestId() {
        return this.restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPrice() {
        return this.price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }


    
}

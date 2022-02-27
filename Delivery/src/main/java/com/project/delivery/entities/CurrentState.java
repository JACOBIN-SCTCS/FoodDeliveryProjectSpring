package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CurrentState 
{
    @Id
    @Column(name = "key")
    int key;
    @Column(name = "value")
    int value;

	public CurrentState()
	{

	}

	public CurrentState(int key, int value)
	{
		this.key = key;
		this.value = value;
	}
	public int getKey() {
		return this.key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		this.value = value;
	}


}

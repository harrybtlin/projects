package com.harry.recipe;

import java.util.Date;

public class Ingredient {
	private String item;
	
	private int amount;
	
	private Unit unit;
	
	private Date useBy;
	
	public Ingredient() {
		
	}
	
	public Ingredient(String item, int amount, Unit unit, Date useBy) {
		this.item = item;
		this.amount = amount;
		this.unit = unit;
		this.useBy = useBy;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Date getUseBy() {
		return useBy;
	}

	public void setUseBy(Date useBy) {
		this.useBy = useBy;
	}
	
}

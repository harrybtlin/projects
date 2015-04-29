package com.harry.recipe;

public enum Unit {
	of("of", 1), 
	grams("grams", 2), 
	ml("ml", 3), 
	slices("slices", 4);
	
	private String label;
	
	private int value;
	
	private Unit(String label, int value) {
		this.label = label;
		this.value = value;
	}
	
	public String getLable() {
		return label;
	}
	
	public int getValue() {
		return value;
	}
	
	public static Unit valueOf(int value) {
		for(Unit unit : values()) {
			if(unit.value == value) {
				return unit;
			}
		}
		throw new IllegalArgumentException("the value is illegal:" +value);
	}
}

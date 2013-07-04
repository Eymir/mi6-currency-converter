package com.mi6.currencyconverter.dto;

public class RateValues {

	private String name;
	private Double unitsPerCurrency;
	private Double currencyPerUnit;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getUnitsPerCurrency() {
		return unitsPerCurrency;
	}
	public void setUnitsPerCurrency(Double unitsPerCurrency) {
		this.unitsPerCurrency = unitsPerCurrency;
	}
	public Double getCurrencyPerUnit() {
		return currencyPerUnit;
	}
	public void setCurrencyPerUnit(Double currencyPerUnit) {
		this.currencyPerUnit = currencyPerUnit;
	}
	
	
	
}

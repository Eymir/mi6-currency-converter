package com.mi6.currencyconverter.dto;

import java.sql.Date;

public class RateValues {

	private String name;
	private Double unitsPerCurrency;
	private Date cacheDate;
	
	public Date getCacheDate() {
		return cacheDate;
	}
	public void setCacheDate(Date cacheDate) {
		this.cacheDate = cacheDate;
	}
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
}

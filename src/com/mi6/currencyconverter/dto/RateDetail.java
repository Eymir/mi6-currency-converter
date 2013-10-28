package com.mi6.currencyconverter.dto;

import java.sql.Date;

public class RateDetail {

	private String name;
	private Double rate;
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
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
}

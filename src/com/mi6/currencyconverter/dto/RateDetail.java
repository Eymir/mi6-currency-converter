package com.mi6.currencyconverter.dto;

import java.sql.Date;
import java.util.List;

public class RateDetail {

	private String name;
	private Double rate;
	private List <CurrencyHistoricalData> historicalData;
	private Date cacheDate;
	
	
	public List<CurrencyHistoricalData> getHistoricalData() {
		return historicalData;
	}
	public void setHistoricalData(List<CurrencyHistoricalData> historicalData) {
		this.historicalData = historicalData;
	}
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

package com.mi6.currencyconverter.dto;

import java.sql.Date;

public class CurrencyHistoricalData {
	
	private Date date;
	private Double rate;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}

}

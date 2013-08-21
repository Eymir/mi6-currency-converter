package com.mi6.currencyconverter.dto;

import java.util.List;

public class CurrencyDetails {

	private String name;
	private List<RateValues> rateValues;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<RateValues> getRateValues() {
		return rateValues;
	}
	public void setRateValues(List<RateValues> rateValues) {
		this.rateValues = rateValues;
	}
	
	
	
}

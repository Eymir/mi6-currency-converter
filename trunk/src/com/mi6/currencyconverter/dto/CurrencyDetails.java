package com.mi6.currencyconverter.dto;

import java.util.List;

public class CurrencyDetails {

	private String code;
	private String name;
	private String country;
	private String flag;
	
	private List<RateValues> rateValues;
	
	public CurrencyDetails() {
	}
	
	public CurrencyDetails(String code, String name, String country,
			String flagFilePath) {
		this.code = code;
		this.name = name;
		this.country = country;
		this.flag = flagFilePath;
	}
	
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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	
	
	
}

package com.mi6.currencyconverter.dto;

import java.util.List;

public class CurrencyDetails {

	private String code;
	private String name;
	private String country;
	private String flag;
	private double value;
	
	private List<RateDetail> rateDetails;
	
	public CurrencyDetails() {
	}
	
	public CurrencyDetails(String code, String name, String country,
			String flagFilePath) {
		this.code = code;
		this.name = name;
		this.country = country;
		this.flag = flagFilePath;
	}
	
	public Double getSpecificRate(String currencyCode){
		Double specificRate = null;
		for (RateDetail rateDetail:rateDetails) {
			if ((currencyCode != null) && (currencyCode.equalsIgnoreCase(rateDetail.getName()))){
				specificRate = rateDetail.getRate();
			}
		}
		if (specificRate == null) {
			return Double.valueOf(0);
		} else {
			return specificRate;
		}
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<RateDetail> getRateDetails() {
		return rateDetails;
	}
	public void setRateDetails(List<RateDetail> rateDetails) {
		this.rateDetails = rateDetails;
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

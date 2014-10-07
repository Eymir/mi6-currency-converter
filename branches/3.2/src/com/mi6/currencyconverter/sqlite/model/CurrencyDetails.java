package com.mi6.currencyconverter.sqlite.model;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.List;

public class CurrencyDetails {

	private String code;
	private String name;
	private String country;
	private String flag;
	private double value;
	private List<RateDetails> rateDetails;
	
	public CurrencyDetails() {
		super();
	}
	

	public CurrencyDetails(String code, String name, String country, String flag) {
		super();
		this.code = code;
		this.name = name;
		this.country = country;
		this.flag = flag;
	}
	
	public double getValue() {
		return Double.parseDouble(new DecimalFormat("#.###").format(value));
	}

	public void setValue(double value) {
		this.value = value;
	}

	public List<RateDetails> getRateDetails() {
		return rateDetails;
	}
	
	public void setRateDetails(List<RateDetails> rateDetails) {
		this.rateDetails = rateDetails;
	}

	public Double getSpecificRate(String currencyCode, Date date){
		Double specificRate = null;
		for (RateDetails rd:rateDetails) {
			if ((currencyCode != null) && (currencyCode.equalsIgnoreCase(rd.getTargetCurrencyCode())) 
					&& (date != null) && ((date.toString()).equals(rd.getRateDate()))){
				specificRate = rd.getRateValue();
			}
		}
		if (specificRate == null) {
			return Double.valueOf(0);
		} else {
			return specificRate;
		}
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

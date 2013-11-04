package com.mi6.currencyconverter.sqlite.model;


public class RateDetails {

	private String sourceCurrencyCode;
	private String targetCurrencyCode;
	private String rateDate;
	private double rateValue;
	
	public RateDetails() {
		super();
	}
	
	public RateDetails(String sourceCurrencyCode, String targetCurrencyCode,
			String rateDate, double rateValue) {
		super();
		this.sourceCurrencyCode = sourceCurrencyCode;
		this.targetCurrencyCode = targetCurrencyCode;
		this.rateDate = rateDate;
		this.rateValue = rateValue;
	}

	public String getSourceCurrencyCode() {
		return sourceCurrencyCode;
	}

	public void setSourceCurrencyCode(String sourceCurrencyCode) {
		this.sourceCurrencyCode = sourceCurrencyCode;
	}

	public String getTargetCurrencyCode() {
		return targetCurrencyCode;
	}

	public void setTargetCurrencyCode(String targetCurrencyCode) {
		this.targetCurrencyCode = targetCurrencyCode;
	}

	public String getRateDate() {
		return rateDate;
	}

	public void setRateDate(String rateDate) {
		this.rateDate = rateDate;
	}

	public double getRateValue() {
		return rateValue;
	}

	public void setRateValue(double rateValue) {
		this.rateValue = rateValue;
	}

}

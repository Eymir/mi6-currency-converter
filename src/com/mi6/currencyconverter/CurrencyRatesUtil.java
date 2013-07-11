package com.mi6.currencyconverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import au.com.bytecode.opencsv.CSVReader;

import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;


public class CurrencyRatesUtil {
	
	public static final CurrencyDetails ReadCurrencyDetailsFromCsv(Context context, String currencyName) {
		  CurrencyDetails currencyDetails = new CurrencyDetails();
		  List<RateValues> rvList = new ArrayList<RateValues>(); 
		  AssetManager assetManager = context.getAssets();

		  try {
		    InputStream csvStream = assetManager.open("currency_rates/"+currencyName+".csv");
		    InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
		    CSVReader csvReader = new CSVReader(csvStreamReader);
		    currencyDetails.setName(currencyName);
		    String[] line;

		    while ((line = csvReader.readNext()) != null) {
		    	rvList.add(ReadValuesFromLine(line));
		    }
		    
		    currencyDetails.setName(currencyName);
		    currencyDetails.setRateValues(rvList);
		    
		    csvReader.close();
		  } catch (IOException e) {
		    e.printStackTrace();
		  }
		  return currencyDetails;
		}
	
	private static RateValues ReadValuesFromLine(String[] line) {
		RateValues rv = new RateValues();
		
		rv.setName(line[0]);
		rv.setUnitsPerCurrency(Double.parseDouble(line[1]));
		rv.setCacheDate(Date.valueOf(line[1]));
		
		return rv;
	}
	
}

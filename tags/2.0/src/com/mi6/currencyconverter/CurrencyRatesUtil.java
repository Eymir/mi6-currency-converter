package com.mi6.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import au.com.bytecode.opencsv.CSVReader;


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
		rv.setCurrencyPerUnit(Double.parseDouble(line[1]));
		
		return rv;
	}
	
	public static final Double getLiveRates(String fromCurrency, String toCurrency) {
		Double rate = Double.valueOf(0);
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httppost = new HttpGet("http://finance.yahoo.com/d/quotes.csv?e=goog.csv&f=sl1&s="+fromCurrency+toCurrency+"=x");
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		
		        HttpEntity ht = response.getEntity();
		        BufferedHttpEntity buf = new BufferedHttpEntity(ht);
		        InputStream is = buf.getContent();

		        BufferedReader r = new BufferedReader(new InputStreamReader(is));

		        String line = r.readLine();
		        line = line.subSequence(11, line.length()).toString();
		        rate = Double.parseDouble(line);
		        
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		
		return rate;
	}
	
}

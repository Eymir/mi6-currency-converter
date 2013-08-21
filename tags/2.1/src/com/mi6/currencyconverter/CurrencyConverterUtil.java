package com.mi6.currencyconverter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;


public class CurrencyConverterUtil {
	
	public static final String[] CURRENCY_LIST = {"EUR","GBP","HUF","RON","USD"}; 
	
	public static final CurrencyDetails ReadCurrencyDetailsFromCsv(Context context, String currencyName) {
		
		String filename = currencyName;
		FileInputStream fis = null;
		CurrencyDetails currencyDetails = new CurrencyDetails();
		List<RateValues> rvList = new ArrayList<RateValues>();
		
		try {
			fis = context.openFileInput(filename);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		

		  try {
		    InputStreamReader csvStreamReader = new InputStreamReader(fis);
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
	
	public static final boolean WriteCurrencyDetailsToCsv(Context context, CurrencyDetails currDetails) {
		boolean writeStatus = false;
		String filename = currDetails.getName();
		FileOutputStream fos = null;
		String detailsToWrite = "";
		
		List<RateValues> rv = currDetails.getRateValues();
		
		for(RateValues rates:rv) {
			detailsToWrite = detailsToWrite 
							+ rates.getName() 
							+ "," 
							+ rates.getUnitsPerCurrency() 
							+ "," + rates.getCacheDate() 
							+ System.getProperty("line.separator");
		}
		
		try {
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(detailsToWrite.getBytes());
			fos.close();
			writeStatus = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			writeStatus = false;
		} catch (IOException e) {
			e.printStackTrace();
			writeStatus = false;
		}
		
		return writeStatus;
	}
	
	private static RateValues ReadValuesFromLine(String[] line) {
		RateValues rv = new RateValues();
		
		rv.setName(line[0]);
		rv.setUnitsPerCurrency(Double.parseDouble(line[1]));
		//rv.setCacheDate(Date.valueOf(line[1]));
		
		return rv;
	}
	
	public static CurrencyDetails getOnlineRates(String fromCurrency, List<String> targetCurrencies) {
		
		String baseURL1 = "http://query.yahooapis.com/v1/public/yql?q=select%20id%2C%20Rate%2C%20Date%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22";
		String baseURL2 = "%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
		String fullURL = null;
		String line = null;
		String result = null;
		CurrencyDetails currencyDetails = new CurrencyDetails();
		HttpClient httpClient = new DefaultHttpClient();
		//HttpHost proxy = new HttpHost("172.17.0.10", 8080);
		//httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		HttpGet httpGet = new HttpGet();
		try {
				fullURL = baseURL1;
			for(String s:targetCurrencies) {
				fullURL = fullURL + fromCurrency + s + "%22,%22";
			}
			fullURL = fullURL.substring(0, fullURL.length()-7);
			fullURL = fullURL + baseURL2;
			
			httpGet.setURI(new URI(fullURL));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HttpResponse response = null;
		try {
            response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    StringBuilder sBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sBuilder.append(line + "\n");
                    }
                    reader.close();
                    result = sBuilder.toString();
                    currencyDetails = getCurrencyDetailsFromJSON(result);
            } else {
                    Log.e("Getter", "Failed to get response");
            }
    } catch (ClientProtocolException e) {
            e.printStackTrace();
    } catch (IOException e) {
            e.printStackTrace();
    }
		return currencyDetails;
	}
	
	private static CurrencyDetails getCurrencyDetailsFromJSON(String sJSON){
		
		//parse JSON data
		CurrencyDetails currencyDetails = new CurrencyDetails();
        try{
        	Object results = new JSONObject(sJSON).getJSONObject("query").getJSONObject("results").get("rate");
        	if (results instanceof JSONObject) {
        		JSONObject rateObject = (JSONObject) results;
        		Log.d("JSON", "there is just one rate");
        		String fromCurrency = null;
        		RateValues rv = new RateValues();
        		List<RateValues> rvList = new ArrayList<RateValues>();
                fromCurrency = rateObject.getString("id").subSequence(0, 3).toString();
                rv.setName(rateObject.getString("id").subSequence(3, 6).toString());
                rv.setUnitsPerCurrency(rateObject.getDouble("Rate"));
                //rv.setCacheDate(Date.valueOf(rateObject.getString("Date")));
                rvList.add(rv);
                currencyDetails.setName(fromCurrency);
	            currencyDetails.setRateValues(rvList);
        	} else {
        	JSONArray jsonData = (JSONArray) results;
            String fromCurrency = null;
            List<RateValues> rvList = new ArrayList<RateValues>();
	            for (int i=0; i<jsonData.length(); i++) {
	            	RateValues rv = new RateValues();
	                JSONObject item = jsonData.getJSONObject(i);
	                fromCurrency = item.getString("id").subSequence(0, 3).toString();
	                rv.setName(item.getString("id").subSequence(3, 6).toString());
	                rv.setUnitsPerCurrency(item.getDouble("Rate"));
	                //rv.setCacheDate(Date.valueOf(item.getString("Date")));
	                rvList.add(rv);
	            }
	            
	            currencyDetails.setName(fromCurrency);
	            currencyDetails.setRateValues(rvList);
        	}
        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        } 
		return currencyDetails;
	}
	
	public static boolean CacheRates(Context context, List <String> currencies) {
		boolean cacheStatus = false;
		Log.i("CacheRates", "Start caching rates at:" + GetFormatedCurrentTime());
		for (String curr:currencies) {
			CurrencyDetails currDetails = getOnlineRates(curr, Arrays.asList(CURRENCY_LIST));
			WriteCurrencyDetailsToCsv(context, currDetails);
		}
		Log.i("CacheRates", "End caching rates at:" + GetFormatedCurrentTime());
		
		return cacheStatus;
	}
	
	
	public static String GetFormatedCurrentTime() {

		long timeInMillis = System.currentTimeMillis();
		
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS",Locale.getDefault());

        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("EEST"));
        calendar.setTimeInMillis(timeInMillis);
        
        return sdf.format(calendar.getTime());
		
	}
	
}

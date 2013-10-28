package com.mi6.currencyconverter.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateDetail;


public class CurrencyConverterUtil {
	
	public static final CurrencyDetails ReadCurrencyDetailsFromCsv(Context context, String currencyName) {
		
		String filename = currencyName;
		FileInputStream fis = null;
		CurrencyDetails currencyDetails = new CurrencyDetails();
		List<RateDetail> rvList = new ArrayList<RateDetail>();
		
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
		    currencyDetails.setRateDetails(rvList);
		    
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
		
		List<RateDetail> rv = currDetails.getRateDetails();
		
		for(RateDetail rates:rv) {
			detailsToWrite = detailsToWrite 
							+ rates.getName() 
							+ "," 
							+ rates.getRate() 
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
	
	private static RateDetail ReadValuesFromLine(String[] line) {
		RateDetail rv = new RateDetail();
		
		rv.setName(line[0]);
		rv.setRate(Double.parseDouble(line[1]));
		//rv.setCacheDate(Date.valueOf(line[1]));
		
		return rv;
	}
	
	public static CurrencyDetails getOnlineRates(String fromCurrency, List<String> targetCurrencies) 
													throws ConnectTimeoutException, UnknownHostException{
		
		String baseURL1 = "http://query.yahooapis.com/v1/public/yql?q=select%20id%2C%20Rate%2C%20Date%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22";
		String baseURL2 = "%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
		String fullURL = null;
		String line = null;
		String result = null;
		CurrencyDetails currencyDetails = new CurrencyDetails();
		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
	    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
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
			e1.printStackTrace();
			return null;
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
                    Log.e("CurrencyConverterUtil.getOnlineRates", "Failed to get response");
            }
    } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
    } catch (ConnectTimeoutException e) {
    		Log.e("CurrencyConverterUtil.getOnlineRates", "<<<< Throw ConnectTimeoutException >>>>");
            throw e;
    } catch (UnknownHostException e) {
    		Log.e("CurrencyConverterUtil.getOnlineRates", "<<<< Throw UnknownHostException >>>>");
    		throw e;
    } catch (IOException e) {
    		e.printStackTrace();
    		return null;
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
        		RateDetail rv = new RateDetail();
        		List<RateDetail> rvList = new ArrayList<RateDetail>();
                fromCurrency = rateObject.getString("id").subSequence(0, 3).toString();
                rv.setName(rateObject.getString("id").subSequence(3, 6).toString());
                rv.setRate(rateObject.getDouble("Rate"));
                //rv.setCacheDate(Date.valueOf(rateObject.getString("Date")));
                rvList.add(rv);
                currencyDetails.setName(fromCurrency);
	            currencyDetails.setRateDetails(rvList);
        	} else {
        	JSONArray jsonData = (JSONArray) results;
            String fromCurrency = null;
            List<RateDetail> rvList = new ArrayList<RateDetail>();
	            for (int i=0; i<jsonData.length(); i++) {
	            	RateDetail rv = new RateDetail();
	                JSONObject item = jsonData.getJSONObject(i);
	                fromCurrency = item.getString("id").subSequence(0, 3).toString();
	                rv.setName(item.getString("id").subSequence(3, 6).toString());
	                if (fromCurrency.equalsIgnoreCase(item.getString("id").subSequence(3, 6).toString())){
	                	rv.setRate(Double.valueOf(1));
	                } else {
	                	rv.setRate(item.getDouble("Rate"));
	                }
	                //rv.setCacheDate(Date.valueOf(item.getString("Date")));
	                rvList.add(rv);
	            }
	            
	            currencyDetails.setName(fromCurrency);
	            currencyDetails.setRateDetails(rvList);
        	}
        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        } 
		return currencyDetails;
	}
	
	public static boolean CacheRates(Context context, List <String> currencies) throws ConnectTimeoutException, UnknownHostException{
		boolean cacheStatus = false;
		Log.i("CacheRates", "Start caching rates at:" + GetFormatedCurrentTime());
		for (String curr:currencies) {
			CurrencyDetails currDetails = getOnlineRates(curr, currencies);
			if (currDetails != null) {
				WriteCurrencyDetailsToCsv(context, currDetails);
			} else {
				Log.d("CacheRates", "Currency details from Yahoo site is null");
			}
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
	
	public static List<CurrencyDetails> removeCurrenciesFromConvertionList(List<CurrencyDetails> currencyList, Set<String> convertList) {
		
		List<CurrencyDetails> updatedList = new ArrayList<CurrencyDetails>();
		
		for (CurrencyDetails currency:currencyList) {
			
			if (!convertList.contains(currency.getCode())) {
				updatedList.add(currency);
			}
			
		}
		
		return updatedList;
	}
	
	public static CurrencyDetails getCurrencyByCode(List<CurrencyDetails> currencyList, String code) {
		
		CurrencyDetails currency = null;
		
		for (CurrencyDetails curr:currencyList) {
			if (code.equalsIgnoreCase(curr.getCode())) {
				currency = curr;
			}
		}
		return currency;
	}
	
}

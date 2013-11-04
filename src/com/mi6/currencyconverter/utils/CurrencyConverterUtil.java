package com.mi6.currencyconverter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.ParseException;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.mi6.currencyconverter.sqlite.helper.DatabaseHelper;
import com.mi6.currencyconverter.sqlite.model.CurrencyDetails;
import com.mi6.currencyconverter.sqlite.model.RateDetails;


public class CurrencyConverterUtil {
	
	
	public static List<RateDetails> GetHistoricalData(String fromCurrency, String toCurrency, Date fromDate, Date toDate) 
			throws ConnectTimeoutException, UnknownHostException {
		
		List<RateDetails> historyData = new ArrayList<RateDetails>();
		String line = null;
		
		String url = "http://currencies.apps.grandtrunk.net/getrange/";
		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		//HttpHost proxy = new HttpHost("172.17.0.10", 8080);
		//httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		HttpGet httpGet = new HttpGet();
		try {
			url = url + fromDate + "/" + toDate + "/" + fromCurrency + "/" + toCurrency;
			Log.i("CurrencyConverterUtil.GetHistoricalData#url: ", url);
		httpGet.setURI(new URI(url));
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
                    
                    while ((line = reader.readLine()) != null) {
                    	RateDetails currHistData = new RateDetails();
                    	currHistData.setSourceCurrencyCode(fromCurrency);
                    	currHistData.setTargetCurrencyCode(toCurrency);
                    	currHistData.setRateDate(line.split("\\s+")[0]);
                    	currHistData.setRateValue(Double.valueOf(line.split("\\s+")[1]));
                    	historyData.add(currHistData); 
                    }
                    reader.close();
            } else {
                    Log.e("CurrencyConverterUtil.GetHistoricalData", "Failed to get response");
                    return null;
            }
    } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
    } catch (ConnectTimeoutException e) {
    		Log.e("CurrencyConverterUtil.GetHistoricalData", "<<<< Throw ConnectTimeoutException >>>>");
            throw e;
    } catch (UnknownHostException e) {
    		Log.e("CurrencyConverterUtil.GetHistoricalData", "<<<< Throw UnknownHostException >>>>");
    		throw e;
    } catch (IOException e) {
    		e.printStackTrace();
    		return null;
    }
		return historyData;
	}
	
	public static List<RateDetails> getOnlineRates(String fromCurrency, List<String> targetCurrencies) 
													throws ConnectTimeoutException, UnknownHostException{
		
		String baseURL1 = "http://query.yahooapis.com/v1/public/yql?q=select%20id%2C%20Rate%2C%20Date%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22";
		String baseURL2 = "%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
		String fullURL = null;
		String line = null;
		String result = null;
		List<RateDetails> rdList = new ArrayList<RateDetails>();
		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
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
                    rdList = getCurrencyDetailsFromJSON(result);
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
		return rdList;
	}
	
	private static List<RateDetails> getCurrencyDetailsFromJSON(String sJSON){
		
		//parse JSON data
		List<RateDetails> rdList = new ArrayList<RateDetails>();
        try{
        	Object results = new JSONObject(sJSON).getJSONObject("query").getJSONObject("results").get("rate");
        	if (results instanceof JSONObject) {
        		JSONObject rateObject = (JSONObject) results;
        		Log.d("JSON", "there is just one rate");
        		RateDetails rd = new RateDetails();
                rd.setSourceCurrencyCode(rateObject.getString("id").subSequence(0, 3).toString());
                rd.setTargetCurrencyCode(rateObject.getString("id").subSequence(3, 6).toString());
                rd.setRateValue(rateObject.getDouble("Rate"));
                rd.setRateDate(FormatToSqlDate(rateObject.getString("Date")).toString());
                rdList.add(rd);
        	} else {
        	JSONArray jsonData = (JSONArray) results;
            String fromCurrency = null;
	            for (int i=0; i<jsonData.length(); i++) {
	            	RateDetails rd = new RateDetails();
	                JSONObject rateObject = jsonData.getJSONObject(i);
	                fromCurrency = rateObject.getString("id").subSequence(0, 3).toString();
	                rd.setSourceCurrencyCode(rateObject.getString("id").subSequence(0, 3).toString());
	                rd.setTargetCurrencyCode(rateObject.getString("id").subSequence(3, 6).toString());
	                if (fromCurrency.equalsIgnoreCase(rateObject.getString("id").subSequence(3, 6).toString())){
	                	rd.setRateValue(Double.valueOf(1));
	                } else {
	                	rd.setRateValue(rateObject.getDouble("Rate"));
	                }
	                rd.setRateValue(rateObject.getDouble("Rate"));
	                rd.setRateDate(FormatToSqlDate(rateObject.getString("Date")).toString());
	                rdList.add(rd);
	            }
        	}
        } catch (JSONException e) {
            Log.e("JSONException", "Error: " + e.toString());
        } 
		return rdList;
	}
	
	public static boolean CacheRates(Context context, List <String> currencies) throws ConnectTimeoutException, UnknownHostException{
		boolean cacheStatus = false;
		// Database Helper
	    DatabaseHelper db= null;;
		Log.i("CacheRates", "Start caching rates at:" + GetFormatedCurrentTime());
		for (String curr:currencies) {
			List<RateDetails> rdList = getOnlineRates(curr, currencies);
			if ((rdList != null) && (rdList.size() > 0)) {
				db = new DatabaseHelper(context);
				db.addMultipleRateDetails(rdList);
			} else {
				Log.d("CacheRates", "Currency details from Yahoo site is null ");
			}
		}
		Log.i("CacheRates", "End caching rates at:" + GetFormatedCurrentTime());
		db.closeDB();
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
	
	public static boolean IsNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public static String FormatToSqlDate(String date) {
		
		final String OLD_FORMAT = "MM/dd/yyyy";
		final String NEW_FORMAT = "yyyy-MM-dd";

		String newDateString;

		SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
		java.util.Date d = null;;
		try {
			d = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sdf.applyPattern(NEW_FORMAT);
		newDateString = sdf.format(d);
		
		return newDateString;
	}
	

}

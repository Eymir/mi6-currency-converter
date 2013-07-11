package com.mi6.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;

public class CurrencyConverter extends Activity implements OnClickListener {
    private static final int TIMER_RUNTIME = 5000;

	/** Called when the activity is first created. */
	
	private List<Double> liveRates;
	
	private EditText curr0, curr1, curr2, curr3, curr4;
	private Button convert;
	private Spinner spinner0, spinner1, spinner2, spinner3, spinner4;
	private CheckBox onlineRates;
	ProgressBar progressBar;
	
	private AlertDialog alertDialog;
	private Button alertButton;
	
	
	private Menu menu;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        addMainLayout();
        addAlertDialog();
    	
    }

	private void addAlertDialog() {
		//alert dialog layout
        AlertDialog.Builder builder = new AlertDialog.Builder(CurrencyConverter.this)
        .setTitle("Alert");
        final FrameLayout frameView = new FrameLayout(CurrencyConverter.this);
        builder.setView(frameView);
        alertDialog = builder.create();
        LayoutInflater inflater = alertDialog.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.message_dialog, frameView);
                
        // Setting OK Button
        alertButton = (Button)dialogLayout.findViewById(R.id.buttonAlert);
        alertButton.setOnClickListener(new OnClickListener() {

        	@Override
			public void onClick(View v) {
        		alertDialog.cancel();
            }
        });
	}

	private void addMainLayout() {
		//main layout
        curr0 = (EditText)findViewById(R.id.curr0);
        curr1 = (EditText)findViewById(R.id.curr1);
        curr2 = (EditText)findViewById(R.id.curr2);
        curr3 = (EditText)findViewById(R.id.curr3);
        curr4 = (EditText)findViewById(R.id.curr4);
        
        spinner0 = (Spinner) findViewById(R.id.spinner_0);
        spinner1 = (Spinner) findViewById(R.id.spinner_1);
        spinner2 = (Spinner) findViewById(R.id.spinner_2);
        spinner3 = (Spinner) findViewById(R.id.spinner_3);
        spinner4 = (Spinner) findViewById(R.id.spinner_4);
        
        spinner0.setSelection(0);
        spinner1.setSelection(1);
        spinner2.setSelection(2);
        spinner3.setSelection(3);
        spinner4.setSelection(4);
        
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        
        convert = (Button)this.findViewById(R.id.convert);        
        convert.setOnClickListener(this);
        onlineRates = (CheckBox)findViewById(R.id.onlineRates);
	}
	   
	public void convertAction(View view) {
			String mainCurrency;
			if (!hasMainFieldData()) {
				// Showing Alert Message
				alertDialog.setMessage((String)this.getString(R.string.alertDialog_messages_add_value_in_field));
		        alertDialog.show();
			} else {
				mainCurrency = getMainCurrency();
				convert(mainCurrency);
				}
	}

	protected void convert(String mainCurrency) {
		
		if (onlineRates.isChecked()) {
			new GetLiveRatesTask().execute();
			
		} else {
			
			Double valueToConvert;
			CurrencyDetails currencyDetails;
			currencyDetails = getCurencyDetails(mainCurrency);
			valueToConvert = getMainFieldData();
			curr0.setText(convertSingleValue(currencyDetails, (String)spinner0.getSelectedItem(), valueToConvert));
			updateProgress(1000);
			curr1.setText(convertSingleValue(currencyDetails, (String)spinner1.getSelectedItem(), valueToConvert));
			updateProgress(2000);
			curr2.setText(convertSingleValue(currencyDetails, (String)spinner2.getSelectedItem(), valueToConvert));
			updateProgress(3000);
			curr3.setText(convertSingleValue(currencyDetails, (String)spinner3.getSelectedItem(), valueToConvert));
			updateProgress(4000);
			curr4.setText(convertSingleValue(currencyDetails, (String)spinner4.getSelectedItem(), valueToConvert));
			updateProgress(5000);
			updateProgress(0);
		}


	}

	private String getMainCurrency(){
		
		String currency = null;
		View currentSelectedItem = getWindow().getCurrentFocus();
		
		if (curr0.equals(currentSelectedItem)) {
			currency = (String)spinner0.getSelectedItem();
		}
		if (curr1.equals(currentSelectedItem)) {
			currency = (String)spinner1.getSelectedItem();
		}
		if (curr2.equals(currentSelectedItem)) {
			currency = (String)spinner2.getSelectedItem();
		}
		if (curr3.equals(currentSelectedItem)) {
			currency = (String)spinner3.getSelectedItem();
		}
		if (curr4.equals(currentSelectedItem)) {
			currency = (String)spinner4.getSelectedItem();
		}
		
		return currency;
	} 
	
	private String convertSingleValue(CurrencyDetails currencyDetails, String toCurr, Double value) {
				
		DecimalFormat f = new DecimalFormat("#.##");
		Double currencyRate = getCurrencyRate(currencyDetails, toCurr);
		return f.format(value*currencyRate);
	}
	
	private String convertOnlineSingleValue(Double rate, Double value) {
		DecimalFormat f = new DecimalFormat("#.##");
		return f.format(value*rate);
	}
	
	private boolean hasMainFieldData() {
		
		View currentView = getWindow().getCurrentFocus();
		if ((currentView != null) && (!"".equalsIgnoreCase(((EditText) currentView).getText().toString()))) {
			return true;
		} else {
			return false;
		}
		
	}
	
	private Double getMainFieldData() {
		Double value;
		View currentView = getWindow().getCurrentFocus();
		if ((currentView != null) && (!"".equalsIgnoreCase(((EditText) currentView).getText().toString()))) {
			value = Double.parseDouble(((EditText) currentView).getText().toString());
		} else {
			value = Double.valueOf(0);
		}
		return value;		
	}
	
	private CurrencyDetails getCurencyDetails(String currency) {
		
		CurrencyDetails currencyDetails;
		currencyDetails = CurrencyRatesUtil.ReadCurrencyDetailsFromCsv(this.getApplicationContext(), currency);
		
		return currencyDetails;
	}
	
	private Double getCurrencyRate(CurrencyDetails currencyDetails, String toCurr) {
		Double currencyRate = Double.valueOf(0);
		
		for (RateValues rv : currencyDetails.getRateValues()) {
	  		if (rv.getName().equals(toCurr)){
	  			currencyRate = rv.getUnitsPerCurrency();
	  		}
		}
	return currencyRate;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_cache_policy:
	        	Toast.makeText(this, 
	        			"Cache Policy selected",
	        			Toast.LENGTH_LONG).show();
	            return true;
	        case R.id.menu_exit:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private class GetLiveRatesTask extends AsyncTask<Void, Void, Void>{
	    	    
	    @Override
	    protected Void doInBackground(Void... params) {
			
	    	liveRates = new ArrayList<Double>();
	    	String mainCurrency = getMainCurrency();
	    	
	    	
	    	liveRates.add(getOnlineRate(mainCurrency,(String) spinner0.getSelectedItem()));
	    	updateProgress(1000);
	    	liveRates.add(getOnlineRate(mainCurrency,(String) spinner1.getSelectedItem()));
	    	updateProgress(2000);
	    	liveRates.add(getOnlineRate(mainCurrency,(String) spinner2.getSelectedItem()));
	    	updateProgress(3000);
	    	liveRates.add(getOnlineRate(mainCurrency,(String) spinner3.getSelectedItem()));
	    	updateProgress(4000);
	    	liveRates.add(getOnlineRate(mainCurrency,(String) spinner4.getSelectedItem()));
	    	updateProgress(5000);
	    	updateProgress(0);
			return null;
	     
	    }


		private Double getOnlineRate(String fromCurrency, String toCurrency) {
			
			String baseURL1 = "http://query.yahooapis.com/v1/public/yql?q=select%20id%2C%20Rate%2C%20Date%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22";
			String baseURL2 = "%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
			String line = null;
			String result = null;
			Double rate = null;
			CurrencyDetails currencyDetails = new CurrencyDetails();
			HttpClient httpClient = new DefaultHttpClient();
			//HttpHost proxy = new HttpHost("172.17.0.10", 8080);
			//httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			HttpGet httpGet = new HttpGet();
			try {
				httpGet.setURI(new URI(baseURL1+fromCurrency+toCurrency+baseURL2));
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
                        rate = currencyDetails.getRateValues().get(0).getUnitsPerCurrency();
                } else {
                        Log.e("Getter", "Failed to get response");
                }
        } catch (ClientProtocolException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
			return rate;
		}
		
		private CurrencyDetails getCurrencyDetailsFromJSON(String sJSON){
			
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
	    
	    
	    @Override
	    protected void onPostExecute(Void result) {
	    	Double valueToConvert;
			valueToConvert = getMainFieldData();
	    	if (hasMainFieldData()) {
	    		curr0.setText(convertOnlineSingleValue(liveRates.get(0), valueToConvert));
				curr1.setText(convertOnlineSingleValue(liveRates.get(1), valueToConvert));
				curr2.setText(convertOnlineSingleValue(liveRates.get(2), valueToConvert));
				curr3.setText(convertOnlineSingleValue(liveRates.get(3), valueToConvert));
				curr4.setText(convertOnlineSingleValue(liveRates.get(4), valueToConvert));
			}
	    	
			super.onPostExecute(result);   
	    }
	}

	public void updateProgress(final int timePassed) {
	       if(null != progressBar) {
	           final int progress = progressBar.getMax() * timePassed / TIMER_RUNTIME;
	           progressBar.setProgress(progress);
	       }
	}

	@Override
	public void onClick(View v) {
		convertAction(v);
		
	}
	
	
}
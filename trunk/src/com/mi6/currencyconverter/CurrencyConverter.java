package com.mi6.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;

public class CurrencyConverter extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private List<Double> liveRates;
	
	private EditText curr0, curr1, curr2, curr3, curr4;
	private Button convert;
	private Spinner spinner0, spinner1, spinner2, spinner3, spinner4;
	private CheckBox onlineRates;
	
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
			curr1.setText(convertSingleValue(currencyDetails, (String)spinner1.getSelectedItem(), valueToConvert));
			curr2.setText(convertSingleValue(currencyDetails, (String)spinner2.getSelectedItem(), valueToConvert));
			curr3.setText(convertSingleValue(currencyDetails, (String)spinner3.getSelectedItem(), valueToConvert));
			curr4.setText(convertSingleValue(currencyDetails, (String)spinner4.getSelectedItem(), valueToConvert));
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
	    	String baseURL = "http://finance.yahoo.com/d/quotes.csv?e=goog.csv&f=sl1&s=";
	    	
	    	liveRates.add(getOnlineRate(baseURL+mainCurrency+(String) spinner0.getSelectedItem()+"=x"));
	    	liveRates.add(getOnlineRate(baseURL+mainCurrency+(String) spinner1.getSelectedItem()+"=x"));
	    	liveRates.add(getOnlineRate(baseURL+mainCurrency+(String) spinner2.getSelectedItem()+"=x"));
	    	liveRates.add(getOnlineRate(baseURL+mainCurrency+(String) spinner3.getSelectedItem()+"=x"));
	    	liveRates.add(getOnlineRate(baseURL+mainCurrency+(String) spinner4.getSelectedItem()+"=x"));
			return null;
	     
	    }


		private Double getOnlineRate(String url) {
			
			String line = null;
			HttpClient httpClient = new DefaultHttpClient();
			//HttpHost proxy = new HttpHost("172.17.0.10", 8080);
			//httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			HttpGet httpGet = new HttpGet();
			try {
				httpGet.setURI(new URI(url));
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
                        BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(content));
                        line = reader.readLine();
                        line = line.subSequence(11, line.length()).toString();
                } else {
                        Log.e("Getter", "Failed to download file");
                }
        } catch (ClientProtocolException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
			return Double.parseDouble(line);
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



	@Override
	public void onClick(View v) {
		convertAction(v);
		
	}
	
	
}
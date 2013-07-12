package com.mi6.currencyconverter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;

public class CurrencyConverter extends Activity implements OnClickListener {
    private static final int TIMER_RUNTIME = 500;

	/** Called when the activity is first created. */
	
	private List<Double> liveRates;
	
	private EditText curr0, curr1, curr2, curr3, curr4;
	private Button convert;
	private Spinner spinner0, spinner1, spinner2, spinner3, spinner4;
	ProgressBar progressBar;
	
	private AlertDialog alertDialog;
	private Button alertButton;
	
	
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
		
		if (isNetworkAvailable()) {
			new GetLiveRatesTask().execute();
			
		} else {
			alertDialog.setMessage((String)this.getString(R.string.alertDialog_messages_no_internet_connection));
	        alertDialog.show();
	        
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
		currencyDetails = CurrencyConverterUtil.ReadCurrencyDetailsFromCsv(this.getApplicationContext(), currency);
		
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
	    protected void onPreExecute(){
	        progressBar.setVisibility(View.VISIBLE);
	    }
		
	    @Override
	    public Void doInBackground(Void... params) {
			
	    	liveRates = new ArrayList<Double>();
	    	CurrencyDetails cd = new CurrencyDetails();
	    	String mainCurrency = getMainCurrency();
	    	List<String> targetCurrencies = new ArrayList<String>();
	    	targetCurrencies.add((String)spinner0.getSelectedItem());
	    	targetCurrencies.add((String)spinner1.getSelectedItem());
	    	targetCurrencies.add((String)spinner2.getSelectedItem());
	    	targetCurrencies.add((String)spinner3.getSelectedItem());
	    	targetCurrencies.add((String)spinner4.getSelectedItem());
	    	
	    	updateProgress(100);
	    	
	    	cd = CurrencyConverterUtil.getOnlineRates(mainCurrency,targetCurrencies);
	    	
	    	liveRates.add(cd.getRateValues().get(0).getUnitsPerCurrency());
	    	liveRates.add(cd.getRateValues().get(1).getUnitsPerCurrency());
	    	updateProgress(200);
	    	liveRates.add(cd.getRateValues().get(2).getUnitsPerCurrency());
	    	updateProgress(300);
	    	liveRates.add(cd.getRateValues().get(3).getUnitsPerCurrency());
	    	updateProgress(400);
	    	liveRates.add(cd.getRateValues().get(4).getUnitsPerCurrency());
	    	updateProgress(500);
	    	updateProgress(0);
			return null;
	     
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
	    	progressBar.setVisibility(View.GONE);
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
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void TextFieldClicked(View view){      
		EditText editText = (EditText)view;
		editText.setText("");
	}
	
}
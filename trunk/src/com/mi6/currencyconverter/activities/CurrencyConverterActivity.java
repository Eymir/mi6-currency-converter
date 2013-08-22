package com.mi6.currencyconverter.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mi6.currencyconverter.CurrencyBoxView;
import com.mi6.currencyconverter.CurrencyConverterUtil;
import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.R.id;
import com.mi6.currencyconverter.R.layout;
import com.mi6.currencyconverter.R.string;
import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;

public class CurrencyConverterActivity extends Activity implements OnClickListener {
    private static final int TIMER_RUNTIME = 500;

	/** Called when the activity is first created. */
	
	private List<Double> liveRates;
	
	private List<CurrencyBoxView> boxViewList = new ArrayList<CurrencyBoxView>();
	private Button convert;
	private int defaultTextColor;
	ProgressBar progressBar;
	
	private AlertDialog alertDialog;
	private Button alertButton;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_converter_layout);
        addMainLayout();
        addAlertDialog();
        if (isNetworkAvailable()) {
        	new CacheRates().execute();
        }
    	
    }

	private void addAlertDialog() {
		//alert dialog layout
        AlertDialog.Builder builder = new AlertDialog.Builder(CurrencyConverterActivity.this)
        .setTitle("Alert");
        final FrameLayout frameView = new FrameLayout(CurrencyConverterActivity.this);
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
		
		LinearLayout boxLayout=(LinearLayout)findViewById(R.id.boxLinearlayoutId);

		for (int i = 0; i < 5; i++) {
		
			CurrencyBoxView currencyBox = new CurrencyBoxView(this.getApplicationContext(),i, getLayoutInflater());
			boxLayout.addView(currencyBox.getView());
			
			boxViewList.add(currencyBox);
			((CurrencyBoxView)boxViewList.get(i)).getSpinner().setSelection(i);
		}
        
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        
        convert = (Button)this.findViewById(R.id.convert);        
        convert.setOnClickListener(this);
        
        defaultTextColor = ((CurrencyBoxView)boxViewList.get(0)).getCurrValueField().getTextColors().getDefaultColor();
	}
	   
	public void convertAction(View view) {
			String mainCurrency;
			if (!hasMainFieldData()) {
				// Showing Alert Message
				alertDialog.setMessage((String)this.getString(R.string.alertDialog_messages_add_value_in_field));
		        alertDialog.show();
			} else {
				changeTextColorForMainField();
				mainCurrency = getMainCurrency();
				convert(mainCurrency);
				}
	}

	protected void convert(String mainCurrency) {
		
		if (isNetworkAvailable()) {
			new GetLiveRatesTask().execute();
			
		} else {
			
			Toast.makeText(this, 
					R.string.alertDialog_messages_no_internet_connection,
        			Toast.LENGTH_LONG).show();
				        
			Double valueToConvert;
			CurrencyDetails currencyDetails;
			currencyDetails = getCurencyDetails(mainCurrency);
			valueToConvert = getMainFieldData();
			
			for (int i = 0; i < 5; i++) {
				((CurrencyBoxView)boxViewList.get(i)).getCurrValueField().setText(convertSingleValue(currencyDetails, (String)((CurrencyBoxView)boxViewList.get(i)).getSpinner().getSelectedItem(), valueToConvert));
				updateProgress(i * 1000);
			}
			updateProgress(0);
		}
	}

	private String getMainCurrency(){
		
		String currency = null;
		View currentSelectedItem = getWindow().getCurrentFocus();
		
		for (int i = 0; i < 5; i++) {
			
			if (((CurrencyBoxView)boxViewList.get(i)).getCurrValueField().equals(currentSelectedItem)) {
				currency = (String)((CurrencyBoxView)boxViewList.get(i)).getSpinner().getSelectedItem();
			}
			
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
	    	
	    	for (int i = 0; i < 5; i++) {
	    		
	    		targetCurrencies.add((String)((CurrencyBoxView)boxViewList.get(i)).getSpinner().getSelectedItem());
	    		
	    	}
	    	
	    	updateProgress(100);
	    	
	    	cd = CurrencyConverterUtil.getOnlineRates(mainCurrency,targetCurrencies);
	    	
	    	for (int i = 0; i < 5; i++) {
	    		
	    		liveRates.add(cd.getRateValues().get(i).getUnitsPerCurrency());
		    	updateProgress(i*100);
	    		
	    	}
	    	updateProgress(0);
			return null;
	     
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	    	Double valueToConvert;
			valueToConvert = getMainFieldData();
	    	if (hasMainFieldData()) {
	    		for (int i = 0; i < 5; i++) {
	    			
	    			((CurrencyBoxView)boxViewList.get(i)).getCurrValueField().setText(convertOnlineSingleValue(liveRates.get(i), valueToConvert));
	    			
	    		}
			}
	    	progressBar.setVisibility(View.GONE);
			super.onPostExecute(result);   
	    }
	}
	
	private class CacheRates extends AsyncTask<Void, Void, Void>{
		
	    @Override
	    public Void doInBackground(Void... params) {
			
	    	CurrencyConverterUtil.CacheRates(getApplicationContext(), Arrays.asList(CurrencyConverterUtil.CURRENCY_LIST));
			return null;
	     
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
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void TextFieldClicked(View view){      
		EditText editText = (EditText)view;
		editText.setText("");
	}
	
	public void changeTextColorForMainField() {
		
		EditText currentSelectedItem = (EditText)getWindow().getCurrentFocus();
		setTextColorToDefault();
		currentSelectedItem.setTextColor(Color.RED);
		currentSelectedItem.setTypeface(null,Typeface.BOLD);
		
	}
	
	private void setTextColorToDefault() {
		
		for (int i = 0; i < 5; i++) {
			
			((CurrencyBoxView)boxViewList.get(i)).getCurrValueField().setTextColor(defaultTextColor);
			((CurrencyBoxView)boxViewList.get(i)).getCurrValueField().setTypeface(null,Typeface.NORMAL);
			
		}
		
	}
	
	
}
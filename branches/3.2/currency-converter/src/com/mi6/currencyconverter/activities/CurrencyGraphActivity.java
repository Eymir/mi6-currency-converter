package com.mi6.currencyconverter.activities;

import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.conn.ConnectTimeoutException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.sqlite.helper.DatabaseHelper;
import com.mi6.currencyconverter.sqlite.model.RateDetails;
import com.mi6.currencyconverter.utils.CurrencyConverterConstants;
import com.mi6.currencyconverter.utils.CurrencyConverterUtil;

public class CurrencyGraphActivity extends Activity {
	
	protected Activity activity = this;
	protected List<RateDetails> historicalData = null;
	private Spinner fromSpinner, toSpinner;
	private Button btnSubmit;
	GraphViewSeries exampleSeries;
	GraphView graphView;
	
	DatabaseHelper db = null;
	
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.currency_graph);

		addItemsOnSpinners();
		addListenerOnButton();
		
	}
	
	@Override
	protected void onResume() {
		addItemsOnSpinners();
		addListenerOnButton();
		if (CurrencyConverterUtil.IsNetworkAvailable(getApplicationContext())) {
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			    new GetHistoricalData((String)fromSpinner.getSelectedItem(), (String)toSpinner.getSelectedItem()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
			    new GetHistoricalData((String)fromSpinner.getSelectedItem(), (String)toSpinner.getSelectedItem()).execute();
			}
	    }
	   super.onResume();
	}
	
	// add items into spinners dynamically
	  public void addItemsOnSpinners() {
	 
		Set<String> usedCurrencies = new LinkedHashSet<String>();
	    usedCurrencies = CurrencyConverterUtil.ConvertStringToSet(
	    		PreferenceManager.getDefaultSharedPreferences(this).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
		  
	    	if (usedCurrencies.size() < 2) {
		    	Toast.makeText(activity, 
		    			R.string.alertDialog_messages_no_currencies_on_display_list,
		    			Toast.LENGTH_LONG).show();
		    	setDefaultCurrenciesOnDisplayList();
		    	usedCurrencies = CurrencyConverterUtil.ConvertStringToSet(
		    			PreferenceManager.getDefaultSharedPreferences(this).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
	    	}
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		ArrayAdapter<String> dataAdapterFrom = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, usedCurrencies.toArray(new String[usedCurrencies.size()]));
		dataAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fromSpinner.setAdapter(dataAdapterFrom);
		
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		ArrayAdapter<String> dataAdapterTo = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, usedCurrencies.toArray(new String[usedCurrencies.size()]));
		dataAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		toSpinner.setAdapter(dataAdapterTo);
		toSpinner.setSelection(1);
	  }

	private void setDefaultCurrenciesOnDisplayList() {
		Set<String> defaultUsedCurrencies = new LinkedHashSet<String>();
		defaultUsedCurrencies.add("USD");
		defaultUsedCurrencies.add("EUR");
		Editor e = PreferenceManager.getDefaultSharedPreferences(activity).edit();
		e.clear();
		e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(defaultUsedCurrencies));
		e.commit();
	}
	  
	  // get the selected dropdown list value
	  public void addListenerOnButton() {
	 
		fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
		toSpinner = (Spinner) findViewById(R.id.toSpinner);
		btnSubmit = (Button) findViewById(R.id.btnSubmit);
	 
		btnSubmit.setOnClickListener(new OnClickListener() {
	 
		  @Override
		  public void onClick(View v) {
			  Log.i("CurrencyGraphActivity.onCreate.addListenerOnButton", "Button clicked");
			  if (CurrencyConverterUtil.IsNetworkAvailable(getApplicationContext())) {
					if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
					    new GetHistoricalData((String)fromSpinner.getSelectedItem(), (String)toSpinner.getSelectedItem()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} else {
					    new GetHistoricalData((String)fromSpinner.getSelectedItem(), (String)toSpinner.getSelectedItem()).execute();
					}
			    }
		  }

	 
		});
	  }
	
	private class GetHistoricalData extends AsyncTask<Void, Void, Void>{

		String fromCurrency;
		String tocurrency;
		
		
		
	    public GetHistoricalData(String fromCurrency, String tocurrency) {
			super();
			this.fromCurrency = fromCurrency;
			this.tocurrency = tocurrency;
		}

		@Override
	    public Void doInBackground(Void... params) {
			
			db = new DatabaseHelper(getApplicationContext());
	    	Calendar fromDate = Calendar.getInstance();
	    	fromDate.add(Calendar.DAY_OF_MONTH, -30);
	    	Calendar toDate = Calendar.getInstance();  
	    	historicalData = db.getHistoricalDataForRate(fromCurrency, tocurrency);
	    	if ((historicalData == null) || (historicalData.size() < 19)) {
		    		try {
		    			historicalData = null; 
		    			historicalData = CurrencyConverterUtil.GetHistoricalData(fromCurrency, tocurrency, new Date(fromDate.getTimeInMillis()), new Date(toDate.getTimeInMillis()));
		    			//cache rates
		    			db.addMultipleRateDetails(historicalData);
				} catch (ConnectTimeoutException e) {
					Log.e("CurrencyGraphActivity", "ConnectionTimeoutException !!!!!!!!!");
					activity.runOnUiThread(new Runnable() {
					    public void run() {
					        Toast.makeText(activity, R.string.alertDialog_messages_connect_timeout, Toast.LENGTH_SHORT).show();
					    }
					});
					e.printStackTrace();
				} catch (UnknownHostException e) {
					Log.e("CurrencyGraphActivity", "UnknownHostException !!!!!!!!!");
					activity.runOnUiThread(new Runnable() {
					    public void run() {
					        Toast.makeText(activity, R.string.alertDialog_messages_unknown_host, Toast.LENGTH_SHORT).show();
					    }
					});
					e.printStackTrace();
				}
	    	} else {
	    		Log.i("CurrencyGraphActivity", "Values already stored in DB. No call to server needed.");
	    	}
			return null;
	     
	    }
	    
	    @Override
	    protected void onPostExecute(Void result) {
	    	
			if (historicalData == null) {
				activity.runOnUiThread(new Runnable() {
				    public void run() {
				        Toast.makeText(activity, R.string.alertDialog_messages_connect_timeout, Toast.LENGTH_SHORT).show();
				    }
				});
			} else {
	
				/*
				 * use Date as x axis label
				 */
				GraphViewData[] data = new GraphViewData[historicalData.size()];
				for (int i=0; i<historicalData.size(); i++) {
					data[i] = new GraphViewData((Date.valueOf(historicalData.get(i).getRateDate()).getTime()), historicalData.get(i).getRateValue()); // previous day
				}
				if (exampleSeries != null) {
					exampleSeries.resetData(data);
				} else {
					exampleSeries = new GraphViewSeries(data);
				}
	
					graphView = new LineGraphView(activity, "Historical Data");
					((LineGraphView) graphView).setDrawBackground(true);
				graphView.addSeries(exampleSeries); // data

	
				/*
				 * date as label formatter
				 */
				final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
				graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
					@Override
					public String formatLabel(double value, boolean isValueX) {
						if (isValueX) {
							Date d = new Date((long) value);
							return dateFormat.format(d);
						}
						return null; // let graphview generate Y-axis label for us
					}
				});
				graphView.setFitsSystemWindows(true);
				graphView.getGraphViewStyle().setTextSize(15);
				LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
				layout = (LinearLayout) findViewById(R.id.graph);
				layout.addView(graphView);
		    	
		    }
			db.closeDB();
	    }
	}
	
}


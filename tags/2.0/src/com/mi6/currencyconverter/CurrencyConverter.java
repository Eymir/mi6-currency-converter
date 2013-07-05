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
import android.widget.Toast;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.dto.RateValues;

public class CurrencyConverter extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private EditText firstCurr;
	private EditText secondCurr;
	private Double liveRates;
	private Button convert;
	private Spinner spinnerFrom, spinnerTo;
	private CheckBox onlineRates;
	
	private AlertDialog alertDialog;
	private Button alertButton;
	
	private CurrencyDetails currencyDetails;
	
	private Menu menu;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        addMainLayout();
        addAlertDialog();
        addItemsOnSpinner2();
    	addListenerOnSpinnerItemSelection();
    	
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
        firstCurr = (EditText)findViewById(R.id.firstCurr);
        secondCurr = (EditText)findViewById(R.id.secondCurr);
        convert = (Button)this.findViewById(R.id.convert);        
        convert.setOnClickListener(this);
        onlineRates = (CheckBox)findViewById(R.id.onlineRates);
	}
    
    // add items into spinner dynamically
    public void addItemsOnSpinner2() {
   
	  	spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
	  	spinnerFrom = (Spinner) findViewById(R.id.spinnerFrom);
	  	
	  	currencyDetails = CurrencyRatesUtil.ReadCurrencyDetailsFromCsv(this.getApplicationContext(), (String)spinnerFrom.getSelectedItem());
	  	List<RateValues> rateValuesList = currencyDetails.getRateValues();
	  	List<String> list = new ArrayList<String>();
	  	
	  	for (RateValues rv : rateValuesList) {
	  		list.add(rv.getName());
	  	}
	  	
	  	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
	  		android.R.layout.simple_spinner_item, list);
	  	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	  	spinnerTo.setAdapter(dataAdapter);
	    }
	   
	public void addListenerOnSpinnerItemSelection() {
	  	spinnerFrom = (Spinner) findViewById(R.id.spinnerFrom);
	  	spinnerFrom.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }
   
	public void convertAction(View view) {
		// TODO Auto-generated method stub
	
		if (hasFirstFieldData() && hasSecondFieldData()) {
			// Showing Alert Message
			alertDialog.setMessage((String)this.getString(R.string.alertDialog_messages_delete_value_from_field));
	        alertDialog.show();
		} else {
			if (!hasFirstFieldData() && !hasSecondFieldData()) {
				// Showing Alert Message
				alertDialog.setMessage((String)this.getString(R.string.alertDialog_messages_add_value_in_field));
		        alertDialog.show();
			} else {
				if (hasFirstFieldData()) {
					convertFirstToSecond();
				}
				if (hasSecondFieldData()) {
					convertSecondToFirst();
				}
			}
		}
	}

	protected void convertSecondToFirst() {
		Double currencyRate;
		if (onlineRates.isChecked()) {
			new GetLiveRatesTask().execute();
		} else {
			String amount;
			DecimalFormat f = new DecimalFormat("#.##");
			currencyRate = getCurencyRate();
			double val = Double.parseDouble(secondCurr.getText().toString());
			amount = f.format(val/currencyRate);
			firstCurr.setText(amount);
		}
		

	}


	protected void convertFirstToSecond() {
		Double currencyRate;
		if (onlineRates.isChecked()) {
			new GetLiveRatesTask().execute();
			
		} else {
			String amount;
			DecimalFormat f = new DecimalFormat("#.##");
			currencyRate = getCurencyRate();
			double val = Double.parseDouble(firstCurr.getText().toString());
			amount = f.format(val*currencyRate);
			secondCurr.setText(amount);
		}


	}
	
	private boolean hasFirstFieldData() {
		
		if ((firstCurr != null) && (!"".equalsIgnoreCase(firstCurr.getText().toString()))) {
			return true;
		} else {
			return false;
		}
		
	}
	private boolean hasSecondFieldData() {
		
		if ((secondCurr != null) && (!"".equalsIgnoreCase(secondCurr.getText().toString()))) {
			return true;
		} else {
			return false;
		}
		
	}
	
	private Double getCurencyRate() {
		String spinnerToValue;
		Double currencyRate = Double.valueOf(0);
		spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
		spinnerToValue = String.valueOf(spinnerTo.getSelectedItem());
		
		for (RateValues rv : currencyDetails.getRateValues()) {
	  		if (rv.getName().equals(spinnerToValue)){
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
			
	    	String url = "http://finance.yahoo.com/d/quotes.csv?e=goog.csv&f=sl1&s="+(String)spinnerFrom.getSelectedItem()+(String)spinnerTo.getSelectedItem()+"=x";
	    	
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
                        String line = reader.readLine();
                        line = line.subSequence(11, line.length()).toString();
                        liveRates = Double.parseDouble(line);
                } else {
                        Log.e("Getter", "Failed to download file");
                }
        } catch (ClientProtocolException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
			return null;
	     
	    }
	    
	    
	    @Override
	    protected void onPostExecute(Void result) {
	     
	    	if (hasFirstFieldData()) {
	    		String amount;
	    		DecimalFormat f = new DecimalFormat("#.##");
	    		double val = Double.parseDouble(firstCurr.getText().toString());
	    		amount = f.format(val*liveRates);
				secondCurr.setText(amount);
			}
			if (hasSecondFieldData()) {
				String amount;
	    		DecimalFormat f = new DecimalFormat("#.##");
				double val = Double.parseDouble(secondCurr.getText().toString());
				amount = f.format(val/liveRates);
				firstCurr.setText(amount);
			}
	    	
			super.onPostExecute(result);   
	    }
	}



	@Override
	public void onClick(View v) {
		convertAction(v);
		
	}
	
	
}
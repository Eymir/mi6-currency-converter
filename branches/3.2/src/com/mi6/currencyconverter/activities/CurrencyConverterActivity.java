package com.mi6.currencyconverter.activities;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.conn.ConnectTimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.adapters.CurrencyConvertorArrayAdapter;
import com.mi6.currencyconverter.sqlite.helper.DatabaseHelper;
import com.mi6.currencyconverter.sqlite.model.CurrencyDetails;
import com.mi6.currencyconverter.sqlite.model.RateDetails;
import com.mi6.currencyconverter.ui.TouchListView;
import com.mi6.currencyconverter.utils.CurrencyConverterConstants;
import com.mi6.currencyconverter.utils.CurrencyConverterUtil;
import com.mi6.currencyconverter.utils.CurrencyParser;
import com.mi6.currencyconverter.utils.SwipeDismissListViewTouchListener;

public class CurrencyConverterActivity extends ListActivity implements OnClickListener {

	private static final int defaultTextColor = Color.WHITE;
	/** Called when the activity is first created. */
	
	Activity activity = this;
	private Button convert;
	private ProgressDialog progressDialog ;
	private AlertDialog alertDialog;
	private Button alertButton;
	// Create a customized ArrayAdapter
	CurrencyConvertorArrayAdapter adapter = null;
	List<CurrencyDetails> displayedList = new ArrayList<CurrencyDetails>();
	CurrencyDetails mainCurrency = null;
	// Database Helper
    DatabaseHelper db;
				
	// Get reference to ListView holder
    TouchListView lv = null;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convertor_listview);
        addMainLayout();
        addProgressDialog();
        addAlertDialog();
        if (CurrencyConverterUtil.IsNetworkAvailable(getApplicationContext())) {
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			    new CacheRates(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
			    new CacheRates(this).execute();
			}
	    }
    }
	
	@Override
	protected void onResume() {
		addMainLayout();
		addProgressDialog();
	   super.onResume();
	}
	
	private void addProgressDialog() {
		
		progressDialog = new ProgressDialog (this) ;
        progressDialog.setCancelable (false) ;
        progressDialog.setMessage ("Retrieving online rates...") ;
        progressDialog.setTitle ("Please wait") ;
        progressDialog.setIndeterminate (true) ;
		
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
		
		// Create Parser for raw/currencies.xml
		CurrencyParser currencyParser = new CurrencyParser();
		InputStream inputStream = getResources().openRawResource(R.raw.currencies);
				
		// Parse the inputstream
		currencyParser.parse(inputStream);

		// Get Currencies
		List<CurrencyDetails> currencyList = currencyParser.getList();
		
		Set<String> usedCurrencies = new LinkedHashSet<String>();
		Set<String> defaultUsedCurrencies = new LinkedHashSet<String>();
		defaultUsedCurrencies.add("USD");
		defaultUsedCurrencies.add("EUR");
    	
    	usedCurrencies = CurrencyConverterUtil.ConvertStringToSet(
    			PreferenceManager.getDefaultSharedPreferences(this).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
    	
    	if (usedCurrencies == null || usedCurrencies.size() == 0) {
    		Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
    		e.clear();
	    	e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(defaultUsedCurrencies));
	    	e.commit();
	    	usedCurrencies = defaultUsedCurrencies;
    	}
			for (CurrencyDetails currency:currencyList) {
				if ((usedCurrencies.contains(currency.getCode())) && (CurrencyConverterUtil.getCurrencyByCode(displayedList, currency.getCode())) == null) {
					displayedList.add(currency);
				}
			}
			displayedList = getSortedCurrencyList(displayedList, usedCurrencies);
			adapter = new CurrencyConvertorArrayAdapter(
					getApplicationContext(), R.layout.convertor_listitem, displayedList);
			// Set the ListView adapter
			lv = (TouchListView)getListView();
			lv.setAdapter(adapter);
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv.setSelector(android.R.color.holo_blue_light);
			lv.setDropListener(onDrop);
			// Create a ListView-specific touch listener. ListViews are given special treatment because
	        // by default they handle touches for their list items... i.e. they're in charge of drawing
	        // the pressed state (the list selector), handling list item clicks, etc.
			SwipeDismissListViewTouchListener touchListener =
	                new SwipeDismissListViewTouchListener(
	                        lv,
	                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
	                            @Override
	                            public boolean canDismiss(int position) {
	                                return true;
	                            }

	                            @Override
	                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	                                for (int position : reverseSortedPositions) {
	                                	Set<String> usedCurr = CurrencyConverterUtil.ConvertStringToSet(
	                                			PreferenceManager.getDefaultSharedPreferences(listView.getContext()).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
		                                usedCurr.remove((adapter.getItem(position)).getCode());
		                                Editor e = PreferenceManager.getDefaultSharedPreferences(listView.getContext()).edit();
		                                e.clear();
		                    	    	e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(usedCurr));
		                    	    	e.commit();
	                                    adapter.remove(adapter.getItem(position));
	                                }
	                                adapter.notifyDataSetChanged();
	                            }
	                        });
			lv.setOnTouchListener(touchListener);
	        // Setting this scroll listener is required to ensure that during ListView scrolling,
	        // we don't look for swipes.
	        lv.setOnScrollListener(touchListener.makeScrollListener());
        
        convert = (Button)this.findViewById(R.id.convert);        
        convert.setOnClickListener(this);
        
	}
	   
	private TouchListView.DropListener onDrop=new TouchListView.DropListener() {
		@Override
		public void drop(int from, int to) {
				CurrencyDetails item=adapter.getItem(from);
				Set<String> displayedCurrencies = new LinkedHashSet<String>();
				List<CurrencyDetails> currList = new ArrayList<CurrencyDetails>();				
				adapter.remove(item);
				adapter.insert(item, to);
				currList = adapter.getList();
				
				for(CurrencyDetails currency:currList){
					displayedCurrencies.add(currency.getCode());
				}
				Editor e = PreferenceManager.getDefaultSharedPreferences(activity).edit();
                e.clear();
        	    e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(displayedCurrencies));
        	    e.commit();
		}
	};
	
	public void convertAction(View view) {
		if ((displayedList != null) && (displayedList.size() > 0)) {
			if (!hasMainFieldData()) {
				// Showing Alert Message
				alertDialog.setMessage((String)this.getString(R.string.alertDialog_messages_add_value_in_field));
		        alertDialog.show();
			} else {
				//changeTextColorForMainField();
				mainCurrency = getMainCurrency();
				convert(mainCurrency.getCode());
				}
		} else {
			activity.runOnUiThread(new Runnable() {
			    public void run() {
			        Toast.makeText(activity, R.string.alertDialog_messages_no_currencies_on_display_list, Toast.LENGTH_SHORT).show();
			    }
			});
    		Set<String> defaultUsedCurrencies = new LinkedHashSet<String>();
    		defaultUsedCurrencies.add("USD");
    		defaultUsedCurrencies.add("EUR");
    		Editor e = PreferenceManager.getDefaultSharedPreferences(activity).edit();
    		e.clear();
	    	e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(defaultUsedCurrencies));
	    	e.commit();
	    	addMainLayout();
		}
	}

	protected void convert(String mainCurr) {
		
			if (CurrencyConverterUtil.IsNetworkAvailable(getApplicationContext())) {
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
					new GetLiveRatesTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					new GetLiveRatesTask(this).execute();
				}
			} else {
				Toast.makeText(this, 
						R.string.alertDialog_messages_no_internet_connection,
	        			Toast.LENGTH_LONG).show();
				TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		    	Date today = new Date(cal.getTimeInMillis());       
				Double valueToConvert;
				db = new DatabaseHelper(getApplicationContext());
				mainCurrency.setRateDetails(db.getAllRatesForSpecificDay(mainCurrency.getCode(), today));
				valueToConvert = mainCurrency.getValue();
		    		for (CurrencyDetails cd:displayedList) {
		    			Double convertedValue = mainCurrency.getSpecificRate(cd.getCode(), today)*valueToConvert;
		    			cd.setValue(convertedValue);
		    			if (convertedValue == 0) {
		    				Toast.makeText(this, 
		    						R.string.alertDialog_messages_no_cached_value,
		    	        			Toast.LENGTH_LONG).show();
		    			}
		    		}
				db.closeDB();
			adapter.notifyDataSetChanged();
			}
	}

	private CurrencyDetails getMainCurrency(){
		
		CurrencyDetails mainCurr = null;
		View currentSelectedItem = getWindow().getCurrentFocus();
		
		int position = lv.getPositionForView(currentSelectedItem);
		mainCurr = adapter.getItem(position);
		mainCurr.setValue(Double.parseDouble(((EditText)currentSelectedItem).getText().toString()));
		return mainCurr;
	} 
	
	private boolean hasMainFieldData() {
		
		View currentView = getWindow().getCurrentFocus();
		if (!(currentView instanceof ListView)){
			if ((currentView != null) && (((EditText)currentView).getText()).length()>0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.layout.menu, menu);
	    return true;
	}
	
	private class GetLiveRatesTask extends AsyncTask<Void, Void, Void>{
	    	  
		Context context;
		
		public GetLiveRatesTask(Context context) {
	        this.context = context;
	    }
		
		@Override
	    protected void onPreExecute(){
	        progressDialog.show();
	    }
		
	    @Override
	    public Void doInBackground(Void... params) {
			
	    	List<RateDetails> rd = new ArrayList<RateDetails>();
	    	List<String> targetCurrencies = new ArrayList<String>();
	    	
	    	if ((displayedList != null) && (displayedList.size() > 0)){
	    	
	    	for (CurrencyDetails currency:displayedList) {
	    		
	    		targetCurrencies.add(currency.getCode());
	    		
	    	}
	    	
	    	try {
				rd = CurrencyConverterUtil.getOnlineRates(mainCurrency.getCode(),targetCurrencies);
			} catch (ConnectTimeoutException e) {
				Log.e("CurrencyConverterActivity", "ConnectTimeoutException !!!!!!!!!");
				e.printStackTrace();
			} catch (UnknownHostException e) {
				Log.e("CurrencyConverterActivity", "UnknownHostException !!!!!!!!!");
				e.printStackTrace();
			}
	    	
	    	mainCurrency.setRateDetails(rd);
	    	} else {
	    		activity.runOnUiThread(new Runnable() {
				    public void run() {
				        Toast.makeText(activity, R.string.alertDialog_messages_no_currencies_on_display_list, Toast.LENGTH_SHORT).show();
				    }
				});
	    		Set<String> defaultUsedCurrencies = new LinkedHashSet<String>();
	    		defaultUsedCurrencies.add("USD");
	    		defaultUsedCurrencies.add("EUR");
	    		Editor e = PreferenceManager.getDefaultSharedPreferences(activity).edit();
	    		e.clear();
		    	e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(defaultUsedCurrencies));
		    	e.commit();
	    	}
			return null;
	     
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	    	Double valueToConvert;
	    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	    	Date today = new Date(cal.getTimeInMillis()); 
			valueToConvert = mainCurrency.getValue();
	    		for (CurrencyDetails cd:displayedList) {
	    			if (mainCurrency.getRateDetails() != null) {
	    				cd.setValue(mainCurrency.getSpecificRate(cd.getCode(), today)*valueToConvert);
	    			} else {
	    				activity.runOnUiThread(new Runnable() {
	    				    public void run() {
	    				    	Toast.makeText(activity, 
	    								R.string.alertDialog_messages_no_internet_connection,
	    			        			Toast.LENGTH_LONG).show();
	    							        
	    						Double valueToConvert;
	    						db = new DatabaseHelper(getApplicationContext());
	    						mainCurrency.setRateDetails(db.getAllRatesForSpecificDay(mainCurrency.getCode(), new Date(Calendar.getInstance().getTimeInMillis())));
	    						valueToConvert = mainCurrency.getValue();
	    				    		for (CurrencyDetails cd:displayedList) {
	    				    			Double convertedValue = mainCurrency.getSpecificRate(cd.getCode(), new Date(Calendar.getInstance().getTimeInMillis()))*valueToConvert;
	    				    			cd.setValue(convertedValue);
	    				    			if (convertedValue == 0) {
	    				    				Toast.makeText(activity, 
	    				    						R.string.alertDialog_messages_no_cached_value,
	    				    	        			Toast.LENGTH_LONG).show();
	    				    			}
	    				    		}
	    				    		db.closeDB();
	    				    }
	    				});
	    			}
	    			
	    		}
	    	adapter.notifyDataSetChanged();
	    	if(progressDialog != null && progressDialog.isShowing()){
	            progressDialog.dismiss() ;
	        }
			super.onPostExecute(result);   
	    }
	}
	
	private class CacheRates extends AsyncTask<Void, Void, Void>{
		
		Context context;
		
		public CacheRates(Context context) {
	        this.context = context;
	    }
		
	    @Override
	    public Void doInBackground(Void... params) {
			
			Set<String> usedCurrencies = new LinkedHashSet<String>();
			db = new DatabaseHelper(getApplicationContext());
			boolean shouldCacheRates = false;
			
	    	usedCurrencies = CurrencyConverterUtil.ConvertStringToSet(
	    			PreferenceManager.getDefaultSharedPreferences(getParent()).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
	    	
	    	TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	    	Date today = new Date(cal.getTimeInMillis());
	    	
	    	for (String usedCurr:usedCurrencies) {
	    		List<RateDetails> rdList = db.getAllRatesForSpecificDay(usedCurr, today);
	    		if ((rdList != null) && (rdList.size() > 0) && (shouldCacheRates == false)) {
	    			shouldCacheRates = false;
	    		} else {
	    			shouldCacheRates = true;
	    		}
	    	}
	    	if (shouldCacheRates) {
		    	try {
					CurrencyConverterUtil.CacheRates(getApplicationContext(), new ArrayList<String>(usedCurrencies));
				} catch (ConnectTimeoutException e) {
					Log.e("CurrencyConverterActivity", "ConnectionTimeoutException !!!!!!!!!");
					activity.runOnUiThread(new Runnable() {
					    public void run() {
					        Toast.makeText(activity, R.string.alertDialog_messages_connect_timeout, Toast.LENGTH_SHORT).show();
					        Toast.makeText(activity, R.string.alertDialog_messages_not_caching_rates, Toast.LENGTH_SHORT).show();
					    }
					});
					e.printStackTrace();
				} catch (UnknownHostException e) {
					Log.e("CurrencyConverterActivity", "UnknownHostException !!!!!!!!!");
					activity.runOnUiThread(new Runnable() {
					    public void run() {
					        Toast.makeText(activity, R.string.alertDialog_messages_unknown_host, Toast.LENGTH_SHORT).show();
					        Toast.makeText(activity, R.string.alertDialog_messages_not_caching_rates, Toast.LENGTH_SHORT).show();
					    }
					});
					e.printStackTrace();
				}
	    }
	    	db.closeDB();
			return null;
	     
	    }

	}

	@Override
	public void onClick(View v) {
		convertAction(v);
		
	}
	
	public void changeTextColorForMainField() {
		
		EditText currentSelectedItem = (EditText)getWindow().getCurrentFocus();
		setTextColorToDefault();
		currentSelectedItem.setTextColor(Color.RED);
		currentSelectedItem.setTypeface(null,Typeface.BOLD);
		
	}
	
	private void setTextColorToDefault() {
		
		for( int i = 0; i < lv.getChildCount(); i++ )
			for (int j = 0; j < ((LinearLayout)lv.getChildAt(i)).getChildCount(); j++) {
				LinearLayout ll = (LinearLayout)lv.getChildAt(i);
				if (ll.getChildAt(j) instanceof LinearLayout){
				for (int k = 0; k < ((LinearLayout)ll.getChildAt(j)).getChildCount(); k++) {
			  if( lv.getChildAt(k) instanceof EditText) {
				  ((EditText)lv.getChildAt(k)).setTextColor(defaultTextColor);
				  ((EditText)lv.getChildAt(k)).setTypeface(null,Typeface.NORMAL);
			  }
				}
				}
			}
	}
	
	public void removeFromDisplayList(View view) {
		LinearLayout parentLayout = (LinearLayout)view.getParent();
		Set<String> usedCurrencies = new LinkedHashSet<String>();
    	
		TextView currencyCode = (TextView)parentLayout.findViewById(R.id.currency_code);
		usedCurrencies = CurrencyConverterUtil.ConvertStringToSet(
				PreferenceManager.getDefaultSharedPreferences(this).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
    	usedCurrencies.remove(currencyCode.getText());
    	Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
    	e.clear();
	    e.putString(CurrencyConverterConstants.LISTED_CURRENCIES, CurrencyConverterUtil.ConvertSetToString(usedCurrencies));
	    e.commit();
		
	}
	
	  protected void removeListItem(View rowView, final int positon) {

		  final Animation animation = AnimationUtils.loadAnimation(CurrencyConverterActivity.this,android.R.anim.slide_out_right);
	    //final Animation animation = AnimationUtils.loadAnimation(rowView.getContext(), R.anim.splashfadeout);
	      rowView.startAnimation(animation);
	      Handler handle = new Handler();
	      handle.postDelayed(new Runnable() {

			@Override
	          public void run() {
	              // TODO Auto-generated method stub
	        	  displayedList.remove(positon);
	              adapter.notifyDataSetChanged();
	              animation.cancel();
	          }
	      },1000);

	  }
	  private List<CurrencyDetails> getSortedCurrencyList(List<CurrencyDetails> curencies, Set<String> sortedCodes) {
		  List<CurrencyDetails> newList = new ArrayList<CurrencyDetails>();
		  
		  for(String code:sortedCodes) {
			  for(CurrencyDetails currDetails:curencies){
				  if (code.equalsIgnoreCase(currDetails.getCode())) {
					  newList.add(currDetails);
				  }
			  }
		  }
		  
		  return newList;
	  }
	
}
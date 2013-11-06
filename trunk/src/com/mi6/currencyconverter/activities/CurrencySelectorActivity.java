package com.mi6.currencyconverter.activities;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.adapters.CurrencySelectorArrayAdapter;
import com.mi6.currencyconverter.sqlite.model.CurrencyDetails;
import com.mi6.currencyconverter.utils.CurrencyConverterConstants;
import com.mi6.currencyconverter.utils.CurrencyConverterUtil;
import com.mi6.currencyconverter.utils.CurrencyParser;

public class CurrencySelectorActivity extends Activity {
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the View layer
		setContentView(R.layout.selector_listview);
		setTitle("Select Currencies");

		// Create Parser for raw/currencies.xml
		CurrencyParser currencyParser = new CurrencyParser();
		InputStream inputStream = getResources().openRawResource(R.raw.currencies);
		
		// Parse the inputstream
		currencyParser.parse(inputStream);

		// Get Currencies
		List<CurrencyDetails> currencyList = currencyParser.getList();
		Collections.sort(currencyList, new Comparator<CurrencyDetails>() {

	        public int compare(CurrencyDetails c1, CurrencyDetails c2) {
	            return c1.getCode().compareTo(c2.getCode());
	        }
		  });
		
		Set<String> usedCurrencies = new HashSet<String>();
    	
    	usedCurrencies = PreferenceManager.getDefaultSharedPreferences(this).getStringSet(CurrencyConverterConstants.LISTED_CURRENCIES, null);
    	if (usedCurrencies != null) {
    		currencyList = CurrencyConverterUtil.removeCurrenciesFromConvertionList(currencyList,usedCurrencies);
    	}
		
		// Create a customized ArrayAdapter
		final CurrencySelectorArrayAdapter adapter = new CurrencySelectorArrayAdapter(
				getApplicationContext(), R.layout.selector_listitem, currencyList);
		
		// Get reference to ListView holder
		ListView lv = (ListView) this.findViewById(R.id.selectorLV);
		
		// Set the ListView adapter
		lv.setAdapter(adapter);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		       // When clicked, show a toast with the TextView text
		    	CurrencyDetails currency = (CurrencyDetails) parent.getItemAtPosition(position);
		    	
		    	Set<String> usedCurrencies = new HashSet<String>();
		    	
		    	usedCurrencies = PreferenceManager.getDefaultSharedPreferences(getParent()).getStringSet(CurrencyConverterConstants.LISTED_CURRENCIES, null);
		    	usedCurrencies.add(currency.getCode());
		    	Editor e = PreferenceManager.getDefaultSharedPreferences(getParent()).edit();
		    	e.clear();
		    	e.putStringSet(CurrencyConverterConstants.LISTED_CURRENCIES, usedCurrencies);
		    	e.commit();
		    	adapter.remove(currency);
		    	
		      }
		     });
		
	}
	
	@Override
	protected void onResume() {

	   super.onResume();
	   this.onCreate(null);
	}
    
}

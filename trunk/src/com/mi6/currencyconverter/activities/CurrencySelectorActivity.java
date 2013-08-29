package com.mi6.currencyconverter.activities;

import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.adapters.CurrencyArrayAdapter;
import com.mi6.currencyconverter.dto.CurrencyDetails;
import com.mi6.currencyconverter.utils.CurrencyParser;

public class CurrencySelectorActivity extends Activity {
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the View layer
		setContentView(R.layout.selector_listview);
		setTitle("Select Currencies");

		// Create Parser for raw/countries.xml
		CurrencyParser currencyParser = new CurrencyParser();
		InputStream inputStream = getResources().openRawResource(R.raw.currencies);
		
		// Parse the inputstream
		currencyParser.parse(inputStream);

		// Get Countries
		List<CurrencyDetails> currencyList = currencyParser.getList();
		
		
		// Create a customized ArrayAdapter
		final CurrencyArrayAdapter adapter = new CurrencyArrayAdapter(
				getApplicationContext(), R.layout.selector_listitem, currencyList);
		
		// Get reference to ListView holder
		ListView lv = (ListView) this.findViewById(R.id.countryLV);
		
		// Set the ListView adapter
		lv.setAdapter(adapter);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		       // When clicked, show a toast with the TextView text
		    	CurrencyDetails currency = (CurrencyDetails) parent.getItemAtPosition(position);
		    	adapter.remove(currency);
		       Toast.makeText(getApplicationContext(),
		         "Clicked on Row: " + currency.getName(), 
		         Toast.LENGTH_LONG).show();
		      }
		     });
		
	}
    
}

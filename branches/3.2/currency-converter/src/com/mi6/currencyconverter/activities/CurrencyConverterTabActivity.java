package com.mi6.currencyconverter.activities;
 
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.mi6.currencyconverter.R;
 
@SuppressWarnings("deprecation")
public class CurrencyConverterTabActivity extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         
        TabHost tabHost = getTabHost();
         
        // Tab for Convert
        TabSpec convert = tabHost.newTabSpec("Convert");
        // setting Title and Icon for the Tab
        convert.setIndicator("Convert");
        Intent convertIntent = new Intent(this, CurrencyConverterActivity.class);
        convert.setContent(convertIntent);
         
     // Tab for Graph
        TabSpec graph = tabHost.newTabSpec("Historical Data");
        // setting Title and Icon for the Tab
        graph.setIndicator("Historical Data");
        Intent graphIntent = new Intent(this, CurrencyGraphActivity.class);
        graph.setContent(graphIntent);
        
        // Tab for Select
        TabSpec select = tabHost.newTabSpec("Select Currency");        
        select.setIndicator("Select Currency");
        Intent selectIntent = new Intent(this, CurrencySelectorActivity.class);
        select.setContent(selectIntent);
         
        // Adding all TabSpec to TabHost
        tabHost.addTab(convert); // Adding convert tab
        tabHost.addTab(select); // Adding select tab
        tabHost.addTab(graph); // Adding graph tab
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
	        case R.id.menu_exit:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
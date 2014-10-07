package com.mi6.currencyconverter.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.activities.CurrencyConverterActivity;
import com.mi6.currencyconverter.sqlite.model.CurrencyDetails;
import com.mi6.currencyconverter.utils.CurrencyConverterConstants;
import com.mi6.currencyconverter.utils.CurrencyConverterUtil;

public class CurrencyConvertorArrayAdapter extends ArrayAdapter<CurrencyDetails> {

	private static final String tag = "CurrencyConvertorArrayAdapter";
	private static final String ASSETS_DIR = "flags/";
	private Context context;
	private CurrencyConverterActivity activity;
	Set<String> usedCurrencies = CurrencyConverterUtil.ConvertStringToSet(
			PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString(CurrencyConverterConstants.LISTED_CURRENCIES, null));
	Editor e = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
	
	private List<CurrencyDetails> currencies = new ArrayList<CurrencyDetails>();

	public CurrencyConvertorArrayAdapter(Context context, int textViewResourceId,
			List<CurrencyDetails> objects, CurrencyConverterActivity activity) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.currencies = objects;
		this.activity = activity;
	}

	public int getCount() {
		return this.currencies.size();
	}
	
	public List<CurrencyDetails> getList() {
		return this.currencies;
	}

	public CurrencyDetails getItem(int index) {
		return this.currencies.get(index);
	}
	
	private class ViewHolder {
		
		ImageView currencyFlag;
		TextView currencyCode;
		EditText currencyValue;
		
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;
		// Get item
			CurrencyDetails currency = getItem(position);
		if (row == null) {
			// ROW INFLATION
			Log.d(tag, "Starting XML Row Inflation ... ");
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.convertor_listitem, parent, false);
			holder = new ViewHolder();
			holder.currencyFlag = (ImageView) row.findViewById(R.id.currency_flag);

			holder.currencyCode = (TextView) row.findViewById(R.id.currency_code);
			holder.currencyValue = (EditText) row.findViewById(R.id.currValue);
			
			holder.currencyValue.setOnEditorActionListener(new OnEditorActionListener() {        
			    @Override
			    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			        if(actionId==EditorInfo.IME_ACTION_DONE){
			        	activity.convertAction(v);
			        	Log.i("Convert softkey", "Converting ... ");
			        }
			    return false;
			    }

			});
			holder.currencyValue.setImeActionLabel("Convert", EditorInfo.IME_ACTION_DONE);
			holder.currencyCode.setText(currency.getCode());
			String imgFilePath = ASSETS_DIR + currency.getFlag();
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().getAssets()
						.open(imgFilePath));
				holder.currencyFlag.setImageBitmap(bitmap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Log.d(tag, "Image File: " + imgFilePath + " " + "Size: " +
			// bitmap.getHeight());

			if (currency.getValue() == 0) {
				holder.currencyValue.setText("");
			} else {
				holder.currencyValue.setText(Double.toString(currency.getValue()));
			}
			
			row.setTag(holder);
			Log.d(tag, "Successfully completed XML Row Inflation!");
		} else {
			
			holder = (ViewHolder) convertView.getTag();

		holder.currencyCode.setText(currency.getCode());
		String imgFilePath = ASSETS_DIR + currency.getFlag();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().getAssets()
					.open(imgFilePath));
			holder.currencyFlag.setImageBitmap(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Log.d(tag, "Image File: " + imgFilePath + " " + "Size: " +
		// bitmap.getHeight());

		if (currency.getValue() == 0) {
			holder.currencyValue.setText("");
		} else {
			holder.currencyValue.setText(Double.toString(currency.getValue()));
		}
		
		}
		return row;
	}

}

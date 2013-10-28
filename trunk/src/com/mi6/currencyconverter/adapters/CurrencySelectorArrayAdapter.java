package com.mi6.currencyconverter.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mi6.currencyconverter.R;
import com.mi6.currencyconverter.dto.CurrencyDetails;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CurrencySelectorArrayAdapter extends ArrayAdapter<CurrencyDetails> {

	private static final String tag = "CurrencySelectorArrayAdapter";
	private static final String ASSETS_DIR = "flags/";
	private Context context;

	private ImageView currencyFlag;
	private TextView currencyCode;
	private TextView currencyName;
	private TextView countryName;
	private List<CurrencyDetails> currencies = new ArrayList<CurrencyDetails>();

	public CurrencySelectorArrayAdapter(Context context, int textViewResourceId,
			List<CurrencyDetails> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.currencies = objects;
	}

	public int getCount() {
		return this.currencies.size();
	}

	public CurrencyDetails getItem(int index) {
		return this.currencies.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			// ROW INFLATION
			Log.d(tag, "Starting XML Row Inflation ... ");
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.selector_listitem, parent, false);
			Log.d(tag, "Successfully completed XML Row Inflation!");
		}

		// Get item
		CurrencyDetails currency = getItem(position);
		currencyFlag = (ImageView) row.findViewById(R.id.currency_flag);
		currencyCode = (TextView) row.findViewById(R.id.currency_code);
		currencyName = (TextView) row.findViewById(R.id.currency_name);
		countryName = (TextView) row.findViewById(R.id.country_name);

		currencyCode.setText(currency.getCode());
		currencyName.setText(currency.getName());
		String imgFilePath = ASSETS_DIR + currency.getFlag();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().getAssets()
					.open(imgFilePath));
			currencyFlag.setImageBitmap(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Log.d(tag, "Image File: " + imgFilePath + " " + "Size: " +
		// bitmap.getHeight());

		countryName.setText(currency.getCountry());

		return row;
	}

}

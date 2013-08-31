package com.mi6.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CurrencyBoxView {
    private LinearLayout boxLayout;
    private ImageView currencyFlag;
	private EditText currValueField;
    private TextView currencyCode;
    private View view;

    public CurrencyBoxView(Context context, int boxNr, LayoutInflater li) {

        init(boxNr, li);
    }

    private void init(int boxNr, LayoutInflater li) {
        this.view = li.inflate(R.layout.currency_box, null);

        this.boxLayout = (LinearLayout)view.findViewById(R.id.currLayoutId);
    	this.currValueField = (EditText)view.findViewById(R.id.curr);
    	this.currencyCode = (TextView)view.findViewById(R.id.currency_code); 
    	this.currencyFlag = (ImageView)view.findViewById(R.id.currency_flag);
    	 

    }

    public ImageView getCurrencyFlag() {
		return currencyFlag;
	}

	public void setCurrencyFlag(ImageView currencyFlag) {
		this.currencyFlag = currencyFlag;
	}
    
    public LinearLayout getBoxLayout() {
		return boxLayout;
	}

	public void setBoxLayout(LinearLayout boxLayout) {
		this.boxLayout = boxLayout;
	}

	public EditText getCurrValueField() {
		return currValueField;
	}

	public void setCurrValueField(EditText currValueField) {
		this.currValueField = currValueField;
	}

	public TextView getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(TextView currencyCode) {
		this.currencyCode = currencyCode;
	}

	public View getView() {
		return view;
	}

	public void TextFieldClicked(View view){      
		EditText editText = (EditText)view;
		editText.setText("");
	}

}

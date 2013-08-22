package com.mi6.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class CurrencyBoxView {
    private LinearLayout boxLayout;
    private EditText currValueField;
    private Spinner spinner;
    private View view;

    public CurrencyBoxView(Context context, int boxNr, LayoutInflater li) {

        init(boxNr, li);
    }

    private void init(int boxNr, LayoutInflater li) {
        this.view = li.inflate(R.layout.currency_box, null);

        this.boxLayout = (LinearLayout)view.findViewById(R.id.currLayoutId);
    	this.currValueField = (EditText)view.findViewById(R.id.curr);
    	this.spinner = (Spinner)view.findViewById(R.id.spinner); 
    	 

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

	public Spinner getSpinner() {
		return spinner;
	}

	public void setSpinner(Spinner spinner) {
		this.spinner = spinner;
	}

	public View getView() {
		return view;
	}

	public void TextFieldClicked(View view){      
		EditText editText = (EditText)view;
		editText.setText("");
	}

}

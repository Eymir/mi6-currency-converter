package com.mi6.currencyconverter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.mi6.currencyconverter.R;

public class CurrencyGraphActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.currency_graph);

		// draw sin curve
		int num = 150;
		GraphViewData[] data = new GraphViewData[num];
		double v=0;
		GraphView graphView;

		// draw random curve
		num = 1000;
		data = new GraphViewData[num];
		v=0;
		for (int i=0; i<num; i++) {
			v += 0.2;
			data[i] = new GraphViewData(i, Math.sin(Math.random()*v));
		}
		// graph with dynamically genereated horizontal and vertical labels
			graphView = new LineGraphView(
					this
					, "CurrencyConverter"
			);
			((LineGraphView) graphView).setDrawBackground(true);
		// add data
		graphView.addSeries(new GraphViewSeries(data));
		// set view port, start=2, size=10
		graphView.setViewPort(2, 10);
		graphView.setScalable(true);
		// set manual Y axis bounds
		graphView.setManualYAxisBounds(2, -1);
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.addView(graphView);
	}
}


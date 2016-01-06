package com.sina.util.dnscache.datagraph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.sina.util.dnscache.R;
import com.sina.util.dnscache.simulationtask.TaskManager;
import com.sina.util.dnscache.simulationtask.TaskModel;

public class ServerSpeedActivity extends Activity implements
		OnSeekBarChangeListener, OnChartValueSelectedListener {

	public ImageButton leftBtn = null;
	protected BarChart mChart;
	private Typeface mTf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_speed);

		leftBtn = (ImageButton) findViewById(R.id.left);
		leftBtn.setOnClickListener(new ImageButton.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		super.onCreate(savedInstanceState);

//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mChart = (BarChart) findViewById(R.id.chart1);
		mChart.setOnChartValueSelectedListener(this);

		mChart.setDrawBarShadow(false);
		mChart.setDrawValueAboveBar(true);

		mChart.setDescription("");

		// if more than 60 entries are displayed in the chart, no values will be
		// drawn
		mChart.setMaxVisibleValueCount(60);

		// scaling can now only be done on x- and y-axis separately
		mChart.setPinchZoom(false);

		// draw shadows for each bar that show the maximum value
		// mChart.setDrawBarShadow(true);

		// mChart.setDrawXLabels(false);

		mChart.setDrawGridBackground(false);
		// mChart.setDrawYLabels(false);

		mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

		XAxis xAxis = mChart.getXAxis();
		xAxis.setPosition(XAxisPosition.BOTTOM);
		xAxis.setTypeface(mTf);
		xAxis.setDrawGridLines(false);
		xAxis.setSpaceBetweenLabels(2);

		// 就不增加单位了 为了可视范围
		// ValueFormatter custom = new MyValueFormatter();

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.setTypeface(mTf);
		leftAxis.setLabelCount(8);
		// leftAxis.setValueFormatter(custom);
		leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART);
		leftAxis.setSpaceTop(15f);

		YAxis rightAxis = mChart.getAxisRight();
		rightAxis.setDrawGridLines(false);
		rightAxis.setTypeface(mTf);
		rightAxis.setLabelCount(8);
		// rightAxis.setValueFormatter(custom);
		rightAxis.setSpaceTop(15f);

		Legend l = mChart.getLegend();
		l.setPosition(LegendPosition.BELOW_CHART_LEFT);
		l.setForm(LegendForm.SQUARE);
		l.setFormSize(9f);
		l.setTextSize(11f);
		l.setXEntrySpace(4f);

		setData();

		// mChart.setDrawLegend(false);

	}

	static class DataRate {
		public long dataLength;
		public long dataElapsed;

		public DataRate(long dataLength, long dataElapsed) {
			super();
			this.dataLength = dataLength;
			this.dataElapsed = dataElapsed;
		}
	}

	public void setData() {

		ArrayList<TaskModel> list = TaskManager.getInstance().list;
		if (list == null)
			return;

		Hashtable<String, ArrayList<DataRate>> serverIpSpeedList = new Hashtable<String, ArrayList<DataRate>>();
		for (TaskModel taskModel : list) {
			ArrayList<DataRate> ips = serverIpSpeedList.get(taskModel.hostIp);
			if (ips == null) {
				ips = new ArrayList<DataRate>();
				serverIpSpeedList.put(taskModel.hostIp, ips);
			}
			if (null != taskModel.hostInputStreamResult) {
				ips.add(new DataRate(taskModel.hostInputStreamResult.length,
						taskModel.hostExpendTime + taskModel.httpDnsExpendTime));
			}

			ips = serverIpSpeedList.get(taskModel.domainIp);
			if (ips == null) {
				ips = new ArrayList<DataRate>();
				serverIpSpeedList.put(taskModel.domainIp, ips);
			}
			if (null != taskModel.domainInputStreamResult) {
				ips.add(new DataRate(taskModel.domainInputStreamResult.length,
						taskModel.domainExpendTime));
			}
		}

		ArrayList<String> ip = new ArrayList<String>();
		ArrayList<Long> speed = new ArrayList<Long>();
		for (Iterator<String> itr = serverIpSpeedList.keySet().iterator(); itr
				.hasNext();) {
			String key = (String) itr.next();
			ArrayList<DataRate> value = (ArrayList<DataRate>) serverIpSpeedList
					.get(key);
			ip.add(key);
			long sunSize = 0;
			long sunElapsed = 1;
			for (DataRate l : value) {
				sunSize += l.dataLength;
				sunElapsed += l.dataElapsed;
			}
			speed.add((long) ((sunSize / sunElapsed) * 1.024));
		}

		//
		ArrayList<String> xVals = new ArrayList<String>();
		for (int i = 0; i < ip.size(); i++) {
			xVals.add(ip.get(i));
		}

		ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
		for (int i = 0; i < speed.size(); i++) {
			yVals1.add(new BarEntry(speed.get(i), i));
		}

		BarDataSet set1 = new BarDataSet(yVals1, "服务器平均下载速率（单位KB/S）");
		set1.setBarSpacePercent(35f);

		ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
		dataSets.add(set1);

		BarData data = new BarData(xVals, dataSets);
		// data.setValueFormatter(new MyValueFormatter());
		data.setValueTextSize(10f);
		data.setValueTypeface(mTf);

		//

		mChart.setData(data);
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	/**
	 * 分割字符
	 * 
	 * @param data
	 * @param separator
	 * @param len
	 * @return
	 */
	public String DelimitingCharacter(String data, String separator, int len) {
		StringBuffer buff = new StringBuffer(data);
		int index = len;
		while (index < buff.length()) {
			buff.insert(index, separator);
			index = index + len + separator.length();
		}
		return buff.toString();
	}

	class MyValueFormatter implements ValueFormatter {

		private DecimalFormat mFormat;

		public MyValueFormatter() {
			mFormat = new DecimalFormat("###,###,###,##0.0");
		}

		@Override
		public String getFormattedValue(float value) {
			return mFormat.format(value) + " KB/s";
		}

	}

}

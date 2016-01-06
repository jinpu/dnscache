package com.sina.util.dnscache.datagraph;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.sina.util.dnscache.R;
import com.sina.util.dnscache.simulationtask.TaskManager;
import com.sina.util.dnscache.simulationtask.TaskModel;

public class HttpDNSExpendTimeActivity extends Activity implements OnSeekBarChangeListener,
OnChartGestureListener, OnChartValueSelectedListener{

	public ImageButton leftBtn = null ;

	private LineChart mChart = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_httpdns_expendtime);

		leftBtn = (ImageButton)findViewById(R.id.left) ;
		leftBtn.setOnClickListener( new ImageButton.OnClickListener(){
			public void onClick(View v) {
				finish();
			}
		} );
		
		initData(); 
		
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // set the marker to the chart
//        mChart.setMarkerView(mv);

        // enable/disable highlight indicators (the lines that indicate the
        // highlighted Entry)
        mChart.setHighlightIndicatorEnabled(false);
        
        // x-axis limit line
//        LimitLine llXAxis = new LimitLine(10f, "Index 10");
//        llXAxis.setLineWidth(4f);
//        llXAxis.enableDashedLine(10f, 10f, 0f);
//        llXAxis.setLabelPosition(LimitLabelPosition.POS_RIGHT);
//        llXAxis.setTextSize(10f);
//        
//        XAxis xAxis = mChart.getXAxis();
//        xAxis.addLimitLine(llXAxis);
        
        LimitLine ll1 = new LimitLine(MAX_TIME, "最高耗时");
        ll1.setLineWidth(4);
        ll1.enableDashedLine(10, 10, 0);
        ll1.setLabelPosition(LimitLabelPosition.POS_RIGHT);
        ll1.setTextSize(10);

        LimitLine ll2 = new LimitLine(MIN_TIME, "最低耗时");
        ll2.setLineWidth(4);
        ll2.enableDashedLine(10, 10, 0);
        ll2.setLabelPosition(LimitLabelPosition.POS_RIGHT);
        ll2.setTextSize(10);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(MAX_TIME + 50);
        leftAxis.setAxisMinValue(MIN_TIME - 50);
        leftAxis.setStartAtZero(false);
        leftAxis.enableGridDashedLine(10, 10, 0);
        
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        // add data
        setData();
        
//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);
        
        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();
        
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
		
	}
	
	public void setData(){
		
		ArrayList<TaskModel> list = TaskManager.getInstance().list;
		if (list == null)
			return;
		
		
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            xVals.add((i) + "");
        }
		
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < list.size(); i++) {
        	TaskModel taskModel = list.get(i) ;
            yVals.add(new Entry(taskModel.httpDnsExpendTime, i));
        }
        
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "Http DNS lib库 返回结果耗时折线图  (单位: 毫秒)");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10, 5, 0);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1);
        set1.setCircleSize(3);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);
//        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart.setData(data);
		
	}
	
	
	public long MAX_TIME = -1 ; 
	public long MIN_TIME = 99999 ; 
	
	
	public void initData(){
		
		ArrayList<TaskModel> list = TaskManager.getInstance().list;
		if (list == null)
			return;
		
		for( TaskModel taskModel : list ){
			
			if( MAX_TIME < taskModel.httpDnsExpendTime ){
				MAX_TIME = taskModel.httpDnsExpendTime ; 
			}

			if( MIN_TIME > taskModel.httpDnsExpendTime ){
				MIN_TIME = taskModel.httpDnsExpendTime ; 
			}
			
		}
		
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
	public void onChartLongPressed(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onChartSingleTapped(MotionEvent me) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
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


}

package com.ravenmistmedia.MyHealthRecords;


import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.XYChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;
 
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class AnalysisActivity extends Activity {
	
	private static StringBuffer SYSinfoBuffer;
	private Spinner spnTableName;
	private String[] reportnames;
	private String[] reportdescriptions;
	private int iUnitMeasure;
	private int pos;
	
	@Override
    protected void onResume() {
		super.onResume();
		Common.CheckLogin(this);  
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutAnalysis );  
        final DBAdapter db = new DBAdapter(this);
        db.open();
        String s = db.getSetting("BackgroundColor");
        db.close();
        if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        } 	 
		} catch (Exception e) {}
		
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Common.CheckLogin(this); 
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.analysis);
        
        changeTitle();
        
    	reportnames = getResources().getStringArray(R.array.reports_array);
    	reportdescriptions = getResources().getStringArray(R.array.reportsdesc_array);
    	
    	ListView datasetListView = (ListView) findViewById( R.id.datasetListView );  
        
        ArrayList<Reports> menuResults = new ArrayList<Reports>();
            
        
        int len = reportnames.length;
        for (int i = 0; i < len; ++i) {
            Reports mr1 = new Reports();
            mr1.setMenuItem(reportnames[i].toString());
            mr1.setMenuDescription(reportdescriptions[i].toString());
            menuResults.add(mr1);
        }
        
	    	        	      
	    datasetListView.setAdapter(new ReportsBaseAdapter(this,menuResults)); 
	    	    
	    datasetListView.setOnItemClickListener(new OnItemClickListener() {
		    @Override
			public void onItemClick(AdapterView<?> parent, View view,
	    	        int position, long id) {
		    		RelativeLayout rl=(RelativeLayout)findViewById(R.id.graph);
		    		final GraphicalView gv;
					switch (position){
					case 0:
						// blood pressure time of day
						createBloodPressureTimeOfDayAnalysis("All records");
						break;
					case 1:
						// blood pressure progress
						gv = createBloodPressureProgress();
						rl.addView(gv);   
						break;
					case 2:
						// blood sugar time of day
						createBloodSugarTimeOfDayAnalysis("All records");
						break;
					case 3:
						// blood sugar progress
						gv = createBloodSugarProgress();
						rl.addView(gv);   
						break;
					case 4:
						//weight progress
						gv = createWeightProgress();
						rl.addView(gv);      
						break;
					case 5:
						// bmi progress
						gv = createBMIProgress();
						rl.addView(gv);      
						break;
					case 6:
						// temperature progress
						gv = createTemperatureProgress();
						rl.addView(gv);
						break;
					}
		    }

		}); 
		
		
		final DBAdapter db = new DBAdapter(this);
		db.open();
		Cursor c = db.getAllSettings();
        
		if (c.moveToFirst())
        {
			String sSettingName = c.getString(1);
			String sSettingValue = c.getString(2);
			
			if (sSettingName.trim().equals("unitsMeasure")){
				iUnitMeasure = Integer.parseInt(sSettingValue);
			}
        }
        db.close();

    }
    

public GraphicalView createWeightProgress() {
	
	final DBAdapter db = new DBAdapter(this);
	// Create an array of xy-values to plot:
	db.open();
    int personid = db.getCurrentPersonId();
    Cursor c;
    c = db.getWeightRecordsByPersonIdAscending(personid);
    int i = 0;
    double[] weightNumbers = new double[c.getCount()];
    String[] datesWeighed = new String[c.getCount()];
    double lowWeight = (double) 0;
    double highWeight = (double) 0;
    List<double[]> values = new ArrayList<double[]>();
        
    if (c.moveToFirst())
    {
        do {
        	final String sWeight = c.getString(2);
        	final String sRecDate = c.getString(4);
        	
        	long lD = Long.parseLong(sRecDate);
        	DateFormat df = DateFormat.getDateInstance();
        	Date dDate = new Date(lD);
        
        	Double fWeight = Double.parseDouble(sWeight);
        	if (lowWeight == 0 || fWeight < lowWeight){
        		lowWeight = fWeight;
        	}
        	if (highWeight == 0 || fWeight > highWeight){
        		highWeight = fWeight;
        	}
        	
    		weightNumbers[i] = fWeight;
    		datesWeighed[i] = df.format(dDate);
    		i++;
    	}while (c.moveToNext());
    }
	
    values.add(weightNumbers);
    
    String[] titles = new String[] { "Weights"};
    
    int[] colors = new int[] { Color.parseColor("#87294E")};
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        
    String s = "";
    
    if (iUnitMeasure == 0){
    	s = "Weight in pounds";
    } else {
    	s = "Weight in kilograms";
    }
    
    renderer.setOrientation(Orientation.HORIZONTAL);
    
    setChartSettings(renderer, "Weight Progress Analysis Report", "", s, 0.5,
        datesWeighed.length+0.5, lowWeight - 10, highWeight + 10, Color.BLACK, Color.BLACK);
    
    for (int k=0;k <datesWeighed.length;k++){
    	renderer.addXTextLabel(k+1, datesWeighed[k]);
    }
    renderer.setXLabels(0);
    
    int length = renderer.getSeriesRendererCount();
    for (int j = 0; j < length; j++) {
      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(j);
      seriesRenderer.setDisplayChartValues(true);
    }

    final GraphicalView grfv = ChartFactory.getBarChartView(AnalysisActivity.this, 
    		buildBarDataset(titles, values), renderer,Type.DEFAULT);
     
    return grfv;
  }

public GraphicalView createBMIProgress() {
	
	final DBAdapter db = new DBAdapter(this);
	// Create an array of xy-values to plot:
	db.open();
    int personid = db.getCurrentPersonId();
    Cursor c;
    c = db.getWeightRecordsByPersonIdAscending(personid);
    int i = 0;
    double[] weightNumbers = new double[c.getCount()];
    String[] datesWeighed = new String[c.getCount()];
    double lowWeight = (double) 0;
    double highWeight = (double) 0;
    List<double[]> values = new ArrayList<double[]>();
        
    if (c.moveToFirst())
    {
        do {
        	final String sHeight = c.getString(1);
        	final String sWeight = c.getString(2);
        	final String sRecDate = c.getString(4);
        	
        	long lD = Long.parseLong(sRecDate);
        	DateFormat df = DateFormat.getDateInstance();
        	Date dDate = new Date(lD);
        
        	Double fWeight = Double.parseDouble(ComputeBMI(sHeight,sWeight));
        	if (lowWeight == 0 || fWeight < lowWeight){
        		lowWeight = fWeight;
        	}
        	if (highWeight == 0 || fWeight > highWeight){
        		highWeight = fWeight;
        	}
        	
    		weightNumbers[i] = fWeight;
    		datesWeighed[i] = df.format(dDate);
    		i++;
    	}while (c.moveToNext());
    }
	
    values.add(weightNumbers);
    
    String[] titles = new String[] { "Body Mass Index"};
    
    int[] colors = new int[] { Color.parseColor("#87294E")};
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        
    String s = "";
    s = "BMI";
    
    renderer.setOrientation(Orientation.HORIZONTAL);
    
    setChartSettings(renderer, "BMI Progress Analysis Report", "", s, 0.5,
        datesWeighed.length+0.5, lowWeight - 10, highWeight + 10, Color.BLACK, Color.BLACK);
    
    for (int k=0;k <datesWeighed.length;k++){
    	renderer.addXTextLabel(k+1, datesWeighed[k]);
    }
    renderer.setXLabels(0);
    
    int length = renderer.getSeriesRendererCount();
    for (int j = 0; j < length; j++) {
      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(j);
      seriesRenderer.setDisplayChartValues(true);
    }

    final GraphicalView grfv = ChartFactory.getBarChartView(AnalysisActivity.this, 
    		buildBarDataset(titles, values), renderer,Type.DEFAULT);
     
    return grfv;
  }

public GraphicalView createBloodPressureProgress() {
	
	final DBAdapter db = new DBAdapter(this);
	// Create an array of xy-values to plot:
	db.open();
    int personid = db.getCurrentPersonId();
    Cursor c;
    c = db.getBPPulseRecordsByPersonIdAscending(personid,-1);
    int i = 0;
    double[] systolicNumbers = new double[c.getCount()];
    double[] diastolicNumbers = new double[c.getCount()];
    double[] pulseNumbers = new double[c.getCount()];
    String[] datesRecorded = new String[c.getCount()];
    double[] xvals = new double[c.getCount()];
    
    double lowSystolic = (double) 0;
    double highSystolic = (double) 0;
    double lowDiastolic = (double) 0;
    double highDiastolic = (double) 0;
    double lowPulse = (double) 0;
    double highPulse = (double) 0;
    
    List<double[]> yvalues = new ArrayList<double[]>();
    List<double[]> xvalues = new ArrayList<double[]>();
    
    if (c.moveToFirst())
    {
        do {
        	final String sSystolic = c.getString(1);
        	final String sDiastolic = c.getString(2);
        	final String sPulse = c.getString(3);
        	final String sRecDate = c.getString(5);
        	
        	long lD = Long.parseLong(sRecDate);
        	DateFormat df = DateFormat.getDateInstance();
        	Date dDate = new Date(lD);
        
        	Double fSystolic = Double.parseDouble(sSystolic);
        	if (lowSystolic == 0 || fSystolic < lowSystolic){
        		lowSystolic = fSystolic;
        	}
        	if (highSystolic == 0 || fSystolic > highSystolic){
        		highSystolic = fSystolic;
        	}
        	
    		systolicNumbers[i] = fSystolic;
    		
    		Double fDiastolic = Double.parseDouble(sDiastolic);
        	if (lowDiastolic == 0 || fDiastolic < lowDiastolic){
        		lowDiastolic = fDiastolic;
        	}
        	if (highDiastolic == 0 || fDiastolic > highDiastolic){
        		highDiastolic = fDiastolic;
        	}
        	
    		diastolicNumbers[i] = fDiastolic;
    		
    		Double fPulse = Double.parseDouble(sPulse);
        	if (lowPulse == 0 || fPulse < lowPulse){
        		lowPulse = fPulse;
        	}
        	if (highPulse == 0 || fPulse > highPulse){
        		highPulse = fPulse;
        	}
        	
    		pulseNumbers[i] = fPulse;
    		
    		datesRecorded[i] = df.format(dDate);
    		xvals[i] = (double)i;
    		i++;
    	}while (c.moveToNext());
    }
	
    yvalues.add(systolicNumbers);
    yvalues.add(diastolicNumbers);
    yvalues.add(pulseNumbers);
    
    xvalues.add(xvals);
    xvalues.add(xvals);
    xvalues.add(xvals);
    
    String[] titles = new String[] { "Systolic", "Diastolic","Pulse"};
    
    int[] colors = new int[] { Color.parseColor("#87294E"), Color.parseColor("#00294E"), Color.parseColor("#FF294E")};
    PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND,PointStyle.SQUARE };
    
    
    XYMultipleSeriesRenderer renderer = buildLineRenderer(colors,styles);
        
    String s = "";
    s = "mmHg";
    
    renderer.setOrientation(Orientation.HORIZONTAL);
    
    setChartSettings(renderer, "Blood Pressure Progress Analysis Report", "", s, 0.5,
        datesRecorded.length+0.5, 50, 200, Color.BLACK, Color.BLACK);
    
    for (int k=0;k <datesRecorded.length;k++){
    	renderer.addXTextLabel(k+1, datesRecorded[k]);
    }
    renderer.setXLabels(0);
    
    int length = renderer.getSeriesRendererCount();
    for (int j = 0; j < length; j++) {
      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(j);
      seriesRenderer.setDisplayChartValues(true);
    }
    renderer.setClickEnabled(true);
    
    final GraphicalView grfv = ChartFactory.getLineChartView(AnalysisActivity.this, 
    		buildLineDataset(titles, xvalues, yvalues),renderer);
    
    grfv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	
          SeriesSelection seriesSelection = grfv.getCurrentSeriesAndPoint();
          double[] xy = grfv.toRealPoint(0);

          if (seriesSelection == null) {
            Toast.makeText(AnalysisActivity.this, "No chart element was clicked", Toast.LENGTH_SHORT)
                .show();
          } else {
            Toast.makeText(
                    AnalysisActivity.this,
                "" + (int)(seriesSelection.getValue()),
                   Toast.LENGTH_LONG).show();
          }
        }
      }); 
     
    return grfv;
  }

public GraphicalView createBloodSugarProgress() {
	
	final DBAdapter db = new DBAdapter(this);
	// Create an array of xy-values to plot:
	db.open();
    int personid = db.getCurrentPersonId();
    Cursor c;
    c = db.getSugarRecordsByPersonIdAscending(personid,-1);
    int i = 0;
    double[] sugarNumbers = new double[c.getCount()];
    String[] datesSugared = new String[c.getCount()];
    double lowSugar = (double) 0;
    double highSugar = (double) 0;
    List<double[]> values = new ArrayList<double[]>();
        
    if (c.moveToFirst())
    {
        do {
        	final String sSugar = c.getString(1);
        	final String sRecDate = c.getString(7);
        	
        	long lD = Long.parseLong(sRecDate);
        	DateFormat df = DateFormat.getDateInstance();
        	Date dDate = new Date(lD);
        
        	Double fSugar = Double.parseDouble(sSugar);
        	if (lowSugar == 0 || fSugar < lowSugar){
        		lowSugar = fSugar;
        	}
        	if (highSugar == 0 || fSugar > highSugar){
        		highSugar = fSugar;
        	}
        	
    		sugarNumbers[i] = fSugar;
    		datesSugared[i] = df.format(dDate);
    		i++;
    	}while (c.moveToNext());
    }
	
    values.add(sugarNumbers);
    
    String[] titles = new String[] { "Blood Sugar"};
    
    int[] colors = new int[] { Color.parseColor("#87294E")};
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        
    String s = "Blood Sugar in mg/dL";
        
    renderer.setOrientation(Orientation.HORIZONTAL);
    
    setChartSettings(renderer, "Blood Sugar Progress Analysis Report", "", s, 0.5,
        datesSugared.length+0.5, lowSugar - 10, highSugar + 10, Color.BLACK, Color.BLACK);
    
    for (int k=0;k <datesSugared.length;k++){
    	renderer.addXTextLabel(k+1, datesSugared[k]);
    }
    renderer.setXLabels(0);
    
    int length = renderer.getSeriesRendererCount();
    for (int j = 0; j < length; j++) {
      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(j);
      seriesRenderer.setDisplayChartValues(true);
    }
    final GraphicalView grfv = ChartFactory.getBarChartView(AnalysisActivity.this, 
    		buildBarDataset(titles, values), renderer,Type.DEFAULT);
     
    return grfv;
  }

	
private void createBloodPressureTimeOfDayAnalysis(String val){
	final DBAdapter db = new DBAdapter(this);
	// Create an array of xy-values to plot:
	db.open();
    int personid = db.getCurrentPersonId();
    Cursor c;
    
    if (val != "All records"){
    	final Calendar cal = Calendar.getInstance();
    	Date dNewDate = cal.getTime();
    	long currentDate = dNewDate.getTime();
    	
    	Calendar cal2 = Calendar.getInstance();
		
    	// 12 months
    	if (val.equalsIgnoreCase("12 months")){
    		cal2.add(Calendar.DATE, -365);
    	}
    	
    	// 6 months
    	if (val.equalsIgnoreCase("6 months")){
    		cal2.add(Calendar.DATE, -183);
    	}
    	
    	// 90 days
    	if (val.equalsIgnoreCase("90 days")){
    		cal2.add(Calendar.DATE, -90);
    	}
    	
    	// 60 days
    	if (val.equalsIgnoreCase("60 days")){
    		cal2.add(Calendar.DATE, -60);
    	}
    	
    	// 30 days
    	if (val.equalsIgnoreCase("30 days")){
    		cal2.add(Calendar.DATE, -30);
    	}
    	
    	// 14 days
    	if (val.equalsIgnoreCase("14 days")){
    		cal2.add(Calendar.DATE, -14);
    	}
    	
    	// 7 days
    	if (val.equalsIgnoreCase("7 days")){
    		cal2.add(Calendar.DATE, -7);
    	}
    	
    	Date dNextDate = cal2.getTime();
		long dateNeeded = dNextDate.getTime(); 
		c = db.getBPPulseRecordsByPersonIdAscending(personid,dateNeeded);
    } else {
    	c = db.getBPPulseRecordsByPersonIdAscending(personid,-1);
    }


    double[] systolics1 = new double[c.getCount()]; // 0 to 400 - midnight to 4 am
    double[] systolics2 = new double[c.getCount()]; // 401 to 800 - 4:01 am to 8 am
    double[] systolics3 = new double[c.getCount()]; // 801 to 1200 - 8:01 am to noon
    double[] systolics4 = new double[c.getCount()]; // 1201 to 1600- 12:01 pm to 4 pm
    double[] systolics5 = new double[c.getCount()]; // 1601 to 2000 - 4:01 pm to 8 pm
    double[] systolics6 = new double[c.getCount()]; // 2001 to 2359 - 8:01 pm to 11:59 pm
    
    double[] diastolics1 = new double[c.getCount()]; // 0 to 400 - midnight to 4 am
    double[] diastolics2 = new double[c.getCount()]; // 401 to 800 - 4:01 am to 8 am
    double[] diastolics3 = new double[c.getCount()]; // 801 to 1200 - 8:01 am to noon
    double[] diastolics4 = new double[c.getCount()]; // 1201 to 1600- 12:01 pm to 4 pm
    double[] diastolics5 = new double[c.getCount()]; // 1601 to 2000 - 4:01 pm to 8 pm
    double[] diastolics6 = new double[c.getCount()]; // 2001 to 2359 - 8:01 pm to 11:59 pm
    
    double[] pulses1 = new double[c.getCount()]; // 0 to 400 - midnight to 4 am
    double[] pulses2 = new double[c.getCount()]; // 401 to 800 - 4:01 am to 8 am
    double[] pulses3 = new double[c.getCount()]; // 801 to 1200 - 8:01 am to noon
    double[] pulses4 = new double[c.getCount()]; // 1201 to 1600- 12:01 pm to 4 pm
    double[] pulses5 = new double[c.getCount()]; // 1601 to 2000 - 4:01 pm to 8 pm
    double[] pulses6 = new double[c.getCount()]; // 2001 to 2359 - 8:01 pm to 11:59 pm
    
    double[] systolicMeans = new double[6];
    double[] diastolicMeans = new double[6];
    double[] pulseMeans = new double[6];
    
    
    List<double[]> values = new ArrayList<double[]>();
    int l = 0;   
    if (c.moveToFirst())
    {
    	do {
    		final String sSystolic = c.getString(1);
        	final String sDiastolic = c.getString(2);
        	final String sPulse = c.getString(3);
        	final String sRecDate = c.getString(5);
        	
        	long lD = Long.parseLong(sRecDate);
        	DateFormat df = DateFormat.getDateInstance();
        	Date dDate = new Date(lD);
        	int dHour = new Date(lD).getHours();
        	int dMinutes = new Date(lD).getMinutes();
        	
        	Double fSystolic = Double.parseDouble(sSystolic);
        	Double fDiastolic = Double.parseDouble(sDiastolic);
        	Double fPulse = Double.parseDouble(sPulse);
        	
        	switch (dHour){
        	case 0:
        	case 1:
        	case 2:
        	case 3:
        		systolics1[l] = fSystolic;
        		diastolics1[l] = fDiastolic;
        		pulses1[l] = fPulse;
        		break;
        	
        	case 4:
        		if (dMinutes == 0){
        			systolics1[l] = fSystolic;
            		diastolics1[l] = fDiastolic;
            		pulses1[l] = fPulse;
        		} else {
        			systolics2[l] = fSystolic;
            		diastolics2[l] = fDiastolic;
            		pulses2[l] = fPulse;
        		}
        		break;
        	case 5:
        	case 6:
        	case 7:
        		systolics2[l] = fSystolic;
        		diastolics2[l] = fDiastolic;
        		pulses2[l] = fPulse;
        		break;
        	case 8:
        		if (dMinutes == 0){
        			systolics2[l] = fSystolic;
            		diastolics2[l] = fDiastolic;
            		pulses2[l] = fPulse;
        		} else {
        			systolics3[l] = fSystolic;
            		diastolics3[l] = fDiastolic;
            		pulses3[l] = fPulse;
        		}
        		break;
        	case 9:
        	case 10:
        	case 11:
        		systolics3[l] = fSystolic;
        		diastolics3[l] = fDiastolic;
        		pulses3[l] = fPulse;
        		break;
        	case 12:
        		if (dMinutes == 0){
        			systolics3[l] = fSystolic;
            		diastolics3[l] = fDiastolic;
            		pulses3[l] = fPulse;
        		} else {
        			systolics4[l] = fSystolic;
            		diastolics4[l] = fDiastolic;
            		pulses4[l] = fPulse;
        		}
        		break;
        	case 13:
        	case 14:
        	case 15:
        		systolics4[l] = fSystolic;
        		diastolics4[l] = fDiastolic;
        		pulses4[l] = fPulse;
        		break;
        	case 16:
        		if (dMinutes == 0){
        			systolics4[l] = fSystolic;
            		diastolics4[l] = fDiastolic;
            		pulses4[l] = fPulse;
        		} else {
        			systolics5[l] = fSystolic;
            		diastolics5[l] = fDiastolic;
            		pulses5[l] = fPulse;
        		}
        		break;
        	case 17:
        	case 18:
        	case 19:
        		systolics5[l] = fSystolic;
        		diastolics5[l] = fDiastolic;
        		pulses5[l] = fPulse;
        		break;
        	case 20:
        		if (dMinutes == 0){
        			systolics5[l] = fSystolic;
            		diastolics5[l] = fDiastolic;
            		pulses5[l] = fPulse;
        		} else {
        			systolics6[l] = fSystolic;
            		diastolics6[l] = fDiastolic;
            		pulses6[l] = fPulse;
        		}
        		break;
        	case 21:
        	case 22:
        	case 23:
        		systolics6[l] = fSystolic;
        		diastolics6[l] = fDiastolic;
        		pulses6[l] = fPulse;
        		break;
        }
   		l++;
    	}while (c.moveToNext());
    	
    }
    
    int j1 = 0;
    int j2 = 0;
    int j3 = 0;
    int j4 = 0;
    int j5 = 0;
    int j6 = 0;
    
    double k1 = 0;
    double k2 = 0;
    double k3 = 0;
    double k4 = 0;
    double k5 = 0;
    double k6 = 0;	    
    
    
    for (int i=0;i<c.getCount();i++){
    	if (systolics1[i] > 0){
    		j1++;
    		k1=k1+systolics1[i];
    	}
    	if (systolics2[i] > 0){
    		j2++;
    		k2=k2+systolics2[i];
    	}
    	if (systolics3[i] > 0){
    		j3++;
    		k3=k3+systolics3[i];
    	}
    	if (systolics4[i] > 0){
    		j4++;
    		k4=k4+systolics4[i];
    	}
    	if (systolics5[i] > 0){
    		j5++;
    		k5=k5+systolics5[i];
    	}
    	if (systolics6[i] > 0){
    		j6++;
    		k6=k6+systolics6[i];
    	}
   }
    
       double smean1 = 0;
	   double smean2 = 0;
	   double smean3 = 0;
	   double smean4 = 0;
	   double smean5 = 0;
	   double smean6 = 0;
	   
	   if (j1 != 0 && k1 != 0){
		   smean1 = Round(k1/j1,2);
	   }
	   if (j2 != 0 && k2 != 0){
		   smean2 = Round(k2/j2,2);
	   }
	   if (j3 != 0 && k3 != 0){
		   smean3 = Round(k3/j3,2);
	   }
	   if (j4 != 0 && k4 != 0){
		   smean4 = Round(k4/j4,2);
	   }
	   if (j5 != 0 && k5 != 0){
		   smean5 = Round(k5/j5,2);
	   }
	   if (j6 != 0 && k6 != 0){
		   smean1 = Round(k6/j6,2);
	   }
	
	   j1 = 0;
	   j2 = 0;
	   j3 = 0;
	   j4 = 0;
	   j5 = 0;
	   j6 = 0;
	    
	   k1 = 0;
	   k2 = 0;
	   k3 = 0;
	   k4 = 0;
	   k5 = 0;
	   k6 = 0;	    
	    
	    
	    for (int i=0;i<c.getCount();i++){
	    	if (diastolics1[i] > 0){
	    		j1++;
	    		k1=k1+diastolics1[i];
	    	}
	    	if (diastolics2[i] > 0){
	    		j2++;
	    		k2=k2+diastolics2[i];
	    	}
	    	if (diastolics3[i] > 0){
	    		j3++;
	    		k3=k3+diastolics3[i];
	    	}
	    	if (diastolics4[i] > 0){
	    		j4++;
	    		k4=k4+diastolics4[i];
	    	}
	    	if (diastolics5[i] > 0){
	    		j5++;
	    		k5=k5+diastolics5[i];
	    	}
	    	if (diastolics6[i] > 0){
	    		j6++;
	    		k6=k6+diastolics6[i];
	    	}
	   }
	    
	       double dmean1 = 0;
		   double dmean2 = 0;
		   double dmean3 = 0;
		   double dmean4 = 0;
		   double dmean5 = 0;
		   double dmean6 = 0;
		   
		   if (j1 != 0 && k1 != 0){
			   dmean1 = Round(k1/j1,2);
		   }
		   if (j2 != 0 && k2 != 0){
			   dmean2 = Round(k2/j2,2);
		   }
		   if (j3 != 0 && k3 != 0){
			   dmean3 = Round(k3/j3,2);
		   }
		   if (j4 != 0 && k4 != 0){
			   dmean4 = Round(k4/j4,2);
		   }
		   if (j5 != 0 && k5 != 0){
			   dmean5 = Round(k5/j5,2);
		   }
		   if (j6 != 0 && k6 != 0){
			   dmean1 = Round(k6/j6,2);
		   }
		

		   j1 = 0;
		   j2 = 0;
		   j3 = 0;
		   j4 = 0;
		   j5 = 0;
		   j6 = 0;
		    
		   k1 = 0;
		   k2 = 0;
		   k3 = 0;
		   k4 = 0;
		   k5 = 0;
		   k6 = 0;	    
		    
		    
		   for (int i=0;i<c.getCount();i++){
		    	if (pulses1[i] > 0){
		    		j1++;
		    		k1=k1+pulses1[i];
		    	}
		    	if (pulses2[i] > 0){
		    		j2++;
		    		k2=k2+pulses2[i];
		    	}
		    	if (pulses3[i] > 0){
		    		j3++;
		    		k3=k3+pulses3[i];
		    	}
		    	if (pulses4[i] > 0){
		    		j4++;
		    		k4=k4+pulses4[i];
		    	}
		    	if (pulses5[i] > 0){
		    		j5++;
		    		k5=k5+pulses5[i];
		    	}
		    	if (pulses6[i] > 0){
		    		j6++;
		    		k6=k6+pulses6[i];
		    	}
		   }
		    
		       double pmean1 = 0;
			   double pmean2 = 0;
			   double pmean3 = 0;
			   double pmean4 = 0;
			   double pmean5 = 0;
			   double pmean6 = 0;
			   
			   if (j1 != 0 && k1 != 0){
				   pmean1 = Round(k1/j1,2);
			   }
			   if (j2 != 0 && k2 != 0){
				   pmean2 = Round(k2/j2,2);
			   }
			   if (j3 != 0 && k3 != 0){
				   pmean3 = Round(k3/j3,2);
			   }
			   if (j4 != 0 && k4 != 0){
				   pmean4 = Round(k4/j4,2);
			   }
			   if (j5 != 0 && k5 != 0){
				   pmean5 = Round(k5/j5,2);
			   }
			   if (j6 != 0 && k6 != 0){
				   pmean1 = Round(k6/j6,2);
			   }
			
		   
    
    int screenWidth = getResources().getDisplayMetrics().widthPixels;
	   
    LinearLayout listReports = (LinearLayout) findViewById(R.id.LayoutReports);
    int listReportsWidth = listReports.getWidth();
    int minWidth = screenWidth - listReportsWidth;
    int minHeight = listReports.getHeight();
   
    RelativeLayout reportArea = (RelativeLayout) findViewById(R.id.graph);
    reportArea.removeAllViewsInLayout();
    reportArea.setMinimumWidth(minWidth);
    reportArea.setBackgroundColor(Color.WHITE);
   
    TableLayout table = new TableLayout(this);
    TableRow tableRow = new TableRow(this);
   
    TextView title = new TextView(this);
    title.setText("Blood Pressure Time of Day Analysis", BufferType.NORMAL);
    title.setWidth(listReportsWidth);
    title.setTextSize(12);
    title.setTextColor(Color.BLACK);
    title.setGravity(Gravity.CENTER);
    TableRow.LayoutParams tp1 = new TableRow.LayoutParams();
    tp1.span = 4;
    tableRow.addView(title,tp1);
    table.addView(tableRow);
   
    tableRow = new TableRow(this);
   
    TextView txtAverages = new TextView(this);
    txtAverages.setBackgroundColor(Color.parseColor("#880000"));
    txtAverages.setTextColor(Color.WHITE);
    txtAverages.setTextSize(12);
    txtAverages.setText("Time of Day");
    txtAverages.setWidth(minWidth/2);
    txtAverages.setHeight(48);
    tableRow.addView(txtAverages);
   
    txtAverages = new TextView(this);
    txtAverages.setBackgroundColor(Color.parseColor("#880000"));
    txtAverages.setTextColor(Color.WHITE);
    txtAverages.setTextSize(12);
    txtAverages.setText("Systolic Average");
    txtAverages.setWidth(minWidth/6);
    txtAverages.setHeight(48);
    tableRow.addView(txtAverages);
   
    txtAverages = new TextView(this);
    txtAverages.setBackgroundColor(Color.parseColor("#880000"));
    txtAverages.setTextColor(Color.WHITE);
    txtAverages.setTextSize(12);
    txtAverages.setText("Diastolic Average");
    txtAverages.setWidth(minWidth/6);
    txtAverages.setHeight(48);
    tableRow.addView(txtAverages);
   
    txtAverages = new TextView(this);
    txtAverages.setBackgroundColor(Color.parseColor("#880000"));
    txtAverages.setTextColor(Color.WHITE);
    txtAverages.setTextSize(12);
    txtAverages.setText("Pulse Average");
    txtAverages.setWidth(minWidth/6);
    txtAverages.setHeight(48);
    tableRow.addView(txtAverages);
   	   
    table.addView(tableRow);
    
    tableRow = new TableRow(this);
   TextView txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/2);
   txtTime.setText("Midnight until 4:00 AM");
   
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(smean1));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(dmean1));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(pmean1));
   tableRow.addView(txtTime);
   
   tableRow.setBackgroundResource(R.drawable.rowborder);
   table.addView(tableRow);
   
   tableRow = new TableRow(this);
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/2);
   txtTime.setText("4:01 AM until 8:00 AM");
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(smean2));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(dmean2));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(pmean2));
   tableRow.addView(txtTime);
   tableRow.setBackgroundResource(R.drawable.rowborder2);
   table.addView(tableRow);
   
   
   tableRow = new TableRow(this);
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/2);
   txtTime.setText("8:01 AM until Noon");
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(smean3));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(dmean3));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(pmean3));
   tableRow.addView(txtTime);
   
   tableRow.setBackgroundResource(R.drawable.rowborder);
   table.addView(tableRow);
   
   tableRow = new TableRow(this);
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/2);
   txtTime.setText("12:01 PM until 4:00 PM");
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(smean4));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(dmean4));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(pmean4));
   tableRow.addView(txtTime);
   
   tableRow.setBackgroundResource(R.drawable.rowborder2);
   table.addView(tableRow);
   
   tableRow = new TableRow(this);
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/2);
   txtTime.setText("4:01 PM until 8:00 PM");
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(smean5));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(dmean5));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(pmean5));
   tableRow.addView(txtTime);
   
   tableRow.setBackgroundResource(R.drawable.rowborder);
   table.addView(tableRow);
   
   tableRow = new TableRow(this);
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/2);
   txtTime.setText("8:01 PM until 11:59 PM");
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(smean6));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(dmean6));
   tableRow.addView(txtTime);
   
   txtTime = new TextView(this);
   txtTime.setTextColor(Color.BLACK);
   txtTime.setTextSize(14);
   txtTime.setWidth(minWidth/6);
   txtTime.setText(Double.toString(pmean6));
   tableRow.addView(txtTime);
   
   tableRow.setBackgroundResource(R.drawable.rowborder2);
   table.addView(tableRow);
   
   tableRow = new TableRow(this);
   final Spinner timespan = new Spinner(this);
   
   ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.timespan_array,android.R.layout.simple_spinner_item);
   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
   timespan.setAdapter(adapter);
   timespan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	   int count = 0;
	   @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                          int arg2, long arg3) {
		   			if (count >= 1){
		   			pos = timespan.getSelectedItemPosition();
		   			String val = timespan.getSelectedItem().toString();
		   			createBloodPressureTimeOfDayAnalysis(val);
		   			}
		   			count++;
		   			return;
		   	}
       @Override
             public void onNothingSelected(AdapterView<?> arg0) {
    	   		return;
             }
   });
   timespan.setSelection(pos);
   tableRow.addView(timespan);
   table.addView(tableRow); 
   
    RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(minWidth,minHeight);
    lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    reportArea.addView(table,lp1);
	   
}


private void createBloodSugarTimeOfDayAnalysis(String val){
		
		final DBAdapter db = new DBAdapter(this);
		// Create an array of xy-values to plot:
		db.open();
	    int personid = db.getCurrentPersonId();
	    Cursor c = null;
	    if (val != "All records"){
	    	final Calendar cal = Calendar.getInstance();
	    	Date dNewDate = cal.getTime();
	    	long currentDate = dNewDate.getTime();
	    	
	    	Calendar cal2 = Calendar.getInstance();
    		
	    	// 12 months
	    	if (val.equalsIgnoreCase("12 months")){
	    		cal2.add(Calendar.DATE, -365);
	    	}
	    	
	    	// 6 months
	    	if (val.equalsIgnoreCase("6 months")){
	    		cal2.add(Calendar.DATE, -183);
	    	}
	    	
	    	// 90 days
	    	if (val.equalsIgnoreCase("90 days")){
	    		cal2.add(Calendar.DATE, -90);
	    	}
	    	
	    	// 60 days
	    	if (val.equalsIgnoreCase("60 days")){
	    		cal2.add(Calendar.DATE, -60);
	    	}
	    	
	    	// 30 days
	    	if (val.equalsIgnoreCase("30 days")){
	    		cal2.add(Calendar.DATE, -30);
	    	}
	    	
	    	// 14 days
	    	if (val.equalsIgnoreCase("14 days")){
	    		cal2.add(Calendar.DATE, -14);
	    	}
	    	
	    	// 7 days
	    	if (val.equalsIgnoreCase("7 days")){
	    		cal2.add(Calendar.DATE, -7);
	    	}
	    	
	    	Date dNextDate = cal2.getTime();
    		long dateNeeded = dNextDate.getTime(); 
    		c = db.getSugarRecordsByPersonIdAscending(personid,dateNeeded);
	    } else {
	    	c = db.getSugarRecordsByPersonIdAscending(personid,-1);
	    }
	
	    double[] sugarNumbers1 = new double[c.getCount()]; // 0 to 400 - midnight to 4 am
	    double[] sugarNumbers2 = new double[c.getCount()]; // 401 to 800 - 4:01 am to 8 am
	    double[] sugarNumbers3 = new double[c.getCount()]; // 801 to 1200 - 8:01 am to noon
	    double[] sugarNumbers4 = new double[c.getCount()]; // 1201 to 1600- 12:01 pm to 4 pm
	    double[] sugarNumbers5 = new double[c.getCount()]; // 1601 to 2000 - 4:01 pm to 8 pm
	    double[] sugarNumbers6 = new double[c.getCount()]; // 2001 to 2359 - 8:01 pm to 11:59 pm
	    
	    double[] sugarMeans = new double[6];
	    
	    double lowSugar = (double) 0;
	    double highSugar = (double) 0;
	    List<double[]> values = new ArrayList<double[]>();
	    int l = 0;   
	    if (c.moveToFirst())
	    {
	        do {
	        	final String sSugar = c.getString(1);
	        	final String sRecDate = c.getString(7);
	        	
	        	long lD = Long.parseLong(sRecDate);
	        	DateFormat df = DateFormat.getDateInstance();
	        	int dHour = new Date(lD).getHours();
	        	int dMinutes = new Date(lD).getMinutes();
	        
	        	Double fSugar = Double.parseDouble(sSugar);
	        	if (lowSugar == 0 || fSugar < lowSugar){
	        		lowSugar = fSugar;
	        	}
	        	if (highSugar == 0 || fSugar > highSugar){
	        		highSugar = fSugar;
	        	}
	        	
	        	switch (dHour){
	        	case 0:
	        	case 1:
	        	case 2:
	        	case 3:
	        		sugarNumbers1[l] = fSugar;
	        		break;
	        	
	        	case 4:
	        		if (dMinutes == 0){
	        			sugarNumbers1[l] = fSugar;
	        		} else {
	        			sugarNumbers2[l] = fSugar;
	        		}
	        		break;
	        	case 5:
	        	case 6:
	        	case 7:
	        		sugarNumbers2[l] = fSugar;
	        		break;
	        	case 8:
	        		if (dMinutes == 0){
	        			sugarNumbers2[l] = fSugar;
	        		} else {
	        			sugarNumbers3[l] = fSugar;
	        		}
	        		break;
	        	case 9:
	        	case 10:
	        	case 11:
	        		sugarNumbers3[l] = fSugar;
	        		break;
	        	case 12:
	        		if (dMinutes == 0){
	        			sugarNumbers3[l] = fSugar;
	        		} else {
	        			sugarNumbers4[l] = fSugar;
	        		}
	        		break;
	        	case 13:
	        	case 14:
	        	case 15:
	        		sugarNumbers4[l] = fSugar;
	        		break;
	        	case 16:
	        		if (dMinutes == 0){
	        			sugarNumbers4[l] = fSugar;
	        		} else {
	        			sugarNumbers5[l] = fSugar;
	        		}
	        		break;
	        	case 17:
	        	case 18:
	        	case 19:
	        		sugarNumbers5[l] = fSugar;
	        		break;
	        	case 20:
	        		if (dMinutes == 0){
	        			sugarNumbers5[l] = fSugar;
	        		} else {
	        			sugarNumbers6[l] = fSugar;
	        		}
	        		break;
	        	case 21:
	        	case 22:
	        	case 23:
	        		sugarNumbers6[l] = fSugar;
	        		break;
	        }
	        	l++;
	    	}while (c.moveToNext());
	    }
	    int j1 = 0;
	    int j2 = 0;
	    int j3 = 0;
	    int j4 = 0;
	    int j5 = 0;
	    int j6 = 0;
	    
	    double k1 = 0;
	    double k2 = 0;
	    double k3 = 0;
	    double k4 = 0;
	    double k5 = 0;
	    double k6 = 0;	    
	    
	    
	    for (int i=0;i<c.getCount();i++){
	    	if (sugarNumbers1[i] > 0){
	    		j1++;
	    		k1=k1+sugarNumbers1[i];
	    	}
	    	if (sugarNumbers2[i] > 0){
	    		j2++;
	    		k2=k2+sugarNumbers2[i];
	    	}
	    	if (sugarNumbers3[i] > 0){
	    		j3++;
	    		k3=k3+sugarNumbers3[i];
	    	}
	    	if (sugarNumbers4[i] > 0){
	    		j4++;
	    		k4=k4+sugarNumbers4[i];
	    	}
	    	if (sugarNumbers5[i] > 0){
	    		j5++;
	    		k5=k5+sugarNumbers5[i];
	    	}
	    	if (sugarNumbers6[i] > 0){
	    		j6++;
	    		k6=k6+sugarNumbers6[i];
	    	}
	   }
	    
	   double mean1 = 0;
	   double mean2 = 0;
	   double mean3 = 0;
	   double mean4 = 0;
	   double mean5 = 0;
	   double mean6 = 0;
	   
	   if (j1 != 0 && k1 != 0){
		   mean1 = Round(k1/j1,2);
	   }
	   if (j2 != 0 && k2 != 0){
		   mean2 = Round(k2/j2,2);
	   }
	   if (j3 != 0 && k3 != 0){
		   mean3 = Round(k3/j3,2);
	   }
	   if (j4 != 0 && k4 != 0){
		   mean4 = Round(k4/j4,2);
	   }
	   if (j5 != 0 && k5 != 0){
		   mean5 = Round(k5/j5,2);
	   }
	   if (j6 != 0 && k6 != 0){
		   mean1 = Round(k6/j6,2);
	   }
	
	   int screenWidth = getResources().getDisplayMetrics().widthPixels;
	   	   
	   LinearLayout listReports = (LinearLayout) findViewById(R.id.LayoutReports);
	   int listReportsWidth = listReports.getWidth();
	   int minWidth = screenWidth - listReportsWidth;
	   int minHeight = listReports.getHeight();
	   
	   RelativeLayout reportArea = (RelativeLayout) findViewById(R.id.graph);
	   reportArea.removeAllViewsInLayout();
	   reportArea.setMinimumWidth(minWidth);
	   reportArea.setBackgroundColor(Color.WHITE);
	   
	   TableLayout table = new TableLayout(this);
	   TableRow tableRow = new TableRow(this);
	   
	   TextView title = new TextView(this);
	   title.setText("Blood Sugar Time of Day Analysis", BufferType.NORMAL);
	   title.setWidth(listReportsWidth);
	   title.setTextSize(12);
	   title.setTextColor(Color.BLACK);
	   title.setGravity(Gravity.CENTER);
	   TableRow.LayoutParams tp1 = new TableRow.LayoutParams();
	   tp1.span = 4;
	   tableRow.addView(title,tp1);
	   table.addView(tableRow);
	   	   
	   tableRow = new TableRow(this);
	   
	   TextView txtAverages = new TextView(this);
	   txtAverages.setBackgroundColor(Color.parseColor("#880000"));
	   txtAverages.setTextColor(Color.WHITE);
	   txtAverages.setTextSize(14);
	   txtAverages.setWidth(2*minWidth/3);
	   txtAverages.setText("Time of Day");
	   tableRow.addView(txtAverages);
	   
	   txtAverages = new TextView(this);
	   txtAverages.setBackgroundColor(Color.parseColor("#880000"));
	   txtAverages.setTextColor(Color.WHITE);
	   txtAverages.setTextSize(14);
	   txtAverages.setWidth(minWidth/3);
	   txtAverages.setText("Mean Average");
	   tableRow.addView(txtAverages);
	   	   
	   table.addView(tableRow);

	   tableRow = new TableRow(this);
	   TextView txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(2*minWidth/3);
	   txtTime.setText("Midnight until 4:00 AM");
	   
	   tableRow.addView(txtTime);
	   
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(minWidth/3);
	   txtTime.setText(Double.toString(mean1));
	   tableRow.addView(txtTime);
	   
	   tableRow.setBackgroundResource(R.drawable.rowborder);
	   table.addView(tableRow);
	   
	   tableRow = new TableRow(this);
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(2*minWidth/3);
	   txtTime.setText("4:01 AM until 8:00 AM");
	   tableRow.addView(txtTime);
	   
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(minWidth/3);
	   txtTime.setText(Double.toString(mean2));
	   tableRow.addView(txtTime);
	   
	   tableRow.setBackgroundResource(R.drawable.rowborder2);
	   table.addView(tableRow);
	   
	   
	   tableRow = new TableRow(this);
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(2*minWidth/3);
	   txtTime.setText("8:01 AM until Noon");
	   tableRow.addView(txtTime);
	   
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(minWidth/3);
	   txtTime.setText(Double.toString(mean3));
	   tableRow.addView(txtTime);
	   
	   tableRow.setBackgroundResource(R.drawable.rowborder);
	   table.addView(tableRow);
	   
	   tableRow = new TableRow(this);
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(2*minWidth/3);
	   txtTime.setText("12:01 PM until 4:00 PM");
	   tableRow.addView(txtTime);
	   
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(minWidth/3);
	   txtTime.setText(Double.toString(mean4));
	   tableRow.addView(txtTime);
	   
	   tableRow.setBackgroundResource(R.drawable.rowborder2);
	   table.addView(tableRow);
	   
	   tableRow = new TableRow(this);
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(2*minWidth/3);
	   txtTime.setText("4:01 PM until 8:00 PM");
	   tableRow.addView(txtTime);
	   
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(minWidth/3);
	   txtTime.setText(Double.toString(mean5));
	   tableRow.addView(txtTime);
	   
	   tableRow.setBackgroundResource(R.drawable.rowborder);
	   table.addView(tableRow);
	   
	   tableRow = new TableRow(this);
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(2*minWidth/3);
	   txtTime.setText("8:01 PM until 11:59 PM");
	   tableRow.addView(txtTime);
	   
	   txtTime = new TextView(this);
	   txtTime.setTextColor(Color.BLACK);
	   txtTime.setTextSize(14);
	   txtTime.setWidth(minWidth/3);
	   txtTime.setText(Double.toString(mean6));
	   tableRow.addView(txtTime);
	   
	   tableRow.setBackgroundResource(R.drawable.rowborder2);
	   table.addView(tableRow);
	   
	   tableRow = new TableRow(this);
	   
	   final Spinner timespan = new Spinner(this);
	   
	   ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.timespan_array,android.R.layout.simple_spinner_item);
	   adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	   timespan.setAdapter(adapter);
	   timespan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		   int count = 0;
		   @Override
	            public void onItemSelected(AdapterView<?> arg0, View arg1,
	                          int arg2, long arg3) {
			   			if (count >= 1){
			   			pos = timespan.getSelectedItemPosition();
			   			String val = timespan.getSelectedItem().toString();
			   			createBloodSugarTimeOfDayAnalysis(val);
			   			}
			   			count++;
			   			return;
			   	}
	       @Override
	             public void onNothingSelected(AdapterView<?> arg0) {
	    	   		return;
	             }
	   });
	   timespan.setSelection(pos);
	   tableRow.addView(timespan);
	   table.addView(tableRow);
	   
	   RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(minWidth,minHeight);
	   lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	   reportArea.addView(table,lp1);
	   
	}

public GraphicalView createTemperatureProgress() {
	
	final DBAdapter db = new DBAdapter(this);
	// Create an array of xy-values to plot:
	db.open();
    int personid = db.getCurrentPersonId();
    Cursor c;
    c = db.getTemperatureRecordsByPersonIdAscending(personid);
    int i = 0;
    double[] temperatureNumbers = new double[c.getCount()];
    String[] datesMeasured = new String[c.getCount()];
    double lowTemperature = (double) 0;
    double highTemperature = (double) 0;
    List<double[]> values = new ArrayList<double[]>();
        
    if (c.moveToFirst())
    {
        do {
        	final String sTemperature = c.getString(1);
        	final String sRecDate = c.getString(3);
        	
        	long lD = Long.parseLong(sRecDate);
        	DateFormat df = DateFormat.getDateInstance();
        	Date dDate = new Date(lD);
        
        	Double fTemperature = Double.parseDouble(sTemperature);
        	if (lowTemperature == 0 || fTemperature < lowTemperature){
        		lowTemperature = fTemperature;
        	}
        	if (highTemperature == 0 || fTemperature > highTemperature){
        		highTemperature = fTemperature;
        	}
        	
        	temperatureNumbers[i] = fTemperature;
    		datesMeasured[i] = df.format(dDate);
    		i++;
    	}while (c.moveToNext());
    }
	
    values.add(temperatureNumbers);
    
    String[] titles = new String[] { "Temperatures"};
    
    int[] colors = new int[] { Color.parseColor("#87294E")};
    XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
        
    String s = "";
    
    if (iUnitMeasure == 0){
    	s = "Temperature in °F";
    } else {
    	s = "Temperature in °C";
    }
    
    renderer.setOrientation(Orientation.HORIZONTAL);
    
    setChartSettings(renderer, "Temperature Progress Analysis Report", "", s, 0.5,
        datesMeasured.length+0.5, lowTemperature - 10, highTemperature + 10, Color.BLACK, Color.BLACK);
    
    for (int k=0;k <datesMeasured.length;k++){
    	renderer.addXTextLabel(k+1, datesMeasured[k]);
    }
    renderer.setXLabels(0);
    
    int length = renderer.getSeriesRendererCount();
    for (int j = 0; j < length; j++) {
      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(j);
      seriesRenderer.setDisplayChartValues(true);
    }

    final GraphicalView grfv = ChartFactory.getBarChartView(AnalysisActivity.this, 
    		buildBarDataset(titles, values), renderer,Type.DEFAULT);
     
    return grfv;
  }



  protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(16);
        renderer.setChartTitleTextSize(20);
        renderer.setLabelsTextSize(10);
        renderer.setLegendTextSize(15);
        renderer.setBarSpacing(1);
         
        renderer.setXLabelsAngle(90f);
        renderer.setMarginsColor(Color.parseColor("#FBFBFC"));
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
         
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.parseColor("#FBFBFC"));
        renderer.setZoomButtonsVisible(true);
        renderer.setZoomEnabled(true);
         
        int length = colors.length;
        for (int i = 0; i < length; i++) {
          SimpleSeriesRenderer r = new SimpleSeriesRenderer();
          r.setColor(colors[i]);
          r.setChartValuesSpacing(15);
          renderer.addSeriesRenderer(r);
        }
        return renderer;
      }

  protected XYMultipleSeriesRenderer buildLineRenderer(int[] colors, PointStyle[] styles) {
      XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
      setLineSettings(renderer, colors, styles);
      return renderer;
    }
  
  protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        int length = titles.length;
        for (int i = 0; i < length; i++) {
          CategorySeries series = new CategorySeries(titles[i]);
          double[] v = values.get(i);
          int seriesLength = v.length;
          for (int k = 0; k < seriesLength; k++) {
            series.add(v[k]);
          }
          dataset.addSeries(series.toXYSeries());
        }
        return dataset;
      }
  
  protected XYMultipleSeriesDataset buildLineDataset(String[] titles, List<double[]> xValues,
          List<double[]> yValues) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        addXYSeries(dataset, titles, xValues, yValues, 0);
        return dataset;
  }
  
  public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
          List<double[]> yValues, int scale) {
        int length = titles.length;
        for (int i = 0; i < length; i++) {
          XYSeries series = new XYSeries(titles[i], scale);
          double[] xV = xValues.get(i);
          double[] yV = yValues.get(i);
          int seriesLength = xV.length;
          for (int k = 0; k < seriesLength; k++) {
            series.add(xV[k], yV[k]);
          }
          dataset.addSeries(series);
        }
   }
  
  protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
          String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
          int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setMargins(new int[] { 50,50, 20, 10 });
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
      }

  protected void setLineSettings(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
	  renderer.setAxisTitleTextSize(16);
      renderer.setChartTitleTextSize(20);
      renderer.setLabelsTextSize(10);
      renderer.setLegendTextSize(15);
      renderer.setBarSpacing(1);
       
      renderer.setXLabelsAngle(90f);
      renderer.setMarginsColor(Color.parseColor("#FBFBFC"));
      renderer.setXLabelsColor(Color.BLACK);
      renderer.setYLabelsColor(0,Color.BLACK);
       
      renderer.setApplyBackgroundColor(true);
      renderer.setBackgroundColor(Color.parseColor("#FBFBFC"));
      renderer.setZoomButtonsVisible(true);
      renderer.setZoomEnabled(true);
       
      int length = colors.length;
      for (int i = 0; i < length; i++) {
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(colors[i]);
        r.setPointStyle(styles[i]);
        renderer.addSeriesRenderer(r);
      }
    }
  
  private String ComputeBMI(String sHeight, String sWeight){
  	double dBMI = 0;
  	if (iUnitMeasure == 0){
  		dBMI = (Float.parseFloat(sWeight) * 703)/(Float.parseFloat(sHeight)*Float.parseFloat(sHeight));
  	} else {
  		dBMI = (Float.parseFloat(sWeight)/(Float.parseFloat(sHeight)*Float.parseFloat(sHeight))) * 10000;
  	}
  	
  	NumberFormat nf = NumberFormat.getInstance();
  	nf.setMaximumFractionDigits(2);
  	nf.setGroupingUsed(false);
  	String formattedvalue = nf.format(dBMI);
  	return formattedvalue;
  } 
  
  private void changeTitle(){
  	String[] menuitems = getResources().getStringArray(R.array.mainmenu_array);
  	String menutitle = menuitems[9];
  	
  	DBAdapter db = new DBAdapter(this);
  	db.open();
  	Cursor c = db.getCurrentPerson();
  	if (c!=null){
  		if (c.moveToFirst()){
  			String personname = "";
  			try {
  				personname = c.getString(1);
  				menutitle = menutitle + " for " + personname;
  			} catch (Exception ex){}
  			
  		}
  	}
  	db.close();
   	
	    this.setTitle(menutitle);
  }
  
  private static double Round(double Rval, int Rpl) {
	  double p = (double)Math.pow(10,Rpl);
	  Rval = Rval * p;
	  double tmp = Math.round(Rval);
	  return (double)tmp/p;
	  }
  

}

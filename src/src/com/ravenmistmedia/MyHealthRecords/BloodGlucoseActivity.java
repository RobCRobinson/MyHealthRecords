package com.ravenmistmedia.MyHealthRecords;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

public class BloodGlucoseActivity extends Activity {
	
	Common common = new Common();
	private Button dialogmSave;
	private Button dialogmDelete;
		
	static final int VISIBLE = 0;
	static final int INVISIBLE = 4;
	
	private int mYear;
    private int mMonth;
    private int mDay;
    String s = "Blue";
    int lastRecords = 20;
    int rowmax = -1;
    String rowfilter = "";
    
	protected void onResume() {
		super.onResume();
		Common.CheckLogin(this); 
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutBloodGlucose );  
        final DBAdapter db = new DBAdapter(this);
        db.open();
        s = db.getSetting("BackgroundColor");
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
		} catch (Exception e){}
        
	}  
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Common.CheckLogin(this); 
        setContentView(R.layout.bloodglucose);
	
        changeTitle();
	    	    
	    try {
		    String sLastRecords = getResources().getString(R.string.lastRecords);
		    lastRecords = Integer.parseInt(sLastRecords);
		} catch (Exception ex){}
	    rowmax = lastRecords;
	    rowfilter = "";
		DisplayRecords();
    }
    
    private void changeTitle(){
    	String[] menuitems = getResources().getStringArray(R.array.mainmenu_array);
    	String menutitle = menuitems[5];
    	
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
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.bpmenu, menu);
	    return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	final DBAdapter db = new DBAdapter(this);
    	switch (item.getItemId()) {
	        case R.id.exit:     
	            finish();
	        	break;
	        case R.id.showall:
	        	rowmax = -1;
	        	rowfilter = "";
	        	DisplayRecords();
	        	break;
	        case R.id.showten:
	        	rowmax = lastRecords;
	        	rowfilter = "";
	        	DisplayRecords();
	        	break;
	        case R.id.filter:
	        	AlertDialog.Builder alrtFilter = new AlertDialog.Builder(this);
	        	String sTitle = getResources().getString(R.string.filterTitle);
	        	String sInstruction = getResources().getString(R.string.filterInstruction);
	        	
	        	alrtFilter.setTitle(sTitle);
	        	alrtFilter.setMessage(sInstruction);

	        	final EditText input = new EditText(this);
	        	alrtFilter.setView(input);

	        	alrtFilter.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int whichButton) {
	        	  String value = input.getText().toString();
	        	  rowmax = -1;
	        	  rowfilter = value;
	        	  DisplayRecords();
	        	  }
	        	});

	        	alrtFilter.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int whichButton) {
	        	    // Canceled.
	        	  }
	        	});

	        	alrtFilter.show();
	        	break;
	        case R.id.emailRecords:
	        	db.open();
	        	String rowData = "";
	        	try {
	        		rowData = db.exportTableString(db.DATABASE_TABLE_SUGARS, "Blood Glucose Records");
	        	} catch (IOException e) {
	        		rowData = e.getMessage();
	        	}
	        	db.close();
	        	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
	      	  
	        	String msgBody = rowData;
    	    	
	        	emailIntent.setType("plain/text");  
	        	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msgBody);  
	        	  
	        	startActivity(Intent.createChooser(emailIntent, "Send your email in:"));  
	        	
	        	break;
	        case R.id.textMessageRecords:
	        	db.open();
	        	String rowData2 = "";
	        	try {
	        		rowData2 = db.exportTableString(db.DATABASE_TABLE_SUGARS, "Blood Glucose Records");
	        	} catch (IOException e) {
	        		rowData2 = e.getMessage();
	        	}
	        	db.close();
	        	Intent textIntent = new Intent(android.content.Intent.ACTION_VIEW);  
	      	  
	        	String smsBody = rowData2;
	        	textIntent.putExtra("sms_body", smsBody); 
	        	textIntent.setType("vnd.android-dir/mms-sms");	        	
	        	  
	        	startActivity(Intent.createChooser(textIntent, "Send your text in:"));  
	        	
	        	break;
	        case R.id.copyClipboard:
	        	db.open();
	        	String rowData3 = "";
	        	try {
	        		rowData3 = db.exportTableString(db.DATABASE_TABLE_SUGARS, "Blood Glucose Records");
	        	} catch (IOException e) {
	        		rowData3 = e.getMessage();
	        	}
	        	db.close();
	        	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

	        	clipboard.setText(rowData3);
	        	Toast toast = Toast.makeText(this, "Records have been copied to the clipboard", Toast.LENGTH_LONG);
				toast.show();
	        		 
	        	break;
	        	
	        case R.id.persons:
	        	CharSequence[] listOfPersons = null;
	        	ArrayList<ArrayList<String>> arrayOfPersons = new ArrayList<ArrayList<String>>();
	        	List list = new ArrayList<String>();
	        	String sAdd = getResources().getString(R.string.titlePerson);
	        	list.add("-- " + sAdd + " --");
	        	String item1 = "";
	        	db.open();
	        	Cursor c = db.getAllPersons();
	        	int selIndex = 0;
	        	int selPersonId = db.getCurrentPersonId();
	        	
	        	if (c!=null){
	        		if (c.moveToFirst()){
	        			int rownum = c.getCount();
	        			int index = rownum;
	        			
	                	do  {
	                		int personid = c.getInt(0);
	                		if (personid == selPersonId){
	                			selIndex = index - rownum + 1;
	                		}
	                		String personname = c.getString(1);
	                		int currentuser = c.getInt(2);
	                		ArrayList<String> personInfo = new ArrayList<String>();
	                		if (currentuser > 0 || rownum==1){
	                			item1 = personname;
	                		} else {
	                			item1 = personname;
	                		}
	                		list.add(item1);
	                		personInfo.add(c.getString(0));
	                		personInfo.add(personname);
	                		personInfo.add(c.getString(2));
	                		arrayOfPersons.add(personInfo);
	                		rownum--;
	                	}while (rownum >= 0 && c.moveToNext());
	        		}
	        	}
	        	listOfPersons = (CharSequence[]) list.toArray(new CharSequence[list.size()]);
	        	final CharSequence[] list2 = listOfPersons;
	        	final ArrayList<ArrayList<String>> array2 = arrayOfPersons;
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle("Persons");
	        	builder.setSingleChoiceItems(list2, selIndex, new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int item) {
	        			String displaythis = "";
	        			if (item > 0){
		        			ArrayList<String> personInfo = array2.get(item-1);
		        			String personid = personInfo.get(0);
		        			String personname = personInfo.get(1);
		        			String currentuser = personInfo.get(2);
		        			
		        			displaythis = getResources().getString(R.string.txtSwitching) + " " + personname;
		        			db.setCurrentPerson(Integer.parseInt(personid));
		        			Toast.makeText(getApplicationContext(), displaythis, Toast.LENGTH_SHORT).show();
		        			changeTitle();
		        			rowmax = lastRecords;
		        			rowfilter = "";
		        			DisplayRecords();
		        	    }else {
		        	    	final Dialog dialog1 = new Dialog(BloodGlucoseActivity.this);
		        	    	dialog1.setContentView(R.layout.personinput);
		        	    	dialog1.setTitle(getResources().getString(R.string.titlePerson));
		        	    	dialog1.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		        	    	RelativeLayout l = (RelativeLayout) dialog1.findViewById( R.id.LayoutPersonInput );
		        	    	if(s.equals("")|| s.equals("Blue")){
		        	        	l.setBackgroundResource(R.drawable.backrepeat);
		        	        } else if (s.equals("Lavender")){
		        	        	l.setBackgroundResource(R.drawable.backrepeat2);
		        	        } else if (s.equals("Peach")){
		        	        	l.setBackgroundResource(R.drawable.backrepeat3);
		        	        } else if (s.equals("Green")){
		        	        	l.setBackgroundResource(R.drawable.backrepeat4);
		        	        }
		        	    	
		        	    	final EditText dialogmPersonName = (EditText) dialog1.findViewById(R.id.edtInputPersonName);
		        	    	dialogmSave = (Button) dialog1.findViewById(R.id.btnSavePerson);
		        	    	dialogmSave.setOnClickListener(new View.OnClickListener() {
		        	            public void onClick(View v) {
		        	            	String sPersonName = dialogmPersonName.getText().toString();
		        	               
		        	                db.open();
		        	                long l = db.insertPerson(sPersonName);
		        	            	if (l != -1){
		        	            		int personid = db.FindPerson(sPersonName);
		        	            		db.setCurrentPerson(personid);
		        	                	dialog1.dismiss();
		        	                	String displaythis2 = getResources().getString(R.string.txtSwitching) + " "  + sPersonName;
		        	                	Toast.makeText(getApplicationContext(), displaythis2, Toast.LENGTH_SHORT).show();
		        	        			changeTitle();
		        	        			rowmax = lastRecords;
		        	        			rowfilter = "";
		        	        			DisplayRecords();
		        	                }
		        	                   
		        	            }
		        	        });
		        	    	
		        	    	dialog1.show();
		        	    	
		        	    }
	        			
	        			
	        	        dialog.dismiss();
	        	    }
	        	});

	        	AlertDialog alert = builder.create();
	        	alert.show();
	        	break;

	    }
	    return true;
	}
    
    public void newRecord(View v){
    	final DBAdapter db = new DBAdapter(this);
    	final Dialog dialog = new Dialog(BloodGlucoseActivity.this);
    	final AlertDialog ad = new AlertDialog.Builder(BloodGlucoseActivity.this).create();
    	
    	dialog.setContentView(R.layout.bloodglucoseinput);
    	dialog.setTitle(getResources().getString(R.string.inputTitle));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	RelativeLayout l = (RelativeLayout) dialog.findViewById( R.id.LayoutBloodGlucoseInput );
    	if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        }
    	String[] prepostitems = getResources().getStringArray(R.array.bloodsugarprepost);
    	
	    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, prepostitems);
		 
	    Spinner s = (Spinner) dialog.findViewById(R.id.spBloodGlucosePrePost);
	    s.setAdapter(spinnerArrayAdapter);
	    
	    String[] mealtypes = getResources().getStringArray(R.array.bloodsugarmealtype);
    	
	    ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, mealtypes);
		 
	    Spinner s2 = (Spinner) dialog.findViewById(R.id.spBloodGlucoseMealType);
	    s2.setAdapter(spinnerArrayAdapter2);
	    
    	
    	final EditText dialogtxtBloodGlucoseReading = (EditText) dialog.findViewById(R.id.edtBloodGlucoseReading);
    	final Spinner dialogtxtBloodGlucosePrePost = (Spinner) dialog.findViewById(R.id.spBloodGlucosePrePost);
    	final Spinner dialogtxtBloodGlucoseMealType = (Spinner) dialog.findViewById(R.id.spBloodGlucoseMealType);
    	final EditText dialogtxtBloodGlucoseMedication = (EditText) dialog.findViewById(R.id.edtBloodGlucoseMedication);
    	final EditText dialogtxtBloodGlucoseFoods = (EditText) dialog.findViewById(R.id.edtBloodGlucoseFoods);
    	final EditText dialogtxtBloodGlucoseNotes = (EditText) dialog.findViewById(R.id.edtBloodGlucoseNotes);
    	final EditText dialogtxtRecDate = (EditText) dialog.findViewById(R.id.edtBloodGlucoseInputRecDate);    	
    	
    	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteBloodGlucose);
    	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveBloodGlucose);
    	
    	EditText txtRecDate = (EditText) dialog.findViewById(R.id.edtBloodGlucoseInputRecDate);
		
		final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
                
        c.set(mYear,mMonth,mDay);
		Date dNewDate = c.getTime();
		
		DateFormat df = DateFormat.getDateTimeInstance();
		txtRecDate.setText(df.format(dNewDate));
				
		dialogmDelete.setVisibility(INVISIBLE);
    	
    	dialogmSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String sBloodGlucoseReading = dialogtxtBloodGlucoseReading.getText().toString();
            	int iBloodGlucosePrePost = dialogtxtBloodGlucosePrePost.getSelectedItemPosition();
            	int iBloodGlucoseMealType = dialogtxtBloodGlucoseMealType.getSelectedItemPosition();
            	String sBloodGlucoseMedication = dialogtxtBloodGlucoseMedication.getText().toString();
            	String sBloodGlucoseFoods = dialogtxtBloodGlucoseFoods.getText().toString();
            	String sBloodGlucoseNotes = dialogtxtBloodGlucoseNotes.getText().toString();
            	String sRecDate = dialogtxtRecDate.getText().toString();
            	
            	boolean IsError = false;
            	
            	float fBloodGlucoseReading = 0;
            	try {
            		fBloodGlucoseReading = Float.parseFloat(sBloodGlucoseReading);
            	} catch (NumberFormatException e){
                    	String titleOops = getResources().getString(R.string.alertOops);
    					String alertUnableParseInt = getResources().getString(R.string.alertUnableParseFloat).replace("{FIELD}", "blood glucose reading");
    					
    					ad.setTitle(titleOops);
    					ad.setMessage(alertUnableParseInt); 
    					ad.setButton("OK", new DialogInterface.OnClickListener() {
    					   public void onClick(DialogInterface dialog, int which) {
    					      ad.dismiss();
    					   }
    					});
    					IsError = true;
    					ad.show();  
            	}
            	
            	
        		Date dRecDate = Common.FormatDate(sRecDate, getBaseContext());
        		        		
        		long l = -1;
        		try {
					long lRecDate = dRecDate.getTime();
					if (!IsError){
						db.open();
						int personid = db.getCurrentPersonId();
						l = db.insertSugar(fBloodGlucoseReading, iBloodGlucosePrePost, sBloodGlucoseMedication, iBloodGlucoseMealType, sBloodGlucoseFoods, sBloodGlucoseNotes, lRecDate,personid);
						if (l != -1){
							dialog.dismiss();
						}else {
							dialog.dismiss();
							String titleOops = getResources().getString(R.string.alertOops);
							String alertUnableSave = getResources().getString(R.string.alertUnableSave);
							ad.setTitle(titleOops);
							ad.setMessage(alertUnableSave); 
							ad.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							   ad.dismiss();
						   	}
							});
							ad.show();
						}
	                	db.close();
					}
				} catch (Exception e) {
					
					String titleOops = getResources().getString(R.string.alertOops);
					String alertUnableParseDate = getResources().getString(R.string.alertUnableParseDate);
					
					final Calendar c = Calendar.getInstance();
			        mYear = c.get(Calendar.YEAR);
			        mMonth = c.get(Calendar.MONTH);
			        mDay = c.get(Calendar.DAY_OF_MONTH);
			        
			        c.set(mYear,mMonth,mDay);
					Date dNewDate = c.getTime();
					
					alertUnableParseDate = alertUnableParseDate + " \n"  + 
							Common.DateMessage(dNewDate,getBaseContext());
					
					ad.setTitle(titleOops);
					ad.setMessage(alertUnableParseDate); 
					ad.setButton("OK", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int which) {
					      ad.dismiss();
					   }
					});
					ad.show();
				}
				rowmax = lastRecords;
				rowfilter = "";
                DisplayRecords();    
            }
        });
    	
    	dialog.show();
    	
    	return;
    }
    
    public void DisplayRecords(){
    	final DBAdapter db = new DBAdapter(this);
    	
    	TableLayout tl = (TableLayout)findViewById(R.id.tlRecords);
    	tl.removeAllViews();
    	TableRow tr = new TableRow(this);
        tr.setLayoutParams(new LayoutParams(        	
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));   

        // Create TextViews for the column title
        String[] columntitles = getResources().getStringArray(R.array.bloodglucose_array);
        
        for (String sColTitle: columntitles){
        	tr.addView(common.ColumnTVTitle(this,sColTitle));
        }
        
        tl.addView(tr, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        
        db.open();
        
        int personid = db.getCurrentPersonId();
        Cursor c;
        if (rowfilter.length() > 0){
        	c = db.getSugarRecordsByPersonIdAndKeyword(personid,rowfilter);
        }else {
        	c = db.getSugarRecordsByPersonId(personid);
        }
        int rownum = rowmax;
        int rowcount = rowmax;
        if (rowmax == -1){
        	rownum = c.getCount();  
        	rowcount = rownum;
        }else {
        	if (c.getCount() < rowmax){
        		rowcount = c.getCount();
        	}
        	rownum = rowmax - 1;
        }
        
        if (c.moveToFirst())
        {
            do {
            	final String sId = c.getString(0);
            	final String sBloodGlucoseReading = c.getString(1);
            	String sBloodGlucosePrePost = c.getString(2);
            	final String sBloodGlucoseMedication = c.getString(3);
            	String sBloodGlucoseMealType = c.getString(4);
            	final String sBloodGlucoseFoods = c.getString(5);
            	final String sBloodGlucoseNotes = c.getString(6);
            	final String sRecDate = c.getString(7);

            	final int iBloodGlucosePrePost = Integer.parseInt(sBloodGlucosePrePost);
            	final int iBloodGlucoseMealType = Integer.parseInt(sBloodGlucoseMealType);
            	
            	String[] preposts = getResources().getStringArray(R.array.bloodsugarprepost);
            	String[] mealtypes = getResources().getStringArray(R.array.bloodsugarmealtype);
            	
            	sBloodGlucosePrePost = preposts[iBloodGlucosePrePost].toString();
            	sBloodGlucoseMealType = mealtypes[iBloodGlucoseMealType].toString();
            	            	
            	long lD = Long.parseLong(sRecDate);
            	DateFormat df = DateFormat.getDateTimeInstance();
            	Date dDate = new Date(lD);
            	String sDateDone = df.format(dDate);
            	
            	if (sDateDone.contains(" 12:00:00 AM") || sDateDone.contains(" 12:00:31 AM")){
            		DateFormat dfDevice = android.text.format.DateFormat.getDateFormat(getBaseContext());
            		sDateDone = dfDevice.format(dDate);
            	}
            	final String sDate = sDateDone;
            	
            	tr = new TableRow(this);
            	if (rownum % 2 == 0){
            		tr.setBackgroundDrawable(getResources().getDrawable(R.drawable.eventablerow));
            	} else {
            		tr.setBackgroundDrawable(getResources().getDrawable(R.drawable.oddtablerow));
            	}
            	
            	TableLayout.LayoutParams tableRowParams=
            		  new TableLayout.LayoutParams
            		  (TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
            	
            	tr.setLayoutParams(tableRowParams);  
            	
            	tr.addView(common.ColumnTV(this,sBloodGlucoseReading));
            	tr.addView(common.ColumnTV(this,sBloodGlucosePrePost));
            	tr.addView(common.ColumnTV(this,sBloodGlucoseMedication));
            	tr.addView(common.ColumnTV(this,sBloodGlucoseMealType));
            	tr.addView(common.ColumnTV(this,sBloodGlucoseFoods));
            	tr.addView(common.ColumnTV(this,sDate));
            	tr.addView(common.ColumnTV(this,sBloodGlucoseNotes));
            	
            	           
                tr.setFocusableInTouchMode(true); 
                tr.setFocusable(false);
                
                tr.setOnClickListener(new  OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(BloodGlucoseActivity.this);
                        final AlertDialog ad = new AlertDialog.Builder(BloodGlucoseActivity.this).create();
                        
                    	dialog.setContentView(R.layout.bloodglucoseinput);
                    	dialog.setTitle(getResources().getString(R.string.inputTitle));
                    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    	RelativeLayout l = (RelativeLayout) dialog.findViewById( R.id.LayoutBloodGlucoseInput );
                    	if(s.equals("")|| s.equals("Blue")){
                        	l.setBackgroundResource(R.drawable.backrepeat);
                        } else if (s.equals("Lavender")){
                        	l.setBackgroundResource(R.drawable.backrepeat2);
                        } else if (s.equals("Peach")){
                        	l.setBackgroundResource(R.drawable.backrepeat3);
                        } else if (s.equals("Green")){
                        	l.setBackgroundResource(R.drawable.backrepeat4);
                        }
                    	String[] prepostitems = getResources().getStringArray(R.array.bloodsugarprepost);
                    	
                	    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                	            dialog.getContext(), android.R.layout.simple_spinner_item, prepostitems);
                		 
                	    Spinner s = (Spinner) dialog.findViewById(R.id.spBloodGlucosePrePost);
                	    s.setAdapter(spinnerArrayAdapter);
                	    
                	    String[] mealtypes = getResources().getStringArray(R.array.bloodsugarmealtype);
                    	
                	    ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
                	            dialog.getContext(), android.R.layout.simple_spinner_item, mealtypes);
                		 
                	    Spinner s2 = (Spinner) dialog.findViewById(R.id.spBloodGlucoseMealType);
                	    s2.setAdapter(spinnerArrayAdapter2);
                	    
                    	dialog.show();
                        
                        try {
                    	                        	                       	
                        	final EditText dialogtxtBloodGlucoseReading = (EditText) dialog.findViewById(R.id.edtBloodGlucoseReading);
                        	final Spinner dialogtxtBloodGlucosePrePost = (Spinner) dialog.findViewById(R.id.spBloodGlucosePrePost);
                        	final Spinner dialogtxtBloodGlucoseMealType = (Spinner) dialog.findViewById(R.id.spBloodGlucoseMealType);
                        	final EditText dialogtxtBloodGlucoseMedication = (EditText) dialog.findViewById(R.id.edtBloodGlucoseMedication);
                        	final EditText dialogtxtBloodGlucoseFoods = (EditText) dialog.findViewById(R.id.edtBloodGlucoseFoods);
                        	final EditText dialogtxtBloodGlucoseNotes = (EditText) dialog.findViewById(R.id.edtBloodGlucoseNotes);
                        	final EditText dialogtxtRecDate = (EditText) dialog.findViewById(R.id.edtBloodGlucoseInputRecDate);    	
                        	
                        	dialogtxtBloodGlucoseReading.setText(sBloodGlucoseReading);
                        	dialogtxtBloodGlucoseMedication.setText(sBloodGlucoseMedication);
                        	dialogtxtBloodGlucosePrePost.setSelection(iBloodGlucosePrePost);
                        	dialogtxtBloodGlucoseMealType.setSelection(iBloodGlucoseMealType);
                        	dialogtxtBloodGlucoseFoods.setText(sBloodGlucoseFoods);
                        	dialogtxtBloodGlucoseNotes.setText(sBloodGlucoseNotes);
                        	dialogtxtRecDate.setText(sDate);
                        	
                        	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteBloodGlucose);
                        	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveBloodGlucose);
        
                        	dialogmDelete.setVisibility(VISIBLE);
                        	dialogmDelete.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    boolean d = db.deleteSugarRecord(Long.parseLong(sId));
                                    if (d){
                                    	dialog.dismiss();
                                    }else {
                                    	dialog.dismiss();
                                    	String titleOops = getResources().getString(R.string.alertOops);
                                    	String alertUnableDelete = getResources().getString(R.string.alertUnableDelete);
                                    	ad.setTitle(titleOops);
                                    	ad.setMessage(alertUnableDelete); 
                                    	ad.setButton("OK", new DialogInterface.OnClickListener() {
                                    		   public void onClick(DialogInterface dialog, int which) {
                                    		      ad.dismiss();
                                    		   }
                                    		});
                                    	ad.show();
                                    }
                                    v.setBackgroundColor(Color.TRANSPARENT);
                                    DisplayRecords();    
                                }
                            });
                        	
                        	dialogmSave.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	String sBloodGlucoseReading = dialogtxtBloodGlucoseReading.getText().toString();
                                	int iBloodGlucosePrePost = dialogtxtBloodGlucosePrePost.getSelectedItemPosition();
                                	int iBloodGlucoseMealType = dialogtxtBloodGlucoseMealType.getSelectedItemPosition();
                                	String sBloodGlucoseMedication = dialogtxtBloodGlucoseMedication.getText().toString();
                                	String sBloodGlucoseFoods = dialogtxtBloodGlucoseFoods.getText().toString();
                                	String sBloodGlucoseNotes = dialogtxtBloodGlucoseNotes.getText().toString();
                                	String sRecDate = dialogtxtRecDate.getText().toString();
                                	
                                	float fBloodGlucoseReading = Float.parseFloat(sBloodGlucoseReading);
                                	
                            		Date dRecDate = Common.FormatDate(sRecDate, getBaseContext());
                            		                            		
                            		long l = -1;
                            		try {
                    					long lRecDate = dRecDate.getTime();
                    					db.open();
                    					int personid = db.getCurrentPersonId();
	                                	boolean s = db.updateSugarRecord(Long.parseLong(sId), fBloodGlucoseReading, iBloodGlucosePrePost, sBloodGlucoseMedication, iBloodGlucoseMealType, sBloodGlucoseFoods, sBloodGlucoseNotes, lRecDate,personid);
	                                	if (s){
	                                    	dialog.dismiss();
	                                    }else {
	                                    	dialog.dismiss();
	                                    	String titleOops = getResources().getString(R.string.alertOops);
	                                    	String alertUnableSave = getResources().getString(R.string.alertUnableSave);
	                                    	ad.setTitle(titleOops);
	                                    	ad.setMessage(alertUnableSave); 
	                                    	ad.setButton("OK", new DialogInterface.OnClickListener() {
	                                    		   public void onClick(DialogInterface dialog, int which) {
	                                   		      ad.dismiss();
	                                    		   }
	                                   		});
	                                   	ad.show();
	                                    }
									} catch (Exception e) {
										String titleOops = getResources().getString(R.string.alertOops);
										String alertUnableParseDate = getResources().getString(R.string.alertUnableParseDate);
										
										final Calendar c = Calendar.getInstance();
								        mYear = c.get(Calendar.YEAR);
								        mMonth = c.get(Calendar.MONTH);
								        mDay = c.get(Calendar.DAY_OF_MONTH);
								        
								        c.set(mYear,mMonth,mDay);
										Date dNewDate = c.getTime();
										
										alertUnableParseDate = alertUnableParseDate + " \n"  + 
												Common.DateMessage(dNewDate, getBaseContext());
										
										ad.setTitle(titleOops);
										ad.setMessage(alertUnableParseDate); 
										ad.setButton("OK", new DialogInterface.OnClickListener() {
										   public void onClick(DialogInterface dialog, int which) {
										      ad.dismiss();
										   }
										});
										ad.show();
									}
                        			
                                    v.setBackgroundColor(Color.TRANSPARENT);
                                    DisplayRecords();    
                                }
                            });
                        	
                        	dialog.show();
                        } catch (Exception ex){
                        	
                        	
                        }
                    }
                });

                
                // Add the TableRow to the TableLayout
                tl.addView(tr, new TableLayout.LayoutParams(
                        LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT));
                
                
                rownum--;
            	
            } while (rownum >= 0 && c.moveToNext());
        }
 
        
        db.close();
        
        TextView recTV = (TextView)findViewById(R.id.txtRecords);
        String sRecords = getResources().getString(R.string.records);
        String sAll = getResources().getString(R.string.allrecords);
        String sLast = getResources().getString(R.string.last);
        
    	if (rowmax == -1){
    		sRecords = sAll + " " + Integer.toString(rowcount) + " " + sRecords;;
    	}else {
    		if (rowcount < rowmax+1){
    			sRecords = Integer.toString(rowcount) + " " + sRecords;
    		}else {
    			sRecords = sLast + " " + Integer.toString(rowmax) + " " + sRecords;
    		}
    	}
        
        recTV.setText(sRecords);
    
    }
    
}
    

/*
The MIT License (MIT)

Copyright (c) 2014 Robert C. Robinson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.ravenmistmedia.MyHealthRecords;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

public class HistoryActivity extends Activity {
	
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
	private int rowmax = -1;
	private String rowfilter = "";
	
	
	protected void onResume() {
		super.onResume();
		Common.CheckLogin(this); 
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutHistory );  
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
        setContentView(R.layout.history);
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
    	String menutitle = menuitems[0];
    	
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
	        		rowData = db.exportTableString(db.DATABASE_TABLE_HISTORY, "Medical History");
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
	        		rowData2 = db.exportTableString(db.DATABASE_TABLE_HISTORY, "Medical History");
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
	        		rowData3 = db.exportTableString(db.DATABASE_TABLE_HISTORY, "Medical History");
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
		        			DisplayRecords();
		        	    }else {
		        	    	final Dialog dialog1 = new Dialog(HistoryActivity.this);
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
    	final Dialog dialog = new Dialog(HistoryActivity.this);
    	final AlertDialog ad = new AlertDialog.Builder(HistoryActivity.this).create();
    	
    	dialog.setContentView(R.layout.historyinput);
    	dialog.setTitle(getResources().getString(R.string.inputTitle));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutHistoryInput );
    	if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        }
    	final EditText dialogtxtHistoryDate = (EditText) dialog.findViewById(R.id.edtHistoryInputRecDate);
    	final EditText dialogtxtHistory = (EditText) dialog.findViewById(R.id.edtInputHistoryDetail);
    	
    	final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
                
        c.set(mYear,mMonth,mDay);
		Date dNewDate = c.getTime();
		DateFormat df = DateFormat.getDateTimeInstance();
		String sNewDate = df.format(dNewDate);
		dialogtxtHistoryDate.setText(sNewDate);
    	
    	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteHistory);
    	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveHistory);
    	
    	dialogmDelete.setVisibility(INVISIBLE);
    	
    	dialogmSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String sDate = dialogtxtHistoryDate.getText().toString();
                String sHist = dialogtxtHistory.getText().toString();
                db.open();
                int personid = db.getCurrentPersonId();
            	long l = db.insertHistory(sDate,sHist,personid);
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
        String[] columntitles = getResources().getStringArray(R.array.history_array);
        
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
        	c = db.getHistoryByPersonIdandKeyword(personid,rowfilter);
        } else {
        	c = db.getHistoryByPersonId(personid);
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
            	final String sHistory = c.getString(2);
            	String sDate = "";
            	try {
            		sDate = c.getString(1);
            	} catch (Exception e){
            		  		
            	}
            	final String sHistoryDate = sDate;
            	
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
            	
            	tr.addView(common.ColumnTV(this,sDate));
            	tr.addView(common.ColumnTV(this,sHistory));
                           
                tr.setFocusableInTouchMode(true); 
                tr.setFocusable(false);
                
                tr.setOnClickListener(new  OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	                    	
                        final Dialog dialog = new Dialog(HistoryActivity.this);
                        final AlertDialog ad = new AlertDialog.Builder(HistoryActivity.this).create();
                        
                    	dialog.setContentView(R.layout.historyinput);
                    	dialog.setTitle(getResources().getString(R.string.inputTitle));
                    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutHistoryInput );
                    	if(s.equals("")|| s.equals("Blue")){
                        	l.setBackgroundResource(R.drawable.backrepeat);
                        } else if (s.equals("Lavender")){
                        	l.setBackgroundResource(R.drawable.backrepeat2);
                        } else if (s.equals("Peach")){
                        	l.setBackgroundResource(R.drawable.backrepeat3);
                        } else if (s.equals("Green")){
                        	l.setBackgroundResource(R.drawable.backrepeat4);
                        }
                    	dialog.show();
                    	
                    	try {
                        	final EditText dialogtxtHistoryDate = (EditText) dialog.findViewById(R.id.edtHistoryInputRecDate);
                        	final EditText dialogtxtHistory = (EditText) dialog.findViewById(R.id.edtInputHistoryDetail);
                        	
                        	dialogtxtHistoryDate.setText(sHistoryDate);
                        	dialogtxtHistory.setText(sHistory);
                        	
                        	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteHistory);
                        	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveHistory);
        
                        	dialogmDelete.setVisibility(VISIBLE);
                        	dialogmDelete.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    boolean d = db.deleteHistory(Long.parseLong(sId));
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
                                	String sDate = dialogtxtHistoryDate.getText().toString();
                                    String sHist = dialogtxtHistory.getText().toString();
                                    int personid = db.getCurrentPersonId();
                                	boolean s = db.updateHistory(Long.parseLong(sId),sDate,sHist,personid);
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


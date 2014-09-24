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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLConnection;
import java.text.DateFormat;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

public class DocumentsActivity extends Activity {
	
	Common common = new Common();
	private Button dialogmSave;
	private Button dialogmDelete;
	private Button dialogmSelectFile;
	private Button dialogmSelectCamera;
	private Button dialogmViewFile;
	
	Dialog dialog;
	
	static final int VISIBLE = 0;
	static final int INVISIBLE = 4;
	static final int ACTIVITY_CHOOSE_FILE = 16;
	static final int ACTIVITY_CHOOSE_CAMERA = 17;
	private int mYear;
    private int mMonth;
    private int mDay;
	String s = "Blue";
	int lastRecords = 20;
	private int rowmax = -1;
	private String rowfilter = "";
	private String mCurrentPhotoPath;
	
	
	protected void onResume() {
		super.onResume();
		Common.CheckLogin(this); 
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutDocuments );  
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
        setContentView(R.layout.documents);
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
    	String menutitle = menuitems[10];
    	
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
	        		rowData = db.exportTableString(db.DATABASE_TABLE_DOCUMENTS, "Documents");
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
	        		rowData2 = db.exportTableString(db.DATABASE_TABLE_DOCUMENTS, "Documents");
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
		        	    	final Dialog dialog1 = new Dialog(DocumentsActivity.this);
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
    	final AlertDialog ad = new AlertDialog.Builder(DocumentsActivity.this).create();
    	dialog = new Dialog(DocumentsActivity.this);
    	dialog.setContentView(R.layout.documentsinput);
    	dialog.setTitle(getResources().getString(R.string.inputTitle));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutDocumentInput );
    	if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        }
    	final EditText dialogtxtRecordDate = (EditText) dialog.findViewById(R.id.edtDocumentInputRecDate);
    	final EditText dialogtxtFilename = (EditText) dialog.findViewById(R.id.edtDocumentFilename);
    	final EditText dialogtxtNotes = (EditText) dialog.findViewById(R.id.edtInputDocumentDetail);
    	
    	final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
                
        c.set(mYear,mMonth,mDay);
		Date dNewDate = c.getTime();
		DateFormat df = DateFormat.getDateTimeInstance();
		String sNewDate = df.format(dNewDate);
		dialogtxtRecordDate.setText(sNewDate);
    	
    	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteDocument);
    	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveDocument);
    	dialogmSelectFile = (Button) dialog.findViewById(R.id.btnSelectFile);
    	dialogmSelectCamera = (Button) dialog.findViewById(R.id.btnSelectCamera);
    	dialogmViewFile = (Button) dialog.findViewById(R.id.btnViewFile);
    	
    	dialogmSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String sRecDate = dialogtxtRecordDate.getText().toString();
                String sFilename = dialogtxtFilename.getText().toString();
                String sNotes = dialogtxtNotes.getText().toString();
                Date dRecDate = Common.FormatDate(sRecDate, getBaseContext());
                
                db.open();
                int personid = db.getCurrentPersonId();
                long lRecDate = dRecDate.getTime();
            	long l = db.insertDocument(sFilename,sNotes,lRecDate,personid);

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
    	
    	dialogmSelectFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);	        
            }
        });
    	
    	dialogmViewFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent viewFile;
            	viewFile  = new Intent(Intent.ACTION_VIEW);
             	String filename = dialogtxtFilename.getText().toString();
            	String mimeType = URLConnection.guessContentTypeFromName(filename);
            	viewFile.setDataAndType(Uri.parse("file://" + filename), mimeType);
            	
            	try {
            	    startActivity(viewFile);
            	} catch (android.content.ActivityNotFoundException e) {
            	    Toast.makeText(DocumentsActivity.this, "No handler for this type of file.", 4000).show();
            	}
       
            }
        });
    	
    	final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        	dialogmSelectCamera.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	// Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                    	Toast toast = Toast.makeText(DocumentsActivity.this, "The file cannot be created for the camera.", Toast.LENGTH_LONG);
        				toast.show();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, ACTIVITY_CHOOSE_CAMERA);
                    }
                }
            });
        } else {
        	dialogmSelectCamera.setVisibility(View.INVISIBLE);
        }
     	
        dialogmDelete.setVisibility(View.INVISIBLE);
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
        String[] columntitles = getResources().getStringArray(R.array.documents_array);
        
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
        	c = db.getDocumentRecordsByPersonIdAndKeyword(personid,rowfilter);
        } else {
        	c = db.getDocumenteRecordsByPersonId(personid);
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
            	final String sFilename = c.getString(1);
            	final String sNotes = c.getString(2);
            	String sRecDate = "";
            	try {
            		sRecDate = c.getString(3);
            	} catch (Exception e){
            		  		
            	}
            	
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
            	
            	tr.addView(common.ColumnTV(this,sDate));
            	tr.addView(common.ColumnTV(this,sFilename));
            	tr.addView(common.ColumnTV(this,sNotes));
            	
                tr.setFocusableInTouchMode(true); 
                tr.setFocusable(false);
                
                tr.setOnClickListener(new  OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	dialog = new Dialog(DocumentsActivity.this);
                        final AlertDialog ad = new AlertDialog.Builder(DocumentsActivity.this).create();
                        
                    	dialog.setContentView(R.layout.documentsinput);
                    	dialog.setTitle(getResources().getString(R.string.inputTitle));
                    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutDocumentInput );
                    	if(s.equals("")|| s.equals("Blue")){
                        	l.setBackgroundResource(R.drawable.backrepeat);
                        } else if (s.equals("Lavender")){
                        	l.setBackgroundResource(R.drawable.backrepeat2);
                        } else if (s.equals("Peach")){
                        	l.setBackgroundResource(R.drawable.backrepeat3);
                        } else if (s.equals("Green")){
                        	l.setBackgroundResource(R.drawable.backrepeat4);
                        }
                    	
                    	try {
                        	final EditText dialogtxtRecDate = (EditText) dialog.findViewById(R.id.edtDocumentInputRecDate);
                        	final EditText dialogtxtFilename = (EditText) dialog.findViewById(R.id.edtDocumentFilename);
                        	final EditText dialogtxtNotes = (EditText) dialog.findViewById(R.id.edtInputDocumentDetail);
                        	
                        	dialogtxtRecDate.setText(sDate);
                        	dialogtxtFilename.setText(sFilename);
                        	dialogtxtNotes.setText(sNotes);
                        	
                        	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteDocument);
                        	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveDocument);
                        	dialogmSelectFile = (Button) dialog.findViewById(R.id.btnSelectFile);
                        	dialogmSelectCamera = (Button) dialog.findViewById(R.id.btnSelectCamera);
                        	dialogmViewFile = (Button) dialog.findViewById(R.id.btnViewFile);
                        	
                        	dialogmSave.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	String sRecDate = dialogtxtRecDate.getText().toString();
                                    String sFilename = dialogtxtFilename.getText().toString();
                                    String sNotes = dialogtxtNotes.getText().toString();
                                    Date dRecDate = Common.FormatDate(sRecDate, getBaseContext());
                                    
                                    db.open();
                                    int personid = db.getCurrentPersonId();
                                    long lRecDate = dRecDate.getTime();
                                	boolean s = db.updateDocumentRecord(Long.parseLong(sId),sFilename,sNotes,lRecDate,personid);

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
                                    db.close();
                                    DisplayRecords();    
                                }
                            });
                        	
                        	dialogmSelectFile.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	Intent chooseFile;
                                    Intent intent;
                                    chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                                    chooseFile.setType("*/*");
                                    intent = Intent.createChooser(chooseFile, "Choose a file");
                                    startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);	        
                                }
                            });
                        	
                        	dialogmViewFile.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	Intent viewFile;
                                	viewFile  = new Intent(Intent.ACTION_VIEW);
                                 	String filename = dialogtxtFilename.getText().toString();
                                	String mimeType = URLConnection.guessContentTypeFromName(filename);
                                	viewFile.setDataAndType(Uri.parse("file://" + filename), mimeType);
                                	
                                	try {
                                	    startActivity(viewFile);
                                	} catch (android.content.ActivityNotFoundException e) {
                                	    Toast.makeText(DocumentsActivity.this, "No handler for this type of file.", 4000).show();
                                	}
                           
                                }
                            });
                        	
                        	final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // Ensure that there's a camera activity to handle the intent
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            	dialogmSelectCamera.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                    	// Create the File where the photo should go
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {
                                        	Toast toast = Toast.makeText(DocumentsActivity.this, "The file cannot be created for the camera.", Toast.LENGTH_LONG);
                            				toast.show();
                                        }
                                        // Continue only if the File was successfully created
                                        if (photoFile != null) {
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                    Uri.fromFile(photoFile));
                                            startActivityForResult(takePictureIntent, ACTIVITY_CHOOSE_CAMERA);
                                        }
                                    }
                                });
                            } else {
                            	dialogmSelectCamera.setVisibility(View.INVISIBLE);
                            }
                            
                            dialogmDelete.setVisibility(VISIBLE);
                        	dialogmDelete.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	db.open();
                                    boolean d = db.deleteDocumentRecord(Long.parseLong(sId));
                                    db.close();
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
                        } catch (Exception ex){
                        
                        }
                    	dialog.show();
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
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     switch(requestCode) {
       case ACTIVITY_CHOOSE_FILE: {
    	   if (resultCode == RESULT_OK){
    		   Uri uri = data.getData();
    		   String filePath = uri.getPath();
    		   EditText documentName = (EditText)dialog.findViewById(R.id.edtDocumentFilename);
    		   documentName.setText(filePath);
    	   }
    	   break;
       }
       case ACTIVITY_CHOOSE_CAMERA: {
    	   if(resultCode == RESULT_OK){  
    	       EditText documentName = (EditText)dialog.findViewById(R.id.edtDocumentFilename);
     		   documentName.setText(mCurrentPhotoPath);
    	    }
    	   break;
       }
     }
   }

private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "MyHealthRecords_" + timeStamp;
    File sdDir = Environment.getExternalStorageDirectory();
	String sSdDir = sdDir.toString();
	String mhrFolder = sSdDir + "/MyHealthRecords/";
	File myFolder = new File(mhrFolder);
	String imageFilename = myFolder + "/" + imageFileName + ".jpg";
	File myFile = new File( imageFilename );
    
    // Save a file: path for use with ACTION_VIEW intents
    mCurrentPhotoPath = myFile.getAbsolutePath();
    return myFile;
}

}


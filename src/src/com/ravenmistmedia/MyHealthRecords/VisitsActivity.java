package com.ravenmistmedia.MyHealthRecords;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

public class VisitsActivity extends Activity{
	Common common = new Common();
	private Button dialogmSave;
	private Button dialogmDelete;
	private Spinner spinner;
	
	static final int VISIBLE = 0;
	static final int INVISIBLE = 4;
	
	final static int MHR_PICK_CONTACT = 1;
	
	Dialog inputDialog;
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
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutVisits);  
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
        setContentView(R.layout.visits);
	
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
    	String menutitle = menuitems[7];
    	
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
	        		rowData = db.exportTableString(db.DATABASE_TABLE_VISITS, "Office Visits");
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
	        		rowData2 = db.exportTableString(db.DATABASE_TABLE_VISITS, "Office Visits");
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
	        		rowData3 = db.exportTableString(db.DATABASE_TABLE_VISITS, "Office Visits");
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
		        	    	final Dialog dialog1 = new Dialog(VisitsActivity.this);
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
		        	        			rowmax = -1;
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
    	final Dialog dialog = new Dialog(VisitsActivity.this);
    	final AlertDialog ad = new AlertDialog.Builder(VisitsActivity.this).create();
    	
    	
    	dialog.setContentView(R.layout.visitsinput);
    	RelativeLayout l = (RelativeLayout) dialog.findViewById( R.id.LayoutVisitInput );
    	if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        }
    	dialog.setTitle(getResources().getString(R.string.inputTitle));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	
    	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteVisit);
    	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveVisit);

    	EditText txtRecDate = (EditText) dialog.findViewById(R.id.edtVisitInputRecDate);
		
		final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
                
        c.set(mYear,mMonth,mDay);
		Date dNewDate = c.getTime();
		DateFormat df = DateFormat.getDateTimeInstance();
		String sNewDate = df.format(dNewDate);
		txtRecDate.setText(sNewDate);
    	
    	
    	dialogmDelete.setVisibility(INVISIBLE);
    	
    	spinner = (Spinner) dialog.findViewById(R.id.spnVisitsName);
    	final String sAdd = getResources().getString(R.string.addNewOffice);
    	
    	db.open();
	    final int personid = db.getCurrentPersonId();
	    Cursor csr = db.getOfficeRecordsByPersonId(personid);
	    startManagingCursor(csr);
	   	
	    ArrayAdapter<String> adapter = new ArrayAdapter(VisitsActivity.this, android.R.layout.simple_spinner_item);
	    adapter.add("select an office");
	    adapter.add("-- " + sAdd + " --");
	    boolean officeAdded = false;
	    if (csr != null) {
	    	csr.moveToFirst();
		    try {
	    	adapter.add(csr.getString(1));
	    	officeAdded = true;
		    } catch (Exception ex){
		    	
		    } 
		    try {
	        while(csr.moveToNext()) {
	            adapter.add(csr.getString(1));
	            officeAdded = true;
	        }
		    } catch (Exception ex){
		    	
		    }
	        csr.close();
	    }
	    
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

	    	@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id){
				
	    		switch (position){
	    			case 1:
	    				final Dialog dialog = new Dialog(VisitsActivity.this);
	    				final AlertDialog ad = new AlertDialog.Builder(VisitsActivity.this).create();
	    				dialog.setContentView(R.layout.officeinput);
	    		    	RelativeLayout l = (RelativeLayout) dialog.findViewById( R.id.LayoutOfficeInput );
	    		    	if(s.equals("")|| s.equals("Blue")){
	    		        	l.setBackgroundResource(R.drawable.backrepeat);
	    		        } else if (s.equals("Lavender")){
	    		        	l.setBackgroundResource(R.drawable.backrepeat2);
	    		        } else if (s.equals("Peach")){
	    		        	l.setBackgroundResource(R.drawable.backrepeat3);
	    		        } else if (s.equals("Green")){
	    		        	l.setBackgroundResource(R.drawable.backrepeat4);
	    		        }
	    		    	dialog.setTitle(getResources().getString(R.string.inputTitle));
	    		    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    		    	TextView tLookupKey = (TextView) dialog.findViewById(R.id.LookupKey01);
	    		    	tLookupKey.setVisibility(INVISIBLE);
	    		    	
	    		    	ImageButton btnContactLink = (ImageButton) dialog.findViewById(R.id.btnLinkContact);
	    		    	btnContactLink.setOnClickListener(new View.OnClickListener() {
	    		            public void onClick(View v) {
	    		            	inputDialog = dialog;
	    		            	            	
	    		            	Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
	    		            	startActivityForResult(intent, MHR_PICK_CONTACT);            	
	    		               }	
	    		    			
	    		     	});
	    		    	
	    		    	final EditText dialogtxtOfficeName = (EditText) dialog.findViewById(R.id.edtInputOfficeName);
	    		    	final EditText dialogtxtOfficeHours = (EditText) dialog.findViewById(R.id.edtInputOfficeHours);
	    		    	final EditText dialogtxtNotes = (EditText) dialog.findViewById(R.id.edtInputOfficeNotes);
	    		    	final TextView dialogtxtLookupKey = (TextView) dialog.findViewById(R.id.LookupKey01);
	    		    	final TextView dialogtxtContactId = (TextView) dialog.findViewById(R.id.ContactId01);
	    		    	
	    		    	final Button dialogtxtCallPhone = (Button) dialog.findViewById(R.id.btnCallPhone);
	    		    	final Button dialogtxtViewContact = (Button) dialog.findViewById(R.id.btnViewContact);
	    		    	
	    		    	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteOffice);
	    		    	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveOffice);
	    		    	dialogmDelete.setVisibility(INVISIBLE);
	    		    	dialogtxtCallPhone.setVisibility(INVISIBLE);
	    		    	dialogtxtViewContact.setVisibility(INVISIBLE);
	    		    	
	    		    	
	    		    	dialogmSave.setOnClickListener(new View.OnClickListener() {

	    					@Override
	    					public void onClick(View v) {
	    						String sLookupKey = dialogtxtLookupKey.getText().toString();
	    						String sContactId = dialogtxtContactId.getText().toString();
	    		            	String sOfficeName = dialogtxtOfficeName.getText().toString();
	    		            	String sOfficeHours = dialogtxtOfficeHours.getText().toString();
	    		            	String sNotes = dialogtxtNotes.getText().toString();
	    		            	
	    		            	int iContactId = -1;
	    		            	try {
	    		            		iContactId = Integer.parseInt(sContactId);
	    		            	} catch (Exception ex){
	    		            		iContactId = -1;
	    		            	}
	    		            	
	    		            	db.open();
	    		            	int personid = db.getCurrentPersonId();
	    		            	
	    		            	try{
	    		            		Long l = db.insertOffice(sOfficeName,sOfficeHours,sLookupKey,iContactId,sNotes,personid);
	    		            		if (l != -1){
	    		            			dialog.dismiss();
	    		            			Cursor csr = db.getOfficeRecordsByPersonId(personid);
	    		            		    startManagingCursor(csr);
	    		            			ArrayAdapter<String> adapter = new ArrayAdapter(VisitsActivity.this, android.R.layout.simple_spinner_item);
	    		            		    adapter.add("select an office");
	    		            		    adapter.add("-- " + sAdd + " --");
	    		            		    boolean officeAdded = false;
	    		            		    if (csr != null) {
	    		            		    	csr.moveToFirst();
	    		            		    	try {
	    		            		    		adapter.add(csr.getString(1));
	    		            		    		officeAdded = true;
	    		            		    	} catch (Exception ex){
	    		            		    		
	    		            		    	}
	    		            		    	try {
	    		            			    while(csr.moveToNext()) {
	    		            		            adapter.add(csr.getString(1));
	    		            		            officeAdded = true;
	    		            		        }
	    		            		    	} catch (Exception ex){
	    		            		    	
	    		            		    	}
	    		            		        csr.close();
	    		            		    }
	    		            		    	    
	    		            		    spinner.setAdapter(adapter);
	    		            		    spinner.setSelection(adapter.getCount()-1,true);
	    		            		    
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
	    		            	}catch (Exception ex){
	    		            		dialog.dismiss();
	    		            	}
	    		            	
	    		            	
	    		            	db.close();

	    					}
	    		    	});
	    		    	dialog.show();   	
	    		    	
	    				break;
	    		}
	    		
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}

	    });

		db.close();
    	
    	final EditText dialogtxtVisitHappened = (EditText) dialog.findViewById(R.id.edtInputVisitHappened);
    	final EditText dialogtxtNotes = (EditText) dialog.findViewById(R.id.edtInputVisitNotes);
    	final EditText dialogtxtRecDate = (EditText) dialog.findViewById(R.id.edtVisitInputRecDate);
		   	
    	dialogmSave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String officename = "";
			
				if (spinner.getSelectedItemPosition() == 0){
					String titleOops = getResources().getString(R.string.alertOfficeRequired);
					String alertUnableSave = getResources().getString(R.string.alertSelectAnOffice);
					ad.setTitle(titleOops);
					ad.setMessage(alertUnableSave); 
					ad.setButton("OK", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int which) {
					      ad.dismiss();
					      spinner.requestFocus();
					   }
					});
					ad.show();
				} else {
				
				officename = spinner.getSelectedItem().toString();
				db.open();
				
				Cursor cOffice = db.getOfficeRecordsByPersonIdAndOfficeName(personid, officename);
				int iOfficeId = -1;
				
							
				if (cOffice != null){
					cOffice.moveToFirst();
					iOfficeId = cOffice.getInt(0);
				}
				String sHappened = dialogtxtVisitHappened.getText().toString();
				String sNotes = dialogtxtNotes.getText().toString();
				String sRecDate = dialogtxtRecDate.getText().toString();
				
				Date dRecDate = Common.FormatDate(sRecDate,getBaseContext());
                
        		long l = -1;
						
				try {
					long lRecDate;
					
					lRecDate = dRecDate.getTime();
					
					l = db.insertVisit(lRecDate,iOfficeId,sHappened,sNotes,personid);
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
					
					ad.setButton("OK", new DialogInterface.OnClickListener() {
					   public void onClick(DialogInterface dialog, int which) {
					      ad.dismiss();
					   }
					});
					
					ad.setTitle(titleOops);
					ad.setMessage(alertUnableParseDate); 
					
					
					ad.show();
				}

				
				db.close();
				}
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
        String[] columntitles = getResources().getStringArray(R.array.visits_array);
        
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
        	c = db.getVisitRecordsByPersonIdAndKeyword(personid,rowfilter);
        }else {
        	c = db.getVisitRecordsByPersonId(personid);	
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
            	final String sRecDate = c.getString(1);
            	final String sOfficeId = c.getString(2);
            	final String sVisitHappened = c.getString(3);
            	final String sNotes = c.getString(4);
            	
            	long lD = Long.parseLong(sRecDate);
            	DateFormat df = DateFormat.getDateTimeInstance();
            	Date dDate = new Date(lD);
            	String sDateDone = df.format(dDate);
            	
            	if (sDateDone.contains(" 12:00:00 AM") || sDateDone.contains(" 12:00:31 AM")){
            		DateFormat dfDevice = android.text.format.DateFormat.getDateFormat(getBaseContext());
            		sDateDone = dfDevice.format(dDate);
            	}
            	final String sDate = sDateDone;
            	String sOName = "";
            	
            	try {
            		long lO = Long.parseLong(sOfficeId);
            		Cursor csO = db.getOfficeRecord(lO);
            		if (csO != null){
            			csO.moveToFirst();
            			try {
            				sOName = csO.getString(1);
            			} catch (Exception ex){
            				sOName = "UNKNOWN";
            			}
            		}
            		
            	}finally{
            	}
            	
            	final String sOfficeName = sOName;
            	
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
            	tr.addView(common.ColumnTV(this,sOfficeName));
            	tr.addView(common.ColumnTV(this,sVisitHappened));
            	tr.addView(common.ColumnTV(this,sNotes));
            	                           
                tr.setFocusableInTouchMode(true); 
                tr.setFocusable(false);
                tr.setOnClickListener(new  OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(VisitsActivity.this);
                        final AlertDialog ad = new AlertDialog.Builder(VisitsActivity.this).create();
                        
                    	dialog.setContentView(R.layout.visitsinput);
                    	dialog.setTitle(getResources().getString(R.string.inputTitle));
                    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    	RelativeLayout l = (RelativeLayout) dialog.findViewById( R.id.LayoutVisitInput );
                    	if(s.equals("")|| s.equals("Blue")){
                        	l.setBackgroundResource(R.drawable.backrepeat);
                        } else if (s.equals("Lavender")){
                        	l.setBackgroundResource(R.drawable.backrepeat2);
                        } else if (s.equals("Peach")){
                        	l.setBackgroundResource(R.drawable.backrepeat3);
                        } else if (s.equals("Green")){
                        	l.setBackgroundResource(R.drawable.backrepeat4);
                        }
                    	
                    	final EditText dialogtxtVisitHappened = (EditText) dialog.findViewById(R.id.edtInputVisitHappened);
                    	final EditText dialogtxtNotes = (EditText) dialog.findViewById(R.id.edtInputVisitNotes);
                    	final EditText dialogtxtRecDate = (EditText) dialog.findViewById(R.id.edtVisitInputRecDate);
                    	final Spinner spinner = (Spinner) dialog.findViewById(R.id.spnVisitsName);
                    	final String sAdd = getResources().getString(R.string.addNewOffice);
	            		
                    	dialogtxtRecDate.setText(sDate);
                    	dialogtxtVisitHappened.setText(sVisitHappened);
                    	dialogtxtNotes.setText(sNotes);
                    	final String sOId = sOfficeId;
                    	
                    	db.open();
                	    final int personid = db.getCurrentPersonId();
                	    Cursor csr = db.getOfficeRecordsByPersonId(personid);
                	    startManagingCursor(csr);
                	   	int position = 2;
                	   	int recnum = 2;
                	    ArrayAdapter<String> adapter = new ArrayAdapter(VisitsActivity.this, android.R.layout.simple_spinner_item);
                	    adapter.add("select an office");
                	    adapter.add("-- " + sAdd + " --");
                	    
                	    if (csr != null) {
                	    	csr.moveToFirst();
                	    	try {
                		    adapter.add(csr.getString(1));
                		    	while(csr.moveToNext()) {
                		    		adapter.add(csr.getString(1));
                		    		if (csr.getInt(0) == Integer.parseInt(sOId)){
                		    			position = recnum + 1;
                		    		}
                		    		recnum++;
                		    	}
                	    	} catch (Exception ex){
                	    		// office unknown
                	    		position = 0;
                	    	}
                	        
                	        csr.close();
                	    }
                	    	    
                	    spinner.setAdapter(adapter);
                	    
                	    spinner.setSelection(position, true);
                	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

                	    	@Override
                			public void onItemSelected(AdapterView<?> parent, View view,
                					int position, long id){
                				
                	    		switch (position){
                	    			case 1:
                	    				final Dialog dialog = new Dialog(VisitsActivity.this);
                	    				final AlertDialog ad = new AlertDialog.Builder(VisitsActivity.this).create();
                	    				dialog.setContentView(R.layout.officeinput);
                	    		    	RelativeLayout l = (RelativeLayout) dialog.findViewById( R.id.LayoutOfficeInput );
                	    		    	if(s.equals("")|| s.equals("Blue")){
                	    		        	l.setBackgroundResource(R.drawable.backrepeat);
                	    		        } else if (s.equals("Lavender")){
                	    		        	l.setBackgroundResource(R.drawable.backrepeat2);
                	    		        } else if (s.equals("Peach")){
                	    		        	l.setBackgroundResource(R.drawable.backrepeat3);
                	    		        } else if (s.equals("Green")){
                	    		        	l.setBackgroundResource(R.drawable.backrepeat4);
                	    		        }
                	    		    	dialog.setTitle(getResources().getString(R.string.inputTitle));
                	    		    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                	    		    	TextView tLookupKey = (TextView) dialog.findViewById(R.id.LookupKey01);
                	    		    	tLookupKey.setVisibility(INVISIBLE);
                	    		    	
                	    		    	ImageButton btnContactLink = (ImageButton) dialog.findViewById(R.id.btnLinkContact);
                	    		    	btnContactLink.setOnClickListener(new View.OnClickListener() {
                	    		            public void onClick(View v) {
                	    		            	inputDialog = dialog;
                	    		            	            	
                	    		            	Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                	    		            	startActivityForResult(intent, MHR_PICK_CONTACT);            	
                	    		               }	
                	    		    			
                	    		     	});
                	    		    	
                	    		    	
                	    		    	final EditText dialogtxtOfficeName = (EditText) dialog.findViewById(R.id.edtInputOfficeName);
                	    		    	final EditText dialogtxtOfficeHours = (EditText) dialog.findViewById(R.id.edtInputOfficeHours);
                	    		    	final EditText dialogtxtNotes = (EditText) dialog.findViewById(R.id.edtInputOfficeNotes);
                	    		    	final TextView dialogtxtLookupKey = (TextView) dialog.findViewById(R.id.LookupKey01);
                	    		    	final TextView dialogtxtContactId = (TextView) dialog.findViewById(R.id.ContactId01);
                	    		    	
                	    		    	final Button dialogtxtCallPhone = (Button) dialog.findViewById(R.id.btnCallPhone);
                	    		    	final Button dialogtxtViewContact = (Button) dialog.findViewById(R.id.btnViewContact);
                	    		    	
                	    		    	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteOffice);
                	    		    	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveOffice);
                	    		    	dialogmDelete.setVisibility(INVISIBLE);
                	    		    	dialogtxtCallPhone.setVisibility(INVISIBLE);
                	    		    	dialogtxtViewContact.setVisibility(INVISIBLE);
                	    		    	
                	    		    	
                	    		    	dialogmSave.setOnClickListener(new View.OnClickListener() {

                	    					@Override
                	    					public void onClick(View v) {
                	    						String sLookupKey = dialogtxtLookupKey.getText().toString();
                	    						String sContactId = dialogtxtContactId.getText().toString();
                	    		            	String sOfficeName = dialogtxtOfficeName.getText().toString();
                	    		            	String sOfficeHours = dialogtxtOfficeHours.getText().toString();
                	    		            	String sNotes = dialogtxtNotes.getText().toString();
                	    		            	
                	    		            	int iContactId = -1;
                	    		            	try {
                	    		            		iContactId = Integer.parseInt(sContactId);
                	    		            	} catch (Exception ex){
                	    		            		iContactId = -1;
                	    		            	}
                	    		            	
                	    		            	db.open();
                	    		            	int personid = db.getCurrentPersonId();
                	    		            	
                	    		            	try{
                	    		            		Long l = db.insertOffice(sOfficeName,sOfficeHours,sLookupKey,iContactId,sNotes,personid);
                	    		            		if (l != -1){
                	    		            			dialog.dismiss();
                	    		            			Cursor csr = db.getOfficeRecordsByPersonId(personid);
                	    		            		    startManagingCursor(csr);
                	    		            			ArrayAdapter<String> adapter = new ArrayAdapter(VisitsActivity.this, android.R.layout.simple_spinner_item);
                	    		            		    adapter.add("select an office");
                	    		            		    adapter.add("-- " + sAdd + " --");
                	    		            		    if (csr != null) {
                	    		            		    	csr.moveToFirst();
                	    		            			    adapter.add(csr.getString(1));
                	    		            		        while(csr.moveToNext()) {
                	    		            		            adapter.add(csr.getString(1));
                	    		            		        }
                	    		            		        csr.close();
                	    		            		    }
                	    		            		    	    
                	    		            		    spinner.setAdapter(adapter);
                	    		            		    spinner.setSelection(adapter.getCount()-1,true);
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
                	    		            	}catch (Exception ex){
                	    		            		dialog.dismiss();
                	    		            	}
                	    		            	
                	    		            	
                	    		            	db.close();

                	    					}
                	    		    	});
                	    		    	dialog.show();   	
                	    		    	
                	    				break;
                	    		}
                	    		
                			}

                			@Override
                			public void onNothingSelected(AdapterView<?> parent) {
               				
                			}

                	    });
             	    
                		db.close();
                    	
                        try {
                        	    
                        	dialogmDelete = (Button) dialog.findViewById(R.id.btnDeleteVisit);
                        	dialogmSave = (Button) dialog.findViewById(R.id.btnSaveVisit);
                        	
                        	dialogmDelete.setVisibility(VISIBLE);
                        	dialogmDelete.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	db.open();
                                    boolean d = db.deleteVisitRecord(Long.parseLong(sId));
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
                        	
                        	
                        	dialogmSave.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	String officename = "";
                    				officename = spinner.getSelectedItem().toString();
                    				db.open();
                    				Cursor cOffice = db.getOfficeRecordsByPersonIdAndOfficeName(personid, officename);
                    				int iOfficeId = -1;
                    				if (cOffice != null){
                    					cOffice.moveToFirst();
                    					try {
                    					iOfficeId = cOffice.getInt(0);
                    					} catch (Exception ex){
                    						String titleOops = getResources().getString(R.string.alertOfficeRequired);
                    						String alertUnableSave = getResources().getString(R.string.alertSelectAnOffice);
                    						ad.setTitle(titleOops);
                    						ad.setMessage(alertUnableSave); 
                    						ad.setButton("OK", new DialogInterface.OnClickListener() {
                    						   public void onClick(DialogInterface dialog, int which) {
                    						      ad.dismiss();
                    						      spinner.requestFocus();
                    						   }
                    						});
                    						ad.show();
                    						return;
                    					}
                    				}
                    				String sHappened = dialogtxtVisitHappened.getText().toString();
                    				String sNotes = dialogtxtNotes.getText().toString();
                    				String sRecDate = dialogtxtRecDate.getText().toString();
                    				
                    				Date dRecDate = Common.FormatDate(sRecDate, getBaseContext());
                            		                            		
                            		long l = -1;
                    						
                    				try {
                    					long lRecDate = dRecDate.getTime();
                    					boolean b = db.updateVisitRecord(Long.parseLong(sId),lRecDate,iOfficeId,sHappened,sNotes,personid);
                    					if (b){
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
                    					
                    					ad.setButton("OK", new DialogInterface.OnClickListener() {
                    					   public void onClick(DialogInterface dialog, int which) {
                    					      ad.dismiss();
                    					   }
                    					});
                    					
                    					ad.setTitle(titleOops);
                    					ad.setMessage(alertUnableParseDate); 
                    					ad.setButton("OK", new DialogInterface.OnClickListener() {
                    					   public void onClick(DialogInterface dialog, int which) {
                    					      ad.dismiss();
                    					   }
                    					});
                    					ad.show();
                    				} 

                    				
                    				db.close();
                    				v.setBackgroundColor(Color.TRANSPARENT);
                                    DisplayRecords();    
                                }
                            });
                        
                        
                        } catch (Exception ex2){
                    	
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

     super.onActivityResult(requestCode, resultCode, data);
      
     switch (requestCode) {
     case (MHR_PICK_CONTACT) :
       if (resultCode == Activity.RESULT_OK) {
         Uri contactData = data.getData();
         Cursor c =  managedQuery(contactData, null, null, null, null);
         if (c.moveToFirst()) {
           String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
           String lookupkey = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
           long contactid = c.getLong(c.getColumnIndex(ContactsContract.Contacts._ID));
           int hasPhone = c.getInt(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
           
           EditText txtOfficeName = (EditText) inputDialog.findViewById(R.id.edtInputOfficeName);
           TextView txtLookupKey = (TextView) inputDialog.findViewById(R.id.LookupKey01);
           TextView txtContactId = (TextView) inputDialog.findViewById(R.id.ContactId01);
           Button btnPhone = (Button) inputDialog.findViewById(R.id.btnCallPhone);
           ImageButton btnContact = (ImageButton) inputDialog.findViewById(R.id.btnLinkContact);
           txtOfficeName.setText(name);
           txtLookupKey.setText(lookupkey);
           txtLookupKey.setVisibility(INVISIBLE);
           txtContactId.setText(String.valueOf(contactid));
           txtContactId.setVisibility(INVISIBLE);
            
           Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactid);
           try {
           
           InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(this.getContentResolver(),photoUri);
           
           Bitmap bMap = BitmapFactory.decodeStream(is);
           
           if (bMap.getHeight() == 0 ){
        	   btnContact.setImageDrawable(getResources().getDrawable(R.drawable.ic_nophoto)); 
           }
           btnContact.setImageBitmap(bMap);
           } catch (Exception ex){
        	   try {
        		   btnContact.setImageDrawable(getResources().getDrawable(R.drawable.ic_nophoto));
        	   } finally{
        	   }
           }
           
           if (hasPhone == 1){
        	   Cursor phonesCursor = getContentResolver().query(
   					Phone.CONTENT_URI,
   					null,
   					Phone.CONTACT_ID + "=" + contactid,
   					null,
   					null);
   			if(phonesCursor.getCount()>0) {
   				phonesCursor.moveToNext();
   				final String phoneNum = phonesCursor.getString(
   					phonesCursor.getColumnIndex(Phone.NUMBER));

   				btnPhone.setText("CALL " + phoneNum);
   				btnPhone.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                                	Intent i = new Intent( Intent.ACTION_CALL );
                    				i.setData( Uri.parse( "tel:" + phoneNum ) );
                    				startActivity( i );
                      }});
   				
   			} else {
   				btnPhone.setText("No Phone Number"); 
   			}
           }else {
        	   btnPhone.setText("No Phone Number"); 
           }
           
         }
       }
       break;
   }
    }
    
    
    	
    
    
}

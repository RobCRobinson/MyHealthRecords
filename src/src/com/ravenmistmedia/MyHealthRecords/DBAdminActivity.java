package com.ravenmistmedia.MyHealthRecords;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.ravenmistmedia.MyHealthRecords.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;


public class DBAdminActivity extends Activity {
	
	private String[] mFileList;
	private File mPath = new File(Environment.getExternalStorageDirectory() + "//MyHealthRecords//");
	private String mChosenFile;
	private static final String FTYPE = ".txt";    
	private static final int DIALOG_LOAD_FILE = 1000;
	private ProgressDialog dPleaseWait;
	String s = "Blue";
	
	protected void onResume() {
		Common.CheckLogin(this); 
		super.onResume();
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutDBAdmin );  
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
        
        l = (LinearLayout) findViewById( R.id.LayoutButtons );  
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
    	Common.CheckLogin(this); 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbadmin);
        
        String[] menuitems = getResources().getStringArray(R.array.optionsmenu_array);
	    this.setTitle(menuitems[1]);
	    
	}
    
    public void dbReset(View v){
    	final DBAdapter db = new DBAdapter(this);
    	new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(R.string.warning)
        .setMessage(R.string.warningMessage)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dPleaseWait = ProgressDialog.show(DBAdminActivity.this, "","Please wait...", true);
            	
            	db.open();        
                db.dropAndCreateTables();
                db.close();
                dPleaseWait.dismiss();
                Toast t = Toast.makeText(DBAdminActivity.this, "The database has been reset.", Toast.LENGTH_SHORT);
            	t.show();
            }

        })
        .setNegativeButton(R.string.no, null)
        .show();
    	
	}
    
    public void dbExport(View v)
    {
    	final Dialog dialog = new Dialog(DBAdminActivity.this);
    	dialog.setContentView(R.layout.dbadminexport);
    	dialog.setTitle(getResources().getString(R.string.inputTitle));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutDBAdminExport);
    	if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        }
    	final DBAdapter db = new DBAdapter(this);
    	
    	final Spinner spnTableName = (Spinner) dialog.findViewById(R.id.spnTableName);
    	String[] tablenames = getResources().getStringArray(R.array.tablenames_array);
		
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, tablenames);
		spnTableName.setAdapter(spinnerArrayAdapter);
    	
    	Button dialogmExport = (Button) dialog.findViewById(R.id.btnDBExport);
    	
    	final RadioButton radXML = (RadioButton) dialog.findViewById(R.id.btnXMLOutput);
    	final RadioButton radTXT= (RadioButton) dialog.findViewById(R.id.btnTXTOutput);
    	final EditText edtOutputFile = (EditText) dialog.findViewById(R.id.edtLocationToExport);
    	
    	radXML.setChecked(true);
    	radXML.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				edtOutputFile.setText(R.string.ExportLocation);
			}
		});
    	
    	radTXT.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				edtOutputFile.setText(R.string.ExportLocation2);
			}
		});
    	
    	dialogmExport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	dPleaseWait = ProgressDialog.show(v.getContext(), "","Please wait...", true);
            	
            	final String outputFile = edtOutputFile.getText().toString();
            	final String tableName = spnTableName.getSelectedItem().toString();
            	
            	if (radXML.isChecked()){
            		db.ExportXML(outputFile, tableName);
            	}
            	if (radTXT.isChecked())
            	{
            		db.ExportTXT(outputFile, tableName);
            	}
            	
            	dPleaseWait.dismiss();
            	dialog.dismiss();
            	Toast t = Toast.makeText(DBAdminActivity.this, "All records have been exported.", Toast.LENGTH_SHORT);
            	t.show();
            };
    	});
    	
    	dialog.show();    	
    }
    
    public void dbImport(View v)
    {
    	final Dialog dialog = new Dialog(DBAdminActivity.this);
    	dialog.setContentView(R.layout.dbadminimport);
    	dialog.setTitle(getResources().getString(R.string.inputTitle));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutDBAdminImport);
    	if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        }
    	final DBAdapter db = new DBAdapter(this);
    	
    	final Spinner spnTableName = (Spinner) dialog.findViewById(R.id.spnTableNameI);
    	String[] tablenames = getResources().getStringArray(R.array.tablenames_array);
		
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, tablenames);
		spnTableName.setAdapter(spinnerArrayAdapter);
		
		final Spinner spnInputFile = (Spinner) dialog.findViewById(R.id.spnFileToImport);
		List<String> filenames = new ArrayList<String>();
		File sdDir = Environment.getExternalStorageDirectory();
    	String sSdDir = sdDir.toString();
    	String mhrFolder = sSdDir + "/MyHealthRecords/";
    	File f = new File(mhrFolder);        
    	File file[] = f.listFiles();
    	for (int i=0; i < file.length; i++)
    	{
    		String filename = file[i].getName();
    		if (filename.toUpperCase().contains(".XML")) {
    			filenames.add(file[i].getName());
    		}
    	}
		ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, filenames);
		spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnInputFile.setAdapter(spinnerArrayAdapter2);
		Button dialogmImport = (Button) dialog.findViewById(R.id.btnDBImport);
		
		dialogmImport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	dPleaseWait = ProgressDialog.show(v.getContext(), "","Please wait...", true);
            	final String inputFile = spnInputFile.getSelectedItem().toString();
            	final String tableName = spnTableName.getSelectedItem().toString();
            	db.open();
            	boolean b = db.ImportXML(inputFile, tableName);
            	db.close();
            	dPleaseWait.dismiss();
            	if (b){
            		dialog.dismiss();
            		Toast t = Toast.makeText(DBAdminActivity.this, "All records have been imported.", Toast.LENGTH_SHORT);
            		t.show();
            	} else {
            		Toast t = Toast.makeText(DBAdminActivity.this, "The XML file is not in the correct format for the selected table.", Toast.LENGTH_LONG);
            		t.show();
            	}
            };
    	});
    	
    	dialog.show();    	
    	
    }
    
    public void dbBackup(View v)
    {
    	String[] dbList = getApplicationContext().databaseList();
    	File dbFolder = getApplicationContext().getDatabasePath(dbList[0]);
    	
    	
    	dPleaseWait = ProgressDialog.show(v.getContext(), "","Please wait...", true);
    	final DBAdapter db = new DBAdapter(this);
    	String filename = db.BackupDatabase(dbFolder);
    	dPleaseWait.dismiss();
    	if (filename.contains("does not exist")){
    		Toast t = Toast.makeText(DBAdminActivity.this, "The database does not exist at \r\n" + filename + ".", Toast.LENGTH_SHORT);
        	t.show();
    	
    	} else {
    		Toast t = Toast.makeText(DBAdminActivity.this, "The database has been backed up to \r\n" + filename + ".", Toast.LENGTH_SHORT);
        	t.show();
    	}
    
    }
    
    public void dbRestore(View v)
    {
    	String[] dbList = getApplicationContext().databaseList();
    	File dbFolder = getApplicationContext().getDatabasePath(dbList[0]);
    	
    	dPleaseWait = ProgressDialog.show(v.getContext(), "","Please wait...", true);
    	final DBAdapter db = new DBAdapter(this);
    	String filename = db.RestoreDatabase(dbFolder);
    	dPleaseWait.dismiss();
    	if (filename.contains("does not exist")){
    		Toast t = Toast.makeText(DBAdminActivity.this, "The database does not exist at \r\n" + filename + ".", Toast.LENGTH_SHORT);
        	t.show();
    	
    	} else {
    		Toast t = Toast.makeText(DBAdminActivity.this, "The database has been restored from \r\n" + filename + ".", Toast.LENGTH_SHORT);
        	t.show();
    	}
    
    }
    
    
    
}


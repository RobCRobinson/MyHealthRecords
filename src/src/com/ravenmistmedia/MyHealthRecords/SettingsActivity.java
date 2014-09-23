package com.ravenmistmedia.MyHealthRecords;

import com.ravenmistmedia.MyHealthRecords.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;


public class SettingsActivity extends Activity {
	
	private Spinner spinner;
	private EditText eFontSize;
	private Spinner spBackgroundColor;
	private CheckBox chkPassword;
	private EditText edtPassword;
	private EditText edtSecretQ;
	private EditText edtSecretA;
	private static String sSeed = "nh'fd-=l;[]zv";
	
	@Override
    protected void onResume() {
		super.onResume();
		Common.CheckLogin(this); 
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutSettings );  
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
		} catch (Exception e){}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.CheckLogin(this); 
        setContentView(R.layout.settings);
        
        String[] menuitems = getResources().getStringArray(R.array.optionsmenu_array);
	    this.setTitle(menuitems[2]);
	    
	    String[] measureitems = getResources().getStringArray(R.array.spnMeasures_array);
    			
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, measureitems);

		spinner = (Spinner) findViewById(R.id.spnMeasures);
		spinner.setAdapter(spinnerArrayAdapter);
		
		String[] coloritems = getResources().getStringArray(R.array.backgroundColors_array);
		
		ArrayAdapter<String> spinnerColorsArrayAdapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, coloritems);

		spBackgroundColor = (Spinner) findViewById(R.id.spnBackgroundColor);
		spBackgroundColor.setAdapter(spinnerColorsArrayAdapter);
				
		eFontSize = (EditText) findViewById(R.id.edtFontSize);
		chkPassword = (CheckBox) findViewById(R.id.chkPasswordEnabled);
		edtPassword = (EditText) findViewById(R.id.edtPassword);
		edtSecretQ = (EditText) findViewById(R.id.edtSecretQuestion);
		edtSecretA = (EditText) findViewById(R.id.edtSecretAnswer);
		
		final DBAdapter db = new DBAdapter(this);
		db.open();
		Cursor c = db.getAllSettings();
        int rownum = c.getCount(); 
		if (c.moveToFirst())
        {
			do {
				eFontSize.setText("16");
				String sSettingName = c.getString(1);
				String sSettingValue = c.getString(2);
			
				if (sSettingName.trim().equals("unitsMeasure")){
					int iUnitMeasure = Integer.parseInt(sSettingValue);
					try {
						spinner.setSelection(iUnitMeasure);
					} catch (Exception e){
						spinner.setSelection(0);
					}
				}
			
				if (sSettingName.trim().equals("FontSize")){
					try {
						eFontSize.setText(sSettingValue);
					} catch (Exception e) {
						eFontSize.setText("16");
					}
				}
				
				if (sSettingName.trim().equals("BackgroundColor")){
					try {
						int iPos = 0;
						for(int i = 0; i < spBackgroundColor.getCount(); i++) {
							if (spBackgroundColor.getItemAtPosition(i).toString().equals(sSettingValue)) {
							      iPos = i;
							}
						}
						spBackgroundColor.setSelection(iPos);
					} catch (Exception e){
						spBackgroundColor.setSelection(0);
					}
				}
				
				if (sSettingName.trim().equals("PasswordEnabled")){
					try {
						if (sSettingValue.equalsIgnoreCase("1")){
							chkPassword.setChecked(true);
						} else{
							chkPassword.setChecked(false);
						}
					
					}catch (Exception e) {
						chkPassword.setChecked(false);
					}
				}
				
				if (sSettingName.trim().equals("Password")){
					try {
						if (sSettingValue != ""){
							edtPassword.setText(Common.decrypt(sSeed, sSettingValue));
						} else{
							edtPassword.setText("");
						}
					
					}catch (Exception e) {
						edtPassword.setText("");
					}
				}
				
				if (sSettingName.trim().equals("SecretQuestion")){
					try {
						if (sSettingValue != ""){
							edtSecretQ.setText(sSettingValue);
						} else{
							edtSecretQ.setText("");
						}
					
					}catch (Exception e) {
						edtSecretQ.setText("");
					}
				}
				
				if (sSettingName.trim().equals("SecretAnswer")){
					try {
						if (sSettingValue != ""){
							edtSecretA.setText(sSettingValue);
						} else{
							edtSecretA.setText("");
						}
					
					}catch (Exception e) {
						edtSecretA.setText("");
					}
				}
				
				rownum--;
			} while (rownum >= 0 && c.moveToNext());
			
			
        }
        db.close();
	}
	
	public void saveSettings(View v){
		spinner = (Spinner) findViewById(R.id.spnMeasures);
		spBackgroundColor = (Spinner) findViewById(R.id.spnBackgroundColor);
		String sBColor = spBackgroundColor.getSelectedItem().toString();
		int iUnits = spinner.getSelectedItemPosition();
		String sUnits = Integer.toString(iUnits);
		eFontSize = (EditText) findViewById(R.id.edtFontSize);
		chkPassword = (CheckBox) findViewById(R.id.chkPasswordEnabled);
		String sChk = "0";
		if (chkPassword.isChecked()){
			sChk = "1";
		}
		edtPassword = (EditText) findViewById(R.id.edtPassword);
		String sPassword = edtPassword.getText().toString();
		int iPassLength = sPassword.length();
		try{
			sPassword = Common.encrypt(sSeed,sPassword.toLowerCase());
		} catch (Exception ex) {
					
		}
		edtSecretQ = (EditText) findViewById(R.id.edtSecretQuestion);
		String sSecretQ = edtSecretQ.getText().toString();
		edtSecretA = (EditText) findViewById(R.id.edtSecretAnswer);
		String sSecretA = edtSecretA.getText().toString();
		
		final AlertDialog ad = new AlertDialog.Builder(SettingsActivity.this).create();
		try {		
			DBAdapter db = new DBAdapter(this);
			db.open();
			db.deleteAllSettings();
			db.insertSettings("unitsMeasure",sUnits);
			db.insertSettings("FontSize", eFontSize.getText().toString());
			db.insertSettings("BackgroundColor", sBColor);
			db.insertSettings("PasswordEnabled", sChk);
			db.insertSettings("Password", sPassword);
			db.insertSettings("PasswordLength", String.valueOf(iPassLength));
			db.insertSettings("SecretQuestion", sSecretQ);
			db.insertSettings("SecretAnswer", sSecretA);
			db.close();
			
			String titleSaved = getResources().getString(R.string.alertSaved);
        	String alertSettingsSaved = getResources().getString(R.string.alertSettingsSaved);
        	ad.setTitle(titleSaved);
        	ad.setMessage(alertSettingsSaved); 
        	ad.setButton("OK", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialog, int which) {
        		      ad.dismiss();
        		   }
        		});
        	ad.show();
		} catch (Exception ex){
			String titleOops = getResources().getString(R.string.alertOops);
        	String alertSettingsSaved = getResources().getString(R.string.alertUnableSave);
        	ad.setTitle(titleOops);
        	ad.setMessage(alertSettingsSaved); 
        	ad.setButton("OK", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialog, int which) {
        		      ad.dismiss();
        		   }
        		});
        	ad.show();
        	
        }
		onResume();
        
	}
}


package com.ravenmistmedia.MyHealthRecords;

import java.util.ArrayList;

import com.ravenmistmedia.MyHealthRecords.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.MemoryInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class StartupActivity extends Activity {
	int requestCode;
	private static String sSeed = "nh'fd-=l;[]zv";
	
	@Override
	public void onBackPressed() {
		finish();
	    return;
	}
	
	@Override
    protected void onResume() {
		super.onResume();
		try {
			LinearLayout l = (LinearLayout) findViewById( R.id.LayoutStartup );  
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
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // if password is not enabled, then show main
    	final DBAdapter db = new DBAdapter(this);
    	db.open();
    	db.insertSettings("LoginHasntRun","1");	
    	String sPasswordEnabled = db.getSetting("PasswordEnabled");
        if (!sPasswordEnabled.equalsIgnoreCase("1")){
        	db.insertSettings("LoginHasntRun","0");		
        	StartMain();            	
            }
    		db.close();
      
        setContentView(R.layout.startup);       
 	}
	
	
	
	public void VerifyPassword(View v){
		final DBAdapter db = new DBAdapter(this);
		db.open();
		EditText edtPassword = (EditText) findViewById(R.id.edtPasswordVerify);
		String sPassword = edtPassword.getText().toString();
		sPassword = sPassword.toLowerCase();
		String sPasswordDB = "";
		try {
			sPasswordDB = Common.decrypt(sSeed, db.getSetting("Password"));
		} catch (Exception ex){
			
		}
		
		if (sPassword.equalsIgnoreCase(sPasswordDB))
		{
			db.insertSettings("LoginHasntRun","0");	
			StartMain();
		} else {
			Toast toast = Toast.makeText(this, "Invalid Password.  Try again.", Toast.LENGTH_LONG);
			toast.show();
			edtPassword.setText("");
			//StartMain();
		}
		db.close();
	}
	
	public void ForgotPassword(View v){
		
		final Dialog dialog = new Dialog(StartupActivity.this);
		dialog.setContentView(R.layout.forgotpassword);
    	dialog.setTitle(getResources().getString(R.string.SecretQuestion));
    	dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	LinearLayout l = (LinearLayout) dialog.findViewById( R.id.LayoutForgotInput );
    	final DBAdapter db = new DBAdapter(this);
        db.open();
        db.insertSettings("ForgottenTries","0");
    	String s = db.getSetting("BackgroundColor");
        String sq = db.getSetting("SecretQuestion");
        final String sa = db.getSetting("SecretAnswer");
        
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
    	TextView t = (TextView) dialog.findViewById(R.id.txtSecretQuestion);
    	t.setText(sq);
    	
    	Button dialogConfirm = (Button) dialog.findViewById(R.id.btnConfirmForgot);
    	final Button dialogUnlockHelp = (Button) dialog.findViewById(R.id.btnUnlockHelp);
    	final EditText e = (EditText) dialog.findViewById(R.id.edtSecretAnswerQ); 
    	dialogUnlockHelp.setVisibility(View.INVISIBLE);
    	    	
    	dialogConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String sa2 = e.getText().toString();
                if (sa2.equalsIgnoreCase(sa)){
                	Toast toast = Toast.makeText(dialog.getContext(),"Good Answer.  Now, go to Settings and adjust your password settings.", Toast.LENGTH_LONG);
                	toast.show();
                	db.open();
                	db.insertSettings("ForgottenTries","0");
                	db.insertSettings("LoginHasntRun","0");	
                	db.close();
                	StartMain();
                } else {
                	Toast toast = Toast.makeText(dialog.getContext(),"Wrong Answer", Toast.LENGTH_LONG);
                	e.setText("");
                	toast.show();
                	db.open();
                	String sTries = db.getSetting("ForgottenTries");
                	if (sTries == null || sTries == ""){
                		db.insertSettings("ForgottenTries", "2");
                		sTries = "2";
                	}
                	int iTries = Integer.parseInt(sTries);
                	if (iTries < 2){
                		iTries++;
                		sTries = String.valueOf(iTries);
                		db.insertSettings("ForgottenTries", sTries);
                	} else {
                		sTries = "3";
                		db.insertSettings("ForgottenTries", sTries);
                		dialogUnlockHelp.setVisibility(View.VISIBLE);	
                	}
                	db.close();
                	
                }
            }
        });
    	
    	dialogUnlockHelp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                	final DBAdapter db = new DBAdapter(StartupActivity.this);
                	db.open();
                	db.insertSettings("ForgottenTries","0");
                	db.close();
					String saEnc = Common.encrypt(sSeed, sa);
					String sSubject = getResources().getString(R.string.helpme);
					String sBody = getResources().getString(R.string.helpmebody);
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
			    	  
			    	String aEmailList[] = { "ravenmistmedia+android@gmail.com" };  
			    	  
			    	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);  
			    	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, sSubject);  
			    	
			    	ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			    	MemoryInfo mi = new MemoryInfo();
			    	activityManager.getMemoryInfo(mi);
			    	
					String msgBody = sBody + "\n\n--START ENCRYPTION--" + saEnc + "\n--END ENCRYPTION--";
			    	    	
			    	emailIntent.setType("plain/text");  
			    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msgBody);  
			    	  
			    	startActivity(Intent.createChooser(emailIntent, "Send your email in:"));  
				} catch (Exception e) {
					
				}
                
            }
        });
    	
    	dialog.show();
    	
	}
	
	private void StartMain(){
		final DBAdapter db = new DBAdapter(StartupActivity.this);
    	db.open();
    	String sMessage = db.updateDatabase();
    	if (sMessage != "success")
    	{
    		sMessage = sMessage + "\nAn error has occurred in your database update.\nPlease contact the developer.";
    		Toast toast = Toast.makeText(null, sMessage, requestCode);
    		toast.show();
    	}
    	db.close();
		
		
		Intent myIntentPassword;
    	String packageName = getResources().getString(R.string.package_name);
      	String className = "MainActivity";
      	
      	myIntentPassword = new Intent();
      	myIntentPassword.setComponent(new ComponentName(
      			packageName,
      			packageName + "." + className));
      	myIntentPassword.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
      	
      	startActivityForResult(myIntentPassword, requestCode);
	
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            finish();
    }
}
	

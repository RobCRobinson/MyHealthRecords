package com.ravenmistmedia.MyHealthRecords;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;



public class AboutActivity extends Activity {
	
	private static StringBuffer SYSinfoBuffer;
	
	@Override
    protected void onResume() {
		super.onResume();
		Common.CheckLogin(this);  
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutAbout );  
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
        setContentView(R.layout.about);
        
        String[] menuitems = getResources().getStringArray(R.array.optionsmenu_array);
	    this.setTitle(menuitems[3]);
        TextView tAbout = (TextView) findViewById(R.id.AboutText03);
        tAbout.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    public void FeedbackClick(View v){
    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
    	  
    	String aEmailList[] = { "ravenmistmedia+android@gmail.com" };  
    	  
    	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);  
    	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android App - My Health Records");  
    	
    	String dWidth = Integer.toString(getWindow().getWindowManager().getDefaultDisplay().getWidth());
    	String dHeight = Integer.toString(getWindow().getWindowManager().getDefaultDisplay().getHeight());
    	String sAppVersion = getResources().getString(R.string.version);
    	ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    	MemoryInfo mi = new MemoryInfo();
    	activityManager.getMemoryInfo(mi);
    	

    	
    	SYSinfoBuffer = new StringBuffer();
    	SYSinfoBuffer.append("My Health Records: " + sAppVersion + "\n");
    	SYSinfoBuffer.append("Device: " + android.os.Build.DEVICE + "\n");
    	SYSinfoBuffer.append("Build: " + android.os.Build.VERSION.RELEASE + "\n");
    	SYSinfoBuffer.append("Brand: " + android.os.Build.BRAND + "\n");
    	SYSinfoBuffer.append("Hardware: " + android.os.Build.HARDWARE + "\n");
    	SYSinfoBuffer.append("Manufacturer: " + android.os.Build.MANUFACTURER + "\n");
    	SYSinfoBuffer.append("Model: " + android.os.Build.MODEL + "\n");
    	SYSinfoBuffer.append("Product: " + android.os.Build.PRODUCT + "\n");
    	SYSinfoBuffer.append("Memory: " + mi.availMem + " low:" + Boolean.toString(mi.lowMemory) + "\n");
    	SYSinfoBuffer.append("Display Size: Width - " + dWidth + " Height - " + dHeight + "\n\n\n");
    			

		String msgBody = SYSinfoBuffer.toString();
    	    	
    	emailIntent.setType("plain/text");  
    	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msgBody);  
    	  
    	startActivity(Intent.createChooser(emailIntent, "Send your email in:"));  
    }
    
    public void UserManualClick(View v){
    	// Google
    	String sUserManual = getResources().getString(R.string.linkUserManual);
    	
    	// Amazon
    	//String sUserManual = getResources().getString(R.string.linkUserManualRaven);
    	
    	Intent browserIntent = 
                new Intent(Intent.ACTION_VIEW, Uri.parse(sUserManual));
    	startActivity(browserIntent);
    }
    
    
}

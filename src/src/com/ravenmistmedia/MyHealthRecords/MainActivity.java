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
import java.util.ArrayList;
import java.util.List;

import com.ravenmistmedia.MyHealthRecords.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends Activity {
	
	private ListView mainListView ;
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
		
	@Override
    protected void onResume() {
		super.onResume();
		try {
		mainListView = (ListView) findViewById( R.id.mainListView );  
        final DBAdapter db = new DBAdapter(this);
        db.open();
        String s = db.getSetting("BackgroundColor");
        Common.CheckLogin(this);        
        db.close();
        if(s.equals("")|| s.equals("Blue")){
        	mainListView.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	mainListView.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	mainListView.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	mainListView.setBackgroundResource(R.drawable.backrepeat4);
        } 	 
		} catch (Exception e){}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	Common.CheckLogin(this);  
    	Intent in = new Intent();
        setResult(1,in);
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);       
                
        mainListView = (ListView) findViewById( R.id.mainListView );  
          
        ArrayList<MenuResults> menuResults = new ArrayList<MenuResults>();
        
        String[] menuitems = getResources().getStringArray(R.array.mainmenu_array);
        String[] descriptions = getResources().getStringArray(R.array.mainmenu_descriptions_array);
        
        int len = menuitems.length;
        for (int i = 0; i < len; ++i) {
            MenuResults mr1 = new MenuResults();
            mr1.setMenuItem(menuitems[i].toString());
            mr1.setMenuDescription(descriptions[i].toString());
            menuResults.add(mr1);
        }
        
	    final String[] activities = getResources().getStringArray(R.array.mainmenuactivities_array);
	        	      
	    mainListView.setAdapter(new MyCustomBaseAdapter(this,menuResults)); 
	    
	    mainListView.setOnItemClickListener(new OnItemClickListener() {
	    	public void onItemClick(AdapterView<?> parent, View view,
	    	        int position, long id) {
	    	      	Intent myIntent;
	    	      	
	    	      	String packageName = getResources().getString(R.string.package_name);
	    	      	String className = activities[position];
	    	      	if (className.equalsIgnoreCase("Exit"))	{
	    	      		final DBAdapter db = new DBAdapter(MainActivity.this);
	    	            db.open();
	    	            db.insertSettings("LoginHasntRun","1");	
	    	            db.close();
	    	            finish();
	    	      		return;
	    	      	}
	    	      	
	    	      	myIntent = new Intent();
	    	      	myIntent.setComponent(new ComponentName(
	    	      			packageName,
	    	      			packageName + "." + className));
	    	      	
	    	      	startActivity(myIntent);
	    	      
	    	    }
	    	  });
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settingsmenu, menu);
	    return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	final DBAdapter db = new DBAdapter(this);
    	Intent myIntent;
      	
      	String packageName = getResources().getString(R.string.package_name);
      	String className = "MainActivity";	
      	
    	switch (item.getItemId()) {
	        case R.id.editpersons:
	        	className = "PersonsActivity";
	        	break;
	        case R.id.dbadmin:
	        	className = "DBAdminActivity";
	        	break;
	        case R.id.editsettings:
	        	className = "SettingsActivity";
	        	break;
	        case R.id.editabout:
	        	className = "AboutActivity";
	        	break;
	    }
    	
    	myIntent = new Intent();
      	myIntent.setComponent(new ComponentName(
      			packageName,
      			packageName + "." + className));
      	
      	startActivity(myIntent);
	    return true;
	}
}
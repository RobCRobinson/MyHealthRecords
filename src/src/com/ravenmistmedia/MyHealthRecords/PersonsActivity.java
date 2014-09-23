package com.ravenmistmedia.MyHealthRecords;

import com.ravenmistmedia.MyHealthRecords.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class PersonsActivity extends Activity {
	
	Common common = new Common();
	private Spinner spinner;
	private static String sSeed = "nh'fd-=l;[]zv";
	String s = "Blue";
	private Button dialogmSave;
	
	@Override
    protected void onResume() {
		super.onResume();
		Common.CheckLogin(this); 
		try {
		LinearLayout l = (LinearLayout) findViewById( R.id.LayoutPersons );  
		LinearLayout l2 = (LinearLayout) findViewById( R.id.LayoutPersonsOptions ); 
        final DBAdapter db = new DBAdapter(this);
        db.open();
        String s = db.getSetting("BackgroundColor");
        db.close();
        if(s.equals("")|| s.equals("Blue")){
        	l.setBackgroundResource(R.drawable.backrepeat);
        	l2.setBackgroundResource(R.drawable.backrepeat);
        } else if (s.equals("Lavender")){
        	l.setBackgroundResource(R.drawable.backrepeat2);
        	l2.setBackgroundResource(R.drawable.backrepeat2);
        } else if (s.equals("Peach")){
        	l.setBackgroundResource(R.drawable.backrepeat3);
        	l2.setBackgroundResource(R.drawable.backrepeat3);
        } else if (s.equals("Green")){
        	l.setBackgroundResource(R.drawable.backrepeat4);
        	l2.setBackgroundResource(R.drawable.backrepeat4);
        } 	 
		} catch (Exception e){}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.CheckLogin(this); 
        setContentView(R.layout.persons);
        
        String[] menuitems = getResources().getStringArray(R.array.optionsmenu_array);
	    this.setTitle(menuitems[0]);
	    
	    bindList();
		
	}
	
    public void addPerson(View v){
    	
    	final Dialog dialog1 = new Dialog(PersonsActivity.this);
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
            	DBAdapter db = new DBAdapter(v.getContext());
                db.open();
                long l = db.insertPerson(sPersonName);
            	if (l != -1){
            		int personid = db.FindPerson(sPersonName);
            		db.setCurrentPerson(personid);
                	dialog1.dismiss();
                	String displaythis2 = getResources().getString(R.string.txtAdding) + " "  + sPersonName;
                	Toast.makeText(getApplicationContext(), displaythis2, Toast.LENGTH_SHORT).show();
        			bindList();
                }
                   
            }
        });
    	
    	dialog1.show();
    }
    
    public void changeName(View v){
    	DBAdapter db = new DBAdapter(this);
    	spinner = (Spinner) findViewById(R.id.spnPersons);
    	String personid = null;
    	String personname = null;
    	Cursor cc = (Cursor)(spinner.getSelectedItem());
    	if (cc != null) {
    	    personid = cc.getString(cc.getColumnIndex(db.KEY_ROWID));
    	    personname = cc.getString(cc.getColumnIndex("personname"));
    	    String sChangeName = getResources().getString(R.string.buttonChangeName);
    	    
    	    final Dialog dialog1 = new Dialog(PersonsActivity.this);
        	dialog1.setContentView(R.layout.personinput);
        	dialog1.setTitle(sChangeName);
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
        	final String sPersonNameOld = personname;
        	final String sPersonId = personid;
        	
        	final EditText dialogmPersonName = (EditText) dialog1.findViewById(R.id.edtInputPersonName);
        	final Button dialogmButtonSave = (Button) dialog1.findViewById(R.id.btnSavePerson);
        	dialogmPersonName.setText(personname);
        	
        	dialogmButtonSave.setText(sChangeName);
        	dialogmSave = (Button) dialog1.findViewById(R.id.btnSavePerson);
        	dialogmSave.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	DBAdapter db = new DBAdapter(v.getContext());
                    db.open();
                    String sPersonName = dialogmPersonName.getText().toString();
                    String sTo = getResources().getString(R.string.txtTo);
                    
                		db.renamePerson(Integer.parseInt(sPersonId), sPersonName);
                		dialog1.dismiss();
                    	String displaythis2 = getResources().getString(R.string.txtRenaming) + " "  + sPersonNameOld + " " + sTo + " " + sPersonName;
                    	Toast.makeText(getApplicationContext(), displaythis2, Toast.LENGTH_SHORT).show();
            			bindList();
                    
                       
                }
            });
        	
        	dialog1.show();
    	}
    	
    }
    
    public void setDefault(View v){
    	DBAdapter db = new DBAdapter(this);
    	spinner = (Spinner) findViewById(R.id.spnPersons);
    	String personid = null;
    	String personname = null;
    	Cursor cc = (Cursor)(spinner.getSelectedItem());
    	if (cc != null) {
    	    personid = cc.getString(cc.getColumnIndex(db.KEY_ROWID));
    	    personname = cc.getString(cc.getColumnIndex("personname"));
    	    db.open();
    	    String displaythis = getResources().getString(R.string.txtSwitching) + " " + personname;
    	    db.setCurrentPerson(Integer.parseInt(personid));
    	    Toast.makeText(getApplicationContext(), displaythis, Toast.LENGTH_SHORT).show();
    	    db.close();
    	}
    }
    
    public void deletePerson(View v){
    	DBAdapter db = new DBAdapter(this);
    	spinner = (Spinner) findViewById(R.id.spnPersons);
    	String personid = null;
    	String personname = null;
    	Cursor cc = (Cursor)(spinner.getSelectedItem());
    	if (cc != null) {
    	    personid = cc.getString(cc.getColumnIndex(db.KEY_ROWID));
    	    personname = cc.getString(cc.getColumnIndex("personname"));
    	    db.open();
    	    Cursor cd = db.getAllPersons();
    	    if (cd == null || cd.getCount() < 2){
    	    	String di = getResources().getString(R.string.txtCantDelete);
    	    	Toast.makeText(getApplicationContext(), di, Toast.LENGTH_SHORT).show();
    	    } else {
    	        String displaythis = getResources().getString(R.string.txtDeleting) + " " + personname;
    	        db.deletePerson(Integer.parseInt(personid));
    	        Toast.makeText(getApplicationContext(), displaythis, Toast.LENGTH_SHORT).show();
    	    }
    	    db.close();
    	    bindList();
    	}
    }
    
    private void bindList(){
    	DBAdapter db = new DBAdapter(this);
	    db.open();
	    Cursor c = db.getAllPersons();
	    startManagingCursor(c);
	    String[] from = new String[]{"personname"};
	    int[] to = new int[]{android.R.id.text1};
	    SimpleCursorAdapter adapter = 
	    		new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, from, to );
	    adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
	 	 
	    spinner = (Spinner) findViewById(R.id.spnPersons);
		spinner.setAdapter(adapter);
		db.close();
    }
	
}


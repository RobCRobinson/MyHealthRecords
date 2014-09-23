package com.ravenmistmedia.MyHealthRecords;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ravenmistmedia.MyHealthRecords.R.array;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ParseException;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DBAdapter {
	public static final String KEY_ROWID = "_id";
	private static final String TAG = "MyHealthRecords";
	private static final String DATABASE_NAME = "MyHealthRecords";
	private static final int DATABASE_VERSION = 1;
	private final Context context; 
	private DatabaseHelper DBHelper;
    private static SQLiteDatabase db;
    private Exporter _exporter;
    private Context _ctx;
    private static String EXPORT_FILE_NAME = "";
    private static String IMPORT_FILE_NAME = "";
    
    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
	
	// Tables
	public static final String DATABASE_TABLE_HISTORY = "history";
	public static final String DATABASE_TABLE_MEDICATIONS = "medications";
	public static final String DATABASE_TABLE_WEIGHTS = "weights";
	public static final String DATABASE_TABLE_TEMPERATURES = "temperatures";
	public static final String DATABASE_TABLE_SETTINGS = "settings";
	public static final String DATABASE_TABLE_BPPULSES = "bppulses";
	public static final String DATABASE_TABLE_SUGARS = "sugars";
	public static final String DATABASE_TABLE_PERSONS = "persons";
	public static final String DATABASE_TABLE_TESTS = "tests";
	public static final String DATABASE_TABLE_VISITS = "visits";
	public static final String DATABASE_TABLE_OFFICES = "offices";
	public static final String DATABASE_TABLE_DOCUMENTS = "documents";
	
	// fieldtype arrays
	String[][] historyArray = {{"_id","integer"},{"historydate","text"},{"history","text"},{"personid","integer"}};
	String[][] medicationsArray = {{"_id","integer"},{"medication","text"},{"medsstartdate","text"},{"medsenddate","text"},{"adverse","text"},{"personid","integer"}};
	String[][] weightsArray = {{"_id","integer"},{"height","real"},{"weight","real"},{"notes","text"},{"recdate","number"},{"personid","integer"}};
	String[][] temperaturesArray = {{"_id","integer"},{"temperature","real"},{"notes","text"},{"recdate","number"},{"personid","integer"}};
	String[][] bppulsesArray = {{"_id","integer"},{"systolic","number"},{"diastolic","number"},{"pulse","number"},{"notes","text"},{"recdate","number"},{"personid","integer"}};
	String[][] sugarsArray = {{"_id","integer"},{"bloodglucose","number"},{"prepost","integer"},{"medication","text"},{"mealtype","number"},{"foods","text"},{"notes","text"},{"recdate","number"},{"personid","integer"}};
	String[][] personsArray = {{"_id","integer"},{"personname","text"},{"currentuser","integer"}};
	String[][] testsArray = {{"_id","integer"},{"testdate","number"},{"testname","text"},{"testresult","text"},{"notes","text"},{"personid","integer"}};
	String[][] visitsArray = {{"_id","integer"},{"visitdate","number"},{"officeid","integer"},{"visithappened","text"},{"visitnotes","text"},{"personid","integer"}};
	String[][] officesArray = {{"_id","integer"},{"officename","text"},{"officehours","text"},{"officenotes","text"},{"contactlookupkey","text"},{"contactid","number"},{"personid","integer"}};
	String[][] documentsArray = {{"_id","integer"},{"filename","text"},{"notes","text"},{"recdate","number"},{"personid","integer"}};
		
	// Create statements
	private static final String DATABASE_CREATE_TABLE_HISTORY =
        "create table if not exists " + DATABASE_TABLE_HISTORY + " (_id integer primary key autoincrement, "
        + "historydate text, history text,personid integer not null);";

	private static final String DATABASE_CREATE_TABLE_MEDICATIONS =
        "create table if not exists " + DATABASE_TABLE_MEDICATIONS + " (_id integer primary key autoincrement, "
        + "medication text, medsstartdate text, medsenddate text, adverse text, personid integer not null);";
	
	private static final String DATABASE_CREATE_TABLE_WEIGHTS =
        "create table if not exists " + DATABASE_TABLE_WEIGHTS + " (_id integer primary key autoincrement, "
        + "height real not null, weight real not null, " 
        + "notes text, recdate number not null,personid integer not null);";
	
	private static final String DATABASE_CREATE_TABLE_TEMPERATURES =
	        "create table if not exists " + DATABASE_TABLE_TEMPERATURES + " (_id integer primary key autoincrement, "
	        + "temperature real not null, " 
	        + "notes text, recdate number not null,personid integer not null);";
    
	private static final String DATABASE_CREATE_TABLE_SETTINGS =
        "create table if not exists " + DATABASE_TABLE_SETTINGS + " (_id integer primary key autoincrement, "
        + "settingsname text not null, settingsvalue text not null);";

	private static final String DATABASE_CREATE_TABLE_BPPULSES =
        "create table if not exists " + DATABASE_TABLE_BPPULSES + " (_id integer primary key autoincrement, "
        + "systolic number not null, diastolic number not null, " 
        + "pulse number not null, notes text, recdate number not null,personid integer not null);";
    
	private static final String DATABASE_CREATE_TABLE_SUGARS =
		"create table if not exists " + DATABASE_TABLE_SUGARS + " (_id integer primary key autoincrement, "
        + "bloodglucose number not null,  prepost integer not null, " 
        + "medication text not null, mealtype number not null, "
        + "foods text not null, notes text, recdate number not null,personid integer not null);";	
	
	private static final String DATABASE_CREATE_TABLE_PERSONS =
			"create table if not exists " + DATABASE_TABLE_PERSONS + " (_id integer primary key autoincrement, "
			+ "personname text not null, currentuser integer not null);";
	
	private static final String DATABASE_CREATE_TABLE_TESTS = 
		"create table if not exists " + DATABASE_TABLE_TESTS + " (_id integer primary key autoincrement, "
		+ "testdate number not null, testname text not null, testresult text not null, notes text, personid integer not null);";
	
	private static final String DATABASE_CREATE_TABLE_VISITS =
			"create table if not exists " + DATABASE_TABLE_VISITS + " (_id integer primary key autoincrement, "
					+ "visitdate number, officeid number, visithappened text, visitnotes text, personid integer not null);";
	
	private static final String DATABASE_CREATE_TABLE_OFFICES =
			"create table if not exists " + DATABASE_TABLE_OFFICES + " (_id integer primary key autoincrement, "
					+ "officename text not null, officehours text, officenotes text, contactlookupkey text, contactid integer, personid integer not null);";
	
	private static final String DATABASE_CREATE_TABLE_DOCUMENTS =
	        "create table if not exists " + DATABASE_TABLE_DOCUMENTS + " (_id integer primary key autoincrement, "
	        + "filename text not null, " 
	        + "notes text, recdate number not null,personid integer not null);";	
	
	
	// Helper
	private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	db.execSQL(DATABASE_CREATE_TABLE_HISTORY);
        	db.execSQL(DATABASE_CREATE_TABLE_MEDICATIONS);
        	db.execSQL(DATABASE_CREATE_TABLE_TEMPERATURES);
        	db.execSQL(DATABASE_CREATE_TABLE_WEIGHTS);
        	db.execSQL(DATABASE_CREATE_TABLE_SETTINGS);
        	db.execSQL(DATABASE_CREATE_TABLE_BPPULSES);
        	db.execSQL(DATABASE_CREATE_TABLE_SUGARS);
        	db.execSQL(DATABASE_CREATE_TABLE_PERSONS);
        	db.execSQL(DATABASE_CREATE_TABLE_TESTS);
        	db.execSQL(DATABASE_CREATE_TABLE_VISITS);
        	db.execSQL(DATABASE_CREATE_TABLE_OFFICES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MEDICATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_WEIGHTS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TEMPERATURES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_BPPULSES);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SUGARS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PERSONS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TESTS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_VISITS);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_OFFICES);
            
            onCreate(db);
        }
    } 
	
	// Open the database
    public DBAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        db.execSQL(DATABASE_CREATE_TABLE_HISTORY);
    	db.execSQL(DATABASE_CREATE_TABLE_MEDICATIONS);
    	db.execSQL(DATABASE_CREATE_TABLE_WEIGHTS);
    	db.execSQL(DATABASE_CREATE_TABLE_TEMPERATURES);
    	db.execSQL(DATABASE_CREATE_TABLE_SETTINGS);
    	db.execSQL(DATABASE_CREATE_TABLE_BPPULSES);
    	db.execSQL(DATABASE_CREATE_TABLE_SUGARS);
    	db.execSQL(DATABASE_CREATE_TABLE_PERSONS);
    	db.execSQL(DATABASE_CREATE_TABLE_TESTS);
    	db.execSQL(DATABASE_CREATE_TABLE_VISITS);
    	db.execSQL(DATABASE_CREATE_TABLE_OFFICES);
    	db.execSQL(DATABASE_CREATE_TABLE_DOCUMENTS);
    	
    	return this;
    }

    // Close the database    
    public void close() 
    {
        DBHelper.close();
    }
    
    // update the database for this version - runs only once
    public String updateDatabase(){
    	int persId = getCurrentPersonId();
    	String UpdateComplete = "success";
    	// version 1.6.2 -- if there's not a person if 0, then set all records in all tables with personid of 0 to 1
    	try {
    		Cursor c = getAllPersons();
    		if (c.getCount()>0){
    			c.moveToFirst();
    			int personid = c.getInt(0);
    			String sql = "update " + DATABASE_TABLE_HISTORY + " set personid=" + personid + " where personid=0";
    			
    			db.execSQL(sql);
    			sql = "update " + DATABASE_TABLE_MEDICATIONS + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    			
    			sql = "update " + DATABASE_TABLE_WEIGHTS + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    			
    			sql = "update " + DATABASE_TABLE_BPPULSES + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    			
    			sql = "update " + DATABASE_TABLE_SUGARS + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    			
    			sql = "update " + DATABASE_TABLE_TESTS + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    			
    			sql = "update " + DATABASE_TABLE_VISITS + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    			
    			sql = "update " + DATABASE_TABLE_OFFICES + " set personid=" + personid + " where personid=0";
    			db.execSQL(sql);
    		}
    		
    	} catch (Exception ex){
    		// ignore
    	}
    	
    	// version 1.6 - multiple persons (new table and update all other tables with personid field set to 0)
    	try {
    		Cursor c = getAllPersons();
    		if (c.getCount()<1){
    			long d = insertPerson("Primary Person");
    			int personid = FindPerson("Primary Person");
    			setCurrentPerson(personid);
    			persId = getCurrentPersonId();
    		}
    		
    	} catch (Exception ex){
    		if (ex.toString().contains("column: personname")){
    			long d = insertPerson("Primary Person");
    			int personid = FindPerson("Primary Person");
    			setCurrentPerson(personid);
    		}
    	}
    	
    			
    	// version 1.5 - history table has new field historydate
    	// version 1.6 - history table has new field personid
    	try {
    		Cursor c = getAllHistory();
     		
    	}catch (Exception ex){
    		if (ex.toString().contains("column: historydate")){
    			String alterTable =  "ALTER TABLE " + DATABASE_TABLE_HISTORY + " ADD historydate CHAR(30) DEFAULT '' NOT NULL";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    		}
    		if (ex.toString().contains("column: personid")){
    			
    			String alterTable =  "ALTER TABLE " + DATABASE_TABLE_HISTORY + " ADD personid integer DEFAULT " + persId + " NOT NULL";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			
    			alterTable =  "UPDATE " + DATABASE_TABLE_HISTORY + " SET personid= " + persId;
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    		}
    	}
    	
    	// version 1.6 - medications table has new fields personid, medsstartdate, medsenddate, adverse
    	try {
    		Cursor c = getAllMedications();
     		
    	}catch (Exception ex){
    		if (ex.toString().contains("column: personid") ||
    				ex.toString().contains("column: medsstartdate") ||
    				ex.toString().contains("column: medsenddate") ||
    				ex.toString().contains("column: adverse") 
    				){
    			String alterTable =  "ALTER TABLE " + DATABASE_TABLE_MEDICATIONS + " ADD personid integer DEFAULT " + persId + " NOT NULL";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			alterTable =  "ALTER TABLE " + DATABASE_TABLE_MEDICATIONS + " ADD medsstartdate text";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			alterTable =  "ALTER TABLE " + DATABASE_TABLE_MEDICATIONS + " ADD medsenddate text";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			alterTable =  "ALTER TABLE " + DATABASE_TABLE_MEDICATIONS + " ADD adverse text";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			
    			alterTable =  "UPDATE " + DATABASE_TABLE_MEDICATIONS + " SET personid= " + persId;
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    		}
    		
    	}
    	
    	// version 1.6 - weights table has new field personid
    	try {
    		Cursor c = getWeightRecords();
     		
    	}catch (Exception ex){
    		if (ex.toString().contains("column: personid")){
    			String alterTable =  "ALTER TABLE " + DATABASE_TABLE_WEIGHTS + " ADD personid integer DEFAULT " + persId + " NOT NULL";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			
    			alterTable =  "UPDATE " + DATABASE_TABLE_WEIGHTS + " SET personid= " + persId;
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    		}
    	}
    	
    	// version 1.6 - bppulses table has new field personid
    	try {
    		Cursor c = getAllBPPulseRecords();
     		
    	}catch (Exception ex){
    		if (ex.toString().contains("column: personid")){
    			String alterTable =  "ALTER TABLE " + DATABASE_TABLE_BPPULSES + " ADD personid integer DEFAULT " + persId + " NOT NULL";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			
    			alterTable =  "UPDATE " + DATABASE_TABLE_BPPULSES + " SET personid= " + persId;
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    		}
    	}
    	
    	// version 1.6 - sugars table has new field personid
    	try {
    		Cursor c = getAllSugarRecords();
     		
    	}catch (Exception ex){
    		if (ex.toString().contains("column: personid")){
    			String alterTable =  "ALTER TABLE " + DATABASE_TABLE_SUGARS + " ADD personid integer DEFAULT " + persId + " NOT NULL";
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    			
    			alterTable =  "UPDATE " + DATABASE_TABLE_SUGARS + " SET personid= " + persId;
    			try {
    				db.execSQL(alterTable);
    			} catch (Exception ex2){
    				if (UpdateComplete.contains("success")){
    					UpdateComplete =  ex2.toString();
    				} else {
    					UpdateComplete = UpdateComplete + "\r\n" + ex2.toString();
    				}
    			}
    		}
    	}
    	    	
    	return UpdateComplete;
    	
    }
        
    // Reset the database and create new tables
    public void dropAndCreateTables(){
    	db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TEMPERATURES);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_BPPULSES);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_SUGARS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PERSONS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TESTS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_VISITS);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_OFFICES);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_DOCUMENTS);
        
    	db.execSQL(DATABASE_CREATE_TABLE_HISTORY);
    	db.execSQL(DATABASE_CREATE_TABLE_MEDICATIONS);
    	db.execSQL(DATABASE_CREATE_TABLE_WEIGHTS);
    	db.execSQL(DATABASE_CREATE_TABLE_TEMPERATURES);
    	db.execSQL(DATABASE_CREATE_TABLE_SETTINGS);
    	db.execSQL(DATABASE_CREATE_TABLE_BPPULSES);
    	db.execSQL(DATABASE_CREATE_TABLE_SUGARS);
    	db.execSQL(DATABASE_CREATE_TABLE_PERSONS);
    	db.execSQL(DATABASE_CREATE_TABLE_TESTS);
    	db.execSQL(DATABASE_CREATE_TABLE_VISITS);
    	db.execSQL(DATABASE_CREATE_TABLE_OFFICES);
    	db.execSQL(DATABASE_CREATE_TABLE_DOCUMENTS);
    	
    	ContentValues initialValues = new ContentValues();
        initialValues.put("personname", "Current User");
        initialValues.put("currentuser", 1);
    	db.insert(DATABASE_TABLE_PERSONS, null, initialValues);
    	
    }
    
    // -- History, Conditions, and Allergies --
    
    public long insertHistory(String DateDetail,String HistoryDetail, int PersonIdDetail) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("historydate", DateDetail);
        initialValues.put("history", HistoryDetail);
        initialValues.put("personid", PersonIdDetail);
        
        return db.insert(DATABASE_TABLE_HISTORY, null, initialValues);
    }
    
    public boolean deleteHistory(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_HISTORY, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public Cursor getAllHistory() 
    {
        return db.query(DATABASE_TABLE_HISTORY, new String[] {
        		KEY_ROWID, 
        		"historydate",
        		"history",
        		"personid"
        		}, 
                null, 
                null, 
                null, 
                null, 
                KEY_ROWID + " asc");
    }
    
    public Cursor getHistoryByPersonId(int personId){
    	Cursor mCursor =
                db.query(true, DATABASE_TABLE_HISTORY, new String[] {
                		KEY_ROWID,
                		"historydate",
                		"history",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"historydate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
        
    public Cursor getHistory(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_HISTORY, new String[] {
                		KEY_ROWID,
                		"historydate",
                		"history",
                		"personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		"historydate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getHistoryByPersonIdandKeyword(int personid,String keyword){
    	Cursor mCursor =
                db.query(true, DATABASE_TABLE_HISTORY, new String[] {
                		KEY_ROWID,
                		"historydate",
                		"history",
                		"personid"
                		}, 
                		"personid=" + personid + " AND (historydate like '%" + keyword + "%' OR history like '%" + keyword + "%')", 
                		null,
                		null, 
                		null, 
                		"historydate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public boolean updateHistory(long rowId, String DateDetail, String HistoryDetail, int PersonIdDetail) 
    {
       	boolean b = false;
        ContentValues args = new ContentValues();
        args.put("historydate", DateDetail);
        args.put("history", HistoryDetail);
        args.put("personid", PersonIdDetail);
        
        try {
         	open();	
          	db.beginTransaction(); 
           	b = db.update(DATABASE_TABLE_HISTORY, args, 
                       KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
       	} catch (SQLException e) {
       		Log.e("Exception on query", e.toString());
       	} finally {
       		db.endTransaction();
       		close();
       	}
       	return b;
    }

// -- Medications and Supplements --
    
    public long insertMedications(String MedicationDetail, String StartDateDetail, String EndDateDetail, String AdverseDetail, int PersonIdDetail) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("medication", MedicationDetail);
        initialValues.put("medsstartdate", StartDateDetail);
        initialValues.put("medsenddate", EndDateDetail);
        initialValues.put("adverse", AdverseDetail);
        initialValues.put("personid", PersonIdDetail);
        
        return db.insert(DATABASE_TABLE_MEDICATIONS, null, initialValues);
    }
    
    public boolean deleteMedications(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_MEDICATIONS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public Cursor getAllMedications() 
    {
        return db.query(DATABASE_TABLE_MEDICATIONS, new String[] {
        		KEY_ROWID, 
        		"medication",
        		"medsstartdate",
        		"medsenddate",
        		"adverse",
        		"personid"
        		}, 
                null, 
                null, 
                null, 
                null, 
                "medication asc");
    }
    
    public Cursor getMedicationsByPersonId(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_MEDICATIONS, new String[] {
                		KEY_ROWID,
                		"medication",
                		"medsstartdate",
                		"medsenddate",
                		"adverse",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"medication asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getMedicationsByPersonIdAndKeyword(int personId, String keyword) throws SQLException 
    {
    	String q = "personid=" + personId + " AND (medication like '%" + keyword + "' " +
    			"OR medsstartdate like '%" + keyword + "%' OR medsenddate like '%" + keyword + "%' " +
    			"OR adverse like '%" + keyword + "%')";
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_MEDICATIONS, new String[] {
                		KEY_ROWID,
                		"medication",
                		"medsstartdate",
                		"medsenddate",
                		"adverse",
                		"personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"medication asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getMedications(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_MEDICATIONS, new String[] {
                		KEY_ROWID,
                		"medication",
                		"medsstartdate",
                		"medsenddate",
                		"adverse",
                		"personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		"medication asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public boolean updateMedications(long rowId, String MedicationDetail, String StartDateDetail, String EndDateDetail, String AdverseDetail, int PersonIdDetail) 
    {
       	boolean b = false;
        ContentValues args = new ContentValues();
        args.put("medication", MedicationDetail);
        args.put("medsstartdate", StartDateDetail);
        args.put("medsenddate", EndDateDetail);
        args.put("adverse",AdverseDetail);
        args.put("personid", PersonIdDetail);
        try {
         	open();	
          	db.beginTransaction(); 
           	b = db.update(DATABASE_TABLE_MEDICATIONS, args, 
                       KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
       	} catch (SQLException e) {
       		Log.e("Exception on query", e.toString());
       	} finally {
       		db.endTransaction();
       		close();
       	}
       	return b;
    }   
    
// -- Settings --
    
    public long insertSettings(String settingsname,String settingsvalue) 
    {
    	long backAgain = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put("settingsname", settingsname);
        initialValues.put("settingsvalue", settingsvalue);
        
        String alreadyPresent = getSetting(settingsname);
        if (alreadyPresent != ""){
        	db.delete(DATABASE_TABLE_SETTINGS, "settingsname=?", new String [] {settingsname});
        	backAgain = db.insert(DATABASE_TABLE_SETTINGS, null, initialValues);
         } else{
        	backAgain = db.insert(DATABASE_TABLE_SETTINGS, null, initialValues);
        }
        
        return backAgain;
    }
    
    public boolean deleteSetting(long rowId) 
    {
        return db.delete(DATABASE_TABLE_SETTINGS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
    }
    
    public boolean deleteAllSettings() 
    {
        return db.delete(DATABASE_TABLE_SETTINGS, null, null) > 0;
    }
    
    public Cursor getAllSettings() 
    {
        return db.query(DATABASE_TABLE_SETTINGS, new String[] {
        		KEY_ROWID, 
        		"settingsname",
        		"settingsvalue"
                }, 
                null, 
                null, 
                null, 
                null, 
                null);
    }
    
    public String getSetting(String settingName){
    	String sSettingValue = "";
    	Cursor mCursor =
            db.query(true, DATABASE_TABLE_SETTINGS, new String[] {
            		KEY_ROWID,
            		"settingsname",
            		"settingsvalue"
            		}, 
            		"settingsname = '" + settingName + "'", 
            		null,
            		null, 
            		null, 
            		null, 
            		null);
    	if (mCursor != null) {
        	if (mCursor.moveToFirst()){
        	int rownum = mCursor.getCount();
        	do  {
        		String sSettingName = mCursor.getString(1);
				if (sSettingName.equals(settingName)){
					sSettingValue = mCursor.getString(2);
        		}
        		rownum--;
        	}while (rownum >= 0 && mCursor.moveToNext());
        	}
    	}
    	return sSettingValue;
    }
    
    public Cursor getSettingByRowId(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_SETTINGS, new String[] {
                		KEY_ROWID,
                		"settingsname",
                		"settingsvalue"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public boolean updateSetting(long rowId, String settingsname, String settingsvalue) 
    {
        ContentValues args = new ContentValues();
        args.put("settingsname", settingsname);
        args.put("settingsvalue", settingsvalue);
        
        return db.update(DATABASE_TABLE_SETTINGS, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
    
// -- Weights Methods -- //   
    
    public long insertWeight(float height, 
    		float weight, String notes, long recdate, int personId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("height", height);
        initialValues.put("weight", weight);
        initialValues.put("notes", notes);
        initialValues.put("recdate", recdate);
        initialValues.put("personid", personId);
        
        return db.insert(DATABASE_TABLE_WEIGHTS, null, initialValues);
    }

    public boolean deleteWeightRecord(long rowId) 
    {
        return db.delete(DATABASE_TABLE_WEIGHTS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
    }
    
    public boolean deleteAllWeightRecords() 
    {
        return db.delete(DATABASE_TABLE_WEIGHTS, null, null) > 0;
    }

    public Cursor getWeightRecords() 
    {
        return db.query(DATABASE_TABLE_WEIGHTS, new String[] {
        		KEY_ROWID, 
        		"height",
        		"weight",
        		"notes",
                "recdate",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "recdate desc");
    }

    public Cursor getWeightRecordsByPersonId(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_WEIGHTS, new String[] {
                		KEY_ROWID,
                		"height", 
                		"weight",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getWeightRecordsByPersonIdAscending(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_WEIGHTS, new String[] {
                		KEY_ROWID,
                		"height", 
                		"weight",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getWeightRecordsByPersonIdAndKeyword(int personId, String keyword) throws SQLException 
    {
    	String q;
    	try {
    		int k = Integer.parseInt(keyword);
    		q = "personid=" + personId + " AND (height=" + k  + " OR weight=" + k + " OR notes like '%" + keyword + "%')";
    	} catch (Exception ex){
    		q = "personid=" + personId + " AND (notes like '%" + keyword + "%')";
    	}
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_WEIGHTS, new String[] {
                		KEY_ROWID,
                		"height", 
                		"weight",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateWeightRecord(long rowId, float height, float weight, 
    		String notes, long recdate, int personId) 
    {
        ContentValues args = new ContentValues();
        args.put("height", height);
        args.put("weight", weight);
        args.put("notes", notes);
        args.put("recdate", recdate);
        args.put("personid", personId);
        return db.update(DATABASE_TABLE_WEIGHTS, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
    
// -- Temperatures Methods -- //   
    
    public long insertTemperature(float temperature, String notes, long recdate, int personId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("temperature", temperature);
        initialValues.put("notes", notes);
        initialValues.put("recdate", recdate);
        initialValues.put("personid", personId);
        
        return db.insert(DATABASE_TABLE_TEMPERATURES, null, initialValues);
    }

    public boolean deleteTemperatureRecord(long rowId) 
    {
        return db.delete(DATABASE_TABLE_TEMPERATURES, KEY_ROWID + 
        		"=" + rowId, null) > 0;
    }
    
    public boolean deleteAllTemperatureRecords() 
    {
        return db.delete(DATABASE_TABLE_TEMPERATURES, null, null) > 0;
    }

    public Cursor getTemperatureRecords() 
    {
        return db.query(DATABASE_TABLE_TEMPERATURES, new String[] {
        		KEY_ROWID, 
        		"temperature",
        		"notes",
                "recdate",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "recdate desc");
    }

    public Cursor getTemperatureRecordsByPersonId(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TEMPERATURES, new String[] {
                		KEY_ROWID,
                		"temperature",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getTemperatureRecordsByPersonIdAscending(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TEMPERATURES, new String[] {
                		KEY_ROWID,
                		"temperature",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getTemperatureRecordsByPersonIdAndKeyword(int personId, String keyword) throws SQLException 
    {
    	String q;
    	try {
    		int k = Integer.parseInt(keyword);
    		q = "personid=" + personId + " AND (temperature=" + k + " OR notes like '%" + keyword + "%')";
    	} catch (Exception ex){
    		q = "personid=" + personId + " AND (notes like '%" + keyword + "%')";
    	}
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TEMPERATURES, new String[] {
                		KEY_ROWID,
                		"temperature",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    
    public Cursor getTemperatureRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TEMPERATURES, new String[] {
                		KEY_ROWID,
                		"temperature",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateTemperatureRecord(long rowId, float temperature, 
    		String notes, long recdate, int personId) 
    {
        ContentValues args = new ContentValues();
        args.put("temperature", temperature);
        args.put("notes", notes);
        args.put("recdate", recdate);
        args.put("personid", personId);
        return db.update(DATABASE_TABLE_TEMPERATURES, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
    
// -- Blood Pressure and Pulse Methods -- //   
    
    public long insertBPPulse(int systolic, 
    		int diastolic, int pulse, String notes, long recdate, int personId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("systolic", systolic);
        initialValues.put("diastolic", diastolic);
        initialValues.put("pulse", pulse);
        initialValues.put("notes", notes);
        initialValues.put("recdate", recdate);
        initialValues.put("personid", personId);
        
        return db.insert(DATABASE_TABLE_BPPULSES, null, initialValues);
    }

    public boolean deleteBPPulseRecord(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_BPPULSES, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public boolean deleteAllBPPulseRecords() 
    {
        return db.delete(DATABASE_TABLE_BPPULSES, null, null) > 0;
    }

    public Cursor getAllBPPulseRecords() 
    {
        return db.query(DATABASE_TABLE_BPPULSES, new String[] {
        		KEY_ROWID, 
        		"systolic",
        		"diastolic",
                "pulse",
                "notes",
                "recdate",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "recdate desc");
    }
    
    public Cursor getAllBPPulseRecordsAscending() 
    {
        return db.query(DATABASE_TABLE_BPPULSES, new String[] {
        		KEY_ROWID, 
        		"systolic",
        		"diastolic",
                "pulse",
                "notes",
                "recdate",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "recdate asc");
    }

    public Cursor getBPPulseRecordsByPersonId(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_BPPULSES, new String[] {
                		KEY_ROWID,
                		"systolic", 
                		"diastolic",
                		"pulse",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getBPPulseRecordsByPersonIdAscending(int personId, long howfarback) throws SQLException 
    {
        Cursor mCursor = null;
        if (howfarback == -1){
                mCursor = db.query(true, DATABASE_TABLE_BPPULSES, new String[] {
                		KEY_ROWID,
                		"systolic", 
                		"diastolic",
                		"pulse",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate asc", 
                		null);
        } 
        
        if (howfarback > -1){
        	mCursor = db.query(true, DATABASE_TABLE_BPPULSES, new String[] {
            		KEY_ROWID,
            		"systolic", 
            		"diastolic",
            		"pulse",
            		"notes",
            		"recdate",
            		"personid"
            		}, 
            		"personid=" + personId + " and recdate > " + howfarback, 
            		null,
            		null, 
            		null, 
            		"recdate asc", 
            		null);
        	
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getBPPulseRecordsByPersonIdAndKeyword(int personId, String keyword) throws SQLException 
    {
    	String q;
    	try {
    		int k = Integer.parseInt(keyword);
    		q="personid=" + personId + " AND (systolic =" + k + " OR diastolic =" + k + " OR pulse =" + k + " OR notes like '%" + k + "%')";
    	}catch (Exception ex){
    		q="personid=" + personId + " AND notes like '%" + keyword + "%'";
    	}
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_BPPULSES, new String[] {
                		KEY_ROWID,
                		"systolic", 
                		"diastolic",
                		"pulse",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getBPPulseRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_BPPULSES, new String[] {
                		KEY_ROWID,
                		"systolic", 
                		"diastolic",
                		"pulse",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateBPPulseRecord(long rowId, int systolic, 
    int diastolic, int pulse, String notes,float recdate, int personId) 
    {
    	boolean b = false;
        ContentValues args = new ContentValues();
        args.put("systolic", systolic);
        args.put("diastolic", diastolic);
        args.put("pulse", pulse);
        args.put("notes", notes);
        args.put("recdate", recdate);
        args.put("personid", personId);
        try {
        	open();	
        	db.beginTransaction(); 
        	b = db.update(DATABASE_TABLE_BPPULSES, args, 
                    KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
        	} catch (SQLException e) {
        		Log.e("Exception on query", e.toString());
        	} finally {
        		db.endTransaction();
        		close();
        	}
        	return b;
        
    }
    
// -- Blood Sugars -- //   
    
    public long insertSugar(float bloodglucose, 
    		int prepost, String medication,
    		int mealtype, String foods,
    		String notes, long recdate, int personId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("bloodglucose", bloodglucose);
        initialValues.put("prepost", prepost);
        initialValues.put("medication", medication);
        initialValues.put("mealtype", mealtype);
        initialValues.put("foods", foods);
        initialValues.put("notes", notes);
        initialValues.put("recdate", recdate);
        initialValues.put("personid", personId);
        
        return db.insert(DATABASE_TABLE_SUGARS, null, initialValues);
    }

    public boolean deleteSugarRecord(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_SUGARS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public boolean deleteAllSugarRecords() 
    {
        return db.delete(DATABASE_TABLE_SUGARS, null, null) > 0;
    }

    public Cursor getAllSugarRecords() 
    {
        return db.query(DATABASE_TABLE_SUGARS, new String[] {
        		KEY_ROWID,
        		"bloodglucose",
        		"prepost",
                "medication",
                "mealtype",
                "foods",
                "notes",
                "recdate",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "recdate desc");
    }

    public Cursor getSugarRecordsByPersonIdAndKeyword(int personId, String keyword) throws SQLException 
    {
    	String prepost = ""; 
    	String mealtype = "";
    	
    	if (keyword.toLowerCase().contains("before")){
    		prepost = "0";
    	}
    	if (keyword.toLowerCase().contains("after")){
    		prepost = "1";
    	}
    	if (keyword.toLowerCase().contains("no meal")){
    		prepost = "2";
    		mealtype = "5";
    	}
    	if (keyword.toLowerCase().contains("breakfast")){
    		mealtype = "0";
    	}
    	if (keyword.toLowerCase().contains("lunch")){
    		mealtype = "1";
    	}
    	if (keyword.toLowerCase().contains("dinner")){
    		mealtype = "2";
    	}
    	if (keyword.toLowerCase().contains("snack")){
    		mealtype = "3";
    	}
    	if (keyword.toLowerCase().contains("brunch")){
    		mealtype = "4";
    	}
   
    	
    	String q;
    	try {
    		int k = Integer.parseInt(keyword);
    		q="personid=" + personId + " AND (bloodglucose=" + k + " OR medication like '%" + k + "%' OR foods like '%" + k + "%' OR notes like '%" + k + "%')";
    	} catch (Exception ex){
    		q="personid=" + personId + " AND (medication like '%" + keyword + "%' OR foods like '%" + keyword + "%' OR notes like '%" + keyword + "%')";
    	}
    	
    	if (prepost.length() > 0){
    		q = q + " OR prepost=" + prepost;
    	}
    	if (mealtype.length() > 0) {
    		q = q + " OR mealtype=" + mealtype;
    	}
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_SUGARS, new String[] {
                		KEY_ROWID,
                		"bloodglucose",
                		"prepost",
                        "medication",
                        "mealtype",
                        "foods",
                        "notes",
                        "recdate",
                        "personid"
                		},
                		q,
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getSugarRecordsByPersonId(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_SUGARS, new String[] {
                		KEY_ROWID,
                		"bloodglucose",
                		"prepost",
                        "medication",
                        "mealtype",
                        "foods",
                        "notes",
                        "recdate",
                        "personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getSugarRecordsByPersonIdAscending(int personId, long howfarback) throws SQLException 
    {
    	Cursor mCursor = null;
    	if (howfarback == -1){
    		mCursor =
                db.query(true, DATABASE_TABLE_SUGARS, new String[] {
                		KEY_ROWID,
                		"bloodglucose",
                		"prepost",
                        "medication",
                        "mealtype",
                        "foods",
                        "notes",
                        "recdate",
                        "personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate asc", 
                		null);
    	}
    	
    	if (howfarback > -1){
    		mCursor =
                    db.query(true, DATABASE_TABLE_SUGARS, new String[] {
                    		KEY_ROWID,
                    		"bloodglucose",
                    		"prepost",
                            "medication",
                            "mealtype",
                            "foods",
                            "notes",
                            "recdate",
                            "personid"
                    		}, 
                    		"personid=" + personId + " and recdate > " + howfarback, 
                    		null,
                    		null, 
                    		null, 
                    		"recdate asc", 
                    		null);
    	}
    	
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getSugarRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_SUGARS, new String[] {
                		KEY_ROWID,
                		"bloodglucose",
                		"prepost",
                        "medication",
                        "mealtype",
                        "foods",
                        "notes",
                        "recdate",
                        "personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateSugarRecord(long rowId, float bloodglucose, int prepost,
    		String medication, int mealtype, String foods, String notes,float recdate,int personid) 
    {
    	boolean b = false;
        ContentValues initialValues = new ContentValues();
        initialValues.put("bloodglucose", bloodglucose);
        initialValues.put("prepost", prepost);
        initialValues.put("medication", medication);
        initialValues.put("mealtype", mealtype);
        initialValues.put("foods", foods);
        initialValues.put("notes", notes);
        initialValues.put("recdate", recdate);
        initialValues.put("personid", personid);
                
        try {
        	open();	
        	db.beginTransaction(); 
        	b = db.update(DATABASE_TABLE_SUGARS, initialValues, 
                    KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
        	} catch (SQLException e) {
        		Log.e("Exception on query", e.toString());
        	} finally {
        		db.endTransaction();
        		close();
        	}
        	return b;
        
    }

    /* multiple persons */
    public Cursor getAllPersons(){
    	return db.query(DATABASE_TABLE_PERSONS, new String[] {
        		KEY_ROWID, 
        		"personname",
        		"currentuser"}, 
                null, 
                null, 
                null, 
                null, 
                KEY_ROWID + " asc");
    }
    
    public static Cursor getCurrentPerson(){
    	Cursor mCursor =
                db.query(true, DATABASE_TABLE_PERSONS, new String[] {
                		KEY_ROWID,
                		"personname",
                		"currentuser"
                		}, 
                		"currentuser=1", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()){
            	return mCursor;
            }else {
            	mCursor.close();
            	setCurrentPersonByPersonName("Primary Person");
            	Cursor mCursor1 =
                        db.query(true, DATABASE_TABLE_PERSONS, new String[] {
                        		KEY_ROWID,
                        		"personname",
                        		"currentuser"
                        		}, 
                        		"currentuser=1", 
                        		null,
                        		null, 
                        		null, 
                        		null, 
                        		null);
            	if (mCursor1 != null) {
                    if (mCursor1.moveToFirst()){
                    	return mCursor1;
                    }else {
                    	return null;
                    }
            	}else {
            		return null;
            	}
            }
        } else {
        	return null;
        }
    }
    
    public static int getCurrentPersonId(){
		int personid = 0;
		
		try {
			Cursor mCursor = getCurrentPerson();
			if (mCursor != null){
				if (mCursor.moveToFirst()){
					try {
						personid = mCursor.getInt(0);
					} catch (Exception ex){
						personid = 0;
					}
				}
			}
		} catch (Exception ex){
			personid = 0;
		}
				
		return personid;
	}
    
    public long insertPerson(String personname) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("personname", personname);
        initialValues.put("currentuser", 0);
        
        return db.insert(DATABASE_TABLE_PERSONS, null, initialValues);
    }
    
    public int FindPerson(String personname)
    {
    	Cursor c = db.rawQuery("select " + KEY_ROWID + " from " + DATABASE_TABLE_PERSONS + " where personname like ?", new String[]{personname});
    	if (c !=null){
    		if (c.moveToFirst()){
    			int personid = c.getInt(0);
    			return personid;
    		}else{
    			return -1;
    		}
    	}else {
    		return -1;
    	}
    }
    
    public boolean setCurrentPerson(int personid)
    {
    	String sql = "update " + DATABASE_TABLE_PERSONS + " set currentuser=0";
    	String sql2 = "update " + DATABASE_TABLE_PERSONS + " set currentuser=1 where "+ KEY_ROWID + "=" + personid;
    	
    	try {
    		db.execSQL(sql);
    		db.execSQL(sql2);
    	} catch (Exception ex){
    		return false;
    	}
    	return true;
    }
    
    public static boolean setCurrentPersonByPersonName(String personname)
    {
    	String sql = "update " + DATABASE_TABLE_PERSONS + " set currentuser=0";
    	String sql2 = "update " + DATABASE_TABLE_PERSONS + " set currentuser=1 where personname='" + personname + "'";
    	
    	try {
    		db.execSQL(sql);
    		db.execSQL(sql2);
    	} catch (Exception ex){
    		return false;
    	}
    	return true;
    }
    
    public boolean renamePerson(int personid, String personname)
    {
    	String sql = "update " + DATABASE_TABLE_PERSONS + " set personname='" + personname + "' where " + KEY_ROWID + "=" + personid;
    	
    	try {
    		db.execSQL(sql);
    	} catch (Exception ex){
    		return false;
    	}
    	return true;
    }
    
    public boolean deletePerson(int personid)
    {    	
    	String sql = "delete from " + DATABASE_TABLE_PERSONS + " where " + KEY_ROWID + "=" + personid;
    	String sql2 = "delete from " + DATABASE_TABLE_HISTORY + " where " + KEY_ROWID + "=" + personid;
    	String sql3 = "delete from " + DATABASE_TABLE_WEIGHTS + " where " + KEY_ROWID + "=" + personid;
    	String sql4 = "delete from " + DATABASE_TABLE_BPPULSES + " where " + KEY_ROWID + "=" + personid;
    	String sql5 = "delete from " + DATABASE_TABLE_SUGARS + " where " + KEY_ROWID + "=" + personid;
    	String sql6 = "delete from " + DATABASE_TABLE_TESTS + " where " + KEY_ROWID + "=" + personid;
    	String sql7 = "delete from " + DATABASE_TABLE_VISITS + " where " + KEY_ROWID + "=" + personid;
    	String sql8 = "delete from " + DATABASE_TABLE_OFFICES + " where " + KEY_ROWID + "=" + personid;
 
    	try {
    		db.execSQL(sql);
    		db.execSQL(sql2);
    		db.execSQL(sql3);
    		db.execSQL(sql4);
    		db.execSQL(sql5);
    		db.execSQL(sql6);
    		db.execSQL(sql7);
    		db.execSQL(sql8);
    	} catch (Exception ex){
    		return false;
    	}
    	
    	return true;
    }

    // -- Lab Tests -- //   
    
    public long insertTest(long DateDetail,String TestNameDetail, String TestResultDetail,String NotesDetail, int PersonIdDetail) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("testdate", DateDetail);
        initialValues.put("testname", TestNameDetail);
        initialValues.put("testresult", TestResultDetail);
        initialValues.put("notes", NotesDetail);
        initialValues.put("personid", PersonIdDetail);
        
        return db.insert(DATABASE_TABLE_TESTS, null, initialValues);
    }

    public boolean deleteTestRecord(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_TESTS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public boolean deleteAllTestRecords() 
    {
        return db.delete(DATABASE_TABLE_TESTS, null, null) > 0;
    }

    public Cursor getAllTestRecords() 
    {
        return db.query(DATABASE_TABLE_TESTS, new String[] {
        		KEY_ROWID, 
        		"testdate",
        		"testname",
                "testresult",
                "notes",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "testdate desc");
    }

    public Cursor getTestRecordsByPersonId(int personid) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TESTS, new String[] {
                		KEY_ROWID,
                		"testdate",
                		"testname",
                        "testresult",
                        "notes",
                        "personid"
                		}, 
                		"personid=" + personid, 
                		null,
                		null, 
                		null, 
                		"testdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getTestRecordsByPersonIdAndKeyword(int personid, String keyword) throws SQLException 
    {
    	String q = "personid=" + personid + " AND (testname like '%" + keyword + "%' OR testresult='%" + keyword + "%' OR " +
    			"notes like '%" + keyword + "%')";
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TESTS, new String[] {
                		KEY_ROWID,
                		"testdate",
                		"testname",
                        "testresult",
                        "notes",
                        "personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"testdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getTestRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_TESTS, new String[] {
                		KEY_ROWID,
                		"testdate",
                		"testname",
                        "testresult",
                        "notes",
                        "personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateTestRecord(int rowId, long DateDetail,String TestNameDetail, String TestResultDetail, String NotesDetail, int PersonIdDetail) 
    {
    	boolean b = false;
        ContentValues initialValues = new ContentValues();
        initialValues.put("testdate", DateDetail);
        initialValues.put("testname", TestNameDetail);
        initialValues.put("testresult", TestResultDetail);
        initialValues.put("notes", NotesDetail);
        initialValues.put("personid", PersonIdDetail);
                        
        try {
        	open();	
        	db.beginTransaction(); 
        	b = db.update(DATABASE_TABLE_TESTS, initialValues, 
                    KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
        	} catch (SQLException e) {
        		Log.e("Exception on query", e.toString());
        	} finally {
        		db.endTransaction();
        		close();
        	}
        	return b;
    }

// -- Visits -- //
    public long insertVisit(long RecordDate, int OfficeId, String sHappened, String Notes, int personid){
    	ContentValues initialValues = new ContentValues();
    	initialValues.put("visitdate", RecordDate);
    	initialValues.put("officeid", OfficeId);
    	initialValues.put("visithappened", sHappened);
    	initialValues.put("visitnotes", Notes);
    	initialValues.put("personid", personid);
    	
    	return db.insert(DATABASE_TABLE_VISITS, null, initialValues);
    }
    
    public boolean deleteVisitRecord(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_VISITS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public boolean deleteAllVisitRecords() 
    {
        return db.delete(DATABASE_TABLE_OFFICES, null, null) > 0;
    }

    public Cursor getAllVisitRecords() 
    {
        return db.query(DATABASE_TABLE_VISITS, new String[] {
        		KEY_ROWID, 
        		"visitdate",
        		"officeid",
                "visithappened",
                "visitnotes",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "visitdate desc");
    }

    public Cursor getVisitRecordsByPersonId(int personid) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_VISITS, new String[] {
                		KEY_ROWID,
                		"visitdate",
                		"officeid",
                        "visithappened",
                        "visitnotes",
                        "personid"
                		}, 
                		"personid=" + personid, 
                		null,
                		null, 
                		null, 
                		"visitdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getVisitRecordsByPersonIdAndKeyword(int personid,String keyword) throws SQLException 
    {
    	final String sql = "select v." + KEY_ROWID + ",v.visitdate,v.officeid,v.visithappened,v.visitnotes,v.personid from " +
    				DATABASE_TABLE_VISITS + " v INNER JOIN " + DATABASE_TABLE_OFFICES + " o " +
    				" ON v.officeid=o." + KEY_ROWID + 
    				" where v.visithappened like '%" + keyword + "%' OR " +
    				"v.visitnotes like '%" + keyword + "%' OR " +
    				"o.officename like '%" + keyword + "%' " +
    				"order by v.visitdate desc";
    	
    	
        Cursor mCursor = db.rawQuery(sql, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getVisitRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_VISITS, new String[] {
                		KEY_ROWID,
                		"visitdate",
                		"officeid",
                        "visithappened",
                        "visitnotes",
                        "personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateVisitRecord(long rowId, long RecordDate, int OfficeId, String sHappened, String Notes, int personid) 
    {
    	boolean b = false;
        ContentValues initialValues = new ContentValues();
        initialValues.put("visitdate", RecordDate);
    	initialValues.put("officeid", OfficeId);
    	initialValues.put("visithappened", sHappened);
    	initialValues.put("visitnotes", Notes);
    	initialValues.put("personid", personid);
                        
        try {
        	open();	
        	db.beginTransaction(); 
        	b = db.update(DATABASE_TABLE_VISITS, initialValues, 
                    KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
        	} catch (SQLException e) {
        		Log.e("Exception on query", e.toString());
        	} finally {
        		db.endTransaction();
        		close();
        	}
        	return b;
    }
    
// -- Offices -- //   
    
    public long insertOffice(String OfficeNameDetail, String OfficeHoursDetail,String ContactLookupKey,int contactid, String NotesDetail, int PersonIdDetail) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("officename", OfficeNameDetail);
        initialValues.put("officehours", OfficeHoursDetail);
        initialValues.put("contactlookupkey", ContactLookupKey);
        initialValues.put("contactid",contactid);
        initialValues.put("officenotes", NotesDetail);
        initialValues.put("personid", PersonIdDetail);
        
        return db.insert(DATABASE_TABLE_OFFICES, null, initialValues);
    }

    public boolean deleteOfficeRecord(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_OFFICES, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public boolean deleteAllOfficeRecords() 
    {
        return db.delete(DATABASE_TABLE_OFFICES, null, null) > 0;
    }

    public Cursor getAllOfficeRecords() 
    {
        return db.query(DATABASE_TABLE_OFFICES, new String[] {
        		KEY_ROWID, 
        		"officename",
        		"officehours",
                "contactlookupkey",
                "contactid",
                "officenotes",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "officename asc");
    }

    public Cursor getOfficeRecordsByPersonId(int personid) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_OFFICES, new String[] {
                		KEY_ROWID,
                		"officename",
                		"officehours",
                        "contactlookupkey",
                        "contactid",
                        "officenotes",
                        "personid"
                		}, 
                		"personid=" + personid, 
                		null,
                		null, 
                		null, 
                		"officename asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getOfficeRecordsByPersonIdAndKeyword(int personid, String keyword) throws SQLException 
    {
    	String q = "personid=" + personid + " AND (officename like '%" + keyword + "%' OR officehours like '%" + keyword + "%' OR " +
    				"officenotes like '%" + keyword + "%')";
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_OFFICES, new String[] {
                		KEY_ROWID,
                		"officename",
                		"officehours",
                        "contactlookupkey",
                        "contactid",
                        "officenotes",
                        "personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"officename asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getOfficeRecordsByPersonIdAndOfficeName(int personid, String officename) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_OFFICES, new String[] {
                		KEY_ROWID,
                		"officename",
                		"officehours",
                        "contactlookupkey",
                        "contactid",
                        "officenotes",
                        "personid"
                		}, 
                		"personid=" + personid + " AND officename='" + officename + "'", 
                		null,
                		null, 
                		null, 
                		"officename asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getOfficeRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_OFFICES, new String[] {
                		KEY_ROWID,
                		"officename",
                		"officehours",
                        "contactlookupkey",
                        "contactid",
                        "officenotes",
                        "personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		"officename asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateOfficeRecord(int rowId, String OfficeNameDetail, String OfficeHoursDetail, String ContactLookupKey, int contactid, String NotesDetail, int PersonIdDetail) 
    {
    	boolean b = false;
        ContentValues initialValues = new ContentValues();
        initialValues.put("officename", OfficeNameDetail);
        initialValues.put("officehours", OfficeHoursDetail);
        initialValues.put("contactlookupkey", ContactLookupKey);
        initialValues.put("contactid",contactid);
        initialValues.put("officenotes", NotesDetail);
        initialValues.put("personid", PersonIdDetail);
                        
        try {
        	open();	
        	db.beginTransaction(); 
        	b = db.update(DATABASE_TABLE_OFFICES, initialValues, 
                    KEY_ROWID + "=" + rowId, null) > 0;
            db.setTransactionSuccessful();
        	} catch (SQLException e) {
        		Log.e("Exception on query", e.toString());
        	} finally {
        		db.endTransaction();
        		close();
        	}
        	return b;
    }
 
// -- Documents Methods -- //   
    
    public long insertDocument(String filename, String notes, long recdate, int personId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("filename", filename);
        initialValues.put("notes", notes);
        initialValues.put("recdate", recdate);
        initialValues.put("personid", personId);
        
        return db.insert(DATABASE_TABLE_DOCUMENTS, null, initialValues);
    }

    public boolean deleteDocumentRecord(long rowId) 
    {
    	boolean b = false;
    	try {
    	open();	
    	db.beginTransaction(); 
    	b = db.delete(DATABASE_TABLE_DOCUMENTS, KEY_ROWID + 
        		"=" + rowId, null) > 0;
        db.setTransactionSuccessful();
    	} catch (SQLException e) {
    		Log.e("Exception on query", e.toString());
    	} finally {
    		db.endTransaction();
    		close();
    	}
    	return b;
    }
    
    public boolean deleteAllDocumentRecords() 
    {
        return db.delete(DATABASE_TABLE_DOCUMENTS, null, null) > 0;
    }

    public Cursor getDocumentRecords() 
    {
        return db.query(DATABASE_TABLE_DOCUMENTS, new String[] {
        		KEY_ROWID, 
        		"filename",
        		"notes",
                "recdate",
                "personid"
                }, 
                null, 
                null, 
                null, 
                null, 
                "recdate desc");
    }

    public Cursor getDocumenteRecordsByPersonId(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_DOCUMENTS, new String[] {
                		KEY_ROWID,
                		"filename",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getDocumentRecordsByPersonIdAscending(int personId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_DOCUMENTS, new String[] {
                		KEY_ROWID,
                		"filename",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		"personid=" + personId, 
                		null,
                		null, 
                		null, 
                		"recdate asc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor getDocumentRecordsByPersonIdAndKeyword(int personId, String keyword) throws SQLException 
    {
    	String q;
    	try {
    		int k = Integer.parseInt(keyword);
    		q = "personid=" + personId + " AND (filename=" + k + " OR notes like '%" + keyword + "%')";
    	} catch (Exception ex){
    		q = "personid=" + personId + " AND (notes like '%" + keyword + "%')";
    	}
    	
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_DOCUMENTS, new String[] {
                		KEY_ROWID,
                		"filename",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		q, 
                		null,
                		null, 
                		null, 
                		"recdate desc", 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    
    public Cursor getDocumentRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE_DOCUMENTS, new String[] {
                		KEY_ROWID,
                		"filename",
                		"notes",
                		"recdate",
                		"personid"
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateDocumentRecord(long rowId, String filename, 
    		String notes, long recdate, int personId) 
    {
        ContentValues args = new ContentValues();
        args.put("filename", filename);
        args.put("notes", notes);
        args.put("recdate", recdate);
        args.put("personid", personId);
        return db.update(DATABASE_TABLE_DOCUMENTS, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public void ExportXML(String outputFile, String tableName)
    {
    	if (!tableName.equalsIgnoreCase("All Records")){
    		tableName = TableDescrToName(tableName);
    	}
    	File sdDir = Environment.getExternalStorageDirectory();
    	String sSdDir = sdDir.toString();
    	
    	_ctx = this.context;
    	
    	String newFolder = sSdDir + "/MyHealthRecords/";
    	EXPORT_FILE_NAME = newFolder + outputFile;
    	File myFolder = new File(newFolder);
    	File myFile = new File( EXPORT_FILE_NAME );
        if (!myFile.getParentFile().exists() && !myFile.getParentFile().mkdirs() && !myFolder.mkdirs()){
        	Log.i("MyHealthRecords","Unable to create " + myFile.getParentFile()); 
        }

        
    	try
		{
			
			myFile.createNewFile();

            FileOutputStream fOut =  new FileOutputStream(myFile);
            BufferedOutputStream bos = new BufferedOutputStream( fOut );

			_exporter = new Exporter( bos );
			
			try
			{
				_exporter.startDbExport( DATABASE_NAME );
				if (tableName.equalsIgnoreCase("All Records")){
					exportTable(DATABASE_TABLE_HISTORY);
					exportTable(DATABASE_TABLE_MEDICATIONS);
					exportTable(DATABASE_TABLE_WEIGHTS);
					exportTable(DATABASE_TABLE_SETTINGS);
					exportTable(DATABASE_TABLE_BPPULSES);
					exportTable(DATABASE_TABLE_SUGARS);
					exportTable(DATABASE_TABLE_PERSONS);
					exportTable(DATABASE_TABLE_TESTS);
					exportTable(DATABASE_TABLE_VISITS);
					exportTable(DATABASE_TABLE_OFFICES);
					exportTable(DATABASE_TABLE_TEMPERATURES);
					exportTable(DATABASE_TABLE_DOCUMENTS);
				} else {
					exportTable( tableName );
				}

		        _exporter.endDbExport();
				_exporter.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    	
    }
    
    
	public void ExportTXT(String outputFile, String tableName)
    {
		if (!tableName.equalsIgnoreCase("All Records")){
    		tableName = TableDescrToName(tableName);
    	}
    	File sdDir = Environment.getExternalStorageDirectory();
    	String sSdDir = sdDir.toString();
    	
    	_ctx = this.context;
    	
    	String newFolder = sSdDir + "/MyHealthRecords/";
    	EXPORT_FILE_NAME = newFolder + outputFile;
    	File myFolder = new File(newFolder);
    	File myFile = new File( EXPORT_FILE_NAME );
        if (!myFile.getParentFile().exists() && !myFile.getParentFile().mkdirs() && !myFolder.mkdirs()){
        	Log.i("MyHealthRecords","Unable to create " + myFile.getParentFile()); 
        }

        
    	try
		{
			
			myFile.createNewFile();

            FileOutputStream fOut =  new FileOutputStream(myFile);
            BufferedOutputStream bos = new BufferedOutputStream( fOut );

			_exporter = new Exporter( bos );
			
			try
			{
				if (tableName.equalsIgnoreCase("All Records")){
					exportTableTXT(DATABASE_TABLE_HISTORY);
					exportTableTXT(DATABASE_TABLE_MEDICATIONS);
					exportTableTXT(DATABASE_TABLE_WEIGHTS);
					exportTableTXT(DATABASE_TABLE_SETTINGS);
					exportTableTXT(DATABASE_TABLE_BPPULSES);
					exportTableTXT(DATABASE_TABLE_SUGARS);
					exportTableTXT(DATABASE_TABLE_PERSONS);
				} else {
					exportTableTXT( tableName );
				}

				_exporter.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    
    public boolean ImportXML(String inputFile, String tableName)
    {
    	boolean  GoodOrNot = false;
    	if (!tableName.equalsIgnoreCase("All Records")){
    		tableName = TableDescrToName(tableName);
    	}
    	File sdDir = Environment.getExternalStorageDirectory();
    	String sSdDir = sdDir.toString();
    	
    	_ctx = this.context;
    	
    	String newFolder = sSdDir + "/MyHealthRecords/";
    	IMPORT_FILE_NAME = newFolder + inputFile;
    	File myFolder = new File(newFolder);
    	File myFile = new File( IMPORT_FILE_NAME );
    	if (!myFile.getParentFile().exists() || !myFile.exists()){
    		Log.i("MyHealthRecords","Unable to locate " + myFile.getParentFile()); 
    	}
    	
    	try
		{
    		
    		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		  DocumentBuilder dbdr = dbf.newDocumentBuilder();
    		  Document doc = dbdr.parse(myFile);
    		  doc.getDocumentElement().normalize();
    		  NodeList nodelst1 = doc.getElementsByTagName("table");
    		  for (int nNum = 0; nNum < nodelst1.getLength(); nNum++){
    			  Node curNode = nodelst1.item(nNum);
    			  if (curNode.getNodeType() == Node.ELEMENT_NODE) {
        		      Element curElmnt = (Element) curNode;
        		      tableName = curElmnt.getAttribute("name");
    		   		  
        		      NodeList nodeLst = curElmnt.getChildNodes();
        		      
        		      for (int s = 0; s < nodeLst.getLength(); s++) {

        		    	  Node fstNode = nodeLst.item(s);
    		    
        		    	  if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
        		    		  Element fstElmnt = (Element) fstNode;
        		    		  NodeList fstElmtNodeList = fstElmnt.getChildNodes();
        		    		  int iNodeListLength = fstElmtNodeList.getLength();
        		    		  String sNames = "";
        		    		  String sValues = "";
        		    		  String sType = "";
    		      
        		    		  for (int i = 0; i< iNodeListLength; i++ )
        		    		  {
        		    			  
        		    			  Element n = (Element) fstElmtNodeList.item(i);
        		    			  String sNodeMajorName = n.getNodeName();
    		    	  
        		    			  String sNodeName = n.getAttribute("name");
        		    			  
        		    			  if (!sNodeName.equals("_id")){
        		    				  
        		    				  String sNodeType = n.getAttribute("type");
        		    				  Node firstChild = n.getFirstChild();
        		    				  String sNodeValue = "";
        		    				  try {
        		    					  sNodeValue = firstChild.getNodeValue() ;
        		    				 	  sNodeValue = sNodeValue.replace("'", "''");		  
        		    				  } catch (Exception ex){}
        		    				  
        		    				  sNames += sNodeName + ",";
        		    				  if (sNodeType.equals("text") && sNodeValue != null){
        		    					  sValues += "'" + sNodeValue + "',";
        		    				  } else {
        		    					  sValues += sNodeValue + ",";
        		    				  }
    		    	  				  
        		    			  }
        		    			  
        		    		  } 
        		    		  
        		    		  sNames = sNames.substring(0, sNames.length() -1);
        		    		  sValues = sValues.substring(0, sValues.length() -1);
        		    		  if (sNames.length() > 0){
        		    			  String sSql = "insert into " + tableName + " (" + sNames + ") values (" + sValues + ")";
        		    			  sNames = "";
        		    			  sValues = "";
        		    			  //Log.i("MyHealthRecords","*** SQL *** " + sValues); 
        		    			  try {
        		    				  db.execSQL(sSql);
        		    				  GoodOrNot = true;
        		    			  } catch (Exception e) {
        		    				  GoodOrNot = false;
        		    			  }
        		    		  } else {
        		    			GoodOrNot = true;  
        		    		  }
        		      		
        		    	  }
        		      }
        		      
    			  }
    	}
    		
		} catch (Exception e) {
		    e.printStackTrace();
		    
		}
    	return GoodOrNot;
    	
    }
    
    public String BackupDatabase(File currentDB){
    	try {
    		File sdDir = Environment.getExternalStorageDirectory();
    		String sSdDir = sdDir.toString();
        	
    		String newFolder = sSdDir + "/MyHealthRecords/";
    		
        	EXPORT_FILE_NAME = newFolder + DATABASE_NAME;
        	File myFolder = new File(newFolder);
        	File myFile = new File( EXPORT_FILE_NAME );
            if (!myFile.getParentFile().exists() && !myFile.getParentFile().mkdirs() && !myFolder.mkdirs()){
            	Log.i("MyHealthRecords","Unable to create " + myFile.getParentFile()); 
            }
    		
    		
    		if (sdDir.canWrite()) {
    			String currentDBPath = currentDB.getAbsolutePath();
    			String backupDBPath = DATABASE_NAME;
    			
    			File backupDB = new File(newFolder, DATABASE_NAME);

    			if (currentDB.exists()) {
    				FileChannel src = new FileInputStream(currentDB).getChannel();
    				FileChannel dst = new FileOutputStream(backupDB).getChannel();
    				dst.transferFrom(src, 0, src.size());
    				src.close();
    				dst.close();
    			} else {
    				return DATABASE_NAME + " does not exist";
    			}
    		}
    		return EXPORT_FILE_NAME;

    	} catch (Exception e) {
    		return "";
    	}

    }
    
    public String RestoreDatabase(File currentDB){
    	try {
    		File sdDir = Environment.getExternalStorageDirectory();
    		String sSdDir = sdDir.toString();
        	
    		String newFolder = sSdDir + "/MyHealthRecords/";
    		
        	IMPORT_FILE_NAME = newFolder + DATABASE_NAME;
        	File myFolder = new File(newFolder);
        	File myFile = new File( IMPORT_FILE_NAME );
            if (!myFile.getParentFile().exists() && !myFile.getParentFile().mkdirs() && !myFolder.mkdirs()){
            	Log.i("MyHealthRecords","Unable to create " + myFile.getParentFile()); 
            }
    		
    		if (sdDir.canWrite()) {
    			String currentDBPath = currentDB.getAbsolutePath();
    			String backupDBPath = DATABASE_NAME;
    			
    			File backupDB = new File(newFolder, DATABASE_NAME);

    			if (currentDB.exists()) {
    				FileChannel src = new FileInputStream(backupDB).getChannel();
    				FileChannel dst = new FileOutputStream(currentDB).getChannel();
    				dst.transferFrom(src, 0, src.size());
    				src.close();
    				dst.close();
    			} else {
    				return DATABASE_NAME + " does not exist";
    			}
    		}
    		return IMPORT_FILE_NAME;

    	} catch (Exception e) {
    		return "";
    	}

    }
    
    private String checkForIllegalChars(String val) {
    	if (val == null){
    		return val;
    	}
    	String newStr = val;

    	if (newStr.contains("&")) { //make sure we do this check first
    	newStr = newStr.replaceAll("&", "&");
    	}

    	if (newStr.contains("<")) {
    	newStr = newStr.replaceAll("<",""); 
    	newStr = newStr.replaceAll(">", "");
    	}

    	if (newStr.contains("\"")) {
    	newStr = newStr.replaceAll("\"", "''");
    	}

    	if (newStr.contains("‘")) {
    	newStr = newStr.replaceAll("‘", "'");
    	}

    	return newStr;
    	}
    
    private String TableDescrToName(String tabledesc)
    {
    	String tableName = "";
    	if (tabledesc.trim().equalsIgnoreCase("History")) {
    		tableName = DATABASE_TABLE_HISTORY;
    	} else if (tabledesc.trim().equalsIgnoreCase("Medications")) {
    		tableName = DATABASE_TABLE_MEDICATIONS;
    	} else if (tabledesc.trim().equalsIgnoreCase("Weight")) {
    		tableName = DATABASE_TABLE_WEIGHTS;
    	} else if (tabledesc.trim().equalsIgnoreCase("Temperature")) {
    		tableName = DATABASE_TABLE_TEMPERATURES;
    	} else if (tabledesc.trim().equalsIgnoreCase("Blood Pressure")) {
    		tableName = DATABASE_TABLE_BPPULSES;
    	} else if (tabledesc.trim().equalsIgnoreCase("Blood Sugar")) {
    		tableName = DATABASE_TABLE_SUGARS;
    	} else if (tabledesc.trim().equalsIgnoreCase("Persons")) {
    		tableName = DATABASE_TABLE_PERSONS;
    	} else if (tabledesc.trim().equalsIgnoreCase("Lab Tests and Results")) {
    		tableName = DATABASE_TABLE_TESTS;
    	}else if (tabledesc.trim().equalsIgnoreCase("Offices")) {
    		tableName = DATABASE_TABLE_OFFICES;
    	}else if (tabledesc.trim().equalsIgnoreCase("Temperatures")){
    		tableName = DATABASE_TABLE_TEMPERATURES;
    	}else if(tabledesc.trim().equalsIgnoreCase("Documents")){
    		tableName = DATABASE_TABLE_DOCUMENTS;
    	}
    	return tableName;
    }
    
    private void exportTable( String tableName ) throws IOException
	{
    	db = DBHelper.getWritableDatabase();
		_exporter.startTable(tableName);

		String sql = "select * from " + tableName;
		Cursor cur = db.rawQuery( sql, new String[0] );
		int numcols = cur.getColumnCount();

		cur.moveToFirst();

		while( cur.getPosition() < cur.getCount() )
		{
			_exporter.startRow();
			String name;
			String val;
			String type;
			String[][] fieldAndTypeArray = historyArray;
			
			if (tableName.equals("history")) {
				fieldAndTypeArray = historyArray;
			}else if (tableName.equals("medications")){
				fieldAndTypeArray = medicationsArray;
			}else if (tableName.equals("weights")){
				fieldAndTypeArray = weightsArray;
			}else if (tableName.equals("temperatures")){
				fieldAndTypeArray = temperaturesArray;
			}else if (tableName.equals("bppulses")){
				fieldAndTypeArray = bppulsesArray;
			}else if (tableName.equals("sugars")){
				fieldAndTypeArray = sugarsArray;
			} else if (tableName.equals("persons")){
				fieldAndTypeArray = personsArray;
			} else if(tableName.equals("tests")){
				fieldAndTypeArray = testsArray;
			} else if(tableName.equals("visits")){
				fieldAndTypeArray = visitsArray;
			} else if(tableName.equals("offices")){
				fieldAndTypeArray = officesArray;
			} else if(tableName.equals("documents")){
				fieldAndTypeArray = documentsArray;
			}
			
			
			for( int idx = 0; idx < numcols; idx++ )
			{
				name = cur.getColumnName(idx);
				val = checkForIllegalChars(cur.getString( idx ));
				type="";
				for (int jdx=0;jdx< fieldAndTypeArray.length;jdx++){
					if (fieldAndTypeArray[jdx][0].equals(name)){
						type=fieldAndTypeArray[jdx][1];
					}
				}
				
				_exporter.addColumn( name, val, type );
			}

			_exporter.endRow();
			cur.moveToNext();
		}

		cur.close();
		DBHelper.close();

		_exporter.endTable();
	}
    
    public String exportTableString( String tableName, String header ) throws IOException
	{
    	String s = header + "\r\n";
    	db = DBHelper.getWritableDatabase();

		String sql = "select * from " + tableName;
		Cursor cur = db.rawQuery( sql, new String[0] );
		int numcols = cur.getColumnCount();
		int numrows = cur.getCount();
		if (numrows == 0){
			s = s + "*** no records found ***\r\n";
		}else {
			s = s + numrows + " record(s)\r\n";			
		}

		cur.moveToFirst();
		int recnumber = 0;
		while( cur.getPosition() < cur.getCount() )
		{
			recnumber++;
			
			String name;
			String val;
			
			for( int idx = 0; idx < numcols; idx++ )
			{
				
				name = cur.getColumnName(idx);
				val = checkForIllegalChars(cur.getString( idx ));
				
				if (name.equals("recdate")){
					name = "record date";
					long lD = Long.parseLong(val);
	            	DateFormat df = DateFormat.getDateTimeInstance();
	            	Date dDate = new Date(lD);
	            	val = df.format(dDate);
	            						
				}
				
				if (name.equals("historydate")){
					name = "record date";	            						
				}
				
				if (name.equals("_id")){
					name = "record number";
					val = String.valueOf(recnumber);
				}
				
				if (name.equals("bloodglucose")){
					name = "blood glucose reading";
				}
				
				if (name.equals("prepost")){
					name = "meal time";
					String[] itemarray = this.context.getResources().getStringArray(R.array.bloodsugarprepost);
					val = itemarray[Integer.parseInt(val)].toString();		
				}
				
				if (name.equals("mealtype"))
				{
					name = "meal type";
					String[] itemarray = this.context.getResources().getStringArray(R.array.bloodsugarmealtype);
					val = itemarray[Integer.parseInt(val)].toString();			
				}
				
				s = s + name + ": " + val + "\r\n";
			}
			s = s +"\r\n";
			cur.moveToNext();
		}

		cur.close();
		return s;

	}
    
    private void exportTableTXT( String tableName ) throws IOException
	{
    	_exporter.addTXT("Table Name: " + tableName + "\r\n");
    	db = DBHelper.getWritableDatabase();

		String sql = "select * from " + tableName;
		Cursor cur = db.rawQuery( sql, new String[0] );
		int numcols = cur.getColumnCount();
		int numrows = cur.getCount();
		if (numrows == 0){
			_exporter.addTXT("*** no records found ***\r\n");
		}else {
			_exporter.addTXT(numrows + " record(s)\r\n");			
		}

		cur.moveToFirst();
		
		int recnumber = 0;
		
		while( cur.getPosition() < cur.getCount() )
		{
			recnumber++;
			_exporter.startRowTXT();
			String name;
			String val;
			for( int idx = 0; idx < numcols; idx++ )
			{
				name = cur.getColumnName(idx);
				val = checkForIllegalChars(cur.getString( idx ));
				
				if (name.equals("recdate")){
					long lD = Long.parseLong(val);
	            	DateFormat df = DateFormat.getDateTimeInstance();
	            	Date dDate = new Date(lD);
	            	val = df.format(dDate);
	            						
				}
				
				if (name.equals("historydate")){
					name = "record date";	            						
				}
				
				if (name.equals("_id")){
					name = "record number";
					val = String.valueOf(recnumber);
				}
				
				if (name.equals("bloodglucose")){
					name = "blood glucose reading";
				}
				
				if (name.equals("prepost")){
					name = "meal time";
					String[] itemarray = this.context.getResources().getStringArray(R.array.bloodsugarprepost);
					val = itemarray[Integer.parseInt(val)].toString();		
				}
				
				if (name.equals("mealtype"))
				{
					name = "meal type";
					String[] itemarray = this.context.getResources().getStringArray(R.array.bloodsugarmealtype);
					val = itemarray[Integer.parseInt(val)].toString();			
				}
				
				_exporter.addColumnTXT( name, val );
			}

			_exporter.endRowTXT();
			cur.moveToNext();
		}

		cur.close();
		DBHelper.close();
		_exporter.addTXT("\r\n");

	}
    
    class Exporter
	{
		private static final String CLOSING_WITH_TICK = "'>";
		private static final String START_DB = "<export-database name='";
		private static final String END_DB = "</export-database>";
		private static final String START_TABLE = "<table name='";
		private static final String END_TABLE = "</table>";
		private static final String START_ROW = "<row>";
		private static final String END_ROW = "</row>";
		private static final String START_COL = "<col name='";
		private static final String END_COL = "</col>";

		private BufferedOutputStream _bos;

		public Exporter() throws FileNotFoundException
		{
			this( new BufferedOutputStream(
					_ctx.openFileOutput( EXPORT_FILE_NAME,
					Context.MODE_WORLD_READABLE ) ) );
		}

		public Exporter( BufferedOutputStream bos )
		{
			_bos = bos;
		}

		public void close() throws IOException
		{
			if ( _bos != null )
			{
				_bos.close();
			}
		}

		public void startDbExport( String dbName ) throws IOException
		{
			String stg = START_DB + dbName + CLOSING_WITH_TICK;
			_bos.write( stg.getBytes() );
		}

		public void endDbExport() throws IOException
		{
			_bos.write( END_DB.getBytes() );
		}

		public void startTable( String tableName ) throws IOException
		{
			String stg = START_TABLE + tableName + CLOSING_WITH_TICK;
			_bos.write( stg.getBytes() );
		}

		public void endTable() throws IOException
		{
			_bos.write( END_TABLE.getBytes() );
		}

		public void startRow() throws IOException
		{
			_bos.write( START_ROW.getBytes() );
		}

		public void endRow() throws IOException
		{
			_bos.write( END_ROW.getBytes() );
		}
		
		public void startRowTXT() throws IOException
		{
			return;
		}

		public void endRowTXT() throws IOException
		{
			String stg = "\r\n";
			_bos.write( stg.getBytes() );
		}

		public void addColumn( String name, String val, String type ) throws IOException
		{
			String stg = START_COL + name + "' type='" + type + CLOSING_WITH_TICK + val + END_COL;
			_bos.write( stg.getBytes() );
		}
		
		public void addColumnTXT( String name, String val) throws IOException
		{
			String stg = name + ": " + val + "\t";
			_bos.write( stg.getBytes());
		}
		
		public void addTXT (String val) throws IOException
		{
			_bos.write( val.getBytes());
		}
	}


}


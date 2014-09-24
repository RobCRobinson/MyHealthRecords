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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

public class Common {
	
	TextView ColumnTV(Context c, String sText) {
    	TextView lbl = new TextView(c);
        lbl.setText(sText);
        final DBAdapter db = new DBAdapter(c);
		db.open();
		String sFontSize = "16";
		try {
			sFontSize = db.getSetting("FontSize");
			if (sFontSize == "''"){
				sFontSize = "16";
			}
		} catch (Exception e){
			sFontSize = "16";
			
		}
		Integer iFontSize = 16;
        try {
        	iFontSize = Integer.parseInt(sFontSize);
        } catch (Exception ex){
        	  db.insertSettings("FontSize", "16");     
        }
		lbl.setTextSize(iFontSize);
        
        lbl.setTextColor(Color.BLACK);
        lbl.setPadding(0,0,10,0);
        lbl.setLayoutParams(new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        return lbl;
    }
	
	TextView ColumnTVTitle(Context c, String sText) {
    	TextView lbl = new TextView(c);
        lbl.setText(sText);
        lbl.setTextSize(12);
        
        lbl.setTextColor(Color.BLACK);
        lbl.setPadding(0,0,10,0);
        lbl.setLayoutParams(new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        return lbl;
    }
	
	public static final String md5(final String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
 
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();
 
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	public static String encrypt(String seed, String cleartext) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}
	
	public static String decrypt(String seed, String encrypted) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
	    kgen.init(128, sr); // 192 and 256 bits may not be available
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    return raw;
	}

	
	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}
	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}
	
	public static byte[] toByte(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}

	public static void CheckLogin(Context c){
    	final DBAdapter db = new DBAdapter(c);
        db.open();

        String sLogin = db.getSetting("LoginHasntRun");	
        db.close();
        if (sLogin.equalsIgnoreCase("1")){
        	
        	Intent myIntentPassword;
        	String packageName = c.getResources().getString(R.string.package_name);
          	String className = "StartupActivity";
          	
          	myIntentPassword = new Intent();
          	myIntentPassword.setComponent(new ComponentName(
          			packageName,
          			packageName + "." + className));
          	myIntentPassword.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
          	
          	c.startActivity(myIntentPassword);       	
        } 
   }
	
	public static Date FormatDate(String sRecDate,Context c){
		DateFormat df = DateFormat.getDateTimeInstance();
        DateFormat dfS = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        DateFormat dfM = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        DateFormat dfL = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        DateFormat dfF = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
        
        DateFormat dfNoTime = DateFormat.getDateInstance();
        DateFormat dfSNoTime = DateFormat.getDateInstance(DateFormat.SHORT,Locale.ENGLISH);
        DateFormat dfMNoTime = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.ENGLISH);
        DateFormat dfLNoTime = DateFormat.getDateInstance(DateFormat.LONG,Locale.ENGLISH);
        DateFormat dfFNoTime = DateFormat.getDateInstance(DateFormat.FULL,Locale.ENGLISH);
        DateFormat dfDevice = android.text.format.DateFormat.getDateFormat(c);
        
        Date dRecDate = null;
		
		try {
			dRecDate = dfDevice.parse(sRecDate);
		} catch (ParseException e){
			try {
				dRecDate = df.parse(sRecDate);
			} catch (ParseException e2) {
			
				try {
					dRecDate = dfS.parse(sRecDate);
				} catch (ParseException e3) {
		
					try {
						dRecDate = dfM.parse(sRecDate);
					} catch (ParseException e4) {
		
						try {
							dRecDate = dfL.parse(sRecDate);
						} catch (ParseException e5) {
							
							try {
								dRecDate = dfF.parse(sRecDate);
							} catch (ParseException e6) {
		
								try {
									dRecDate = dfNoTime.parse(sRecDate);
								} catch (ParseException e7) {
								
									try {
										dRecDate = dfSNoTime.parse(sRecDate);
									} catch (ParseException e8) {
		
										try {
											dRecDate = dfMNoTime.parse(sRecDate);
										} catch (ParseException e9) {
		
											try {
												dRecDate = dfLNoTime.parse(sRecDate);
											} catch (ParseException e10) {
		
												try {
													dRecDate = dfFNoTime.parse(sRecDate);
												} catch (ParseException e11) {
												
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return dRecDate;
        
	}
	
	public static String DateMessage(Date d,Context c){
		DateFormat df = DateFormat.getDateTimeInstance();
        DateFormat dfS = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        DateFormat dfM = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        DateFormat dfL = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        DateFormat dfF = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
        
        DateFormat dfNoTime = DateFormat.getDateInstance();
        DateFormat dfSNoTime = DateFormat.getDateInstance(DateFormat.SHORT,Locale.ENGLISH);
        DateFormat dfMNoTime = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.ENGLISH);
        DateFormat dfLNoTime = DateFormat.getDateInstance(DateFormat.LONG,Locale.ENGLISH);
        DateFormat dfFNoTime = DateFormat.getDateInstance(DateFormat.FULL,Locale.ENGLISH);
        DateFormat dfDevice = android.text.format.DateFormat.getDateFormat(c);
        
        String s =
        	dfDevice.format(d) +
        " or \n" + dfS.format(d) +
		" or \n" + dfM.format(d) +
		" or \n" + dfL.format(d) +
		" or \n" + dfF.format(d);
        
        return s;
		
	}
	
	
	
	
}

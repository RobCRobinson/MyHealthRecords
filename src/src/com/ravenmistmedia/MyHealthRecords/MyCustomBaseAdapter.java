package com.ravenmistmedia.MyHealthRecords;

import java.util.ArrayList;

import android.content.Context;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyCustomBaseAdapter extends BaseAdapter {
 private static ArrayList<MenuResults> searchArrayList;
 
 private LayoutInflater mInflater;

 public MyCustomBaseAdapter(Context context, ArrayList<MenuResults> results) {
  searchArrayList = results;
  mInflater = LayoutInflater.from(context);
 }

 public int getCount() {
  return searchArrayList.size();
 }

 public Object getItem(int position) {
  return searchArrayList.get(position);
 }

 public long getItemId(int position) {
  return position;
 }

 public View getView(int position, View convertView, ViewGroup parent) {
  ViewHolder holder;
  if (convertView == null) {
   convertView = mInflater.inflate(R.layout.simplerow, null);
   holder = new ViewHolder();
   holder.txtMenuItem = (TextView) convertView.findViewById(R.id.menuItem);
   holder.txtMenuDescription = (TextView) convertView.findViewById(R.id.menuDescription);
   
   int howbig = parent.getHeight();
   int howwide = parent.getWidth();
   
   if (howbig == 600)
   {
	   // portrait
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 16.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
   }
   else if (howbig == 800) 
   {
	   // portrait
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 16.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
   }
   else if (howbig == 1024){
	   // portrait
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 18.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
   }
   else if (howwide > 700 && howwide < 800) 
   {
	   // landscape
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 12.0);
   } else if (howwide == 800)
   {
	   // landscape
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 13.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 11.0);
   } else if (howwide == 1024) 
   {
	   // landscape
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 18.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
   } else if (howwide == 600) 
   {
	   // landscape
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 18.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
   } else {
	   // anything else
	   holder.txtMenuItem.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 14.0);
	   holder.txtMenuDescription.setTextSize(TypedValue.COMPLEX_UNIT_FRACTION_PARENT, (float) 12.0);
   }
   
   
   convertView.setTag(holder);
  } else {
   holder = (ViewHolder) convertView.getTag();
  }
  
  holder.txtMenuItem.setText(searchArrayList.get(position).getMenuItem());
  holder.txtMenuDescription.setText(searchArrayList.get(position).getMenuDescription());
  
  return convertView;
 }

 static class ViewHolder {
  TextView txtMenuItem;
  TextView txtMenuDescription;
 }
}

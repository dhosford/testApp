package com.pmdsolutions.gentiantestapp.support;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pmdsolutions.gentiantestapp.R;

/**
 * List adapter for the questions in the troubleshooting activity
 * @author Daniel Hosford
 *
 */
public class DeviceListAdapter extends ArrayAdapter<Devices>{

	private Context context;
	private int LayoutID;
	private ArrayList <Devices> data; 
	private int selectedIndex = -1;
	public static DeviceListAdapter instance;

	public DeviceListAdapter(Context context, int layoutResourceId, ArrayList<Devices> deviceList) {
		super(context, layoutResourceId, deviceList);
		this.LayoutID = layoutResourceId;
		this.context = context;
		this.data = deviceList;
		instance = this;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ItemHolder holder = null;

		if(row == null)	{
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(LayoutID, parent, false);

			holder = new ItemHolder();
			holder.txtTitle = (TextView)row.findViewById(R.id.listview_name);
			holder.txtRssi = (TextView)row.findViewById(R.id.listview_rssi);
			row.setTag(holder);
		}
		else{
			holder = (ItemHolder)row.getTag();
		}

		Devices item = data.get(position);
		holder.txtTitle.setText(item.getName());
		holder.txtRssi.setText("" + item.getRssi());
		holder.txtTitle.setPaintFlags(holder.txtTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		
		if(position == selectedIndex){
			row.setBackgroundColor(Color.parseColor("#FFFFAA00"));
		}
		else{
			row.setBackgroundColor(Color.TRANSPARENT);
		}

		return row;
	}
	public int getSelectedIndex(){
		return this.selectedIndex;
	}

	public void setSelectedIndex(int i){
		this.selectedIndex = i;
		notifyDataSetChanged();
	}

	private static class ItemHolder{
			TextView txtRssi;
			TextView txtTitle;
		
	}

}

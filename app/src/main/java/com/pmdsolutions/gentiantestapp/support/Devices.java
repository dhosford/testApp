package com.pmdsolutions.gentiantestapp.support;

import android.os.Parcel;
import android.os.Parcelable;

public class Devices implements Parcelable{
	
	
	String Name;
	int rssi;
	
	
	public Devices(String name, int rssi) {
		super();
		this.Name = name;
		this.rssi = rssi;
	}
	
	
	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		this.Name = name;
	}
	
	public int getRssi() {
		return rssi;
	}
	
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}


	@Override
	public int describeContents() {
		
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		
	}
	
	
}

package br.unisinos.swe.agentjs.engine.signals.info;

import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

import android.location.Location;

public class LocationSignalInfo {

	private Location _location;
	
	public LocationSignalInfo() {
		_location = null;
	}
	
	public LocationSignalInfo(Location location) {
		_location = location;
	}
	
	@JSGetter("lat")
	public double getLat(){
		double lat = 0.0;
		if(_location != null) {
			lat = _location.getLatitude();
		}
		return lat;
	}
	
	@JSGetter("lon")
	public double getLon(){
		double lon = 0.0;
		if(_location != null) {
			lon = _location.getLongitude();
		}
		return lon;
	}
	
	@JSGetter("altitude")
	public double getAltitude(){
		double altitude = 0.0;
		if(_location != null) {
			altitude = _location.getAltitude();
		}
		return altitude;
	}
	
	@JSGetter("time")
	public long getTime(){
		long time = 0L;
		if(_location != null) {
			time = _location.getTime();
		}
		return time;
	}
	
	@JSGetter("accuracy")
	public float getAccuracy(){
		float accuracy = 0.0F;
		if(_location != null) {
			accuracy = _location.getAccuracy();
		}
		return accuracy;
	}

}

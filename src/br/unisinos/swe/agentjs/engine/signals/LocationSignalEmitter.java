package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter.NetworkSignal;
import br.unisinos.swe.agentjs.engine.signals.info.LocationSignalInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

public class LocationSignalEmitter extends AbstractSignalEmitter {
	
	private static final String PROXIMITY_ALERT = "br.unisinos.swe.agentjs.engine.signals.PROXIMITY_ALERT";
	private static final String EXTRA_LISTENER_ID = "LISTENER_ID";
	private static final String EXTRA_SIGNAL_ID = "SIGNAL_ID";
	
	private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 30; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 2000; // in Milliseconds

	public static enum LocationSignal {
		REGION_ENTER("region:enter"), REGION_EXIT("region:exit"), LOCATION_CHANGED("location:changed");

		private LocationSignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}
	}
	
	private LocationManager _locationManager;
	private String _bestProvider;
	private boolean _listeningChanges = false;
	private SignalLocationListener _locationListener;
	
	private BroadcastReceiver _proximityIntentReceiver;
	
	private HashMap<String, PendingIntent> _proximityIntents;

	
	public LocationSignalEmitter() {
		super();
		if(_signals == null) {
			_signals = new ArrayList<String>();
			for (LocationSignal signal : LocationSignal.class.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}
		
		
	}
	
	public static ISignalEmitter create() {
		return new LocationSignalEmitter();
	}
	
	public LocationSignalInfo getLastKnownLocation() {
		LocationSignalInfo locationInfo = null;
		Location location = _locationManager.getLastKnownLocation(_bestProvider);
		if(location != null) {
			locationInfo = new LocationSignalInfo(location);
		}
		return locationInfo;
	}

	@Override
	public void registerListener(String signalString, ISignalListener listener) {
		super.registerListener(signalString, listener);
		
		if(LocationSignal.REGION_ENTER.equals(signalString) || LocationSignal.REGION_EXIT.equals(signalString)) {
			createProximityAlert(signalString, listener);
		} else {
			if(!_listeningChanges) {
				createLocationChangeListener();
			}
		}
		
		// If it is region enter or exit
		// get listener params
		// get region definition (lat, lng, radius)
		
		// create specific pending intent extending broadcast receiver with listener and emitter data (for calling back)
		// create proximity listener for given lat lng radius
		
		// otherwise
		// use standard Location Listener logic
	}

	@Override
	public List<ISignalListener> removeListener(String signalString, UUID listenerId, UUID parentId) {
		List<ISignalListener> removedListeners = super.removeListener(signalString, listenerId, parentId);
		
		if(LocationSignal.REGION_ENTER.equals(signalString) || LocationSignal.REGION_EXIT.equals(signalString)) {
			// in this case we need a way to find out which id related to which parent
			removeProximityAlert(signalString, removedListeners);
			
		} else {
			if(_listeningChanges && getListeners(signalString).size() <= 0) {
				removeLocationChangeListener();
			}
		}
		
		return removedListeners;
	}
	
	private void createProximityAlert(String strSignal, ISignalListener listener) {
		String strLatitude = listener.getParam("latitude");
		String strLongitude = listener.getParam("longitude");
		String strRadius = listener.getParam("radius");
		
		if(strLatitude == null) {
			return;
		}
		
		if(strLongitude == null) {
			return;
		}
		
		if(strRadius == null) {
			return;
		}
		double latitude = Double.valueOf(strLatitude);
		double longitude = Double.valueOf(strLongitude);
		float radius = Float.valueOf(strRadius);
		
		
		Intent intent = new Intent(PROXIMITY_ALERT);
		intent.putExtra(EXTRA_LISTENER_ID, listener.getUuid().toString());
		intent.putExtra(EXTRA_SIGNAL_ID, strSignal);
		PendingIntent proximityIntent = PendingIntent.getBroadcast(EngineContext.instance().getContext(), 0, intent, 0);
		
		_proximityIntents.put(listener.getUuid().toString(), proximityIntent);
		
		
		IntentFilter proximityAlertFilter = new IntentFilter(PROXIMITY_ALERT);
		EngineContext.instance().getContext().registerReceiver(_proximityIntentReceiver, proximityAlertFilter);
		
		
		_locationManager.addProximityAlert(latitude, longitude, radius, -1, proximityIntent);
		
	}
	
	
	private void removeProximityAlert(String signalString, List<ISignalListener> removedListeners) {
		
		for(ISignalListener listener : removedListeners) {
			PendingIntent proximityIntent = _proximityIntents.remove(listener.getUuid().toString());
			if(proximityIntent != null) {
				_locationManager.removeProximityAlert(proximityIntent);
			}
		}
		if(_proximityIntents.isEmpty()) {
			EngineContext.instance().getContext().unregisterReceiver(_proximityIntentReceiver);
		}
		
	}

	private void removeLocationChangeListener() {
		_listeningChanges = false;
		_locationManager.removeUpdates(_locationListener);
	}

	private void createLocationChangeListener() {
		_listeningChanges = true;
		_locationManager.requestLocationUpdates( _bestProvider,
		  										MINIMUM_TIME_BETWEEN_UPDATE,
												MINIMUM_DISTANCECHANGE_FOR_UPDATE,
												_locationListener );
	}

	@Override
	public ISignalEmitter start() {
		_locationManager = ((LocationManager) EngineContext.instance().getContext().getSystemService(Context.LOCATION_SERVICE));
		
		Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);
	    criteria.setAltitudeRequired(false);
	    criteria.setBearingRequired(false);
	    criteria.setCostAllowed(true);
	    criteria.setPowerRequirement(Criteria.POWER_LOW);
	 
	    _bestProvider = _locationManager.getBestProvider(criteria, false);
	    _locationListener = new SignalLocationListener(); 
	    _proximityIntents = new HashMap<String, PendingIntent>();
	    
	    _proximityIntentReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String strSignal = intent.getStringExtra(EXTRA_SIGNAL_ID);
				String strListenerId = intent.getStringExtra(EXTRA_LISTENER_ID);
				
				boolean entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
				ISignalListener listener = null;
				
				boolean signalEnter = (entering && LocationSignal.REGION_ENTER.equals(strSignal));
				boolean signalExit = (!entering && LocationSignal.REGION_EXIT.equals(strSignal));
				
				if(signalEnter || signalExit) {
					listener = getListenerBySignalAndId(strSignal, strListenerId);
					if(listener != null) {
						listener.fire();
					}
				}

			}
		};
		
		return this;
	}

	@Override
	public void stop() {
		removeLocationChangeListener();
		EngineContext.instance().getContext().unregisterReceiver(_proximityIntentReceiver);
	}

	@Override
	public boolean filter(String signal, ISignalListener listener, Object... params) {
		return true;
	}
	
	private class SignalLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			LocationSignalInfo locationInfo = new LocationSignalInfo(location);
			LocationSignalEmitter.this.fire(LocationSignal.LOCATION_CHANGED.toString(), locationInfo);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			
		}
	}


}

package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.util.concurrent.FutureCallback;

import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.info.LocationSignalInfo;
import br.unisinos.swe.agentjs.engine.signals.info.PlaceSignalInfo;
import br.unisinos.swe.http.utils.HttpQueue;
import br.unisinos.swe.http.utils.HttpQueueManager;
import br.unisinos.swe.http.utils.HttpQueueRequest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;

public class LocationSignalEmitter extends AbstractSignalEmitter {

	private static final String PROXIMITY_ALERT = "br.unisinos.swe.agentjs.engine.signals.PROXIMITY_ALERT";
	private static final String EXTRA_LISTENER_ID = "LISTENER_ID";
	private static final String EXTRA_SIGNAL_ID = "SIGNAL_ID";
	private static final String EXTRA_JSON_PLACE = "JSON_PLACE";

	private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 30; // in
																		// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 2000; // in
																	// Milliseconds

	public static enum LocationSignal {
		QUERY_ENTER("query:enter"), QUERY_EXIT("query:exit"), REGION_ENTER("region:enter"), REGION_EXIT("region:exit"), LOCATION_CHANGED(
				"location:changed");

		private LocationSignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}

		public static LocationSignal toSignal(String strSignal) {
			for (LocationSignal signal : LocationSignal.class
					.getEnumConstants()) {
				if (signal.toString().equals(strSignal)) {
					return signal;
				}
			}
			return null;
		}
	}

	private LocationManager _locationManager;
	private String _bestProvider;
	private boolean _listeningChanges = false;
	private SignalLocationListener _locationListener;

	private BroadcastReceiver _proximityIntentReceiver;

	private HashMap<String, ArrayList<PendingIntent>> _proximityIntents;
	
	private ArrayList<JSONObject> _places = new ArrayList<JSONObject>();
	private int _distance = 0;

	public LocationSignalEmitter() {
		super();
		if (_signals == null) {
			_signals = new ArrayList<String>();
			for (LocationSignal signal : LocationSignal.class
					.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}

	}

	public static ISignalEmitter create() {
		return new LocationSignalEmitter();
	}

	public LocationSignalInfo getLastKnownLocation() {
		LocationSignalInfo locationInfo = null;
		Location location = _locationManager
				.getLastKnownLocation(_bestProvider);
		if (location != null) {
			locationInfo = new LocationSignalInfo(location);
		}
		return locationInfo;
	}

	private boolean isRegionBased(String signalString) {

		switch (LocationSignal.toSignal(signalString)) {
		case REGION_ENTER:
			return true;
		case REGION_EXIT:
			return true;
		default:
			return false;
		}
	}

	private boolean isQueryBased(String signalString) {

		switch (LocationSignal.toSignal(signalString)) {
		case QUERY_ENTER:
			return true;
		case QUERY_EXIT:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void registerListener(String signalString, ISignalListener listener) {
		super.registerListener(signalString, listener);

		if (isRegionBased(signalString)) {
			createProximityAlert(signalString, listener);
		} else if(isQueryBased(signalString)) { 
			createQueryAlert(signalString, listener);
		} else {
			if (!_listeningChanges) {
				createLocationChangeListener();
			}
		}

		// If it is region enter or exit
		// get listener params
		// get region definition (lat, lng, radius)

		// create specific pending intent extending broadcast receiver with
		// listener and emitter data (for calling back)
		// create proximity listener for given lat lng radius

		// otherwise
		// use standard Location Listener logic
	}

	@Override
	public List<ISignalListener> removeListener(String signalString,
			UUID listenerId, UUID parentId) {
		List<ISignalListener> removedListeners = super.removeListener(
				signalString, listenerId, parentId);

		if (isRegionBased(signalString)) {
			// in this case we need a way to find out which id related to which
			// parent
			removeProximityAlert(signalString, removedListeners);

		} else if(isQueryBased(signalString)) { 
			removeQueryAlert(signalString, removedListeners);
			
		} else {
			if (_listeningChanges && getListeners(signalString).size() <= 0) {
				removeLocationChangeListener();
			}
		}

		return removedListeners;
	}

	private void createProximityAlert(String strSignal, ISignalListener listener) {
		String strLatitude = listener.getParam("latitude");
		String strLongitude = listener.getParam("longitude");
		String strRadius = listener.getParam("radius");

		if (strLatitude == null) {
			return;
		}

		if (strLongitude == null) {
			return;
		}

		if (strRadius == null) {
			return;
		}
		double latitude = Double.valueOf(strLatitude);
		double longitude = Double.valueOf(strLongitude);
		float radius = Float.valueOf(strRadius);
		
		createGenericProximityAlert(-1, latitude, longitude, radius, listener.getUuid().toString(), strSignal, "", "");
	}
	
	private void createGenericProximityAlert(long expires, double latitude, double longitude, float radius, String listenerKey, String signalId, String placeKey, String extraJsonPlace) {
		String action = String.format("%s[%s][%s]", PROXIMITY_ALERT, listenerKey, placeKey);
		
		
		Intent intent = new Intent(action);
		intent.putExtra(EXTRA_LISTENER_ID, listenerKey);
		intent.putExtra(EXTRA_SIGNAL_ID, signalId);
		intent.putExtra(EXTRA_JSON_PLACE, extraJsonPlace);

		PendingIntent proximityIntent = PendingIntent.getBroadcast(
				EngineContext.instance().getContext(), 0, intent, 0);

		ArrayList<PendingIntent> proximityIntents = null;
		if(_proximityIntents.containsKey(listenerKey)) {
			proximityIntents = _proximityIntents.get(listenerKey);
			proximityIntents.add(proximityIntent);
		} else {
			proximityIntents = new ArrayList<PendingIntent>();
			proximityIntents.add(proximityIntent);
			_proximityIntents.put(listenerKey, proximityIntents);
		}

		IntentFilter proximityAlertFilter = new IntentFilter(action);
		EngineContext
				.instance()
				.getContext()
				.registerReceiver(_proximityIntentReceiver,
						proximityAlertFilter);

		_locationManager.addProximityAlert(latitude, longitude, radius, expires,
				proximityIntent);
	}

	private void removeProximityAlert(String signalString,
			List<ISignalListener> removedListeners) {

		removeGenericProximityAlert(signalString, removedListeners);
	}
	
	private void removeGenericProximityAlert(String signalString,
			List<ISignalListener> removedListeners) {
		
		for (ISignalListener listener : removedListeners) {
			ArrayList<PendingIntent> proximityIntents = _proximityIntents.remove(listener.getUuid().toString());
			if (proximityIntents != null) {
				for(PendingIntent intent : proximityIntents) {
					_locationManager.removeProximityAlert(intent);
				}
			}
		}
		if (_proximityIntents.isEmpty()) {
			try {
				EngineContext.instance().getContext().unregisterReceiver(_proximityIntentReceiver);
			} catch(Exception e) {
			}
		}
		
	}

	private void createQueryAlert(String strSignal, ISignalListener listener) {
		// get location and handle things there:
		EngineContext.log().info("Registering Query for location updates");
		QueryLocationListener queryListener = new QueryLocationListener(strSignal, listener);
		_locationManager.requestSingleUpdate(this._bestProvider, queryListener, Looper.getMainLooper());
		
		// create url like:
		// https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&types=food&name=harbour&sensor=false&key=AddYourOwnKeyHere
	
		// or: (10x multiplier)
		// https://maps.googleapis.com/maps/api/place/textsearch/json?query=restaurants+in+Sydney&sensor=true&key=AddYourOwnKeyHere
		
	}
	
	private void removeQueryAlert(String signalString, List<ISignalListener> removedListeners) {
		removeGenericProximityAlert(signalString, removedListeners);
	}
	
	private void createQueryProximityAlert(String strSignal, ISignalListener listener, JSONObject place) throws JSONException {
		
		String placeKey = place.getString("id");
		double latitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
		double longitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
		float radius = Float.valueOf(listener.getParam("distance"));
		long expires = -1;
		if(listener.getParam("expires") != null) {
			expires = Long.valueOf(listener.getParam("expires"));
		}
		
		createGenericProximityAlert(expires, latitude, longitude, radius, listener.getUuid().toString(), strSignal, placeKey, place.toString());
	}

	protected void parseQueryResponse(String jsonContent, String strSignal, ISignalListener listener) {
		try {
			JSONObject responseRoot = new JSONObject(jsonContent);
			_places = new ArrayList<JSONObject>();
			_distance = Integer.valueOf(listener.getParam("distance"));
			float minDistanceForUpdate = MINIMUM_DISTANCECHANGE_FOR_UPDATE;
			Location lastLocation = _locationManager.getLastKnownLocation(_bestProvider);
			
			String status = responseRoot.getString("status");
			if(status.equals("OK")) {
				JSONArray results = responseRoot.getJSONArray("results");
				
				for(int idx = 0; idx < results.length(); idx++) {
					if(idx > 20) {
						EngineContext.log().info("Google Places: more than 20 results, skipping..");
						break;
					}
					JSONObject place = results.getJSONObject(idx);
					createQueryProximityAlert(strSignal, listener, place); // create the proximity alert
					
					// calculate the maximum distance for next lookup
					PlaceSignalInfo placeInfo = new PlaceSignalInfo(place, lastLocation);
					float distance = placeInfo.getDistance(); 
					if(distance > minDistanceForUpdate) {
						minDistanceForUpdate = distance; 
					}
					
					_places.add(place); // add the place for debug purposes
				}
			
				QueryLocationListener queryListener = new QueryLocationListener(strSignal, listener);
				queryListener.setDistance(minDistanceForUpdate);
				queryListener.setLastLocation(lastLocation);
				
				_locationManager.requestLocationUpdates(_bestProvider, MINIMUM_TIME_BETWEEN_UPDATE * 10, minDistanceForUpdate, queryListener, Looper.getMainLooper());
			
			} else {
				EngineContext.log().error("Status returned by Google places: " + status);
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private void removeLocationChangeListener() {
		_listeningChanges = false;
		_locationManager.removeUpdates(_locationListener);
	}

	private void createLocationChangeListener() {
		_listeningChanges = true;
		_locationManager.requestLocationUpdates(_bestProvider,
				MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE,
				_locationListener, Looper.getMainLooper());
	}

	@Override
	public ISignalEmitter start() {
		_locationManager = ((LocationManager) EngineContext.instance()
				.getContext().getSystemService(Context.LOCATION_SERVICE));

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		_bestProvider = _locationManager.getBestProvider(criteria, false);
		_locationListener = new SignalLocationListener();
		_proximityIntents = new HashMap<String, ArrayList<PendingIntent>>();

		_proximityIntentReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String strSignal = intent.getStringExtra(EXTRA_SIGNAL_ID);
				String strListenerId = intent.getStringExtra(EXTRA_LISTENER_ID);
				
				boolean entering = intent.getBooleanExtra(
						LocationManager.KEY_PROXIMITY_ENTERING, false);
				ISignalListener listener = null;
				
				if(isRegionBased(strSignal)) {
					
					boolean signalEnter = (entering && LocationSignal.REGION_ENTER
							.toString().equals(strSignal));
					boolean signalExit = (!entering && LocationSignal.REGION_EXIT
							.toString().equals(strSignal));

					if (signalEnter || signalExit) {
						listener = getListenerBySignalAndId(strSignal,
								strListenerId);
						if (listener != null) {
							listener.fire();
						}
					}
					
				} else if(isQueryBased(strSignal)) {
					String jsonPlace = intent.getStringExtra(EXTRA_JSON_PLACE);
					
					PlaceSignalInfo info = new PlaceSignalInfo(jsonPlace, _locationManager.getLastKnownLocation(_bestProvider));
					
					boolean signalEnter = (entering && LocationSignal.QUERY_ENTER.toString().equals(strSignal));
					boolean signalExit = (!entering && LocationSignal.QUERY_EXIT.toString().equals(strSignal));

					if (signalEnter || signalExit) {
						listener = getListenerBySignalAndId(strSignal, strListenerId);
						if (listener != null) {
							listener.fire(info);
						}
					}
					
					
				}
			}
		};

		return this;
	}

	@Override
	public void stop() {
		removeLocationChangeListener();
		EngineContext.instance().getContext()
				.unregisterReceiver(_proximityIntentReceiver);
	}

	@Override
	public boolean filter(String signal, ISignalListener listener,
			Object... params) {
		return true;
	}

	private class SignalLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			/*
			for(JSONObject place : LocationSignalEmitter.this._places) {
				PlaceSignalInfo info = new PlaceSignalInfo(place.toString(), location);
				if(info.getDistance() < _distance) {
					EngineContext.log().error("Distance: " + info.getDistance() + " to " + info.getName());
				} else {
					EngineContext.log().info("Distance: " + info.getDistance() + " to " + info.getName());
				}
				
			}
			EngineContext.log().debug("===================");
			*/
			
			LocationSignalInfo locationInfo = new LocationSignalInfo(location);
			LocationSignalEmitter.this.fire(
					LocationSignal.LOCATION_CHANGED.toString(), locationInfo);
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
	
	private class QueryLocationListener implements LocationListener {
		
		private String _signal;
		private ISignalListener _listener;
		private HttpQueue _queue;
		
		private Location _lastLocation = null;
		private float _minDistance = 0;

		public QueryLocationListener(String strSignal, ISignalListener listener) {
			this._signal = strSignal;
			this._listener = listener;
			_queue = HttpQueueManager.create();
		}
		
		public void setLastLocation(Location location) {
			this._lastLocation = location;
		}
		
		public void setDistance(float distance) {
			this._minDistance = distance;
		}
		
		private boolean isTooEarly(Location location) {
			if(_lastLocation != null){ // Avoid too frequent calls 
				if(location.distanceTo(_lastLocation) < _minDistance) {
					return true; // keep listening for changes
				}
			}
			return false;
		}

		@Override
		public void onLocationChanged(Location location) {
			if(isTooEarly(location)) {
				return;
			}
			
			EngineContext.log().info("Query location changed");
			
			_locationManager.removeUpdates(this); // remove update since this will be recreated as needed

			
			if(getListenerBySignalAndId(_signal, _listener.getUuid().toString()) == null) {
				EngineContext.log().info("Listener removed, stopping search loop");
				return; // drop list rebuild
			}
			
			// remove previous alerts
			ArrayList<ISignalListener> listeners = new ArrayList<ISignalListener>();
			listeners.add(_listener);
			
			removeQueryAlert(_signal, listeners);
			
			// API for Nearby search
			String strName = _listener.getParam("name");
			String strKeyword = _listener.getParam("keyword");
			String strType = _listener.getParam("type");
			String strQuery = _listener.getParam("query");
			
			int searchRadius = Integer.valueOf(_listener.getParam("distance")); // not in use right now..
			
			searchRadius = (int)(searchRadius * 2.3);
			
			searchRadius = searchRadius < 1500 ? 1500 : searchRadius;
			searchRadius = searchRadius > 50000 ? 35000 : searchRadius;
			
			
			
			if(strName != null) {
				strName = "&name=" + Uri.encode(strName);
			} else {
				strName = "";
			}
			
			if(strKeyword != null) {
				strKeyword = "&keyword=" + Uri.encode(strKeyword);
			} else {
				strKeyword = "";
			}
			
			if(strType != null) {
				strType = "&type=" + Uri.encode(strType);
			} else {
				strType = "";
			}
			
			if(strQuery != null) {
				strQuery = "&query=" + Uri.encode(strQuery);
			} else {
				strQuery = "";
			}
			
			// API for textsearch
			
			// Call Google Places to find out where
			
			// key AIzaSyDkuXfVO8HMZrKjdpTN-ufVxbDF2yH70sI
			String key = "key=AIzaSyDkuXfVO8HMZrKjdpTN-ufVxbDF2yH70sI";
			// location (current lat,lon)
			String strLatLon = String.format("location=%s,%s", String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())); 
			// radius (4 times distance, no less than 3km, no more than 35km)
			String radius = "radius=" + searchRadius;
			// sensor = true
			String sensor = "sensor=true";
			// language pt-BR
			String language = "language=pt-BR";
			
			StringBuilder locationApiUrl = new StringBuilder();
			locationApiUrl.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
			//locationApiUrl.append("https://maps.googleapis.com/maps/api/place/textsearch/json?");
			locationApiUrl.append(strLatLon);
			locationApiUrl.append("&rankby=distance&");
			//locationApiUrl.append(radius);
			//locationApiUrl.append("&");
			locationApiUrl.append(sensor);
			locationApiUrl.append("&");
			locationApiUrl.append(language);
			locationApiUrl.append("&");
			locationApiUrl.append(key);
			
			locationApiUrl.append(strQuery);
			locationApiUrl.append(strName);
			locationApiUrl.append(strType);
			locationApiUrl.append(strKeyword);
			
			EngineContext.log().info("Calling Google Places url: " + locationApiUrl.toString());
			
			_queue.fireEnsureCallback(new HttpQueueRequest("GET", locationApiUrl.toString(), null, new FutureCallback<HttpEntity>() { // enqueue HTTP requisition
				
				@Override
				public void onSuccess(HttpEntity entity) {
					
					String jsonContent = "";
					try {
						jsonContent = EntityUtils.toString(entity);
					} catch (Exception e) {
					}
					
					LocationSignalEmitter.this.parseQueryResponse(jsonContent, _signal, _listener);
					
				}
				
				@Override
				public void onFailure(Throwable error) {
					error.printStackTrace();
				}
			}));
			
			
			
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
	}

}

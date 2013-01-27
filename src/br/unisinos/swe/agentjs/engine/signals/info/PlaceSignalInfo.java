package br.unisinos.swe.agentjs.engine.signals.info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSGetter;

import android.location.Location;

public class PlaceSignalInfo {

	private String _id = "";
	private String _name = "";
	private double _rating = 0;
	private String _address = "";
	private double _latitude = 0;
	private double _longitude = 0;
	private String _types = "";
	private Location _lastLocation;

	public PlaceSignalInfo() {

	}

	public PlaceSignalInfo(JSONObject place, Location lastLocation) {
		build(place, lastLocation);
	}

	public PlaceSignalInfo(String jsonPlace, Location lastLocation) {
		try {

			JSONObject place = new JSONObject(jsonPlace);
			build(place, lastLocation);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void build(JSONObject place, Location lastLocation) {
		_lastLocation = lastLocation;

		_id = place.optString("id");
		_latitude = place.optJSONObject("geometry").optJSONObject("location")
				.optDouble("lat");
		_longitude = place.optJSONObject("geometry").optJSONObject("location")
				.optDouble("lng");
		_name = place.optString("name");
		_address = place.optString("vicinity");
		_rating = place.optDouble("rating");

		JSONArray arrTypes = place.optJSONArray("types");

		for (int idx = 0; idx < arrTypes.length(); idx++) {
			_types += arrTypes.optString(idx) + ", ";
		}
	}

	@JSGetter("id")
	public String getId() {
		return this._id;
	}

	@JSGetter("name")
	public String getName() {
		return this._name;
	}

	@JSGetter("address")
	public String getAddress() {
		return this._address;
	}

	@JSGetter("rating")
	public double getRating() {
		return this._rating;
	}

	@JSGetter("latitude")
	public double getLatitude() {
		return this._latitude;
	}

	@JSGetter("longitude")
	public double getLongitude() {
		return this._longitude;
	}

	@JSGetter("distance")
	public float getDistance() {
		Location placeLocation = new Location("GooglePlacesAPI");
		placeLocation.setLatitude(this._latitude);
		placeLocation.setLongitude(this._longitude);
		return placeLocation.distanceTo(_lastLocation);
	}

}

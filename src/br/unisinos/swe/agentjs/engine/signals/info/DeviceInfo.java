package br.unisinos.swe.agentjs.engine.signals.info;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import br.unisinos.swe.agentjs.engine.EngineContext;

public class DeviceInfo {
	
	private String _osVersion;
	private int _apiLevel;
	private String _device;
	private String _model;
	private String _product;
	private String _imei;
	private String _imsi;

	public DeviceInfo() {
		this._osVersion = System.getProperty("os.version"); // OS version
		this._apiLevel = android.os.Build.VERSION.SDK_INT;      // API Level
		this._device = android.os.Build.DEVICE;           // Device
		this._model = android.os.Build.MODEL;            // Model 
		this._product = android.os.Build.PRODUCT;          // Product
		
		TelephonyManager telephonyManager = (TelephonyManager) EngineContext.instance().getContext().getSystemService(Context.TELEPHONY_SERVICE);
		this._imei = telephonyManager.getDeviceId();
		this._imsi = telephonyManager.getSubscriberId();
		
	}

	public JSONObject toJson() {
		JSONObject selfJson = new JSONObject();
		
		try {
			selfJson.put("osVersion", _osVersion);
			selfJson.put("apiLevel", _apiLevel);
			selfJson.put("device", _device);
			selfJson.put("model", _model);
			selfJson.put("product", _product);
			selfJson.put("imei", _imei);
			selfJson.put("imsi", _imsi);
			
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating json object of Device Info");
		}
		
		return selfJson;
	}

}

package br.unisinos.swe.agentjs.engine.signals.info;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSGetter;

import br.unisinos.swe.agentjs.engine.EngineContext;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;

@SuppressLint("DefaultLocale")
public class WifiSignalBasicInfo {

	private WifiInfo _wifiInfo;
	
	public WifiSignalBasicInfo() {
		
	}
	
	public WifiSignalBasicInfo(WifiInfo wifiInfo) {
		_wifiInfo = wifiInfo;
	}
	
	@JSGetter("ssid")
	public String getSSID() {
		return _wifiInfo.getSSID();
	}
	
	@JSGetter("rssi")
	public int getRssi() {
		return _wifiInfo.getRssi();
	}
	
	@JSGetter("linkSpeed")
	public int getLinkSpeed() {
		return _wifiInfo.getLinkSpeed();
	}
	
	@JSGetter("ip")
	public String getIp() {
		int ipAddress = _wifiInfo.getIpAddress();
		
		return String.format("%d.%d.%d.%d", 
				(ipAddress & 0xff), 
				(ipAddress >> 8 & 0xff), 
				(ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
	}

	public JSONObject toJson() {
		JSONObject selfJson = new JSONObject();
		
		try {
			selfJson.put("ssid", this.getSSID());
			selfJson.put("rssi", this.getRssi());
			selfJson.put("linkSpeed", this.getLinkSpeed());
			selfJson.put("ip", this.getIp());
			
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating json object of Wifi Info");
		}
		
		return selfJson;
	}

}

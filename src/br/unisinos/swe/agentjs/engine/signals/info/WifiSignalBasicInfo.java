package br.unisinos.swe.agentjs.engine.signals.info;

import org.mozilla.javascript.annotations.JSGetter;

import android.net.wifi.WifiInfo;

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

}

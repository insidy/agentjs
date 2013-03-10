package br.unisinos.swe.agentjs.engine.signals.info;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.annotations.JSGetter;

import android.net.wifi.ScanResult;

public class ScanResultInfo {

	private ScanResult _scan;
	
	public ScanResultInfo() {
		
	}
	
	public ScanResultInfo(ScanResult scanResult) {
		_scan = scanResult;
	}

	public static List<ScanResultInfo> fromScanResultList(
			List<ScanResult> scanResults) {
		List<ScanResultInfo> scanResultInfo = new ArrayList<ScanResultInfo>();
		
		for(ScanResult scanResult : scanResults) {
			scanResultInfo.add(new ScanResultInfo(scanResult));
		}
		
		return scanResultInfo;
	}
	
	@JSGetter("ssid")
	public String getSSID() {
		return _scan.SSID;
	}
	
	@JSGetter("bssid")
	public String getBSSID() {
		return _scan.BSSID;
	}
	
	@JSGetter("frequency")
	public int getFrequency() {
		return _scan.frequency;
	}
	
	@JSGetter("level")
	public int getLevel() {
		return _scan.level;
	}
}

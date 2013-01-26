package br.unisinos.swe.agentjs.engine.signals.info;

import org.mozilla.javascript.annotations.JSFunction;

import android.content.Intent;
import android.os.BatteryManager;

public class BatterySignalInfo {

	protected int _status = 1;
	protected int _plugged = 0;
	protected int _health = 0;
	protected int _level = 0;
	protected int _scale = 0;

	public BatterySignalInfo() {

	}

	public BatterySignalInfo(Intent batteryStatus) {
		_status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
		_plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
		_health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
		_level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		_scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
	}

	@JSFunction("isPlugged")
	public boolean isPlugged() {
		return (_plugged > 0);
	}

	@JSFunction("getPowerSource")
	public String getPowerSource() {
		switch (_plugged) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			return "AC";
		case BatteryManager.BATTERY_PLUGGED_USB:
			return "USB";
		default:
			return "BATTERY";
		}
	}

	@JSFunction("getBatteryLevel")
	public String getBatteryLevel() {
		return String.valueOf((100 * _level / _scale));
	}

	@JSFunction("getStatus")
	public String getStatus() {
		switch (_status) {
		case BatteryManager.BATTERY_STATUS_CHARGING:
			return "CHARGING";
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			return "DISCHARGING";
		case BatteryManager.BATTERY_STATUS_FULL:
			return "FULL";
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			return "NOT CHARGING";
		case BatteryManager.BATTERY_STATUS_UNKNOWN:
			return "UNKNOWN";
		default:
			return "UNKNOWN";
		}
	}
	/*
	 * public String getHealth() { return null; }
	 */

}

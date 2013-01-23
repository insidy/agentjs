package br.unisinos.swe.agentjs.engine.signals.info;

import org.mozilla.javascript.annotations.JSFunction;

import android.net.NetworkInfo;

public class NetworkSignalBasicInfo {	
	private NetworkInfo _info = null;
	
	public NetworkSignalBasicInfo() {
		
	}
	
	public NetworkSignalBasicInfo(NetworkInfo info) {
		this._info = info;
	}
	
	@JSFunction("isRoaming")
	public boolean isRoaming() {
		return _info.isRoaming();
	}
	
	@JSFunction("isFailover")
	public boolean isFailover() {
		return _info.isFailover();
	}
	
	@JSFunction("isConnectedOrConnecting")
	public boolean isConnectedOrConnecting() {
		return _info.isConnectedOrConnecting();
	}
	
	@JSFunction("isConnected")
	public boolean isConnected() {
		return _info.isConnected();
	}
	
	@JSFunction("isAvailable")
	public boolean isAvailable() {
		return _info.isAvailable();
	}
	
	@JSFunction("getTypeName")
	public String getTypeName() {
		return _info.getTypeName();
	}
	
	@JSFunction("getType")
	public int getType() {
		return _info.getType();
	}
	
	@JSFunction("getSubtypeName")
	public String getSubtypeName() {
		return _info.getSubtypeName();
	}
	
	@JSFunction("getSubtype")
	public int getSubtype() {
		return _info.getSubtype();
	}
	
	@JSFunction("getState")
	public String getState() {
		return _info.getState().toString();
	}
	
	@JSFunction("getDetailedState")
	public String getDetailedState() {
		return _info.getDetailedState().toString();
	}
	
}

package br.unisinos.swe.agentjs.engine.db;

import org.json.JSONObject;

import br.unisinos.swe.agentjs.engine.Engine;
import br.unisinos.swe.agentjs.engine.upnp.DeviceWrapper;

public class AgentNetworkScript extends AgentScript {
	
	private DeviceWrapper _device;
	
	public Engine engine;
	public boolean initializeOnSource = false;
	
	public AgentNetworkScript(DeviceWrapper device, JSONObject agentObject) {
		super(agentObject);
		
		_type = AgentScript.AgentScriptLocation.NETWORK;
		_device = device;
	}
	
	public String getOrigin() {
		return _device.getDevice().getDisplayString();
	}
	
	public DeviceWrapper getDeviceWrapper() {
		return _device;
	}

}

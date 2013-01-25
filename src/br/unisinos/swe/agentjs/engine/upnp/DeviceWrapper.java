package br.unisinos.swe.agentjs.engine.upnp;

import java.util.UUID;

import org.teleal.cling.model.meta.Device;

public class DeviceWrapper {

	private final String _udn;
	private final Device _device;
	public DeviceWrapper(Device upnpDevice) {
		_udn = upnpDevice.getIdentity().getUdn().getIdentifierString();
		_device = upnpDevice;
	}
	
	public Device getDevice() {
		return _device;
	}
	
	public String getId() {
		return _udn;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if(otherObject == null)
			return false;

		if(!(otherObject instanceof DeviceWrapper))
			return false;
		
		if(this == otherObject)
			return true;

		DeviceWrapper otherScript = (DeviceWrapper)otherObject;
		
		return otherScript.getId().equals(this.getId());
	}
	
	@Override
	public int hashCode() {
		return _udn.hashCode();
	}
}

package br.unisinos.swe.agentjs.engine.upnp;

import java.util.List;

import br.unisinos.swe.agentjs.engine.db.AgentScript;

public interface IAgentUPnPListener {
	
	public void deviceRemoved(DeviceWrapper deviceWrapper);
	public void deviceAdded(DeviceWrapper deviceWrapper);
	
	public void connected();
	
	public void setAvailableAgents(DeviceWrapper device, List<AgentScript> agents);
	public void setAgentSourceCode(DeviceWrapper device, AgentScript agent);
	
	
}

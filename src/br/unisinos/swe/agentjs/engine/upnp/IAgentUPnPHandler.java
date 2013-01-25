package br.unisinos.swe.agentjs.engine.upnp;

public interface IAgentUPnPHandler {

	void startDiscovery(IAgentUPnPListener listener);
	void stopDiscovery(IAgentUPnPListener listener);
	void getAgents(DeviceWrapper device, IAgentUPnPListener listener);
	void getSourceCode(DeviceWrapper device, String agentId, IAgentUPnPListener listener);

}

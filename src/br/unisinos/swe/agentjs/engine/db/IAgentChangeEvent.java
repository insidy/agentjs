package br.unisinos.swe.agentjs.engine.db;

public interface IAgentChangeEvent {
	
	public void addAgent(AgentScript script);
	public void removeAgent(AgentScript script);
	public void agentStateChanged(AgentScript script);
}

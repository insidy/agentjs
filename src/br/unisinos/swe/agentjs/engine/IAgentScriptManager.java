package br.unisinos.swe.agentjs.engine;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.db.AgentScript;

public interface IAgentScriptManager extends IEngineComponent {

	public abstract ArrayList<AgentScript> getLocalScripts();
	public abstract ArrayList<AgentScript> getNetworkScripts();
	public abstract void startScript(Engine engine, AgentScript script);

}
package br.unisinos.swe.agentjs.engine;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.db.AgentScript;
import br.unisinos.swe.agentjs.engine.db.AgentScript.AgentScriptLocation;
import br.unisinos.swe.agentjs.engine.db.IAgentChangeEvent;

public interface IAgentScriptManager extends IEngineComponent {

	public abstract ArrayList<AgentScript> getLocalScripts();
	public abstract ArrayList<AgentScript> getNetworkScripts();
	public abstract void startScript(Engine engine, AgentScript script);
	public abstract void registerListener(IAgentChangeEvent listener);
	public abstract void removeListener(IAgentChangeEvent listener);
	public abstract void refreshFromWeb();

}
package br.unisinos.swe.agentjs.ui;

import br.unisinos.swe.agentjs.engine.db.AgentScript;
import br.unisinos.swe.agentjs.engine.db.AgentScript.AgentScriptLocation;

public class AgentScriptView {

	private AgentScript _agentScript;
	public AgentScriptView(AgentScript script) {
		_agentScript = script;
	}

	public String getAgentId() {
		return _agentScript.getId();
	}
	
	public String getName() {
		if(_agentScript.getType() == AgentScriptLocation.OWN) {
			return _agentScript.getName();
		} else {
			String origin = _agentScript.getOrigin();
			origin = (origin.length() > 12 ? origin.substring(0, 10) + ".." : origin);
			return _agentScript.getName() + "(" + origin + ")";
		}
			
		
	}
	
	public boolean isRunning() {
		return _agentScript.isRunning();
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if(otherObject == null)
			return false;

		if(!(otherObject instanceof AgentScriptView))
			return false;
		
		if(this == otherObject)
			return true;

		AgentScriptView otherScript = (AgentScriptView)otherObject;
		
		return otherScript.getAgentId().equals(this.getAgentId());
	}

	public AgentScript getScript() {
		return _agentScript;
	}

}

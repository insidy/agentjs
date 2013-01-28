package br.unisinos.swe.agentjs.engine.db;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import br.unisinos.swe.agentjs.engine.AgentExecutor;
import br.unisinos.swe.agentjs.engine.upnp.DeviceWrapper;

public class AgentScript {

	public static enum AgentScriptLocation {
		OWN, NETWORK;
	}
	
	public static enum AgentScriptState {
		NOT_RUNNING(-1),
		RUNNING(1),
		WAITING_CALLBACK(2),
		DONE(3);
		
		private AgentScriptState(int n) { value = n; }        
	    public final int value;
	}
	
	
	
	protected String _id = "";
	protected String _name = "";
	protected String _sourceCode = "";
	protected String _origin = "";
	private AgentScriptState _executeState = AgentScriptState.NOT_RUNNING;
	private AgentExecutor executor = null;
	
	protected AgentScriptLocation _type;

	public AgentScript() {
		_type = AgentScriptLocation.OWN;
		_id = UUID.randomUUID().toString();
	}
	
	public AgentScript(JSONObject agentObject) {
		this();
		
		try {
			if(agentObject.has("id")) {
				this._id = agentObject.getString("id");
			}
			
			if(agentObject.has("name")) {
				this._name = agentObject.getString("name");
			}
			
			if(agentObject.has("sourceCode")) {
				this._sourceCode = agentObject.getString("sourceCode");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setStarted() {
		this._executeState = AgentScriptState.RUNNING;
	}

	public void setSourceCode(String _sourceCode) {
		this._sourceCode = _sourceCode;
	}

	public String getSourceCode() {
		return _sourceCode;
	}
	
	public void setOrigin(String origin) {
		_origin = origin;
	}
	
	public String getOrigin() {
		return _origin;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public boolean hasSource() {
		if (_sourceCode == null || _sourceCode.trim().length() <= 0) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if(otherObject == null)
			return false;

		if(!(otherObject instanceof AgentScript))
			return false;
		
		if(this == otherObject)
			return true;

		AgentScript otherScript = (AgentScript)otherObject;
		
		return otherScript.getId().equals(this.getId());
	}
	
	public AgentScriptLocation getType() {
		return _type;
	}

	public boolean isRunning() {
		return _executeState != AgentScriptState.NOT_RUNNING;
	}

	public void setFinished() {
		_executeState = AgentScriptState.DONE;
	}

}

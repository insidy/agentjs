package br.unisinos.swe.agentjs.engine.db;

public class AgentScript {

	private String _id;
	private String _sourceCode;
	
	
	public void setSourceCode(String _sourceCode) {
		this._sourceCode = _sourceCode;
	}
	public String getSourceCode() {
		return _sourceCode;
	}
	
	public void setId(String _id) {
		this._id = _id;
	}
	public String getId() {
		return _id;
	}
	
}

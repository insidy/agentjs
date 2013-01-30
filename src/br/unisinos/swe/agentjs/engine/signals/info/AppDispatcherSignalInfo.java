package br.unisinos.swe.agentjs.engine.signals.info;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSGetter;

import br.unisinos.swe.agentjs.engine.EngineContext;

public class AppDispatcherSignalInfo {
	private String _title;
	private String _packageName;
	private int _executionCount;
	
	public AppDispatcherSignalInfo() {
		
	}
	
	public AppDispatcherSignalInfo(String title, String packageName, int execCount) {
		_title = title;
		_packageName = packageName;
		_executionCount = execCount;
	}
	
	@JSGetter("title")
	public String getTitle() {
		return _title;
	}
	
	@JSGetter("packageName")
	public String getPackageName() {
		return _packageName;
	}

	@JSGetter("executionCount")
	public int getExecutionCount() {
		return _executionCount;
	}

	public JSONObject toJson() {
		JSONObject selfJson = new JSONObject();
		
		try {
			selfJson.put("title", this.getTitle());
			selfJson.put("packageName", this.getPackageName());
			
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating json object of App Dispatcher Info");
		}
		
		return selfJson;
	}
	
}

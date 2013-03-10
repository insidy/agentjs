package br.unisinos.swe.agentjs.engine.signals.info;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.annotations.JSGetter;

import br.unisinos.swe.agentjs.engine.EngineContext;

public class AppDispatcherSignalInfo {
	private String _title;
	private String _packageName;
	private int _executionCount;
	private int _importance;
	
	public AppDispatcherSignalInfo() {
		
	}
	
	public AppDispatcherSignalInfo(String title, String packageName, int execCount) {
		_title = title;
		_packageName = packageName;
		_executionCount = execCount;
		_importance = 0;
	}
	
	public AppDispatcherSignalInfo(String title, String packageName, int execCount, int importance) {
		_title = title;
		_packageName = packageName;
		_executionCount = execCount;
		_importance = importance;
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
	
	public int getImportance() {
		return _importance;
	}

	public JSONObject toJson() {
		JSONObject selfJson = new JSONObject();
		
		try {
			String title = this.getTitle();
			if(title == null) {
				title = "";
			}
			
			selfJson.put("name", title);
			selfJson.put("pack", this.getPackageName());
			
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating json object of App Dispatcher Info");
		}
		
		return selfJson;
	}
	
}

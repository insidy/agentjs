package br.unisinos.swe.agentjs.engine.signals.info;

import org.mozilla.javascript.annotations.JSGetter;

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
	
}

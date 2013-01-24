package br.unisinos.swe.agentjs.engine.wrappers;

import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

import android.content.pm.ApplicationInfo;


public class ApplicationInfoWrapper {
	private ApplicationInfo _info = null;
	
	public ApplicationInfoWrapper() {
		
	}
	
	public ApplicationInfoWrapper(ApplicationInfo info) {
		_info = info;
	}
	
	@JSGetter("name")
	public String getName() {
		if(_info.name != null)
			return _info.name;
		else
			return "";
	}
	
	@JSGetter("packageName")
	public String getPackageName() {
		if(_info.packageName != null)
			return _info.packageName;
		else
			return "";
	}
}

package br.unisinos.swe.agentjs.engine.api;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.annotations.JSFunction;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.EngineScriptSandbox;
import br.unisinos.swe.agentjs.engine.signals.AppDispatcherSignalEmitter;
import br.unisinos.swe.agentjs.engine.wrappers.ApplicationInfoWrapper;

public class AgentApplications extends AbstractAgentAPIComponent {
	
	protected PackageManager _pm = null;
	public AgentApplications(AgentExecutorHelper helper) {
		helper.register(this);
		_pm = EngineContext.instance().getContext().getPackageManager();
	}

	@JSFunction("launchAppForUrl")
	public void launchAppForUrl(String url, String mime) {
		
		String adjustedMime = mime;
		Context appContext = EngineContext.instance().getContext();
	    Intent launchIntent = new Intent();
	    launchIntent.setAction("android.intent.action.VIEW");
	    
	    Uri localUri = Uri.parse(url);
	    
	    if (mime == null) {
	      adjustedMime = URLConnection.guessContentTypeFromName(url);
	    }
	    
	    launchIntent.setDataAndType(localUri, adjustedMime);
	    launchIntent.addFlags(805306368);
	    appContext.startActivity(launchIntent);
	}
	
	@JSFunction("launchAppByPackage")
	public void launchAppByPackage(String packageName) {
		Intent launchIntent = _pm.getLaunchIntentForPackage(packageName);
		if(launchIntent != null) {
			EngineContext.instance().getContext().startActivity(launchIntent);
		}
	}
	
	@JSFunction("getInstalledApps")
	public Object getInstalledApps() {
		List<ApplicationInfo> packages = _pm.getInstalledApplications(PackageManager.GET_META_DATA);
		Object[] jsArray = new Object[packages.size()];
		
		int n = 0;
		for(ApplicationInfo info : packages) {
			jsArray[n++] = _helper.javaToJS(new ApplicationInfoWrapper(info)); 
		}
		
		
		return _helper.newArray(jsArray);
	}
	
	@JSFunction("findAppByName")
	public Object findAppByName(String appName) {
		List<ApplicationInfo> packages = _pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for(ApplicationInfo appInfo : packages) {
			if(appInfo.name != null) {
				if(appInfo.name.contains(appName)) {
					return _helper.javaToJS(new ApplicationInfoWrapper(appInfo));
				}
			}
		}
		
		return null;
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		if(_signals == null) { // signal list is only useful for multi-origin signal API
			_signals = new ArrayList<String>();
			for (AppDispatcherSignalEmitter.DispatcherSignal signalEnum : AppDispatcherSignalEmitter.DispatcherSignal.class.getEnumConstants()) { // All network signals
				_signals.add(signalEnum.toString());
			}
		}
		
		return _signals.contains(signal);
	}

}

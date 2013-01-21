package br.unisinos.swe.agentjs.engine.api;

import java.net.URLConnection;

import org.mozilla.javascript.annotations.JSFunction;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.EngineContext;

public class AgentApplications extends AbstractAgentAPIComponent {
	
	public AgentApplications(AgentExecutorHelper helper) {
		helper.register(this);
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

	@Override
	protected boolean isOwnSignal(String signal) {
		// TODO Auto-generated method stub
		return false;
	}

}

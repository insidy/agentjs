package br.unisinos.swe.agentjs.engine;

import android.content.Context;
import android.os.Build;

public class EngineUtils {
	
	public static String getUserAgent(Context appContext)
	  {
	    Object[] userAgentParams = new Object[4];
	    StringBuilder sbUserAgent = new StringBuilder("");
	    
	    userAgentParams[0] = "AgentJS"; //@TODO: Make this dynamic
	    userAgentParams[1] = "0.0.1"; //@TODO: Make this dynamic
	    userAgentParams[2] = Build.VERSION.RELEASE;
	    userAgentParams[3] = Build.MODEL;
	    sbUserAgent.append(String.format("%s/%s/ (Android %s; %s )", userAgentParams));
	    return sbUserAgent.toString();
	  }

}

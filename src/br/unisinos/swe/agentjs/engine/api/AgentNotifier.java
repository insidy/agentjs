package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.annotations.JSFunction;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.signals.SmsSignalEmitter.SmsSignal;

import android.telephony.SmsManager;

public class AgentNotifier extends AbstractAgentAPIComponent {

	
	public AgentNotifier(AgentExecutorHelper helper) {
		helper.register(this);
	}
	
	@JSFunction("send")
	public void send(NativeObject requestParams) {
		String title = (String) Context.jsToJava(requestParams.get("title", requestParams), String.class);
		String content = (String) Context.jsToJava(requestParams.get("content", requestParams), String.class);
		Boolean vibrate = (Boolean) Context.jsToJava(requestParams.get("vibrate", requestParams), Boolean.class);
		String sound = (String) Context.jsToJava(requestParams.get("sound", requestParams), String.class);
		
		AgentNotification newNotif = new AgentNotification(title);
		newNotif.setContent(content);
		
		if(vibrate != null) {
			newNotif.setVibrate(vibrate);
		}
		
		if(sound != null) {
			newNotif.setSoundUrl(sound);
		}
		
		newNotif.send();
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		return false;
	}
}

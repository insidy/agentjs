package br.unisinos.swe.agentjs.engine.api;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.annotations.JSFunction;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;

import android.telephony.SmsManager;

public class AgentSMS extends AbstractAgentAPIComponent {

	
	public AgentSMS(AgentExecutorHelper helper) {
		helper.register(this);
	}
	
	@JSFunction("send")
	public void send(NativeObject requestParams, Object callbackFunc) {
		String destination = (String) Context.jsToJava(requestParams.get("destination", requestParams), String.class);
		String message = (String) Context.jsToJava(requestParams.get("message", requestParams), String.class);
		try {
			createSmsMessage(destination, message);
		} catch(Exception e) {
			_helper.callback(callbackFunc, e.getMessage());
			return;
		}
		_helper.callback(callbackFunc);
		
	}
	
	private void createSmsMessage(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		return false;
	}
}

package br.unisinos.swe.agentjs.engine.signals.info;

import org.mozilla.javascript.annotations.JSFunction;

import android.telephony.SmsMessage;

public class SmsSignalInfo {
	
	private SmsMessage _sms;
	
	public SmsSignalInfo() {
		
	}
	
	public SmsSignalInfo(SmsMessage sms) {
		_sms = sms;
		
	}
	
	@JSFunction("getMessage")
	public String getMessage() {
		return _sms.getMessageBody();
	}
	
	@JSFunction("getOriginAddress")
	public String getOriginAddress() {
		return _sms.getOriginatingAddress();
	}

}

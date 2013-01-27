package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.info.SmsSignalInfo;

public class SmsSignalEmitter extends AbstractSignalEmitter {

	public static enum SmsSignal {
		SMS_IN("sms:income");

		private SmsSignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}
	}

	private BroadcastReceiver _incomingSmsReceiver;

	// android.provider.Telephony.SMS_RECEIVED

	public SmsSignalEmitter() {
		super();
		if (_signals == null) {
			_signals = new ArrayList<String>();
			for (SmsSignal signal : SmsSignal.class.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}
	}

	public static ISignalEmitter create() {
		return new SmsSignalEmitter();
	}

	@Override
	public ISignalEmitter start() {
		
		IntentFilter incomingSmsFilter = new IntentFilter();
		incomingSmsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		this._incomingSmsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				

				Bundle localBundle = broadcastIntent.getExtras();
		        if (localBundle != null)
		          try
		          {
		            Object[] arrMessages = (Object[])localBundle.get("pdus");
		            SmsMessage[] arrSms = new SmsMessage[arrMessages.length];
		            for (int i = 0; i < arrSms.length; i++)
		            {
		              SmsMessage smsMessage = SmsMessage.createFromPdu((byte[])arrMessages[i]);
		              SmsSignalInfo smsInfo = new SmsSignalInfo(smsMessage);
		              
		              SmsSignalEmitter.this.fire(SmsSignal.SMS_IN.toString(), smsInfo);
		              
		              //Messaging.this.smsReceived(localSmsMessage.getOriginatingAddress(), localSmsMessage.getMessageBody());
		            }
		          }
		          catch (Exception ex)
		          {
		            ex.printStackTrace();
		          }
				
				
			}
		};
		
		EngineContext.instance().getContext().registerReceiver(this._incomingSmsReceiver, incomingSmsFilter);
		
		return this;
	}

	@Override
	public void stop() {
		EngineContext.instance().getContext().unregisterReceiver(this._incomingSmsReceiver);
	}

	@Override
	public boolean filter(String signalString, ISignalListener listener,
			Object... params) {
		boolean match = true;
		
		if(SmsSignal.SMS_IN.toString().equals(signalString)) {
			SmsSignalInfo info = (SmsSignalInfo)params[0];
			
			
			if(listener.getParam("origin") != null && info != null) { // Filter by origin
				match = false;
				
				if(listener.getParam("origin").equalsIgnoreCase(info.getOriginAddress())) {
					match = true;
				}
			}
		}
		
		return match;
	}

}

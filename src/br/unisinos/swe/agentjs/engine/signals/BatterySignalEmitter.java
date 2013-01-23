package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.EngineContext;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class BatterySignalEmitter extends AbstractSignalEmitter {
	private AlarmManager _polling;
	private PendingIntent _operation;
	
	public static enum BatterySignal {
		BATTERY_LOW("battery:low"), BATTERY_OKAY("battery:ok"), BATTERY_POWER_ON("power:connected"), BATTERY_POWER_OFF("power:disconnected");

		private BatterySignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}
	}
	
	protected static class BatteryPolling extends BroadcastReceiver { 
	     private IntentFilter _batteryStatusFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		 @Override
	     public void onReceive(Context context, Intent intent) {
	    	 Intent batteryStatus = EngineContext.instance().getContext().registerReceiver(null, _batteryStatusFilter);
	    	 
	    	 //TODO fix battery signal
	    	 // Get Extras:
	    	 //http://developer.android.com/reference/android/os/BatteryManager.html
	    	 
	    	 // Fire events
	    	 
	     }
	}
	
	public BatterySignalEmitter() {
		super();
		if(_signals == null) {
			_signals = new ArrayList<String>();
			for (BatterySignal signal : BatterySignal.class.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}
	}

	@Override
	public ISignalEmitter start() {
		
		_polling = (AlarmManager)EngineContext.instance().getContext().getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent(EngineContext.instance().getContext(), BatteryPolling.class);
		_operation = PendingIntent.getBroadcast(EngineContext.instance().getContext(), 0, i, 0);

		_polling.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, _operation);
		
		return this;
	}

	@Override
	public void stop() {
		_polling.cancel(_operation);
	}

}

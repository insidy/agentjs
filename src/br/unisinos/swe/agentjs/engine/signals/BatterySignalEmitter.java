package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.info.BatterySignalInfo;

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
	private BroadcastReceiver _batteryStatusLowReceiver = null;
	private BroadcastReceiver _batteryStatusOkayReceiver;
	private BroadcastReceiver _batteryAcOnReceiver;
	private BroadcastReceiver _batteryAcOffReceiver;
	
	public static enum BatterySignal {
		BATTERY_INFO("battery:info"), BATTERY_LOW("battery:low"), BATTERY_OKAY("battery:ok"), BATTERY_POWER_ON("power:connected"), BATTERY_POWER_OFF("power:disconnected");

		private BatterySignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}
	}
	
	protected class BatteryPolling extends BroadcastReceiver { 
	     private IntentFilter _batteryStatusFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	     protected BatterySignalInfo previousInfo = null; 
	     
		 @Override
	     public void onReceive(Context context, Intent intent) {
	    	 Intent batteryStatus = EngineContext.instance().getContext().registerReceiver(null, _batteryStatusFilter);
	    	 
	    	 BatterySignalInfo info = new BatterySignalInfo(batteryStatus);
	    	 
	    	 // Get Extras:
	    	 //http://developer.android.com/reference/android/os/BatteryManager.html
	    	 
	    	 // Fire events
	    	 BatterySignalEmitter.this.fire(BatterySignal.BATTERY_INFO.toString(), info);
	    	 
	    	 previousInfo = info;
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

	public static ISignalEmitter create() {
		return new BatterySignalEmitter();
	}

	@Override
	public ISignalEmitter start() {
		
		_polling = (AlarmManager)EngineContext.instance().getContext().getSystemService(Context.ALARM_SERVICE);
		
		Intent i = new Intent(EngineContext.instance().getContext(), BatteryPolling.class);
		_operation = PendingIntent.getBroadcast(EngineContext.instance().getContext(), 0, i, 0);

		_polling.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HALF_HOUR, _operation);
		
		
		
		IntentFilter batteryStatusLowFilter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
		this._batteryStatusLowReceiver  = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				BatterySignalEmitter.this.fire(BatterySignal.BATTERY_LOW.toString());
			}
		};
		
		IntentFilter batteryStatusOkayFilter = new IntentFilter(Intent.ACTION_BATTERY_OKAY);
		this._batteryStatusOkayReceiver  = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				BatterySignalEmitter.this.fire(BatterySignal.BATTERY_OKAY.toString());
			}
		};
		
		IntentFilter batteryAcOnFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		this._batteryAcOnReceiver  = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				BatterySignalEmitter.this.fire(BatterySignal.BATTERY_POWER_ON.toString());
			}
		};
		
		IntentFilter batteryAcOffFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
		this._batteryAcOffReceiver  = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				BatterySignalEmitter.this.fire(BatterySignal.BATTERY_POWER_OFF.toString());
			}
		};
		
		
		EngineContext.instance().getContext().registerReceiver(this._batteryStatusOkayReceiver, batteryStatusOkayFilter);
		EngineContext.instance().getContext().registerReceiver(this._batteryStatusLowReceiver, batteryStatusLowFilter);
		EngineContext.instance().getContext().registerReceiver(this._batteryAcOnReceiver, batteryAcOnFilter);
		EngineContext.instance().getContext().registerReceiver(this._batteryAcOffReceiver, batteryAcOffFilter);
		
		
		return this;
	}

	@Override
	public void stop() {
		_polling.cancel(_operation);
		EngineContext.instance().getContext().unregisterReceiver(this._batteryStatusOkayReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._batteryStatusLowReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._batteryAcOnReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._batteryAcOffReceiver);
	}

	@Override
	public boolean filter(String signal, ISignalListener listener, Object...params) {
		return true; // no filter available
	}

}

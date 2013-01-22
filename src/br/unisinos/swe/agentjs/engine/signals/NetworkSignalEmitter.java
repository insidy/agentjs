package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.NativeArray;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import br.unisinos.swe.agentjs.engine.EngineContext;

public class NetworkSignalEmitter extends AbstractSignalEmitter {

	/**
	 * Network Available Signals
	 * @author Paulo
	 *
	 */
	public static enum NetworkSignal {
		WIFI_ON("wifi:on"), WIFI_OFF("wifi:off"), WIFI_SCAN("wifi:scan"), WIFI_CONNECTED("wifi:connected"), WIFI_DISCONNECTED("wifi:disconnected");

		private NetworkSignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}
	}

	private BroadcastReceiver _scanResultsReceiver;
	private BroadcastReceiver _networkStateChangedReceiver;
	private BroadcastReceiver _wifiStateChangedReceiver;
	private WifiManager _wifiManager;
	

	@Override
	public List<String> getSignals() {
		if(_signals == null) {
			_signals = new ArrayList<String>();
			for (NetworkSignal signal : NetworkSignal.class.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}
		return _signals;
	}

	public static ISignalEmitter create() {
		if (_instance == null) {
			_instance = new NetworkSignalEmitter();
		}
		return _instance;
	}

	@Override
	public ISignalEmitter start() {
		// create listeners
		this._wifiManager = ((WifiManager) EngineContext.instance()
				.getContext().getSystemService(Context.WIFI_SERVICE));
		
		IntentFilter scanResultFilter = new IntentFilter();
		scanResultFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		this._scanResultsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				List<ScanResult> scanResults = _wifiManager.getScanResults(); // convert to NativeArray of ?
				//TODO Implement object conversion
				
				NativeArray array = new NativeArray(scanResults.toArray());
				NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_SCAN.toString(), array);

			}
		};
		
		IntentFilter networkStateChangedFilter = new IntentFilter();
		networkStateChangedFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		this._networkStateChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				NetworkInfo networkInfo = broadcastIntent.<NetworkInfo>getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				//TODO Implement object conversion
				
				if(networkInfo.isConnected()) {
					
					WifiInfo wifiInfo = broadcastIntent.<WifiInfo>getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
					NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_CONNECTED.toString());
				} else {
					NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_DISCONNECTED.toString());
				}

			}
		};
		
		IntentFilter wifiStateChangedFilter = new IntentFilter();
		wifiStateChangedFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		this._wifiStateChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				int state = broadcastIntent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				
				if(state == WifiManager.WIFI_STATE_ENABLED) {
					NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_ON.toString());
				} else if(state == WifiManager.WIFI_STATE_DISABLED) {
					NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_OFF.toString());
				}

			}
		};
		
		
		
		EngineContext.instance().getContext().registerReceiver(this._scanResultsReceiver, scanResultFilter);
		EngineContext.instance().getContext().registerReceiver(this._networkStateChangedReceiver, networkStateChangedFilter);
		EngineContext.instance().getContext().registerReceiver(this._wifiStateChangedReceiver, wifiStateChangedFilter);

		return this;
	}

	@Override
	public void stop() {
		EngineContext.instance().getContext().unregisterReceiver(this._scanResultsReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._networkStateChangedReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._wifiStateChangedReceiver);
	}

}

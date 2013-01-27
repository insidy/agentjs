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
import br.unisinos.swe.agentjs.engine.signals.info.NetworkSignalBasicInfo;
import br.unisinos.swe.agentjs.engine.signals.info.WifiSignalBasicInfo;

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

	protected static final int API_LEVEL_14 = 14;

	private BroadcastReceiver _scanResultsReceiver;
	private BroadcastReceiver _networkStateChangedReceiver;
	private BroadcastReceiver _wifiStateChangedReceiver;
	private WifiManager _wifiManager;
	
	public NetworkSignalEmitter() {
		super();
		if(_signals == null) {
			_signals = new ArrayList<String>();
			for (NetworkSignal signal : NetworkSignal.class.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}
	}

	public static ISignalEmitter create() {
		return new NetworkSignalEmitter();
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
		//networkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		this._networkStateChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				NetworkInfo networkInfo = null;
				WifiInfo wifiInfo = null;
				if(broadcastIntent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
					networkInfo = broadcastIntent.<NetworkInfo>getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					
					if(android.os.Build.VERSION.SDK_INT >= API_LEVEL_14) {
						wifiInfo = broadcastIntent.<WifiInfo>getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
					} else {
						wifiInfo = _wifiManager.getConnectionInfo();
					}
				}
				//TODO Implement object conversion
				NetworkSignalBasicInfo basicInfo = new NetworkSignalBasicInfo(networkInfo);
				
				WifiSignalBasicInfo wifBasicInfo = new WifiSignalBasicInfo(wifiInfo);
				
				
				if(networkInfo.isConnected()) {
					NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_CONNECTED.toString(), basicInfo, wifBasicInfo);
				} else {
					NetworkSignalEmitter.this.fire(NetworkSignal.WIFI_DISCONNECTED.toString(), basicInfo);
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
		EngineContext.log().info("Stopping NetworkSignalEmitter");
		EngineContext.instance().getContext().unregisterReceiver(this._scanResultsReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._networkStateChangedReceiver);
		EngineContext.instance().getContext().unregisterReceiver(this._wifiStateChangedReceiver);
	}

	@Override
	public boolean filter(String signal, ISignalListener listener, Object...params) {
		return true; // no filter available
	}

}

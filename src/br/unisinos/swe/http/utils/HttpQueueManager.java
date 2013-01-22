package br.unisinos.swe.http.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter.NetworkSignal;

public class HttpQueueManager {
	
	protected static List<HttpQueue> _queues = new ArrayList<HttpQueue>();
	protected static LinkedList<HttpQueue> _haltedQueues = new LinkedList<HttpQueue>();
	private static BroadcastReceiver _networkStateChangedReceiver;

	protected HttpQueueManager() {
		
	}

	public static HttpQueue create() {
		checkUncompletedQueues();
		
		HttpQueue newQueue = new HttpQueue();
		_queues.add(newQueue);
		return newQueue;
	}

	public static HttpQueue create(int maxthreads) {
		checkUncompletedQueues();
		
		HttpQueue newQueue = new HttpQueue(maxthreads);
		_queues.add(newQueue);
		return newQueue;
		
	}
	
	public static void haltQueue(HttpQueue halt) {
		_haltedQueues.push(halt);
		
		
		if(_networkStateChangedReceiver == null) {
			IntentFilter networkStateChangedFilter = new IntentFilter();
			networkStateChangedFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			_networkStateChangedReceiver = new BroadcastReceiver() {
	
				@Override
				public void onReceive(Context appContext, Intent broadcastIntent) {
					NetworkInfo networkInfo = broadcastIntent.<NetworkInfo>getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					
					if(networkInfo.isConnected()) {
						HttpQueue queue = null;
						while((queue = _haltedQueues.poll()) != null) {
							queue.wakeUp();
						}
						
						EngineContext.instance().getContext().unregisterReceiver(_networkStateChangedReceiver);
						_networkStateChangedReceiver = null;
					}
	
				}
			};
			
			EngineContext.instance().getContext().registerReceiver(_networkStateChangedReceiver, networkStateChangedFilter);
		}
	}
	
	private static void checkUncompletedQueues() {
		
	}
}

package br.unisinos.swe.http.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
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
		_haltedQueues.addLast(halt);
		
		
		if(_networkStateChangedReceiver == null) {
			Log.i(HttpQueue.TAG, "Creating Broadcast receiver for network info");
			IntentFilter networkStateChangedFilter = new IntentFilter();
			networkStateChangedFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			_networkStateChangedReceiver = new BroadcastReceiver() {
	
				@Override
				public void onReceive(Context appContext, Intent broadcastIntent) {
					NetworkInfo networkInfo = broadcastIntent.<NetworkInfo>getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					
					if(networkInfo.isConnected() && isConnected()) {
						HttpQueue queue = null;
						Log.i(HttpQueue.TAG, "Reconnected, waking up Queues");
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
	
	public static boolean isConnected() {
		boolean connected = false;
		
		if(hasNetworkConnectivity()) {
			// Check if route to google does work
			URL url = null; 
			HttpURLConnection urlConnection = null;
			
			try {
				//TODO: must do this async or will raise network on main thread exception
				/*
				url = new URL("http://m.google.com"); // Fixed internet connection check based on m.google.com | TODO: Allow intranet requests
				urlConnection = (HttpURLConnection) url.openConnection();
				if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					connected = true;
				}*/
				connected = true;
				
			//} catch (IOException e) {
				
			} finally {
				if(urlConnection != null) {
					urlConnection.disconnect();
				}
			}
		}

		return connected;
	}
	
	private static boolean hasNetworkConnectivity() {
		try {
            ConnectivityManager cm = (ConnectivityManager) EngineContext.instance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) { // Mobile
                    return true;
            } else if(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){ // Wi-fi
                    return true;
            } else {
            	return false;
            }
        } catch (Exception e) {
                return false;
        }
	}
	
	
	private static void checkUncompletedQueues() {
		
	}
}

package br.unisinos.swe.agentjs.engine.ctx;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import br.unisinos.swe.agentjs.engine.Engine;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.AppDispatcherSignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.ISignalsManager;
import br.unisinos.swe.agentjs.engine.signals.LocationSignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.info.AppDispatcherSignalInfo;
import br.unisinos.swe.agentjs.engine.signals.info.DeviceInfo;
import br.unisinos.swe.agentjs.engine.signals.info.LocationSignalInfo;
import br.unisinos.swe.agentjs.engine.signals.info.NetworkSignalBasicInfo;
import br.unisinos.swe.agentjs.engine.signals.info.UserInfo;
import br.unisinos.swe.agentjs.engine.signals.info.WifiSignalBasicInfo;
import br.unisinos.swe.http.utils.HttpQueue;
import br.unisinos.swe.http.utils.HttpQueueManager;
import br.unisinos.swe.http.utils.HttpQueueRequest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.SystemClock;
import android.text.format.Time;

public class ContextUploader implements IContextUploader {

	private static final String CONTEXT_UPLOAD = "br.unisinos.swe.agentjs.engine.ctx.CONTEXT_UPLOAD";
	
	private AlarmManager _polling; // trigger updates every x time
	private ContextPolling _contextPollingReceiver; // receive alarm trigger
	private PendingIntent _operation; // intent for action CONTEXT_UPLOAD
	
	private Engine _engine; // own engine to retrieve all components
	
	private ISignalsManager _signalManager; // signal manager component
	
	private HttpQueue _httpQueue; // http queue for context uploading
	
	public ContextUploader(Engine engine) {
		Context appContext = EngineContext.instance().getContext().getApplicationContext();
		_engine = engine;
		_polling = (AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);
		
		
		Intent i = new Intent(CONTEXT_UPLOAD);
		_operation = PendingIntent.getBroadcast(appContext, 0, i, 0);
		_contextPollingReceiver = new ContextPolling();
		
		_httpQueue = HttpQueueManager.create();
	}
	
	@Override
	public void start() {
		// capture signal manager
		_signalManager = _engine.getComponent(ISignalsManager.class);
		
		//15min = 900.000ms
		//60000
		
		//create an alarm manager for 15min updates
		//_polling.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, _operation);
		
		IntentFilter proximityAlertFilter = new IntentFilter(CONTEXT_UPLOAD);
		EngineContext.instance().getContext().registerReceiver(_contextPollingReceiver, proximityAlertFilter); // listen to alarm
		
		_polling.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000, _operation);
		
		EngineContext.log().info("Context Uploader scheduled");
	}

	@Override
	public void stop() {
		try {
			EngineContext.log().info("Stopping Context Uploader");
			_polling.cancel(_operation);
		} catch(Exception e) {
			EngineContext.log().error("Unable to complete stop procedure of ContextUplaoder");
		}
		
		try {
			EngineContext.instance().getContext().unregisterReceiver(_contextPollingReceiver);
		} catch(Exception e) {
			EngineContext.log().error("Unable to complete unregister ContextUplaoder handlers");
		}
	}
	
	public void doContextUpload() {
		long timestamp = this.getTimeStamp(); // When?
		
		// get position
		LocationSignalInfo whereAmI = this.getLocation(); // Where?
		
		// get current user data
		UserInfo userInfo = this.getCurrentUser(); // Who?
		
		// get device info
		DeviceInfo device = this.getDeviceInfo();
		
		// get running apps
		AppDispatcherSignalInfo runningApp = this.getRunningApp(); // What?
		
		// get connected network
		WifiSignalBasicInfo wifiInfo = this.getWifiInfo();
		NetworkSignalBasicInfo networkInfo = this.getNetworkInfo();
		
		
		// do Json object conversion
		JSONObject jsonPackage = new JSONObject();
		
		try {
			jsonPackage.put("timestamp", timestamp); // we will always have timestamp
			if(whereAmI != null) { 
				jsonPackage.put("location", whereAmI.toJson());
			}
			jsonPackage.put("user", userInfo.toJson()); // We will always have a user
			jsonPackage.put("device", device.toJson()); // We will always have a device
			
			if(runningApp != null) {
				jsonPackage.put("app", runningApp.toJson());
			}
			
			if(wifiInfo != null) {
				jsonPackage.put("wifi", wifiInfo.toJson());
			}
			
			if(networkInfo != null) {
				jsonPackage.put("network", networkInfo.toJson());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating jsonPackage");
		}
		
		String jsonString = jsonPackage.toString();
		
		
		
		// send post to HttpQueue
		String ctxUrl = EngineContext.instance().getCloudUrl() + "rest/context";
		
		EngineContext.log().info("Uploading context to cloud: " + ctxUrl);
		HttpQueueRequest request = new HttpQueueRequest("POST", ctxUrl, jsonString, null);
		request.setHeader("Content-Type", "application/json");
		_httpQueue.fireEnsureDelivery(request);
	}

	private WifiSignalBasicInfo getWifiInfo() {
		NetworkSignalEmitter network = _signalManager.get(NetworkSignalEmitter.class);
		
		return network.getWifiInfo();
	}

	private NetworkSignalBasicInfo getNetworkInfo() {
		NetworkSignalEmitter network = _signalManager.get(NetworkSignalEmitter.class);
		
		return network.getNetworkInfo();
	}
	private DeviceInfo getDeviceInfo() {
		return new DeviceInfo();
	}

	private AppDispatcherSignalInfo getRunningApp() {
		AppDispatcherSignalEmitter apps = _signalManager.get(AppDispatcherSignalEmitter.class);
		return apps.getLastRunningApp();
	}

	private UserInfo getCurrentUser() {
		return new UserInfo();
	}

	private LocationSignalInfo getLocation() { // last known location or async?
		LocationSignalEmitter location = _signalManager.get(LocationSignalEmitter.class);
		return location.getLastKnownLocation();
	}

	private long getTimeStamp() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		return today.toMillis(false);
	}

	/**
	 * Responible for receiving alam manager wake up calls for context determination
	 * @author Paulo
	 *
	 */
	public class ContextPolling extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			ContextUploader.this.doContextUpload();
		}
		
	}

}

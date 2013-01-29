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
import br.unisinos.swe.agentjs.engine.signals.info.WifiSignalBasicInfo;
import br.unisinos.swe.http.utils.HttpQueue;
import br.unisinos.swe.http.utils.HttpQueueManager;
import br.unisinos.swe.http.utils.HttpQueueRequest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.text.format.Time;

public class ContextUploader implements IContextUploader {

	private AlarmManager _polling;
	private PendingIntent _operation;
	private Engine _engine;
	private ISignalsManager _signalManager;
	private HttpQueue _httpQueue;
	
	public ContextUploader(Engine engine) {
		_engine = engine;
		_polling = (AlarmManager)EngineContext.instance().getContext().getSystemService(Context.ALARM_SERVICE);
		
		
		Intent i = new Intent(EngineContext.instance().getContext(), ContextPolling.class);
		_operation = PendingIntent.getBroadcast(EngineContext.instance().getContext(), 0, i, 0);
		
		_httpQueue = HttpQueueManager.create();
	}
	
	@Override
	public void start() {
		// capture signal manager
		_signalManager = _engine.getComponent(ISignalsManager.class);
		
		//create an alarm manager for 15min updates
		_polling.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, _operation);
	}

	@Override
	public void stop() {
		try {
			_polling.cancel(_operation);
		} catch(Exception e) {
			EngineContext.log().error("Unable to complete stop procedure of ContextUplaoder");
		}
		
	}
	
	public void doContextUpload() {
		long timestamp = this.getTimeStamp(); // When?
		
		// get position
		LocationSignalInfo whereAmI = this.getLocation(); // Where?
		
		// get current user data
		String userId = this.getCurrentUser(); // Who?
		
		// get device info
		DeviceInfo device = this.getDeviceInfo();
		
		// get running apps
		AppDispatcherSignalInfo runningApp = this.getRunningApp(); // What?
		
		// get connected network
		WifiSignalBasicInfo wifiInfo = this.getWifiInfo();
		
		// do Json object conversion
		String jsonString = this.createJsonPackage(whereAmI, userId, device, runningApp, wifiInfo);
		
		// send post to HttpQueue
		HttpQueueRequest request = new HttpQueueRequest("POST", EngineContext.instance().getCloudUrl(), jsonString, null);
		_httpQueue.fireEnsureDelivery(request);
	}
	
	private String createJsonPackage(LocationSignalInfo whereAmI,
			String userId, DeviceInfo device,
			AppDispatcherSignalInfo runningApp, WifiSignalBasicInfo wifiInfo) {
		
		JSONObject jsonPackage = new JSONObject();
		
		try {
			jsonPackage.put("location", whereAmI.toJson());
			jsonPackage.put("user", userId);
			jsonPackage.put("device", device.toJson());
			jsonPackage.put("app", runningApp.toJson());
			jsonPackage.put("wifi", wifiInfo.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
			EngineContext.log().error("Error creating jsonPackage");
		}
		
		return jsonPackage.toString();
	}

	private WifiSignalBasicInfo getWifiInfo() {
		NetworkSignalEmitter network = _signalManager.get(NetworkSignalEmitter.class);
		
		return network.getWifiInfo();
	}

	private DeviceInfo getDeviceInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	private AppDispatcherSignalInfo getRunningApp() {
		AppDispatcherSignalEmitter apps = _signalManager.get(AppDispatcherSignalEmitter.class);
		return apps.getLastRunningApp();
	}

	private String getCurrentUser() {
		// TODO Auto-generated method stub
		return null;
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
	protected class ContextPolling extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			ContextUploader.this.doContextUpload();
		}
		
	}

}

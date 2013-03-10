package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;

import org.mozilla.javascript.annotations.JSFunction;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.LocationSignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.LocationSignalEmitter.LocationSignal;
import br.unisinos.swe.agentjs.engine.signals.info.LocationSignalInfo;

public class AgentLocation  extends AbstractAgentAPIComponent {
	
	public AgentLocation(AgentExecutorHelper helper) {
		helper.register(this);
	}
	
	@JSFunction("lastLocation")
	public Object lastLocation() {
		LocationSignalInfo signalInfo = new LocationSignalInfo();
		LocationSignalEmitter locationSignal = EngineContext.instance().signals().get(LocationSignalEmitter.class);
		if(locationSignal != null) {
			signalInfo = locationSignal.getLastKnownLocation();
		}
		return _helper.javaToJS(signalInfo);
	}
	
	@JSFunction("getCurrentLocation")
	public void getCurrentLocation(final Object callbackFunc) {
		LocationSignalEmitter locationSignal = EngineContext.instance().signals().get(LocationSignalEmitter.class);
		if(locationSignal != null) {
			EngineContext.instance().log().info("Achou o signal emitter");
			locationSignal.getCurrentLocation(new LocationListener() { // TODO: Change this to own interface instead of Android lock-in
				
				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
				}
				
				@Override
				public void onProviderEnabled(String provider) {
				}
				
				@Override
				public void onProviderDisabled(String provider) {
				}
				
				@Override
				public void onLocationChanged(Location location) {
					EngineContext.instance().log().info("Achou a localizacao");
					_helper.callback(callbackFunc, _helper.javaToJS(new LocationSignalInfo(location)));
				}
			});
		}
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		if(_signals == null) { // signal list is only useful for multi-origin signal API
			_signals = new ArrayList<String>();
			for (LocationSignal signalEnum : LocationSignal.class.getEnumConstants()) { // All network signals
				_signals.add(signalEnum.toString());
			}
		}
		
		return _signals.contains(signal);
	}

}

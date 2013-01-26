package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;

import org.mozilla.javascript.annotations.JSFunction;

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

package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;
import java.util.UUID;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSFunction;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.ISignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.SignalListener;

public abstract class AbstractAgentAPIComponent implements IAgentAPIComponent {
	
	protected ArrayList<String> _signals = null;
	protected AgentExecutorHelper _helper;
	protected UUID _uuid;
	
	@Override
	public void setHelper(AgentExecutorHelper helper) {
		_helper = helper;
		_uuid = UUID.randomUUID();
	}
	
	/*
	@JSFunction("on")
	public void on(String signalName, Object callbackFunc) {
		if(isOwnSignal(signalName)) {
			if(callbackFunc != null && callbackFunc instanceof Function) {
				// Search for signal class
				ISignalEmitter signal = EngineContext.instance().signals().search(signalName);
				
				// register for signaling
				if(signal != null) {
					signal.registerListener(signalName, new SignalListener(_uuid, _helper, (Function)callbackFunc));
				}
			}
			
		}
	}*/
	
	@JSFunction("on")
	public String on(String signalName, Object callbackFunc) {
		return this.on(signalName, null, callbackFunc);
	}
	
	@JSFunction("on")
	public String on(String signalName, Object objParams, Object callbackFunc) {
		UUID listenerId = null;
		if(isOwnSignal(signalName)) {
			if(callbackFunc != null && callbackFunc instanceof Function) {
				// Search for signal class
				ISignalEmitter signal = EngineContext.instance().signals().search(signalName);
				
				// register for signaling
				if(signal != null) {
					NativeObject jsParams = null;
					if(objParams != null) {
						jsParams = (NativeObject)objParams;
					}
					
					listenerId = UUID.randomUUID();
					
					//TODO: Allow multiple uuid (by returning a new id to caller agent)
					// off should use this id to remove one listener, or none to remove all
					// infer all by using parent (that is, this _uuid) uuid
					signal.registerListener(signalName, new SignalListener(listenerId, _uuid, _helper, (Function)callbackFunc, jsParams));
					
				}
			}			
		}
		return (listenerId == null ? "" : listenerId.toString());
	}
	
	@JSFunction("off")
	public void off(String signalName) {
		off(signalName, null);
	}
	
	
	@JSFunction("off")
	public void off(String signalName, String strListenerId) {
		if(isOwnSignal(signalName)) {
			// Search for signal class
			ISignalEmitter signal = EngineContext.instance().signals().search(signalName);
			
			// register for signaling
			if(signal != null) {
				UUID listenerId = null;
				if(strListenerId != null) {
					listenerId = UUID.fromString(strListenerId);
				}
				signal.removeListener(signalName, listenerId, _uuid);
			}
			
		}
	}
	
	protected abstract boolean isOwnSignal(String signal);

}

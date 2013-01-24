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
	public void on(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
		String signalName = "";
		Object jsParams = null; 
		Object callbackFunc = null;
		
		if(args.length == 2) {
			signalName = String.valueOf(args[0]);
			callbackFunc = args[1];
		} else if (args.length == 3) {
			signalName = String.valueOf(args[0]);
			jsParams = (NativeObject)args[1];
			callbackFunc = args[2];
		}
		
		
		if(isOwnSignal(signalName)) {
			if(callbackFunc != null && callbackFunc instanceof Function) {
				// Search for signal class
				ISignalEmitter signal = EngineContext.instance().signals().search(signalName);
				
				// register for signaling
				if(signal != null) {
					signal.registerListener(signalName, new SignalListener(_uuid, _helper, (Function)callbackFunc, (NativeObject)jsParams));
				}
			}
			
		}
	}
	
	@JSFunction("off")
	public void off(String signalName) {
		if(isOwnSignal(signalName)) {
			// Search for signal class
			ISignalEmitter signal = EngineContext.instance().signals().search(signalName);
			
			// register for signaling
			if(signal != null) {
				signal.removeListener(signalName, _uuid);
			}
			
		}
	}
	
	protected abstract boolean isOwnSignal(String signal);

}

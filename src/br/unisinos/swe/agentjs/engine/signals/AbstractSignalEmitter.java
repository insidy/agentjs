package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class AbstractSignalEmitter implements ISignalEmitter {
	protected HashMap<String, List<ISignalListener>> _listeners;
	protected static AbstractSignalEmitter _instance;
	protected ArrayList<String> _signals = null;
	
	protected AbstractSignalEmitter() {
		_listeners = new HashMap<String, List<ISignalListener>>();
	}

	@Override
	public void registerListener(String signalString, ISignalListener listener) {
		if (this.getSignals().contains(signalString)) {
			if (_listeners.containsKey(signalString)) {
				getListeners(signalString).add(listener);
			} else {
				List<ISignalListener> signalListeners = new ArrayList<ISignalListener>();
				signalListeners.add(listener);

				_listeners.put(signalString, signalListeners);
			}
		}
	}

	@Override
	public void removeListener(String signalString, UUID listenerId) {
		if (this.getSignals().contains(signalString)) {
			if (_listeners.containsKey(signalString)) {
				List<ISignalListener> signalListeners = getListeners(signalString);
				SignalListener listenerObj = new SignalListener(listenerId);
				while (signalListeners.remove(listenerObj)) {
					// remove all listeners with same UUID
				}
			}
		}
	}

	@Override
	public void fire(String signalString, Object... params) {
		if (this.getSignals().contains(signalString)) {
			if (_listeners.containsKey(signalString)) {
				for(ISignalListener listener : getListeners(signalString)) { // multi-thread this?
					listener.fire(params);
				}
			}
		}
		
	}

	public List<ISignalListener> getListeners(String signal) {
		return _listeners.get(signal);
	}

}

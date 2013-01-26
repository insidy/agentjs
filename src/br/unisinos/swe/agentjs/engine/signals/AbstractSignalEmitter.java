package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class AbstractSignalEmitter implements ISignalEmitter {
	protected HashMap<String, List<ISignalListener>> _listeners;
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
	public List<ISignalListener> removeListener(String signalString, UUID listenerId, UUID parentId) {
		List<ISignalListener> removeListeners = new ArrayList<ISignalListener>();
		if (this.getSignals().contains(signalString)) {
			if (_listeners.containsKey(signalString)) {
				List<ISignalListener> signalListeners = getListeners(signalString);
				
				for(ISignalListener listener : signalListeners) {
					if(listenerId != null && listener.getUuid().equals(listenerId)) { // remove when listener ID was specified
						removeListeners.add(listener);
					} else if(parentId != null && listener.getParentId().equals(parentId)) { // remove all based on parent id
						removeListeners.add(listener);
					}
				}
				
				signalListeners.removeAll(removeListeners);
			}
		}
		return removeListeners;
	}

	@Override
	public void fire(String signalString, Object... params) {
		if (this.getSignals().contains(signalString)) {
			if (_listeners.containsKey(signalString)) {
				for(ISignalListener listener : getListeners(signalString)) { // multi-thread this?
					
					if(listener.hasParams() && params.length > 0) { // listener have parameter and we are returning something
						if(this.filter(signalString, listener, params)) {
							listener.fire(params);
						}
						
					} else { // common scenario
						listener.fire(params);
					}
					
				}
			}
		}
		
	}
	
	@Override
	public List<String> getSignals() {
		return _signals;
	}

	public List<ISignalListener> getListeners(String signal) {
		return _listeners.get(signal);
	}

	public ISignalListener getListenerBySignalAndId(String strSignal, String strListenerId) {
		ISignalListener listener = null;
		
		if (this.getSignals().contains(strSignal)) {
			if (_listeners.containsKey(strSignal)) {
				List<ISignalListener> signalListeners = getListeners(strSignal);
				for(ISignalListener availableListener : signalListeners) {
					if(availableListener.getUuid().toString().equals(strListenerId)) {
						return availableListener;
					}
				}
			}
		}
		
		return listener;
	}

}

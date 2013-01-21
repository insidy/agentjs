package br.unisinos.swe.agentjs.engine.signals;

import java.util.HashSet;

public class SignalsManager implements ISignalsManager {
	
	private HashSet<ISignalEmitter> _globalEmitters;

	public SignalsManager() {
		_globalEmitters = new HashSet<ISignalEmitter>();
	}
	
	@Override
	public ISignalEmitter search(String name) {
		
		for(ISignalEmitter emitter : _globalEmitters) {
			if(emitter.getSignals().contains(name)) {
				return emitter;
			}
		}
		
		return null;
	}
	
	protected void register(ISignalEmitter emitter) {
		_globalEmitters.add(emitter.start());
	}

	@Override
	public void start() {
		// instantiate signals
		register(NetworkSignalEmitter.create());
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		for(ISignalEmitter emitter : _globalEmitters) {
			emitter.stop();
		}
		
		_globalEmitters.clear();
	}
	

}

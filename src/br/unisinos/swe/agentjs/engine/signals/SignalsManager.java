package br.unisinos.swe.agentjs.engine.signals;

import java.util.HashSet;

import br.unisinos.swe.agentjs.engine.IEngineComponent;

public class SignalsManager implements ISignalsManager, IEngineComponent {
	
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
		register(AppDispatcherSignalEmitter.create());
		register(BatterySignalEmitter.create());
		register(SmsSignalEmitter.create());
		
	}

	@Override
	public void stop() {
		for(ISignalEmitter emitter : _globalEmitters) {
			emitter.stop();
		}
		
		_globalEmitters.clear();
	}
	

}

package br.unisinos.swe.agentjs.engine.signals;

import java.util.HashSet;

import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.IEngineComponent;

public class SignalsManager implements ISignalsManager, IEngineComponent {
	
	private HashSet<ISignalEmitter> _globalEmitters;

	public SignalsManager() {
		_globalEmitters = new HashSet<ISignalEmitter>();
	}
	
	@Override
	public final <U> U get(Class<U> clsType) {
		for(Object component : _globalEmitters) {
			if(clsType.isInstance(component)) {
				return clsType.cast(component);
			}
		}
		return null;
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
		EngineContext.log().debug("Starting signal: " + emitter.getClass().getName());
		_globalEmitters.add(emitter.start());
	}

	@Override
	public void start() {
		// instantiate signals
		register(NetworkSignalEmitter.create());
		register(AppDispatcherSignalEmitter.create());
		register(BatterySignalEmitter.create());
		register(SmsSignalEmitter.create());
		register(LocationSignalEmitter.create());
		
	}

	@Override
	public void stop() {
		for(ISignalEmitter emitter : _globalEmitters) {
			emitter.stop();
		}
		
		_globalEmitters.clear();
	}
	

}

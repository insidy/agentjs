package br.unisinos.swe.agentjs.engine.signals;

import br.unisinos.swe.agentjs.engine.IEngineComponent;

public interface ISignalsManager extends IEngineComponent {

	public ISignalEmitter search(String name);

	public <U> U get(Class<U> clsType);
	
}

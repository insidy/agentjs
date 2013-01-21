package br.unisinos.swe.agentjs.engine.signals;

public interface ISignalsManager {

	public ISignalEmitter search(String name);
	public void start();
	public void stop();
}

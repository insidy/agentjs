package br.unisinos.swe.agentjs.engine.signals;

import java.util.List;
import java.util.UUID;

public interface ISignalEmitter {
	public void registerListener(String signal, ISignalListener listener);
	public void removeListener(String signal, UUID listenerId);
	public void fire(String signal, Object...params);
	public List<String> getSignals();
	public ISignalEmitter start();
	public void stop();
	public boolean filter(String signal, ISignalListener listener, Object... params);
}

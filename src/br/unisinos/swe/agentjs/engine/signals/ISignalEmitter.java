package br.unisinos.swe.agentjs.engine.signals;

import java.util.UUID;

public interface ISignalEmitter {
	public void registerListener(SignalListener listener);
	public void removeListener(UUID listenerId);
}

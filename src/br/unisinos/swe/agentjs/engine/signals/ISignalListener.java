package br.unisinos.swe.agentjs.engine.signals;

import java.util.UUID;

public interface ISignalListener {
	public void fire(Object... params);
	public UUID getUuid();
}

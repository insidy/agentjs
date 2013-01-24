package br.unisinos.swe.agentjs.engine.signals;

import java.util.HashMap;
import java.util.UUID;

public interface ISignalListener {
	public void fire(Object... params);
	public UUID getUuid();
	public HashMap<String, String> getParams();
	public String getParam(String key);
}

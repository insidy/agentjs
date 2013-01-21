package br.unisinos.swe.agentjs.engine.signals;

import java.util.UUID;

import org.mozilla.javascript.Function;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;

public class SignalListener {
	protected UUID _uuid;
	private AgentExecutorHelper _helper;
	private Function _callback;
	
	public SignalListener(UUID uuid, AgentExecutorHelper helper, Function callback) {
		_uuid = uuid;
		_helper = helper;
		_callback = callback;
	}
	
	public void emit(Object... params) {
		_helper.callback(_callback, params);
	}
}

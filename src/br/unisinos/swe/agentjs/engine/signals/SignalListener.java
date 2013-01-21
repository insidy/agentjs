package br.unisinos.swe.agentjs.engine.signals;

import java.util.UUID;

import org.mozilla.javascript.Function;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;

public class SignalListener implements ISignalListener {
	protected UUID _uuid;
	private AgentExecutorHelper _helper;
	private Function _callback;
	
	public SignalListener(UUID uuid) {
		_uuid = uuid;
	}
	
	public SignalListener(UUID uuid, AgentExecutorHelper helper, Function callback) {
		_uuid = uuid;
		_helper = helper;
		_callback = callback;
	}
	
	public void fire(Object... params) {
		_helper.callback(_callback, params);
	}
	
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        SignalListener compareTo = (SignalListener) obj;
        return this._uuid.equals(compareTo.getUuid());
    }

	@Override
	public UUID getUuid() {
		return this._uuid;
	}
}

package br.unisinos.swe.agentjs.engine.signals;

import java.util.HashMap;
import java.util.UUID;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;

public class SignalListener implements ISignalListener {
	protected UUID _uuid;
	private AgentExecutorHelper _helper;
	private Function _callback;
	private HashMap<String, String> _jsParams;
	
	public SignalListener(UUID uuid) {
		_uuid = uuid;
	}
	
	public SignalListener(UUID uuid, AgentExecutorHelper helper, Function callback, NativeObject jsParams) {
		_uuid = uuid;
		_helper = helper;
		_callback = callback;
		_jsParams = new HashMap<String, String>();
		
		//Set<Entry<Object, Object>> entrySet = jsParams.entrySet();
		if(jsParams != null) {
			Object[] jsKeys = jsParams.getIds();
			
			for(int idx = 0; idx < jsKeys.length; idx++) {
				String key = String.valueOf(jsKeys[idx]);
				String param = (String) Context.jsToJava(jsParams.get(key, jsParams), String.class);
				_jsParams.put(key, param);
			}
		}
		
	}
	
	public void fire(Object... params) {
		Object[] jsParams = new Object[params.length];
		
		for(int idx = 0; idx < params.length; idx++) {
			jsParams[idx] = _helper.javaToJS(params[idx]);
		}
		
		_helper.callback(_callback, jsParams);
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

	@Override
	public HashMap<String, String> getParams() {
		return _jsParams;
	}

	@Override
	public String getParam(String key) {
		return _jsParams.get(key);
	}
}

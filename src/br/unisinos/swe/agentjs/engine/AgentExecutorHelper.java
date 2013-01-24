package br.unisinos.swe.agentjs.engine;

import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import android.content.pm.ApplicationInfo;
import br.unisinos.swe.agentjs.engine.api.IAgentAPIComponent;


public class AgentExecutorHelper {
	Context _rhino;
	Scriptable _scope;
	
	private ArrayList<Object> _components;
	
	public AgentExecutorHelper(Context rhino, Scriptable scope) {
		_rhino = rhino;
		_scope = scope;
		_components = new ArrayList<Object>();
	}
	
	public void register(IAgentAPIComponent object) {
		this._components.add(object);
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(object.getClass());
		object.setHelper(this);
	}
	
	public final <U> U get(Class<U> clsType) {
		for(Object component : _components) {
			if(clsType.isInstance(component)) {
				return clsType.cast(component);
			}
		}
		return null;
	}
	
	public void callback(Object callbackFunc, Object...params) {
		
		if(callbackFunc != null) {
			try {
				Function typedCallback = (Function)callbackFunc;
				typedCallback.call(_rhino, _scope, _scope, params);
			} catch(Exception e) {
				EngineContext.log().error("Not possible to do callback");
				e.printStackTrace();
			}
		}
	}

	public Object javaToJS(Object toConvert) {
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(toConvert.getClass()); // TODO: If we are converting, we want to expose, right?		
		return Context.javaToJS(toConvert, _scope);
	}
	
	public Scriptable newArray(Object[] elements) {
		return _rhino.newArray(_scope, elements);
	}

}

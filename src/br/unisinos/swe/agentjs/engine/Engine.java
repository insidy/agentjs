package br.unisinos.swe.agentjs.engine;

import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import android.app.Service;
import br.unisinos.swe.agentjs.engine.api.AgentAPI;
import br.unisinos.swe.agentjs.engine.api.AgentNotification;
import br.unisinos.swe.agentjs.engine.ctx.ContextUploader;
import br.unisinos.swe.agentjs.engine.ctx.IContextUploader;
import br.unisinos.swe.agentjs.engine.db.AgentScript;
import br.unisinos.swe.agentjs.engine.db.AgentScriptManager;
import br.unisinos.swe.agentjs.engine.db.IAgentChangeEvent;
import br.unisinos.swe.agentjs.engine.signals.ISignalsManager;
import br.unisinos.swe.agentjs.engine.signals.SignalsManager;
import br.unisinos.swe.agentjs.engine.upnp.UPnPHandler;

public class Engine {

	//private ISignalsManager _signals;
	//private AgentScriptManager _loader;
	
	// Engine Components
	private ArrayList<IEngineComponent> _components;
	
	// Mozilla Rhino references
	private Context _rhino;
	private Scriptable _scope;

	// Engine state
	private boolean _started = false;
	private Service _service;

	/**
	 * Create a new AgentJS engine
	 * @param applicationContext
	 */
	public Engine(Service engineService) {
		EngineContext.create(engineService.getApplicationContext());
		
		_service = engineService;
		_components = new ArrayList<IEngineComponent>();
		
		// initiate components
		_components.add((ISignalsManager)new SignalsManager());
		_components.add((IAgentScriptManager)new AgentScriptManager(new UPnPHandler(engineService)));
		_components.add((IContextUploader)new ContextUploader(this));
		
		// define global signal manager
		EngineContext.setSignalManager(get(ISignalsManager.class));
		
		// setup sandbox
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(EngineContext.class);
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(EngineLogger.class);
		
		ContextFactory.initGlobal(new EngineScriptSandbox());
	}
	
	private final <U> U get(Class<U> clsType) {
		for(Object component : _components) {
			if(clsType.isInstance(component)) {
				return clsType.cast(component);
			}
		}
		return null;
	}

	public <U> U getComponent(Class<U> clsType) { // expose components
		return get(clsType);
	}

	public void start() {
		if (!_started) {
			
			//_signals.start();
			//_loader.start();
			
			for(IEngineComponent component : _components) {
				component.start();
			}
			
			_rhino = Context.enter();

			_scope = _rhino.initStandardObjects();
			
			// We are creating the API's one for each script.. maybe we could share this later
			//createAPI(_rhino, _scope);
			
			//loadLocalScripts(_rhino, _scope);

			_started = true;
		}
	}

	public void stop() {
		_started = false;
		for(IEngineComponent component : _components) {
			component.stop();
		}
		Context.exit();
	}
	
	public ArrayList<AgentScript> getLocalScripts() {
		return get(IAgentScriptManager.class).getLocalScripts();
	}
	
	public ArrayList<AgentScript> getNetworkScripts() {
		return get(IAgentScriptManager.class).getNetworkScripts();
	}
	
	public void startScript(AgentScript script) {
		get(IAgentScriptManager.class).startScript(this, script);
	}
	
	public void registerAgentListener(IAgentChangeEvent eventListener) {
		get(IAgentScriptManager.class).registerListener(eventListener);
	}
	
	public void removeAgentListener(IAgentChangeEvent eventListener) {
		get(IAgentScriptManager.class).removeListener(eventListener);
	}
	
	/*
	private void loadLocalScripts(Context rhino, Scriptable scope) {
		for(AgentScript script : get(IAgentScriptManager.class).getLocalScripts()) {
			// new thread + create API's 
			AgentExecutor executor = new AgentExecutor(this, script);
			executor.execute();
		}
	}*/

	protected void createAPI(Context rhino, Scriptable scope) {
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(String.class);
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(AgentNotification.class); // manual allowed, other added by Helper
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(AgentAPI.class);
		
		AgentExecutorHelper helper = new AgentExecutorHelper(rhino, scope);
		AgentAPI api = new AgentAPI(helper);
		
		ScriptableObject.putProperty(scope, "agent", Context.javaToJS(api, scope));
	}
	
	protected Scriptable scope(Context ctx) {
		Scriptable newScope = ctx.newObject(_scope);
	    newScope.setPrototype(_scope);
	    newScope.setParentScope(null);
	    
	    return newScope;
	}
	
	protected Context context() {
		return _rhino;
	}

}

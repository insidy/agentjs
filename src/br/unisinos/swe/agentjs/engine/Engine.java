package br.unisinos.swe.agentjs.engine;

import java.lang.reflect.InvocationTargetException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import br.unisinos.swe.agentjs.AndroidAgentJSActivity;
import br.unisinos.swe.agentjs.engine.api.AgentAPI;
import br.unisinos.swe.agentjs.engine.api.AgentHttpClient;
import br.unisinos.swe.agentjs.engine.api.AgentNotification;
import br.unisinos.swe.agentjs.engine.api.AgentSMS;
import br.unisinos.swe.agentjs.engine.db.AgentScript;
import br.unisinos.swe.agentjs.engine.db.AgentScriptManager;

public class Engine {

	private AgentScriptManager _loader;
	private Context _rhino;
	private Scriptable _scope;

	private boolean _started = false;

	public Engine(android.content.Context applicationContext) {
		EngineContext.create(applicationContext, null); // TODO create and start signals manager
		_loader = new AgentScriptManager();
		
		// setup sandbox
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(EngineContext.class);
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(EngineLogger.class);
		
		ContextFactory.initGlobal(new EngineScriptSandbox());
	}

	public void start() {
		if (!_started) {

			
			_rhino = Context.enter();

			_scope = _rhino.initStandardObjects();
			
			// We are creating the API's one for each script.. maybe we could share this later
			//createAPI(_rhino, _scope);
			
			loadLocalScripts(_rhino, _scope);

			_started = true;
		}
	}

	private void loadLocalScripts(Context rhino, Scriptable scope) {
		for(AgentScript script : _loader.getLocalScripts()) {
			// new thread + create API's 
			AgentExecutor executor = new AgentExecutor(this, script);
			executor.execute();
		}
	}

	public void stop() {
		_started = false;
		Context.exit();
	}

	public void createAPI(Context rhino, Scriptable scope) {
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(AgentNotification.class); // manual allowed, other added by Helper
		EngineScriptSandbox.SandboxClassShutter.addAllowedScriptableComponent(AgentAPI.class);
		
		AgentExecutorHelper helper = new AgentExecutorHelper(rhino, scope);
		AgentAPI api = new AgentAPI(helper);
		
		ScriptableObject.putProperty(scope, "agent", Context.javaToJS(api, scope));
	}
	
	public Scriptable scope(Context ctx) {
		Scriptable newScope = ctx.newObject(_scope);
	    newScope.setPrototype(_scope);
	    newScope.setParentScope(null);
	    
	    return newScope;
	}

}

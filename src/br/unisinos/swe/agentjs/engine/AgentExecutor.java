package br.unisinos.swe.agentjs.engine;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import br.unisinos.swe.agentjs.engine.db.AgentScript;
import android.os.AsyncTask;

public class AgentExecutor extends AsyncTask<Void, Void, Void> {

	private Engine _engine;
	private AgentScript _script;
	
	private Context _rhino;
	private Scriptable _scope;
	
	public AgentExecutor(Engine engine, AgentScript script) {
		_engine = engine;
		_script = script;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		_rhino = Context.enter();
		_scope = _engine.scope(_rhino);
		
		_engine.createAPI(_rhino, _scope);
		
		_rhino.evaluateString(_scope, _script.getSourceCode(), "ScriptAPI", 1, null);
		Context.exit();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
	}
	

}

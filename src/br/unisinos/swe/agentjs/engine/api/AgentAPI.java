package br.unisinos.swe.agentjs.engine.api;

import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;

import br.unisinos.swe.agentjs.engine.AgentComponent;
import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.EngineContext;

@AgentComponent(name="agent")
public class AgentAPI {

	
	AgentExecutorHelper _helper;
	
	AgentHttpClient _httpClient;
	AgentSMS _sms;
	AgentApplications _apps;
	AgentSound _sound;
	AgentNetwork _network;
	
	public AgentAPI(AgentExecutorHelper helper) {
		_helper = helper;
		
		_httpClient = new AgentHttpClient(_helper);
		_sms = new AgentSMS(_helper);
		_apps = new AgentApplications(_helper);
		_sound = new AgentSound(_helper);
		_network = new AgentNetwork(_helper);
		
	}
	
	@JSGetter("apps")
	public final AgentApplications getApps() {
		return this._apps;
	}
	
	@JSGetter("music")
	public final AgentSound getMusic() {
		return this._sound;
	}
	
	@JSGetter("http")
	public final AgentHttpClient getHttp() {
		return this._httpClient;
	}
	
	@JSGetter("sms")
	public final AgentSMS getSms() {
		return this._sms;
	}
	
	@JSGetter("network")
	public final AgentNetwork getNetwork() {
		return this._network;
	}
	
	@JSFunction("createNotification")
	public final Object createNotification(String title) {
		return _helper.javaToJS(new AgentNotification(title));
	}
	
	@JSFunction("log")
	public final void log(String message) {
		EngineContext.log().info(message);
	}
}

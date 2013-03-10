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
	AgentLocation _location;
	AgentNotifier _notifier;
	
	public AgentAPI(AgentExecutorHelper helper) {
		_helper = helper;
		
		_httpClient = new AgentHttpClient(_helper);
		_sms = new AgentSMS(_helper);
		_apps = new AgentApplications(_helper);
		_sound = new AgentSound(_helper);
		_network = new AgentNetwork(_helper);
		_location = new AgentLocation(_helper);
		_notifier = new AgentNotifier(_helper);
		
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
	
	@JSGetter("location")
	public final AgentLocation getLocation() {
		return this._location;
	}
	
	@JSGetter("notification")
	public final AgentNotifier getNotification() {
		return this._notifier;
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

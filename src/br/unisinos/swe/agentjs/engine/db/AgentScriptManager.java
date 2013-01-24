package br.unisinos.swe.agentjs.engine.db;

import java.util.ArrayList;

public class AgentScriptManager {

	
	public String getScriptFromServer() {
    	return "var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; function doUrlGet(param) { agent.http.get({ url: 'http://www.google.com'}, function(response) { log('aee!'); log(response); }, function(response) {  }); return { foo: \"bar in JavaScript\" }; }; function hello(java) { if (typeof log != 'undefined') { log(\"JavaScript say hello to \" + java); log(\"Also, I can access Java object: \" + javaContext); } return { foo: \"bar in JavaScript\" }; } doUrlGet('gangnam style');";
    }
	
	public ArrayList<AgentScript> getLocalScripts() {
		ArrayList<AgentScript> scripts = new ArrayList<AgentScript>();
		
		AgentScript newScript = new AgentScript();
		newScript.setSourceCode(getScriptFromServer());
		
		scripts.add(newScript);
		
		//TODO: Get source from DB / folder
		newScript = new AgentScript();
		newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; var notif = agent.createNotification('teste'); notif.content = 'opa!'; notif.send(); log('pronto');");
		
		scripts.add(newScript);
		
		newScript = new AgentScript();
		newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; agent.sms.send({ destination: '5556', message: 'Ola!'}, function(err) { log(err); });");
		
		scripts.add(newScript);
		
		newScript = new AgentScript();
		newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; log('music: ' + agent.music); agent.music.playFromUrl('/Music/Chasing The Sun.mp3');");
		
		scripts.add(newScript);
		
		newScript = new AgentScript();
		newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; log('network: ' + agent.network); agent.network.on('wifi:connected', function(data) { log('conectou'); });");
		
		scripts.add(newScript);
		
		newScript = new AgentScript();
		newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; log('application: ' + agent.apps); var installedApps = agent.apps.getInstalledApps(); for(var i = 0; i < installedApps.length; i++){ log(installedApps[i].packageName); } ");
		
		scripts.add(newScript);
		
		newScript = new AgentScript();
		newScript.setSourceCode("agent.apps.launchAppByPackage('com.android.contacts');");
		
		scripts.add(newScript);
		
		
		return scripts;
	}

}

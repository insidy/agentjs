package br.unisinos.swe.agentjs.engine.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.EngineContext;

import android.annotation.SuppressLint;
import android.os.Environment;

@SuppressLint("DefaultLocale")
public class AgentScriptManager {

	private static final String AGENTS_PATH = "/agentjs/src";
	private File rootDir;

	public AgentScriptManager() {
		rootDir = new File(Environment.getExternalStorageDirectory().toString()
				+ AGENTS_PATH);
		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}
	}

	/*
	 * public String getScriptFromServer() { return
	 * "var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; function doUrlGet(param) { agent.http.get({ url: 'http://www.google.com'}, function(response) { log('aee!'); log(response); }, function(response) {  }); return { foo: \"bar in JavaScript\" }; }; function hello(java) { if (typeof log != 'undefined') { log(\"JavaScript say hello to \" + java); log(\"Also, I can access Java object: \" + javaContext); } return { foo: \"bar in JavaScript\" }; } doUrlGet('gangnam style');"
	 * ; }
	 */

	public ArrayList<AgentScript> getLocalScripts() {
		ArrayList<AgentScript> scripts = new ArrayList<AgentScript>();

		scripts.addAll(lookupAgentsInFolder());

		/*
		 * AgentScript newScript = new AgentScript();
		 * newScript.setSourceCode(getScriptFromServer());
		 * 
		 * scripts.add(newScript);
		 */

		// TODO: Get source from DB
		AgentScript newScript = new AgentScript();
		//newScript = new AgentScript();
		//newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; var notif = agent.createNotification('teste'); notif.content = 'opa!'; notif.send(); log('pronto');");

		//scripts.add(newScript);

		newScript = new AgentScript();
		newScript
				.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; agent.sms.send({ destination: '5556', message: 'Ola!'}, function(err) { log(err || 'sms enviado'); });");

		scripts.add(newScript);

		//newScript = new AgentScript();
		//newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; log('music: ' + agent.music); agent.music.playFromUrl('/Music/Chasing The Sun.mp3');");

		//scripts.add(newScript);

		//newScript = new AgentScript();
		//newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; log('network: ' + agent.network); agent.network.on('wifi:connected', function(data) { log('conectou'); });");

		//scripts.add(newScript);

		//newScript = new AgentScript();
		//newScript.setSourceCode("var log = Packages.br.unisinos.swe.agentjs.engine.EngineLogger.i; log('application: ' + agent.apps); var installedApps = agent.apps.getInstalledApps(); for(var i = 0; i < installedApps.length; i++){ log(installedApps[i].packageName); } ");

		//scripts.add(newScript);

		//newScript = new AgentScript();
		//newScript.setSourceCode("agent.apps.launchAppByPackage('com.android.contacts');");

		//scripts.add(newScript);

		return scripts;
	}

	private ArrayList<AgentScript> lookupAgentsInFolder() {
		ArrayList<AgentScript> scripts = new ArrayList<AgentScript>();

		FilenameFilter jsFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".js")) {
					return true;
				} else {
					return false;
				}
			}
		};

		File[] availableAgents = rootDir.listFiles(jsFilter);

		if (availableAgents != null) {

			for (File agentScriptFile : availableAgents) {
				AgentScript newScript = new AgentScript();
				StringBuilder agentSourceCode = new StringBuilder();
				if (agentScriptFile.isFile()) {
					BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(agentScriptFile));

						String line;

						while ((line = br.readLine()) != null) {
							agentSourceCode.append(line);
							agentSourceCode.append('\n');
						}

						br.close();

					} catch (FileNotFoundException e) {
						EngineContext.log().error(
								"unable to read agent source code");
						e.printStackTrace();
					} catch (IOException e) {
						EngineContext.log().error(
								"unable to read agent source code");
						e.printStackTrace();
					}
					newScript.setSourceCode(agentSourceCode.toString());
					scripts.add(newScript);
				}

			}
		}

		return scripts;
	}

}

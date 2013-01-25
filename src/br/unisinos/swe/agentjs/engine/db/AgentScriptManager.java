package br.unisinos.swe.agentjs.engine.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.unisinos.swe.agentjs.engine.AgentExecutor;
import br.unisinos.swe.agentjs.engine.Engine;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.IAgentScriptManager;
import br.unisinos.swe.agentjs.engine.upnp.DeviceWrapper;
import br.unisinos.swe.agentjs.engine.upnp.IAgentUPnPListener;
import br.unisinos.swe.agentjs.engine.upnp.IAgentUPnPHandler;

import android.annotation.SuppressLint;
import android.os.Environment;

@SuppressLint("DefaultLocale")
public class AgentScriptManager implements IAgentUPnPListener, IAgentScriptManager {

	private static final String AGENTS_PATH = "/agentjs/src";
	private File rootDir;
	
	private ArrayList<AgentScript> _localScripts;
	private HashMap<DeviceWrapper, ArrayList<AgentScript>> _networkScripts;
	private IAgentUPnPHandler _upnp;

	public AgentScriptManager(IAgentUPnPHandler upnp) {
		_upnp = upnp;
		
		_localScripts = new ArrayList<AgentScript>();
		_networkScripts = new HashMap<DeviceWrapper, ArrayList<AgentScript>>();
		
		rootDir = new File(Environment.getExternalStorageDirectory().toString()
				+ AGENTS_PATH);
		if (!rootDir.exists()) {
			rootDir.mkdirs();
		}
	}

	/* (non-Javadoc)
	 * @see br.unisinos.swe.agentjs.engine.db.IAgentScriptManager#getLocalScripts()
	 */
	@Override
	public ArrayList<AgentScript> getLocalScripts() {
		if(_localScripts.size() == 0) {
			_localScripts = lookupAgentsInFolder();
		}

		return _localScripts;
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

	/* (non-Javadoc)
	 * @see br.unisinos.swe.agentjs.engine.db.IAgentScriptManager#getNetworkScripts()
	 */
	@Override
	public ArrayList<AgentScript> getNetworkScripts() {
		
		ArrayList<AgentScript> scripts = new ArrayList<AgentScript>();
		for(DeviceWrapper device : _networkScripts.keySet()) {
			scripts.addAll(_networkScripts.get(device));
		}
		
		return scripts;
	}

	@Override
	public void start() {
		_upnp.startDiscovery(this);
	}

	@Override
	public void stop() {
		_upnp.stopDiscovery(this);
	}

	@Override
	public void deviceRemoved(DeviceWrapper deviceWrapper) {
		_networkScripts.remove(deviceWrapper);
	}

	@Override
	public void deviceAdded(DeviceWrapper deviceWrapper) {
		if(!_networkScripts.containsKey(deviceWrapper)) {
			ArrayList<AgentScript> deviceScripts = new ArrayList<AgentScript>();
			_networkScripts.put(deviceWrapper, deviceScripts);
			_upnp.getAgents(deviceWrapper, this);
		}
		
	}

	@Override
	public void connected() {
		_networkScripts.clear();
	}

	@Override
	public void setAvailableAgents(DeviceWrapper device, List<AgentScript> agents) {
		if(!_networkScripts.containsKey(device)) {
			_networkScripts.get(device).addAll(agents);
		}
	}
	
	private AgentNetworkScript getAgentRefFromList(DeviceWrapper device, AgentScript agent) {
		List<AgentScript> currentScripts = _networkScripts.get(device);
		if(currentScripts != null) {
			int agentIdx = currentScripts.indexOf(agent);
			if(agentIdx >= 0) {
				AgentNetworkScript currentScript = (AgentNetworkScript)currentScripts.get(agentIdx);
				return currentScript;
			}
		}
		return null;
	}

	@Override
	public void setAgentSourceCode(DeviceWrapper device, AgentScript agent) {
		AgentNetworkScript currentScript = getAgentRefFromList(device, agent);
		if(currentScript != null) {
			currentScript.setSourceCode(agent.getSourceCode());
			
			if(currentScript.initializeOnSource && currentScript.engine != null){
				startScript(currentScript.engine, currentScript);
			}
		}
	}
	
	@Override
	public void startScript(Engine engine, AgentScript script) {
		if(script.hasSource()) {
			AgentExecutor executor = new AgentExecutor(engine, script);
			executor.execute();
		} else { // delayed/network start
			if(script instanceof AgentNetworkScript) {
				DeviceWrapper device = ((AgentNetworkScript)script).getDeviceWrapper();
				AgentNetworkScript currentScript = getAgentRefFromList(device, script);
				
				currentScript.initializeOnSource = true;
				currentScript.engine = engine;
				
				_upnp.getSourceCode(device, currentScript.getId(), this);
			}
		}
	}

}

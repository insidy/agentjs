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

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.util.concurrent.FutureCallback;

import br.unisinos.swe.agentjs.engine.AgentExecutor;
import br.unisinos.swe.agentjs.engine.Engine;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.IAgentScriptManager;
import br.unisinos.swe.agentjs.engine.signals.info.UserInfo;
import br.unisinos.swe.agentjs.engine.upnp.DeviceWrapper;
import br.unisinos.swe.agentjs.engine.upnp.IAgentUPnPListener;
import br.unisinos.swe.agentjs.engine.upnp.IAgentUPnPHandler;
import br.unisinos.swe.http.utils.HttpQueue;
import br.unisinos.swe.http.utils.HttpQueueManager;
import br.unisinos.swe.http.utils.HttpQueueRequest;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;

@SuppressLint("DefaultLocale")
public class AgentScriptManager implements IAgentUPnPListener, IAgentScriptManager {

	private static final String AGENTS_PATH = "/agentjs/src";
	private File rootDir;
	
	private ArrayList<AgentScript> _localScripts;
	private HashMap<DeviceWrapper, ArrayList<AgentScript>> _networkScripts;
	private IAgentUPnPHandler _upnp;
	
	private List<IAgentChangeEvent> _agentStateListeners; // inform them about any changes in agent list

	private HttpQueue _httpQueue;
	
	public AgentScriptManager(IAgentUPnPHandler upnp) {
		_httpQueue = HttpQueueManager.create();
		_upnp = upnp;
		
		_localScripts = new ArrayList<AgentScript>();
		_networkScripts = new HashMap<DeviceWrapper, ArrayList<AgentScript>>();
		_agentStateListeners = new ArrayList<IAgentChangeEvent>();
		
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
	
	protected void updateLocalScripts(List<AgentScript> webScripts) {
		for(AgentScript script : webScripts) {
			if(_localScripts.contains(script)) {
				int index = _localScripts.indexOf(script);
				AgentScript currentScript = _localScripts.get(index);
				currentScript.setSourceCode(script.getSourceCode());
				this.informChangeAgent(currentScript);
			} else {
				_localScripts.add(script);
				this.informAddAgent(script);
			}
		}
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
					newScript.setName(agentScriptFile.getName());
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
		List<AgentScript> removedScripts = _networkScripts.remove(deviceWrapper);
		if(removedScripts != null) {
			for(AgentScript script : removedScripts) {
				informRemoveAgent(script);
			}
		}
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
		ArrayList<AgentScript> removeScripts = new ArrayList<AgentScript>();
		for(DeviceWrapper device : _networkScripts.keySet()) {
			removeScripts.addAll(_networkScripts.get(device));
		}
		
		for(AgentScript scripts : removeScripts) {
			informRemoveAgent(scripts);
		}
		
		_networkScripts.clear();
	}

	@Override
	public void setAvailableAgents(DeviceWrapper device, List<AgentScript> agents) {
		if(_networkScripts.containsKey(device)) {
			_networkScripts.get(device).addAll(agents);
			for(AgentScript script : agents) {
				informAddAgent(script);
			}
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
	public void startScript(final Engine engine, final AgentScript script) {
		if(script.hasSource()) {
			script.setStarted();
			informChangeAgent(script);
			
			Handler mainHandler = new Handler(EngineContext.instance().getContext().getMainLooper());

			Runnable scriptRunnable = new Runnable() {
				
				@Override
				public void run() {
					AgentExecutor executor = new AgentExecutor(engine, script);
					executor.execute();
				}
			};
			mainHandler.post(scriptRunnable);
			
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
	
	public void informAddAgent(AgentScript script) {
		for(IAgentChangeEvent event : _agentStateListeners) {
			event.addAgent(script);
		}
	}
	
	public void informRemoveAgent(AgentScript script) {
		for(IAgentChangeEvent event : _agentStateListeners) {
			event.removeAgent(script);
		}
	}
	
	public void informChangeAgent(AgentScript script) {
		for(IAgentChangeEvent event : _agentStateListeners) {
			event.agentStateChanged(script);
		}
	}

	@Override
	public void registerListener(IAgentChangeEvent listener) {
		_agentStateListeners.add(listener);
	}

	@Override
	public void removeListener(IAgentChangeEvent listener) {
		_agentStateListeners.remove(listener);
	}

	@Override
	public void refreshFromWeb() {
		
		
		String ctxUrl = EngineContext.instance().getCloudUrl() + "rest/agent/" + (new UserInfo()).getFacebookName();
		HttpQueueRequest request = new HttpQueueRequest("GET", ctxUrl, null, new FutureCallback<HttpEntity>() {
			
			@Override
			public void onSuccess(HttpEntity response) {
				try {
					String jsonResponse = EntityUtils.toString(response);
					
					JSONObject jsRootObject = new JSONObject(jsonResponse);
					JSONArray jsArray = new JSONArray();
					try {
						jsArray = jsRootObject.getJSONArray("agentJson");
					} catch(JSONException ex) {
						JSONObject jsAgentJson = jsRootObject.getJSONObject("agentJson");
						jsArray.put(jsAgentJson);
					}
					
					List<AgentScript> scripts = new ArrayList<AgentScript>();
					
					for(int i = 0; i < jsArray.length(); i++) {
						JSONObject jsAgentObject = jsArray.getJSONObject(i);
						scripts.add(new AgentScript(jsAgentObject));
					}
					
					updateLocalScripts(scripts);
					
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Throwable arg0) {
				
			}
		});
		request.setHeader("Content-Type", "application/json");
		_httpQueue.fireEnsureCallback(request);
	}

}

package br.unisinos.swe.agentjs.engine.upnp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.header.UDAServiceTypeHeader;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.Datatype;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import br.unisinos.swe.agentjs.engine.db.AgentNetworkScript;
import br.unisinos.swe.agentjs.engine.db.AgentScript;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class UPnPHandler extends DefaultRegistryListener implements IAgentUPnPHandler {
	
	private android.app.Service _parentService;
	private ServiceConnection _serviceConnection;
	private AndroidUpnpService _upnpService;
	private ArrayList<IAgentUPnPListener> _listeners;
	
	protected UDAServiceType upnpAgentService = new UDAServiceType("AgentJS");
			
	
	public UPnPHandler(android.app.Service parent) {
		_parentService = parent;
		_listeners = new ArrayList<IAgentUPnPListener>();
		
		_serviceConnection  = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				_upnpService = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				_upnpService = (AndroidUpnpService) service;

		        // Refresh the list with all known devices
				notifyConnected();
		        for (Device device : _upnpService.getRegistry().getDevices()) {
		        	if(device.findService(upnpAgentService) != null) {
		        		deviceAdded(device);
		        	}
		        }

		        // Getting ready for future device advertisements
		        _upnpService.getRegistry().addListener(UPnPHandler.this);

		        // Search asynchronously for all devices
		        _upnpService.getControlPoint().search(new UDAServiceTypeHeader(upnpAgentService));
			}
		};
	}
	
	protected void notifyConnected() {
		for(IAgentUPnPListener listener : _listeners) {
			listener.connected();
		}
	}

	@Override
	public void startDiscovery(IAgentUPnPListener listener) {
		if (_upnpService == null) {
		_parentService.getApplicationContext().bindService(
				new Intent(_parentService, AndroidUpnpServiceImpl.class),
	            _serviceConnection,
	            Context.BIND_AUTO_CREATE
	        );
		}
		
		_listeners.add(listener);
	}

	@Override
	public void stopDiscovery(IAgentUPnPListener listener) {
		if (_upnpService != null) {
            _upnpService.getRegistry().removeListener(this);
            _upnpService = null;
        }
        _parentService.getApplicationContext().unbindService(_serviceConnection);
        
        _listeners.clear();
	}
	
	@Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        deviceRemoved(device);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        deviceAdded(device);
    }

	@Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        deviceRemoved(device);
    }

	@Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        deviceAdded(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        deviceRemoved(device);
    }

    private void deviceRemoved(final Device device) {
		for(IAgentUPnPListener listener : _listeners) {
			listener.deviceRemoved(new DeviceWrapper(device));
		}
	}

    private void deviceAdded(final Device device) {
    	for(IAgentUPnPListener listener : _listeners) {
			listener.deviceAdded(new DeviceWrapper(device));
		}
	}
    
    @Override
	public void getAgents(DeviceWrapper wrapper, IAgentUPnPListener listener) {
    	final DeviceWrapper refDevice = wrapper;
    	final IAgentUPnPListener callback = listener;
    	
		if(_upnpService != null) {
			Service service = wrapper.getDevice().findService(upnpAgentService);
			Action getAgentListAction = service.getAction("GetAgentList");
			
			ActionInvocation getAgentListInvocation = new ActionInvocation(getAgentListAction);
			ActionCallback getAgentListCallback = new ActionCallback(getAgentListInvocation) {
				
				@Override
				public void success(ActionInvocation invocation) {
					List<AgentScript> scripts = new ArrayList<AgentScript>();
					
					ActionArgumentValue agents = invocation.getOutput("agents");
					if(agents != null) {
						if(agents.getDatatype().getBuiltin() == Datatype.Builtin.STRING) {
							String encodedJsonArray = String.valueOf(agents.getValue());
							
							try {
								JSONArray agentJsonArray = new JSONArray(encodedJsonArray);
								
								
								for(int idx = 0; idx < agentJsonArray.length(); idx++) {
									JSONObject agentObject = agentJsonArray.getJSONObject(idx);
									AgentScript networkScript = new AgentNetworkScript(refDevice, agentObject);
									scripts.add(networkScript);
								}
								
							} catch (JSONException e) {
								e.printStackTrace();
							} 
							
						}
					}
					
					callback.setAvailableAgents(refDevice, scripts);
					
				}
				
				@Override
				public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
					// don't callback
				}
			};
			_upnpService.getControlPoint().execute(getAgentListCallback);
		}
	}

	@Override
	public void getSourceCode(DeviceWrapper wrapper, String agentId, IAgentUPnPListener listener) {
		final DeviceWrapper refDevice = wrapper;
    	final IAgentUPnPListener callback = listener;
    	
		if(_upnpService != null) {
			Service service = wrapper.getDevice().findService(upnpAgentService);
			Action getAgentSourceAction = service.getAction("GetAgentSource");
			
			ActionInvocation getAgentSourceInvocation = new ActionInvocation(getAgentSourceAction);
			getAgentSourceInvocation.setInput("AgentId", agentId);
			
			ActionCallback getAgentSourceCallback = new ActionCallback(getAgentSourceInvocation) {
				
				@Override
				public void success(ActionInvocation invocation) {
					AgentScript networkScript = null;
					
					ActionArgumentValue agents = invocation.getOutput("agent");
					if(agents != null) {
						if(agents.getDatatype().getBuiltin() == Datatype.Builtin.STRING) {
							String encodedJsonArray = String.valueOf(agents.getValue());
							
							try {
								JSONObject agentObject = new JSONObject(encodedJsonArray);
								networkScript = new AgentNetworkScript(refDevice, agentObject);
								
							} catch (JSONException e) {
								e.printStackTrace();
							} 
							
						}
					}
					
					callback.setAgentSourceCode(refDevice, networkScript);
				}
				
				@Override
				public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				}
			};
			
			_upnpService.getControlPoint().execute(getAgentSourceCallback);
		}
	}

}

package br.unisinos.swe.agentjs.ui;

import java.util.ArrayList;
import java.util.HashMap;

import br.unisinos.swe.agentjs.EngineService;
import br.unisinos.swe.agentjs.EngineService.EngineBinder;
import br.unisinos.swe.agentjs.R;
import br.unisinos.swe.agentjs.engine.db.AgentScript;
import br.unisinos.swe.agentjs.engine.db.AgentScript.AgentScriptLocation;
import br.unisinos.swe.agentjs.engine.db.IAgentChangeEvent;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class AgentsExapandableListAdapter extends BaseExpandableListAdapter implements IAgentChangeEvent {
	
	private Activity _parentActivity;
	private Context _context;
	private EngineService _engine = null;
	private boolean _bound = false;
	private AgentScriptLocation _type;
	
	private ArrayList<AgentScriptView> _scripts = new ArrayList<AgentScriptView>();
	
	public AgentsExapandableListAdapter(Activity activity, AgentScriptLocation type) {
		_parentActivity = activity;
		_context = _parentActivity;
		_type = type;
		
		
		Intent intent = new Intent(_context.getApplicationContext(), EngineService.class);
		_context.getApplicationContext().bindService(intent, _connection, Context.BIND_AUTO_CREATE);
        
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return _scripts.get(groupPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final int agentPosition = groupPosition;
		
		AgentScriptView scriptView = (AgentScriptView) getChild(groupPosition, childPosition);
		if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.script_child_view, null);
        }
		
		// render child textviews/buttons
		Button btnStart = (Button)convertView.findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Button btn = (Button)view.findViewById(R.id.btnStart);
				btn.setEnabled(false);
				startAgent(agentPosition);
			}
		});
		if(scriptView.isRunning()) {
			btnStart.setEnabled(false);
		} else {
			btnStart.setEnabled(true);
		}
		
		convertView.setFocusableInTouchMode(true);
		
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return _scripts.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return _scripts.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	private final void startAgent(int agentPosition) {
		AgentScriptView scriptView = (AgentScriptView) getGroup(agentPosition);
		_engine.getEngine().startScript(scriptView.getScript());
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final int agentPosition = groupPosition;
		AgentScriptView scriptView = (AgentScriptView) getGroup(groupPosition);
		
		if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.script_group_view, null);
        }
		
		TextView txtAgentName = (TextView) convertView.findViewById(R.id.txtAgentName);
		CheckBox chkAgentRunning = (CheckBox) convertView.findViewById(R.id.chkAgentRunning);
		
		txtAgentName.setText(scriptView.getName());
		chkAgentRunning.setChecked(scriptView.isRunning());
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}
	
	 /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection _connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	EngineBinder binder = (EngineBinder) service;
            _engine = binder.getService();
            _bound = true;
            
            ArrayList<AgentScript> availableScripts = null;
            
            switch(_type) {
            case OWN:
            	availableScripts = _engine.getEngine().getLocalScripts();
            	break;
            case NETWORK:
            	availableScripts = _engine.getEngine().getNetworkScripts();
            	_engine.getEngine().registerAgentListener(AgentsExapandableListAdapter.this);
            	break;
            }
            
            if(availableScripts != null) {
            	for(AgentScript script : availableScripts) {
            		AgentScriptView view = new AgentScriptView(script);
            		_scripts.add(view);
            		notifyDataSetChanged();
            	}
            }
            
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	_bound = false;
        }
    };

	@Override
	public void addAgent(AgentScript script) {
		_scripts.add(new AgentScriptView(script));
		_parentActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
		
	}

	@Override
	public void removeAgent(AgentScript script) {
		_scripts.remove(new AgentScriptView(script));
		_parentActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public void agentStateChanged(AgentScript script) {
		_parentActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

}

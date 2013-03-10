package br.unisinos.swe.agentjs;

import br.unisinos.swe.agentjs.engine.db.AgentScript.AgentScriptLocation;
import br.unisinos.swe.agentjs.ui.AgentsExapandableListAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class LocalAgentsActivity extends Activity {

	private AgentsExapandableListAdapter _adapter;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_agents);
       
        final ExpandableListView listView = (ExpandableListView) findViewById(R.id.localAgentsExpandable);
        
        _adapter = new AgentsExapandableListAdapter(this, AgentScriptLocation.OWN);

        // Set this blank adapter to the list view
        listView.setAdapter(_adapter);
        listView.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				_adapter.startAgent(groupPosition);
				return true;
			}
		});
        
        IntentFilter filter = new IntentFilter("br.unisinos.swe.agentjs.refresh");
        this.registerReceiver(new Receiver(), filter);
        
    }
	
	private class Receiver extends BroadcastReceiver {

		 @Override
		 public void onReceive(Context arg0, Intent arg1) {

		    LocalAgentsActivity.this._adapter.retrieveFromWeb();
		 }
	}
}

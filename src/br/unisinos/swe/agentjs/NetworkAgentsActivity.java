package br.unisinos.swe.agentjs;

import br.unisinos.swe.agentjs.engine.db.AgentScript.AgentScriptLocation;
import br.unisinos.swe.agentjs.ui.AgentsExapandableListAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class NetworkAgentsActivity extends Activity {

	private AgentsExapandableListAdapter _adapter;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_agents);
       
        final ExpandableListView listView = (ExpandableListView) findViewById(R.id.localAgentsExpandable);
        
        _adapter = new AgentsExapandableListAdapter(this, AgentScriptLocation.NETWORK);
        
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
        
    }
}

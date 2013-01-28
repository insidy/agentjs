package br.unisinos.swe.agentjs;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.common.util.concurrent.FutureCallback;

import br.unisinos.swe.agentjs.engine.api.AgentHttpClient;
import br.unisinos.swe.http.utils.HttpQueue;
import br.unisinos.swe.http.utils.HttpQueueManager;
import br.unisinos.swe.http.utils.HttpQueueRequest;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class AndroidAgentJSActivity extends TabActivity {
	
	protected Intent _serviceIntent;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        _serviceIntent = new Intent(this.getApplicationContext(), EngineService.class);
        this.startService(_serviceIntent);
		
		 TabHost tabHost = getTabHost();
		 
        // Tab for Photos
        TabSpec localAgentsTab = tabHost.newTabSpec("LocalAgents");
        //photospec.setIndicator("Photos", getResources().getDrawable(R.drawable.icon_photos_tab));
        localAgentsTab.setIndicator("Local Agents");
        Intent localAgentsActivity = new Intent(this, LocalAgentsActivity.class);
        localAgentsTab.setContent(localAgentsActivity);
        
        TabSpec networkAgentsTab = tabHost.newTabSpec("NetworkAgents");
        //photospec.setIndicator("Photos", getResources().getDrawable(R.drawable.icon_photos_tab));
        networkAgentsTab.setIndicator("Network Agents");
        Intent networkAgentsActivity = new Intent(this, NetworkAgentsActivity.class);
        networkAgentsTab.setContent(networkAgentsActivity);
        
        
        tabHost.addTab(localAgentsTab);
        tabHost.addTab(networkAgentsTab);
    }
    
}
package br.unisinos.swe.agentjs.ui;

import br.unisinos.swe.agentjs.R;
import br.unisinos.swe.agentjs.engine.EngineContext;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ConfigActivity extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preference); // will not use preferences fragment because we are supporting API level 8
        }
        
        protected void onPause() {
                super.onPause();
                
                /*
                SharedPreferences preferences = this.getApplicationContext().getSharedPreferences(EngineContext.USER_FILE, 0);
                SharedPreferences.Editor editor = preferences.edit();
                
                editor.putString("cloud.url", PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("cloud.url", ""));
                editor.putString("user.facebook.name", PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("user.facebook.name", ""));
                editor.commit();*/
                
        }
}

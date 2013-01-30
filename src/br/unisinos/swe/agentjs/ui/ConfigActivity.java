package br.unisinos.swe.agentjs.ui;

import br.unisinos.swe.agentjs.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigActivity extends PreferenceActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preference); // will not use preferences fragment because we are supporting API level 8
        }
        
        protected void onPause() {
                super.onPause();
                
        }
}

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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AndroidAgentJSActivity extends Activity {
	
	protected Intent _serviceIntent;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        _serviceIntent = new Intent(this, EngineService.class);
        
        Button btnStart = (Button)findViewById(R.id.btnStartService);
        Button btnStop = (Button)findViewById(R.id.btnStopService);
        
        btnStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				_serviceIntent = new Intent(AndroidAgentJSActivity.this, EngineService.class);
				startService(_serviceIntent);
			}
		});
        
        btnStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				stopService(_serviceIntent);
			}
		});
        
        
        
        //EngineContext.create(this.getApplicationContext());
        //runScript();
    }
    
    private static final String RHINO_LOG = "var log = Packages.br.unisinos.swe.agentjs.AndroidAgentJSActivity.log;";
    public static void log(String msg) {
        android.util.Log.i("RHINO_LOG", msg);
    }
    
    public String getScriptFromServer() {
    	return "function doUrlGet(param) { http.get({ url: 'http://www.google.com'}, function(response) { log('aee!'); log(response); }, function(response) {  }); return { foo: \"bar in JavaScript\" }; }; function hello(java) { if (typeof log != 'undefined') { log(\"JavaScript say hello to \" + java); log(\"Also, I can access Java object: \" + javaContext); } return { foo: \"bar in JavaScript\" }; }";
    }

    public void runScript() {
        // Get the JavaScript in previous section
        String source = getScriptFromServer();
        String functionName = "doUrlGet";
        Object[] functionParams = new Object[] { "Android" };

        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        Context rhino = Context.enter();

        // Turn off optimization to make Rhino Android compatible
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            // This line set the javaContext variable in JavaScript
            AgentHttpClient httpClient = null; // new AgentHttpClient(rhino, scope);
            
            ScriptableObject.putProperty(scope, "javaContext", Context.javaToJS(this.getApplicationContext(), scope));
            ScriptableObject.putProperty(scope, "http", Context.javaToJS(httpClient, scope));

            // Note the forth argument is 1, which means the JavaScript source has
            // been compressed to only one line using something like YUI
            rhino.evaluateString(scope, RHINO_LOG + source, "ScriptAPI", 1, null);

            Object[] ids = scope.getIds();
            
            // We get the hello function defined in JavaScript
            Function function = (Function) scope.get(functionName, scope);

            // Call the hello function with params
            NativeObject result = (NativeObject) function.call(rhino, scope, scope, functionParams);
            // After the hello function is invoked, you will see logcat output

            // Finally we want to print the result of hello function
            String foo = (String) Context.jsToJava(result.get("foo", result), String.class);
            log(foo);
        } finally {
            // We must exit the Rhino VM
            Context.exit();
        }
    }
}
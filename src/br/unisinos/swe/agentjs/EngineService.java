package br.unisinos.swe.agentjs;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import com.google.common.util.concurrent.FutureCallback;

import br.unisinos.swe.agentjs.engine.Engine;
import br.unisinos.swe.http.utils.HttpQueue;
import br.unisinos.swe.http.utils.HttpQueueManager;
import br.unisinos.swe.http.utils.HttpQueueRequest;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class EngineService extends Service {

	private Engine _engine;
	
	/*
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// Normally we would do some work here, like download a file.
			// For our sample, we just sleep for 5 seconds.
			long endTime = System.currentTimeMillis() + 5 * 1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					try {
						wait(endTime - System.currentTimeMillis());
					} catch (Exception e) {
					}
				}
			}
			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			stopSelf(msg.arg1);
		}
	}
	*/
	
	@Override
	public void onCreate() {
		/*
		 * // Start up the thread running the service. Note that we create a //
		 * separate thread because the service normally runs in the process's //
		 * main thread, which we don't want to block. We also make it //
		 * background priority so CPU-intensive work will not disrupt our UI.
		 * HandlerThread thread = new HandlerThread("ServiceStartArguments",
		 * Process.THREAD_PRIORITY_BACKGROUND); thread.start();
		 * 
		 * // Get the HandlerThread's Looper and use it for our Handler
		 * mServiceLooper = thread.getLooper(); mServiceHandler = new
		 * ServiceHandler(mServiceLooper);
		 */
		
		this._engine = new Engine(this.getApplicationContext());
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "AgentJS Engine starting", Toast.LENGTH_SHORT).show();

		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		/*
		 * Message msg = mServiceHandler.obtainMessage(); msg.arg1 = startId;
		 * mServiceHandler.sendMessage(msg);
		 */
		// If we get killed, after returning from here, restart
		
		//start engine on a new thread
		this._engine.start();
		
		
		//teste de fila http
        HttpQueue queue = HttpQueueManager.create();
        
        queue.fireEnsureCallback(new HttpQueueRequest("GET", "http://www.google.com", null, new FutureCallback<HttpEntity>() {
			
			@Override
			public void onSuccess(HttpEntity arg0) {
				try {
					Log.i("OK", "R1");
					Log.i("OK", EntityUtils.toString(arg0));
					Log.i("OK", "R1");
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Throwable arg0) {
				Log.e("ERR", "Failed R1");
				arg0.printStackTrace();
			}
		}));
		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		this._engine.stop();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}

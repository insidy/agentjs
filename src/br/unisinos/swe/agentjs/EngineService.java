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
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class EngineService extends Service {

	private Engine _engine = null;
	private final EngineBinder _binder = new EngineBinder();
	
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
		super.onCreate();
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
		
		if(this._engine == null) {
			this._engine = new Engine(this);
		}
		
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
		/*
        
        // TODO : make QueueManager HTTP calls on other thread
        HttpQueue queue = HttpQueueManager.create();
        
        queue.fireEnsureCallback(new HttpQueueRequest("GET", "http://www.google.com", null, new FutureCallback<HttpEntity>() {
			
			@Override
			public void onSuccess(HttpEntity arg0) {
				try {
					Log.i("OK", "R1");
					Log.i("OK", EntityUtils.toString(arg0));
					Log.i("OK", "R1");
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Throwable arg0) {
				Log.e("ERR", "Failed R1");
				arg0.printStackTrace();
			}
		}));
        */
		
		/**
		 * Service.START_STICKY is used for services which are explicit started or stopped. 
		 * If these services are terminated by the Android system, they are restarted if sufficient resource are available again.
		 * Services started with Service.START_NOT_STICKY are not automatically restarted if terminated by the Android system.
		 */
		
		
		return Service.START_NOT_STICKY;
		//return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		this._engine.stop();
	}

	/**
	 * Currently we will only run in the same process as AgentJS Activity, therefore we don't need to handle things through IPC
	 */
	public class EngineBinder extends Binder {
		public EngineService getService() {
			return EngineService.this;
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i("EngineContext", "onBind");
		return _binder;
	}
	

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }
	
	public Engine getEngine() {
		return this._engine;
	}

}

package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.info.AppDispatcherSignalInfo;

public class AppDispatcherSignalEmitter extends AbstractSignalEmitter {
	
	private BroadcastReceiver _appDispatcherReceiver;
	
	private AppDispatcherSignalInfo _lastRunningApp = null;
	private Long _lastAppUpdate = 0L;
	private Long _lastOwnUpdate = 0L;
	
	private ActivityManager _activityManager;
	private PackageManager _packageManager;
	
	public static enum DispatcherSignal {
		HOME_APP_STARTED("home:app:started");

		private DispatcherSignal(final String signal) {
			this._signal = signal;
		}

		private final String _signal;

		public String toString() {
			return this._signal;
		}
	}
	
	public AppDispatcherSignalEmitter() {
		super();
		
		if(_signals == null) {
			_signals = new ArrayList<String>();
			for (DispatcherSignal signal : DispatcherSignal.class.getEnumConstants()) {
				_signals.add(signal.toString());
			}
		}
		
		Context androidContext = EngineContext.instance().getContext();
		_activityManager = (ActivityManager)androidContext.getSystemService(Activity.ACTIVITY_SERVICE);
		_packageManager = androidContext.getPackageManager();
	}

	public static ISignalEmitter create() {
		return new AppDispatcherSignalEmitter();
	}

	@Override
	public ISignalEmitter start() {
		IntentFilter appDispatcherFilter = new IntentFilter();
		appDispatcherFilter.addAction("com.unisinos.AppDispatcher");
		
		this._appDispatcherReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context appContext, Intent broadcastIntent) {
				String title = broadcastIntent.getStringExtra("App.Title");
				String packageName = broadcastIntent.getStringExtra("App.PackageName");
				String execCount = broadcastIntent.getStringExtra("App.ExecutionCount");
				AppDispatcherSignalInfo info = new AppDispatcherSignalInfo(title, packageName, Integer.parseInt(execCount));
				
				_lastRunningApp = info; // TODO: refactor this to apps polling method
				_lastAppUpdate = System.currentTimeMillis();
				
				AppDispatcherSignalEmitter.this.fire(DispatcherSignal.HOME_APP_STARTED.toString(), info);
			}
		};
		
		EngineContext.instance().getContext().registerReceiver(this._appDispatcherReceiver, appDispatcherFilter);
		
		return this;
	}

	@Override
	public void stop() {
		EngineContext.instance().getContext().unregisterReceiver(this._appDispatcherReceiver);
	}

	@Override
	public boolean filter(String signal, ISignalListener listener, Object...params) {
		return true; // no filter available
	}
	
	public AppDispatcherSignalInfo getLastRunningApp() {
		if(_lastAppUpdate <= _lastOwnUpdate) {
			findLastRunningApp();
		}
		
		return _lastRunningApp;
	}
	
	private void findLastRunningApp() {
		boolean foundByImportance = false;
		List<AppDispatcherSignalInfo> runningApps = this.getRunningApps();
		/*
		for(AppDispatcherSignalInfo app : runningApps) {
			if(app.getImportance() == 100) {
				foundByImportance = true;
				_lastRunningApp = app;
			}
		}*/
		
		if(!foundByImportance) {
			_lastRunningApp = runningApps.get(0);
		}
		
		_lastAppUpdate = System.currentTimeMillis();
		_lastOwnUpdate = _lastAppUpdate;
	}

	public List<AppDispatcherSignalInfo> getRunningApps() {
		List<AppDispatcherSignalInfo> runningApps = new ArrayList<AppDispatcherSignalInfo>();
	    
	    //List<ActivityManager.RunningAppProcessInfo> processes = _activityManager.getRunningAppProcesses();
	    List<RunningTaskInfo> tasks = _activityManager.getRunningTasks(10);

	    for(RunningTaskInfo task : tasks) {
	    	try {
	    		ApplicationInfo androidAppInfo = _packageManager.getApplicationInfo(task.topActivity.getPackageName(), 0);
				AppDispatcherSignalInfo appInfo = new AppDispatcherSignalInfo(androidAppInfo.name, androidAppInfo.packageName, 0);
				runningApps.add(appInfo);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
	    }

	    /*
	    for(ActivityManager.RunningAppProcessInfo process : processes) {
	    	try {
				ApplicationInfo androidAppInfo = _packageManager.getApplicationInfo(process.processName, 0);
				AppDispatcherSignalInfo appInfo = new AppDispatcherSignalInfo(androidAppInfo.name, androidAppInfo.packageName, 0, process.importance);
				runningApps.add(appInfo);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
	    	
	    }*/
	    
	    return runningApps;
	}

}

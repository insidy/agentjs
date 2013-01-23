package br.unisinos.swe.agentjs.engine.signals;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.signals.info.AppDispatcherSignalInfo;

public class AppDispatcherSignalEmitter extends AbstractSignalEmitter {
	
	private BroadcastReceiver _appDispatcherReceiver;
	
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
	}

	public static ISignalEmitter create() {
		if (_instance == null) {
			_instance = new AppDispatcherSignalEmitter();
		}
		return _instance;
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

}

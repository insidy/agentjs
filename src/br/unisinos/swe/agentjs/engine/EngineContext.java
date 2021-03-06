package br.unisinos.swe.agentjs.engine;


import br.unisinos.swe.agentjs.engine.signals.ISignalsManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class EngineContext {
	public static final String USER_FILE = "UserLocalData";
	private SharedPreferences _preferences = null;
	
	protected static EngineContext m_instance = null;
	
	protected ISignalsManager _signalsManager = null;
	private Context _context;
	private EngineLogger _logger;
	
	
	

	protected EngineContext(Context appContext) {
		_context = appContext;
		_logger = new EngineLogger();
	}

	public final Context getContext() {
		return this._context;
	}
	
	public final ISignalsManager signals() {
		return _signalsManager;
	}

	public static EngineLogger log() {
		return instance()._logger;
	}

	public static EngineContext instance() {
		return m_instance;
	}

	public static EngineContext create(Context appContext) {
		if(m_instance == null) {
			m_instance = new EngineContext(appContext);
		} // shall we do something if instance is already created?
		
		return m_instance;
	}
	
	public static EngineContext setSignalManager(ISignalsManager signalManager) {
		if(m_instance != null) {
			if(m_instance._signalsManager == null) {
				m_instance._signalsManager = signalManager;
			}
		}
		
		return m_instance;
	}

	public String getCloudUrl() {
		return getPreferences().getString("cloud.url", "");
	}

	public SharedPreferences getPreferences() {
		if(_preferences == null) {
			_preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());//getSharedPreferences(USER_FILE, 0); 
		}
		return this._preferences; 
	}

}

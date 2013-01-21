package br.unisinos.swe.agentjs.engine;


import br.unisinos.swe.agentjs.engine.signals.ISignalsManager;

import android.content.Context;

public class EngineContext {
	protected static EngineContext m_instance = null;
	
	protected ISignalsManager _signalsManager;
	private Context _context;
	private EngineLogger _logger;

	protected EngineContext(Context appContext, ISignalsManager signalManager) {
		_context = appContext;
		_signalsManager = signalManager;
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

	public static EngineContext create(Context appContext, ISignalsManager signalManager) {
		if(m_instance != null) {
			m_instance = new EngineContext(appContext, signalManager);
		} // shall we do something if instance is already created?
		
		return m_instance;
	}

}

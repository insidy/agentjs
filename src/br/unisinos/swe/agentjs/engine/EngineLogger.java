package br.unisinos.swe.agentjs.engine;

import android.util.Log;

public class EngineLogger {
	
	private static final String TAG = "AgentJS";
	
	public void error(String message) {
		error(message, new Object[0]);
	}
	
	public void error(String message, Object[] params) {
		Log.e(TAG, String.format(message, params));
	}
	
	public void info(String message) {
		info(message, new Object[0]);
	}
	
	public void info(String message, Object[] params) {
		Log.i(TAG, String.format(message, params));
	}
	
	public static void i(String message) {
		Log.i(TAG, message);
	}
	
	public void debug(String message) {
		debug(message, new Object[0]);
	}
	
	public void debug(String message, Object[] params) {
		Log.d(TAG, String.format(message, params));
	}
	
	public void verbose(String message) {
		verbose(message, new Object[0]);
	}
	
	public void verbose(String message, Object[] params) {
		Log.v(TAG, String.format(message, params));
	}
	
	public void warning(String message) {
		warning(message, new Object[0]);
	}
	
	public void warning(String message, Object[] params) {
		Log.w(TAG, String.format(message, params));
	}
}

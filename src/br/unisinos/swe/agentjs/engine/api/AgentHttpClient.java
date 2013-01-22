package br.unisinos.swe.agentjs.engine.api;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.annotations.JSFunction;

import android.annotation.SuppressLint;
import br.unisinos.swe.agentjs.engine.AgentComponent;
import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.wrappers.HttpWrapper;
import br.unisinos.swe.agentjs.engine.wrappers.IResponseHandler;

@SuppressLint("DefaultLocale")
@AgentComponent(name="http")
public class AgentHttpClient extends AbstractAgentAPIComponent implements IResponseHandler {
	
	HttpWrapper _wrapper;
	
	Object _successCallback;
	Object _errorCallback;
	
	public AgentHttpClient(AgentExecutorHelper helper) {
		helper.register(this);
	}
	
	@JSFunction("get")
	public void get(NativeObject requestParams, Object successFunc, Object errorFunc) {
		
		String url = (String) Context.jsToJava(requestParams.get("url", requestParams), String.class);
		String method = "GET";
		String content = null;
		
		_successCallback = successFunc;
		_errorCallback = errorFunc;
		
		new HttpWrapper(method, url, content, this).execute();
		
	}
	
	@JSFunction("ajax")
	public void ajax(NativeObject requestParams, Object successFunc, Object errorFunc) {
		String url = (String) Context.jsToJava(requestParams.get("url", requestParams), String.class);
		String method = (String) Context.jsToJava(requestParams.get("method", requestParams), String.class);
		String content = null;
		
		if(requestParams.containsKey("data")) {
			content = (String) Context.jsToJava(requestParams.get("data", requestParams), String.class);
		}
		
		_successCallback = successFunc;
		_errorCallback = errorFunc;
		
		new HttpWrapper(method.toUpperCase(), url, content, this).execute();
		
	}
	
	@Override
	public void handleResponse(HttpEntity entity) {
		String content = "";
		try {
			content = EntityUtils.toString(entity);
		} catch (Exception e) {
			_helper.callback(_errorCallback, content);
			return;
		}

		_helper.callback(_successCallback, content);
		
		
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		return false;
	}
}

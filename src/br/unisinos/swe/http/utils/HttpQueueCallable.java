package br.unisinos.swe.http.utils;

import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;

public class HttpQueueCallable implements Callable<HttpEntity> {
	
	protected boolean _persistent = false;
	protected HttpQueueRequest _request;
	protected HttpQueue _parentQueue;
	
	public HttpQueueCallable(HttpQueueRequest request, HttpQueue parent) {
		_request = request;
		_parentQueue = parent;
	}
	
	public void setPersistent(boolean persist) {
		_persistent = persist;
	}

	@Override
	public HttpEntity call() throws Exception {
		// TODO do http call
		
		
		return null;
	}

}

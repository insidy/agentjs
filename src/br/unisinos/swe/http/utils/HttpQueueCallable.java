package br.unisinos.swe.http.utils;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.params.HttpConnectionParams;

import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.EngineUtils;

import android.net.http.AndroidHttpClient;

public class HttpQueueCallable implements Callable<HttpEntity> {
	
	private static final short ENSURE_DELIVERY = 2;
	private static final short ENSURE_CALLBACK = 1;
	private static final short ENSURE_NULL = -1;
	
	protected short _ensure = ENSURE_NULL;
	protected HttpQueueRequest _request;
	protected HttpQueue _parentQueue;
	
	protected int _unknownHostCount = 0;
	
	public HttpQueueCallable(HttpQueueRequest request, HttpQueue parent) {
		_request = request;
		_parentQueue = parent;
	}
	
	public HttpQueueRequest getRequest() {
		return this._request;
	}

	@Override
	public HttpEntity call() throws ConnectivityLostException, Exception {
		if(_parentQueue.isHalted()) {
			_parentQueue.haltAndWaitForNetwork(this);
			throw new ConnectivityLostException("Network unavailable, will retry as soon as possible");
		}
		
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance(_parentQueue.getUserAgent(), _parentQueue.getContext());
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 20000);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 45000);
		
		Exception error = null;
		HttpEntity responseEntity = null;
		
		try {
			HttpResponse httpResponse = httpClient.execute(_request.getHost(), _request.getUriRequest());
			responseEntity = httpResponse.getEntity();
		} catch(Exception e) {
			error = e;
		} finally {
			httpClient.close();
		}
		
		if(error != null) {
			if(error.getClass().equals(UnknownHostException.class) && _unknownHostCount++ < 2) {
				_parentQueue.haltAndWaitForNetwork(this);
				throw new ConnectivityLostException("Network unavailable, will retry as soon as possible");
			} else {
				throw error;
			}
		} else {
			if(_ensure == ENSURE_DELIVERY) {
				// remove from db
			}
			return responseEntity;
		}
	}

	public void ensureDelivery() {
		if(_ensure == ENSURE_NULL) {
			// persist in db
			
			_ensure = ENSURE_DELIVERY;
		}
	}

	public void ensureCallback() {
		if(_ensure == ENSURE_NULL) {
			_ensure = ENSURE_CALLBACK;
		}
		
	}

}

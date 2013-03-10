package br.unisinos.swe.http.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;

import br.unisinos.swe.agentjs.engine.EngineContext;

import com.google.common.util.concurrent.FutureCallback;

import android.net.Uri;

public class HttpQueueRequest {
	protected String _method;
	protected String _url;
	protected String _content;
	protected HashMap<String, String> _headers;
	protected FutureCallback<HttpEntity> _callback;
	
	public HttpQueueRequest(String method, String url, String content, FutureCallback<HttpEntity> callback) {
		_method = method;
		_url = url;
		_content = content;
		_callback = callback;
		_headers = new HashMap<String, String>();
	}
	
	public void setHeader(String key, String value) {
		_headers.put(key, value);
	}
	
	public HashMap<String, String> getHeaders() {
		return _headers;
	}
	
	public HttpRequest getUriRequest() {
		BasicHttpEntityEnclosingRequest request = null;
		try {
		    request = new BasicHttpEntityEnclosingRequest(_method, _url);
	
			if(_content != null) {
				request.setEntity(new StringEntity(_content));
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return request;
	}
	
	public String getContent() {
		return this._content;
	}
	
	public String getMethod() {
		return this._method;
	}
	
	public String getUrl() {
		return this._url;
	}

	public HttpHost getHost() {
		Uri uri = Uri.parse(_url);
		int port = uri.getPort();
		
		return new HttpHost(uri.getHost(), uri.getPort());
	}
	
	public FutureCallback<HttpEntity> getCallback() {
		return this._callback;
	}
}

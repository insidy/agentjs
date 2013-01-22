package br.unisinos.swe.http.utils;

import java.io.UnsupportedEncodingException;

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

import com.google.common.util.concurrent.FutureCallback;

import android.net.Uri;

public class HttpQueueRequest {
	protected String _method;
	protected String _url;
	protected String _content;
	protected FutureCallback<HttpEntity> _callback;
	
	public HttpQueueRequest(String method, String url, String content, FutureCallback<HttpEntity> callback) {
		_method = method;
		_url = url;
		_content = content;
		_callback = callback;
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

	public HttpHost getHost() {
		Uri uri = Uri.parse(_url);
		return new HttpHost(uri.getHost(), uri.getPort());
	}
	
	public FutureCallback<HttpEntity> getCallback() {
		return this._callback;
	}
}

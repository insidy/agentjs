package br.unisinos.swe.http.utils;

import java.io.UnsupportedEncodingException;

import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;

public class HttpQueueRequest {
	protected String _method;
	protected String _url;
	protected String _content;
	
	public HttpQueueRequest(String method, String url, String content) {
		_method = method;
		_url = url;
		_content = content;
	}
	
	public HttpUriRequest getUriRequest() {
		EntityEnclosingRequestWrapper request = null;
		try {
			request = new EntityEnclosingRequestWrapper(new HttpPost(_url));
			request.setMethod(_method);
	
			if(_content != null) {
				request.setEntity(new StringEntity(_content));
			}
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return request;
	}
}

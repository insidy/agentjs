package br.unisinos.swe.agentjs.engine.wrappers;

import org.apache.http.HttpEntity;

public interface IResponseHandler {
	
	public void handleResponse(HttpEntity entity);
}

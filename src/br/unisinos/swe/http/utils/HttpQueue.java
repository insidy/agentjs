package br.unisinos.swe.http.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;

import android.content.Context;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.EngineUtils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class HttpQueue {
	
	
	private static final int DEFAULT_THREADS = 1;

	protected ListeningExecutorService _executorService;
	
	protected LinkedBlockingQueue _executionQueue;
	protected LinkedList<HttpQueueCallable> _networkwaitingQueue;
	
	protected int _threads;
	protected UUID _uuid;
	
	protected boolean _waitingForNetwork = false;


	protected HttpQueue() {
		this(DEFAULT_THREADS);
	}
	
	protected HttpQueue(String strUUID) {
		
	}
	
	protected HttpQueue(int maxthreads) {
		_uuid = UUID.randomUUID();
		_threads = DEFAULT_THREADS;
		_executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		
		_executionQueue = new LinkedBlockingQueue();
		_networkwaitingQueue = new LinkedList<HttpQueueCallable>();
		_executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(_threads, maxthreads, 15, TimeUnit.SECONDS, _executionQueue));

	}
	
	public void fireEnsureDelivery(HttpQueueRequest request) {
		HttpQueueCallable callable = new HttpQueueCallable(request, this);
		callable.ensureDelivery();
		
		if(_waitingForNetwork) {
			_networkwaitingQueue.push(callable);
		} else {
			ListenableFuture<HttpEntity> futureTask = _executorService.submit(callable);
			if(request.getCallback() != null) {
				Futures.addCallback(futureTask, request.getCallback());
			}
		}
	}
	
	public void fireEnsureCallback(HttpQueueRequest request) {
		HttpQueueCallable callable = new HttpQueueCallable(request, this);
		callable.ensureCallback();
		
		if(_waitingForNetwork) {
			_networkwaitingQueue.push(callable);
		} else {
			ListenableFuture<HttpEntity> futureTask = _executorService.submit(callable);
			if(request.getCallback() != null) {
				Futures.addCallback(futureTask, request.getCallback());
			}
		}
	}
	
	public void haltAndWaitForNetwork(HttpQueueCallable failedRequest) {
		_waitingForNetwork = true;
		_executorService.shutdownNow();
		_networkwaitingQueue.push(failedRequest);
	
	
		Object queueItem = null;
		while((queueItem = _executionQueue.poll()) != null) {
			_networkwaitingQueue.push((HttpQueueCallable)queueItem);
		}
		HttpQueueManager.haltQueue(this);
	}

	public void wakeUp() {
		_waitingForNetwork = false;
		HttpQueueCallable callable = null;
		while((callable = _networkwaitingQueue.poll()) != null) {
			
			ListenableFuture<HttpEntity> futureTask = _executorService.submit(callable);
			if(callable.getRequest().getCallback() != null) {
				Futures.addCallback(futureTask, callable.getRequest().getCallback());
			}
		}
	}

	public String getUserAgent() { // User Agent for HTTP call
		return EngineUtils.getUserAgent(EngineContext.instance().getContext());
	}

	public Context getContext() { // Android Context
		return EngineContext.instance().getContext();
	}

}

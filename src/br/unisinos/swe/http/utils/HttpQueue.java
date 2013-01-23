package br.unisinos.swe.http.utils;

import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;

import android.content.Context;
import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.EngineUtils;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class HttpQueue {
	
	
	private static final int DEFAULT_THREADS = 1;

	protected static final String TAG = "HttpQueue";

	protected ListeningExecutorService _executorService;
	
	protected LinkedBlockingQueue<Runnable> _executionQueue;
	protected LinkedList<HttpQueueCallable> _networkwaitingQueue;
	
	protected int _maxthreads;
	protected int _threads;
	protected UUID _uuid;
	
	protected boolean _waitingForNetwork = false;


	protected HttpQueue() {
		this(DEFAULT_THREADS);
	}
	
	protected HttpQueue(int maxthreads) {
		_uuid = UUID.randomUUID();
		_threads = DEFAULT_THREADS;
		_maxthreads = maxthreads;
		//_executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		
		_executionQueue = new LinkedBlockingQueue<Runnable>();
		_networkwaitingQueue = new LinkedList<HttpQueueCallable>();
		startExecutorService();
	}
	
	private void startExecutorService() {
		_executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(_threads, _maxthreads, 15, TimeUnit.SECONDS, _executionQueue));
		if(HttpQueueManager.isConnected()) {
			_waitingForNetwork = false;
		} else {
			waitForNetwork();
		}
		
	}

	public void fireEnsureDelivery(HttpQueueRequest request) {
		HttpQueueCallable callable = new HttpQueueCallable(request, this);
		callable.ensureDelivery();
		
		if(isHalted()) {
			_networkwaitingQueue.addLast(callable);
		} else {
			addCallableToQueue(callable);
		}
	}
	
	public void fireEnsureCallback(HttpQueueRequest request) {
		HttpQueueCallable callable = new HttpQueueCallable(request, this);
		callable.ensureCallback();
		
		if(isHalted()) {
			_networkwaitingQueue.addLast(callable);
		} else {
			addCallableToQueue(callable);
		}
	}
	
	public void haltAndWaitForNetwork(HttpQueueCallable failedRequest) {
		
		if(this.isHalted()) {
			_networkwaitingQueue.addLast(failedRequest); // just add and wait
			
		} else {
			if(!HttpQueueManager.isConnected()) {
				waitForNetwork();
				
				_networkwaitingQueue.addLast(failedRequest);
				
			} else {
				addCallableToQueue(failedRequest);
			}
		}
	}

	private void waitForNetwork() {
		_waitingForNetwork = true;
		HttpQueueManager.haltQueue(this);
	}

	public void wakeUp() {
		startExecutorService();
		HttpQueueCallable callable = null;
		while((callable = _networkwaitingQueue.poll()) != null) {
			
			addCallableToQueue(callable);
			
		}
	}

	private void addCallableToQueue(HttpQueueCallable callable) {
		ListenableFuture<HttpEntity> futureTask = _executorService.submit(callable);
		if(callable.getRequest().getCallback() != null) {
			Futures.addCallback(futureTask, callable.getRequest().getCallback());
		}
	}

	public boolean isHalted() {
		return _waitingForNetwork;
	}

	public String getUserAgent() { // User Agent for HTTP call
		return EngineUtils.getUserAgent(EngineContext.instance().getContext());
	}

	public Context getContext() { // Android Context
		return EngineContext.instance().getContext();
	}

}

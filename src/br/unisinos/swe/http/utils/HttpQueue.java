package br.unisinos.swe.http.utils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class HttpQueue {
	
	
	private static final int DEFAULT_THREADS = 1;

	protected ListeningExecutorService _executorService;
	protected LinkedBlockingQueue _queue;
	protected int _threads;
	protected UUID _uuid;

	
	protected HttpQueue(int maxthreads) {
		_uuid = UUID.randomUUID();
		_threads = DEFAULT_THREADS;
		_executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		
		_queue = new LinkedBlockingQueue();
		_executorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(_threads, maxthreads, 15, TimeUnit.SECONDS, _queue));

	}
	
	public static HttpQueue create() {
		checkUncompletedQueues();
		return new HttpQueue();
	}
	
	public static HttpQueue create(int maxthreads) {
		checkUncompletedQueues();
		return new HttpQueue(maxthreads);
	}
	
	private static void checkUncompletedQueues() {
		// search DB for uncompleted halted queues
	}

	public HttpQueue() {
		this(DEFAULT_THREADS);
	}
	
	public void fireAndForget(HttpQueueRequest request, AbstractHttpQueueCallback callback) {
		HttpQueueCallable callable = new HttpQueueCallable(request, this);
		callable.setPersistent(true);
		
		ListenableFuture<HttpEntity> futureTask = _executorService.submit(callable);
		if(callback != null) {
			Futures.addCallback(futureTask, callback);
		}
	}
	
	public void fireEnsureCallback(HttpQueueRequest request, AbstractHttpQueueCallback callback) {
		HttpQueueCallable callable = new HttpQueueCallable(request, this);
		callable.setPersistent(false);
		
		ListenableFuture<HttpEntity> futureTask = _executorService.submit(callable);
		Futures.addCallback(futureTask, callback);
	}
	
	public void haltAndWaitForNetwork(HttpQueueCallable failedRequest) {
		_executorService.shutdownNow();
		LinkedBlockingQueue<HttpQueueCallable> reprocessList = new LinkedBlockingQueue<HttpQueueCallable>();
		try {
			reprocessList.put(failedRequest);
		
		
			Object queueItem = null;
			while((queueItem = _queue.poll()) != null) {
				 reprocessList.put((HttpQueueCallable)queueItem);
			}
		
		} catch(InterruptedException ex) {
			
		}
		
	}

}

package br.unisinos.swe.agentjs.engine.wrappers;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

import br.unisinos.swe.agentjs.engine.EngineContext;
import br.unisinos.swe.agentjs.engine.EngineUtils;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class HttpWrapper extends AsyncTask<String, Void, HttpEntity> {
	private AndroidHttpClient _httpClient = null;

	private String _method;
	private String _url;
	private String _content = null;
	private IResponseHandler _handler;

	public HttpWrapper(String method, String url, String content, IResponseHandler handler) {
		this.start();

		_method = method;
		_url = url;
		_content = content;
		_handler = handler;

	}

	private void start() {
		start(EngineContext.instance().getContext());
	}

	private final void start(Context paramContext) {
		this._httpClient = AndroidHttpClient.newInstance(
				EngineUtils.getUserAgent(paramContext), paramContext);

		// this._cookieStore = new PersistentCookieStore(paramContext);
		// this._localContext = new SyncBasicHttpContext(new
		// BasicHttpContext());
		// this._localContext.setAttribute("http.cookie-store",
		// this._cookieStore);

		/*
		 * if ("OnXV1Dev".equals(DaemonUtils.getClientName())) {
		 * X509HostnameVerifier localX509HostnameVerifier =
		 * SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER; SchemeRegistry
		 * localSchemeRegistry =
		 * this._httpClient.getConnectionManager().getSchemeRegistry();
		 * SSLSocketFactory localSSLSocketFactory =
		 * SSLSocketFactory.getSocketFactory();
		 * localSSLSocketFactory.setHostnameVerifier
		 * ((X509HostnameVerifier)localX509HostnameVerifier);
		 * localSchemeRegistry.register(new Scheme("https",
		 * localSSLSocketFactory, 443));
		 * HttpsURLConnection.setDefaultHostnameVerifier
		 * (localX509HostnameVerifier); }
		 */
	}
	
	private final HttpEntity ajax(String method, String url, String content, IResponseHandler handler) {

		HttpEntity response = null;
		try {
			EntityEnclosingRequestWrapper request = new EntityEnclosingRequestWrapper(new HttpPost(url));
			request.setMethod(method);
			
			if(content != null) {
				request.setEntity(new StringEntity(content));
			}
			
			
			
			HttpResponse httpResponse = this._httpClient.execute(request);

			response = httpResponse.getEntity();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		
		return response;

	}

	public void stop() {
		/*
		 * DaemonUtils.runInBackgroundThread(new Runnable() { public void run()
		 * { if ((HttpWrapper.this._cookieStore != null) &&
		 * (HttpWrapper.this._clearCookiesOnStop)) {
		 * HttpWrapper.this._cookieStore.clear(); HttpWrapper.this._cookieStore
		 * = null; } HttpWrapper.this._httpClient.close(); } } , 0L);
		 */
		
		this._httpClient.close();
	}

	@Override
	protected HttpEntity doInBackground(String... params) {
		return this.ajax(_method, _url, _content, _handler);
	}
	
	@Override
	protected void onPostExecute(HttpEntity result) {
		this._handler.handleResponse(result);
		this.stop();
	}
}

package br.unisinos.swe.agentjs.engine.wrappers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

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

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

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
	
	private final HttpEntity ajax(String sMethod, String sUrl, String sContent, IResponseHandler handler) {
		HttpEntity response = null;
		
		URL url = null; 
		HttpURLConnection urlConnection = null;
		
		try {
			url = new URL(sUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			
			urlConnection.setRequestMethod(sMethod);
			if(sContent != null) {
				byte[] outData = sContent.getBytes();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Length", Integer.toString(outData.length));
				urlConnection.setUseCaches(false);

			    OutputStream out = urlConnection.getOutputStream();
			    out.write(outData);
			    out.close();
			}
			
			BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
			String strResponse = new String(ByteStreams.toByteArray(in),Charsets.UTF_8);
			in.close();
			
			response = new StringEntity(strResponse);
			
		} catch(Exception e) {
		
		} finally {
			if(urlConnection != null) {
				urlConnection.disconnect();
			}
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

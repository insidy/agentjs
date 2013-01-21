package br.unisinos.swe.agentjs.engine.api;

import org.mozilla.javascript.annotations.JSFunction;
import org.mozilla.javascript.annotations.JSGetter;
import org.mozilla.javascript.annotations.JSSetter;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import br.unisinos.swe.agentjs.R;
import br.unisinos.swe.agentjs.engine.AgentComponent;
import br.unisinos.swe.agentjs.engine.EngineContext;

@AgentComponent(name="notification")
public class AgentNotification {
	
	private String _title;
	private String _content;
	private boolean _vibrate;
	private String _soundUrl;
	
	public AgentNotification() {
	}
	
	public AgentNotification(String title) {
		this();
		
		_title = title;
		_content = "";
		_soundUrl = "";
		_vibrate = false;
	}
	
	@JSGetter("content")
	public String getContent() {
		return this._content;
	}
	
	@JSSetter("content")
	public void setContent(String content) {
		this._content = content;
	}
	
	@JSGetter("vibrate")
	public boolean getVibrate() {
		return this._vibrate;
	}
	
	@JSSetter("vibrate")
	public void setVibrate(boolean vibrate) {
		this._vibrate = vibrate;
	}
	
	@JSGetter("title")
	public String getTitle() {
		return this._title;
	}
	
	@JSSetter("title")
	public void setVibrate(String title) {
		this._title = title;
	}
	
	@JSGetter("soundUrl")
	public String getSoundUrl() {
		return this._soundUrl;
	}
	
	@JSSetter("soundUrl")
	public void setSoundUrl(String soundUrl) {
		this._soundUrl = soundUrl;
	}

	@JSFunction("send")
	public void send() {
		
		Notification notif = new Notification.Builder(EngineContext.instance().getContext())
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(_title)
	    .setContentText(_content)
	    .setDefaults(Notification.DEFAULT_ALL)
	    .setAutoCancel(true)
	    .getNotification();
	    //.setContentIntent(contentIntent);
		
		NotificationManager nManager = (NotificationManager) EngineContext.instance().getContext().getSystemService(Context.NOTIFICATION_SERVICE); 
		nManager.notify(R.drawable.ic_launcher, notif);
	}
}

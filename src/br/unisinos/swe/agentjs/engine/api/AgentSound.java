package br.unisinos.swe.agentjs.engine.api;

import org.mozilla.javascript.annotations.JSFunction;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import br.unisinos.swe.agentjs.engine.AgentComponent;
import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.EngineContext;

@AgentComponent(name="music")
public class AgentSound extends AbstractAgentAPIComponent {
	
	public AgentSound(AgentExecutorHelper helper) {
		helper.register(this);
	}

	@JSFunction("playFromUrl")
	public void playFromUrl(String url) {
		Uri audioUrl = parseToLocalIfNeeded(url);
		AgentApplications apps = _helper.get(AgentApplications.class);
		if (apps != null) {
			apps.launchAppForUrl(audioUrl.toString(), "audio/*");
		}
	}

	@JSFunction("next")
	public final void sendNextCommand() {
		sendCommandToPlayer("next");
	}

	private void sendCommandToPlayer(String command) {
		Intent commandIntent = new Intent("com.android.music.musicservicecommand");
		commandIntent.putExtra("command", command);
		
		EngineContext.log().info("Sending command %s to player", new Object[] { command });
		EngineContext.instance().getContext().sendBroadcast(commandIntent);
	}

	@JSFunction("pause")
	public final void sendPauseCommand() {
		sendCommandToPlayer("pause");
	}

	@JSFunction("play")
	public final void sendPlayCommand() {
		sendCommandToPlayer("play");
	}

	@JSFunction("previous")
	public final void sendPreviousCommand() {
		sendCommandToPlayer("previous");
	}

	@JSFunction("stop")
	public final void sendStopCommand() {
		sendCommandToPlayer("stop");
	}

	public static Uri parseToLocalIfNeeded(String url) {
		Uri parsedUri = Uri.parse(url);
		if(parsedUri.isRelative()) {
			parsedUri = Uri.withAppendedPath(Uri.fromFile(Environment.getExternalStorageDirectory()), url);
		}
		return parsedUri;
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		return false;
	}
}

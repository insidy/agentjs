package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;

public class AgentNetwork extends AbstractAgentAPIComponent {

	public AgentNetwork(AgentExecutorHelper helper) {
		helper.register(this);
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		ArrayList<String> signals = new ArrayList<String>();
		signals.add("wifiOn");
		signals.add("wifiScanResult");
		return signals.contains(signal);
	}
	
}

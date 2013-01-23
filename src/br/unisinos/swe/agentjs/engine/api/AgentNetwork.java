package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter.NetworkSignal;

public class AgentNetwork extends AbstractAgentAPIComponent {

	protected ArrayList<String> _signals = null;
	
	public AgentNetwork(AgentExecutorHelper helper) {
		helper.register(this);
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		if(_signals == null) {
			_signals = new ArrayList<String>();
			for (NetworkSignal signalEnum : NetworkSignal.class.getEnumConstants()) { // All network signals
				_signals.add(signalEnum.toString());
			}
		}
		return _signals.contains(signal);
	}
	
}

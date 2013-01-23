package br.unisinos.swe.agentjs.engine.api;

import java.util.ArrayList;

import br.unisinos.swe.agentjs.engine.AgentExecutorHelper;
import br.unisinos.swe.agentjs.engine.signals.NetworkSignalEmitter.NetworkSignal;

public class AgentNetwork extends AbstractAgentAPIComponent {
	
	public AgentNetwork(AgentExecutorHelper helper) {
		helper.register(this);
	}

	@Override
	protected boolean isOwnSignal(String signal) {
		if(_signals == null) { // signal list is only useful for multi-origin signal API
			_signals = new ArrayList<String>();
			for (NetworkSignal signalEnum : NetworkSignal.class.getEnumConstants()) { // All network signals
				_signals.add(signalEnum.toString());
			}
		}
		
		return _signals.contains(signal);
	}
	
}

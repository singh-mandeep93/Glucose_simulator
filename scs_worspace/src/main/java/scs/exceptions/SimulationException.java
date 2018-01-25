package scs.exceptions;

import scs.gui.MessageText;

public class SimulationException extends Exception {

	private static final long serialVersionUID = 1L;

	public SimulationException(MessageText message) {
		super(message.getText());
	}

	public SimulationException(String message, Throwable cause) {
		super(message, cause);
	}

}

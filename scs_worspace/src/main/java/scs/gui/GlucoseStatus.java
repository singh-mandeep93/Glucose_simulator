package scs.gui;

public enum GlucoseStatus {

	GOOD("Good"), ALERTING("Alerting"), CRITICAL("Critical");

	GlucoseStatus(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	private final String propertyValue;

	public String getValue()
	{
		return propertyValue;
	}
}

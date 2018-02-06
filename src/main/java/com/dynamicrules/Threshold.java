package com.dynamicrules;

import java.io.Serializable;

public class Threshold implements Serializable {

	private static final long serialVersionUID = -6546670542050089336L;

	private long thresholdId;

	private String thresholdName;

	private String thresholdDesc;

	private String thresholdValue;

	public long getThresholdId() {
		return thresholdId;
	}

	public void setThresholdId(long thresholdId) {
		this.thresholdId = thresholdId;
	}

	public String getThresholdName() {
		return thresholdName;
	}

	public void setThresholdName(String thresholdName) {
		this.thresholdName = thresholdName;
	}

	public String getThresholdDesc() {
		return thresholdDesc;
	}

	public void setThresholdDesc(String thresholdDesc) {
		this.thresholdDesc = thresholdDesc;
	}

	public String getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(String thresholdValue) {
		this.thresholdValue = thresholdValue;
	}
}
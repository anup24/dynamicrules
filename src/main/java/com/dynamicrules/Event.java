package com.dynamicrules;

import java.util.Map;

public class Event {

	private Map<String, Object> data;

	public Event() {
	}

	public Event(Map<String, Object> data) {
		this.setData(data);
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}
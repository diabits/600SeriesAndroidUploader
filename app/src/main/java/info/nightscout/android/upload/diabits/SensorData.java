package info.nightscout.android.upload.diabits;

import java.util.HashMap;
import java.util.Map;

class SensorData {
	SensorData(String type) {
		this.type = type;
	}

	private String type;
	private Map<String, Object> values = new HashMap<>();

	void addValue(String name, Object value) {
		values.put(name, value);
	}

	public void getIntValue(String name) {
		Integer.parseInt(String.valueOf(values.get(name)));
	}

	public void getLongValue(String name) {
		Long.parseLong(String.valueOf(values.get(name)));
	}

	public void getStringValue(String name) {
		String.valueOf(values.get(name));
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}
}

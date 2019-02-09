package atsb.eve.dirt.mer;

import java.sql.Timestamp;

/**
 * Simple enum for keeping track of valid types and the associated
 * spec(ification) string in the config file.
 * 
 * @author austin
 */
public enum MappingType {
	LONG("long", Long.class), DOUBLE("double", Double.class), TIMESTAMP(
			"timestamp", Timestamp.class);

	private final String spec;
	private final Class<?> type;

	private MappingType(String spec, Class<?> type) {
		this.spec = spec;
		this.type = type;
	}

	public String spec() {
		return spec;
	}

	public Class<?> type() {
		return type;
	}

	public static MappingType translate(String spec) {
		if (spec.equalsIgnoreCase(LONG.spec)) {
			return LONG;
		} else if (spec.equalsIgnoreCase(DOUBLE.spec)) {
			return DOUBLE;
		} else if (spec.equalsIgnoreCase(TIMESTAMP.spec)) {
			return TIMESTAMP;
		} else {
			return null;
		}
	}
}
package atsb.eve.dirt.mer;

/**
 * Data structure for holding csv -> sql column mapping information.
 * 
 * @author austin
 */
public class FieldMapping {
	private String sqlColumn;
	private String csvHeader;
	private MappingType type;

	public FieldMapping(String sql, String csv, MappingType type) {
		this.sqlColumn = sql;
		this.csvHeader = csv;
		this.type = type;
	}

	public String sqlColumn() {
		return sqlColumn;
	}

	public String csvColumn() {
		return csvHeader;
	}

	public MappingType type() {
		return type;
	}
}
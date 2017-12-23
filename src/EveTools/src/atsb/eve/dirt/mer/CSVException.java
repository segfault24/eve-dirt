package atsb.eve.dirt.mer;

/**
 * Indicates that an invalid column index was provided. Valid column indices
 * are between 0 and getNumColumns(), and their String equivalents.
 * 
 * Indicates that the value in the specified column could not be parsed.
 * This could be because the row entry did not contain enough values or the
 * value was not of the type requested.
 */
public class CSVException extends Exception {
	private static final long serialVersionUID = 1L;

	public CSVException() {
		super();
	}

	public CSVException(String e) {
		super(e);
	}
}
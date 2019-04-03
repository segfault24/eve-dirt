package atsb.eve.dirt.model;

import java.sql.Timestamp;

/**
 * @author austin
 */
public interface OAuthUser {

	/**
	 * @return
	 */
	public int getKeyId();

	/**
	 * @return
	 */
	public int getUserId();

	/**
	 * @return
	 */
	public int getCharId();

	/**
	 * @return
	 */
	public String getCharName();

	/**
	 * @return
	 */
	public String getCharHash();

	/**
	 * @return
	 */
	public String getTokenType();

	/**
	 * @return
	 */
	public Timestamp getTokenExpires();

	/**
	 * @return
	 */
	public String getRefreshToken();

	/**
	 * Get the auth token to pass to API calls.
	 * This function does automatic token refreshes as necessary.
	 * 
	 * @return
	 */
	public String getAuthToken();

	/**
	 * @return
	 */
	public boolean isExpired();

}

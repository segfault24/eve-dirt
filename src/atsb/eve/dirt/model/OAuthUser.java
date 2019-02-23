package atsb.eve.dirt.model;

import java.sql.Timestamp;

/**
 * @author austin
 */
public class OAuthUser {

	private static final int EXPIRES_WITHIN = 60000; // milliseconds

	private int keyId;
	private int userId;
	private int charId;
	private String charName;
	private String charHash;
	private String authToken;
	private String tokenType;
	private Timestamp tokenExpires;
	private String refreshToken;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getCharId() {
		return charId;
	}

	public void setCharId(int charId) {
		this.charId = charId;
	}

	public String getCharName() {
		return charName;
	}

	public void setCharName(String charName) {
		this.charName = charName;
	}

	public String getCharHash() {
		return charHash;
	}

	public void setCharHash(String charHash) {
		this.charHash = charHash;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public Timestamp getTokenExpires() {
		return tokenExpires;
	}

	public void setTokenExpires(Timestamp tokenExpires) {
		this.tokenExpires = tokenExpires;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public boolean tokenExpired() {
		return tokenExpires.before(new Timestamp(System.currentTimeMillis() + EXPIRES_WITHIN));
	}

}

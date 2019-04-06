package atsb.eve.dirt.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import atsb.eve.dirt.DirtAuthException;
import atsb.eve.dirt.model.OAuthUser;
import atsb.eve.dirt.util.Utils;

/**
 * 
 * 
 * @author austin
 */
public class ApiAuthTable {

	private static Logger log = LogManager.getLogger();

	private static final String BYKEYID_SELECT_SQL = "SELECT `charId`,`token`,`expires`,`refresh` FROM dirtApiAuth WHERE `keyId`=?;";
	private static final String BYCHARID_SELECT_SQL = "SELECT `keyId`,`token`,`expires`,`refresh` FROM dirtApiAuth WHERE `charId`=?;";
	private static final String SELECT_ALL_SQL = "SELECT `charId` FROM dirtApiAuth";
	private static final String UPDATE_SQL = "UPDATE dirtApiAuth SET `token`=?, `expires`=?, `refresh`=? WHERE `keyId`=?;";
	private static final String OAUTH_REFRESH_URL = "https://login.eveonline.com/oauth/token";
	private static final int EXPIRES_WITHIN = 60000; // milliseconds

	public static OAuthUser getUserByKeyId(Connection db, int keyId) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(BYKEYID_SELECT_SQL);
		stmt.setInt(1, keyId);
		ResultSet rs = stmt.executeQuery();

		OAuthUserImpl oau = null;
		if (rs.next()) {
			oau = new OAuthUserImpl();
			oau.db = db;
			oau.keyId = keyId;
			oau.charId = rs.getInt("charId");
			oau.authToken = rs.getString("token");
			oau.tokenExpires = rs.getTimestamp("expires");
			oau.refreshToken = rs.getString("refresh");
		}

		Utils.closeQuietly(rs);
		Utils.closeQuietly(stmt);

		return oau;
	}

	public static OAuthUser getUserByCharId(Connection db, int charId) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(BYCHARID_SELECT_SQL);
		stmt.setInt(1, charId);
		ResultSet rs = stmt.executeQuery();

		OAuthUserImpl oau = null;
		if (rs.next()) {
			oau = new OAuthUserImpl();
			oau.db = db;
			oau.keyId = rs.getInt("keyId");
			oau.charId = charId;
			oau.authToken = rs.getString("token");
			oau.tokenExpires = rs.getTimestamp("expires");
			oau.refreshToken = rs.getString("refresh");
		}

		Utils.closeQuietly(rs);
		Utils.closeQuietly(stmt);

		return oau;
	}

	public static List<Integer> getAllCharacters(Connection db) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(SELECT_ALL_SQL);
		ResultSet rs = stmt.executeQuery();

		List<Integer> charIds = new ArrayList<Integer>();
		while (rs.next()) {
			charIds.add(rs.getInt("charId"));
		}

		Utils.closeQuietly(rs);
		Utils.closeQuietly(stmt);

		return charIds;
	}

	private static synchronized void checkExpiredAndRefresh(OAuthUserImpl oau) {
		if (oau.isExpired()) {
			OAuthUserImpl oau2 = null;
			try {
				oau2 = (OAuthUserImpl) getUserByKeyId(oau.db, oau.keyId);
			} catch (SQLException e) {
				log.error("Failed to query ApiAuth table");
				return;
			}

			// if the token in the database doesn't match ours, and isn't expired,
			// some other thread/app must've refreshed it, so just use that
			if (oau2.authToken != oau.authToken && !oau2.isExpired()) {
				oau.authToken = oau2.authToken;
				oau.tokenExpires = oau2.tokenExpires;
				oau.refreshToken = oau2.refreshToken;
				return;
			}

			// otherwise continue with a refresh
			try {
				refresh(oau);
			} catch (IOException | SQLException | DirtAuthException e) {
				log.error("Failed to refresh OAuth token for key=" + oau.getKeyId(), e);
			}
		}
	}

	private static void refresh(OAuthUserImpl oau) throws IOException, DirtAuthException, SQLException {
		log.debug("Performing token refresh for keyId=" + oau.getKeyId());

		URL url = new URL(OAUTH_REFRESH_URL);
		String ssoClientId = Utils.getProperty(oau.db, Utils.PROPERTY_SSO_CLIENT_ID);
		String ssoSecretKey = Utils.getProperty(oau.db, Utils.PROPERTY_SSO_SECRET_KEY);

		String creds = ssoClientId + ":" + ssoSecretKey;
		String auth = "Basic " + new String(Base64.getEncoder().encode(creds.getBytes()));
		String data = "grant_type=refresh_token&refresh_token=" + oau.getRefreshToken();

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", auth);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Length", String.valueOf(data.length()));
		con.setRequestProperty("Host", "login.eveonline.com");
		con.setDoOutput(true);

		OutputStream os = con.getOutputStream();
		os.write(data.getBytes());
		os.close();

		InputStream is;
		if (con.getResponseCode() == 200) {
			is = con.getInputStream();
		} else {
			throw new DirtAuthException(
					"Failed to do refresh (" + con.getResponseCode() + " " + con.getResponseMessage() + ")");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String rsp = "";
		String s;
		while ((s = br.readLine()) != null) {
			rsp += s;
		}
		Utils.closeQuietly(br);
		Utils.closeQuietly(is);

		RefreshData rd = new Gson().fromJson(rsp, RefreshData.class);
		oau.authToken = rd.access_token;
		oau.tokenType = rd.token_type;
		oau.refreshToken = rd.refresh_token;
		oau.tokenExpires = new Timestamp(System.currentTimeMillis() + Long.valueOf(rd.expires_in) * 1000);

		PreparedStatement stmt = oau.db.prepareStatement(UPDATE_SQL);
		stmt.setString(1, oau.getAuthToken());
		stmt.setTimestamp(2, oau.getTokenExpires());
		stmt.setString(3, oau.getRefreshToken());
		stmt.setInt(4, oau.getKeyId());
		stmt.execute();
		Utils.closeQuietly(stmt);
	}

	private class RefreshData {
		public String access_token;
		public String token_type;
		public String expires_in;
		public String refresh_token;
	}

	private static class OAuthUserImpl implements OAuthUser {

		private Connection db;

		private int keyId;
		private int userId;
		private int charId;
		private String charName;
		private String charHash;
		private String authToken;
		private String tokenType;
		private Timestamp tokenExpires;
		private String refreshToken;

		@Override
		public int getKeyId() {
			return keyId;
		}

		@Override
		public int getUserId() {
			return userId;
		}

		@Override
		public int getCharId() {
			return charId;
		}

		@Override
		public String getCharName() {
			return charName;
		}

		@Override
		public String getCharHash() {
			return charHash;
		}

		@Override
		public String getTokenType() {
			return tokenType;
		}

		@Override
		public Timestamp getTokenExpires() {
			return tokenExpires;
		}

		@Override
		public String getRefreshToken() {
			return refreshToken;
		}

		@Override
		public String getAuthToken() {
			checkExpiredAndRefresh(this);
			return authToken;
		}

		@Override
		public boolean isExpired() {
			return tokenExpires.before(new Timestamp(System.currentTimeMillis() + EXPIRES_WITHIN));
		}

	}

}

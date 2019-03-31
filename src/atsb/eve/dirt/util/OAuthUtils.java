package atsb.eve.dirt.util;

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
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import atsb.eve.dirt.DirtAuthException;
import atsb.eve.dirt.model.OAuthUser;

public class OAuthUtils {

	private static Logger log = LogManager.getLogger();

	private static final String SELECT_SQL = "SELECT `token`,`expires`,`refresh` FROM dirtApiAuth WHERE `keyId`=?;";
	private static final String UPDATE_SQL = "UPDATE dirtApiAuth SET `token`=?, `expires`=?, `refresh`=? WHERE `keyId`=?;";
	private static final String OAUTH_REFRESH_URL = "https://login.eveonline.com/oauth/token";

	public static OAuthUser loadFromSql(Connection db, int keyId) throws SQLException {
		PreparedStatement stmt = db.prepareStatement(SELECT_SQL);
		stmt.setInt(1, keyId);
		ResultSet rs = stmt.executeQuery();

		OAuthUser oau = null;
		if (rs.next()) {
			oau = new OAuthUser();
			oau.setKeyId(keyId);
			oau.setAuthToken(rs.getString("token"));
			oau.setTokenExpires(rs.getTimestamp("expires", Utils.getGMTCal()));
			oau.setRefreshToken(rs.getString("refresh"));
		}

		Utils.closeQuietly(rs);
		Utils.closeQuietly(stmt);

		return oau;
	}

	public static String getAuthToken(Connection db, OAuthUser oau) {
		if (oau.tokenExpired()) {
			try {
				refresh(db, oau);
			} catch (IOException | SQLException | DirtAuthException e) {
				log.error("Failed to refresh OAuth token for key=" + oau.getKeyId(), e);
			}
		}
		return oau.getAuthToken();
	}

	private static void refresh(Connection db, OAuthUser oau) throws IOException, DirtAuthException, SQLException {
		log.debug("Performing token refresh for keyId=" + oau.getKeyId());

		URL url = new URL(OAUTH_REFRESH_URL);
		String ssoClientId = Utils.getProperty(db, Utils.PROPERTY_SSO_CLIENT_ID);
		String ssoSecretKey = Utils.getProperty(db, Utils.PROPERTY_SSO_SECRET_KEY);

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
		oau.setAuthToken(rd.access_token);
		oau.setTokenType(rd.token_type);
		oau.setRefreshToken(rd.refresh_token);
		oau.setTokenExpires(new Timestamp(System.currentTimeMillis() + Long.valueOf(rd.expires_in) * 1000));

		PreparedStatement stmt = db.prepareStatement(UPDATE_SQL);
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

}

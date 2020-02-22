package atsb.eve.dirt.esi.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import atsb.eve.db.ApiAuthTable;
import atsb.eve.dirt.DirtConstants;
import atsb.eve.dirt.Stats;
import atsb.eve.model.OAuthUser;
import atsb.eve.util.Utils;

public class OAuthUtil {

	private static Logger log = LogManager.getLogger();

	private static final String OAUTH_REFRESH_URL = "https://login.eveonline.com/oauth/token";

	public static String getAuthToken(Connection db, OAuthUser oau) {
		checkExpiredAndRefresh(db, oau);
		return oau.getAuthToken();
	}

	private static synchronized void checkExpiredAndRefresh(Connection db, OAuthUser oau) {
		if (oau.isExpired()) {
			OAuthUser oau2 = null;
			try {
				oau2 = ApiAuthTable.getUserByKeyId(db, oau.getKeyId());
			} catch (SQLException e) {
				log.error("Failed to query ApiAuth table");
				return;
			}

			// if the token in the database doesn't match ours, and isn't expired,
			// some other thread/app must've refreshed it, so just use that
			if (oau2.getAuthToken() != oau.getAuthToken() && !oau2.isExpired()) {
				oau.setAuthToken(oau2.getAuthToken());
				oau.setTokenExpires(oau2.getTokenExpires());
				oau.setRefreshToken(oau2.getRefreshToken());
				return;
			}

			// otherwise continue with a refresh
			try {
				refresh(db, oau);
			} catch (IOException | SQLException | DirtAuthException e) {
				Stats.ssoErrors++;
				log.error("Failed to refresh OAuth token for key=" + oau.getKeyId(), e);
			}
		}
	}

	private static void refresh(Connection db, OAuthUser oau) throws IOException, DirtAuthException, SQLException {
		log.debug("Performing token refresh for keyId=" + oau.getKeyId());

		URL url = new URL(OAUTH_REFRESH_URL);
		String ssoClientId = Utils.getProperty(db, DirtConstants.PROPERTY_SSO_CLIENT_ID);
		String ssoSecretKey = Utils.getProperty(db, DirtConstants.PROPERTY_SSO_SECRET_KEY);

		String creds = ssoClientId + ":" + ssoSecretKey;
		String auth = "Basic " + new String(Base64.getEncoder().encode(creds.getBytes()));
		String data = "grant_type=refresh_token&refresh_token=" + oau.getRefreshToken();

		Stats.ssoCalls++;
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

		ApiAuthTable.update(db, oau);
	}

	private class RefreshData {
		public String access_token;
		public String token_type;
		public String expires_in;
		public String refresh_token;
	}

}

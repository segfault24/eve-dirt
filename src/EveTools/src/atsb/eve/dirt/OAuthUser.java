package atsb.eve.dirt;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;

import atsb.eve.dirt.util.DirtProperties;
import atsb.eve.dirt.util.Utils;

import com.google.gson.Gson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 
 * 
 * @author austin
 */
public class OAuthUser {

	private static final int EXPIRES_WITHIN = 5000; // milliseconds

	private DirtProperties config;

	private int keyId = -1;

	private String authToken;
	private String tokenType;
	private Timestamp tokenExpires;
	private String refreshToken;

	private OAuthUser(DirtProperties cfg) {
		this.config = cfg;
	}

	public static OAuthUser getApiAuth(DirtProperties cfg, int keyId) throws Exception {
		OAuthUser oau = new OAuthUser(cfg);
		oau.loadFromSql(keyId);
		return oau;
	}

	public String getAuthToken() throws SQLException, DirtAuthException, IOException {
		if (checkExpired()) {
			doRefresh();
			updateSql();
		}
		return authToken;
	}

	private void loadFromSql(int keyId) throws SQLException, DirtAuthException {
		String SELECT_SQL = "SELECT `token`,`expires`,`refresh` FROM dirtApiAuth WHERE `keyId`=?;";

		Connection con = DriverManager.getConnection(
				config.getDbConnectionString(), config.getDbUser(),
				config.getDbPass());
		PreparedStatement stmt = con.prepareStatement(SELECT_SQL);
		stmt.setInt(1, keyId);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			this.keyId = keyId;
			this.authToken = rs.getString("token");
			this.tokenExpires = rs.getTimestamp("expires");
			this.refreshToken = rs.getString("refresh");
		} else {
			this.keyId = -1;
		}

		Utils.closeQuietly(rs);
		Utils.closeQuietly(stmt);
		Utils.closeQuietly(con);

		if (this.keyId == -1) {
			throw new DirtAuthException("key not found");
		}
	}

	private boolean checkExpired() {
		return tokenExpires.before(new Timestamp(System.currentTimeMillis()
				+ EXPIRES_WITHIN));
	}

	private void doRefresh() throws DirtAuthException, IOException {

		URL url = new URL("https://login.eveonline.com/oauth/token");
		String creds = config.getSSOClientId() + ":" + config.getSSOSecretKey();
		String auth = "Basic "
				+ new String(Base64.getEncoder().encode(creds.getBytes()));
		String data = "grant_type=refresh_token&refresh_token=" + refreshToken;

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", auth);
		con.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
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
			throw new DirtAuthException("Failed to do refresh ("
					+ con.getResponseCode() + " " + con.getResponseMessage()
					+ ")");
		}

		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		String rsp = "";
		String s;
		while ((s = dis.readLine()) != null) {
			rsp += s;
		}

		Gson gson = new Gson();
		RefreshData rd = gson.fromJson(rsp, RefreshData.class);

		this.authToken = rd.access_token;
		this.tokenType = rd.token_type;
		this.refreshToken = rd.refresh_token;
		this.tokenExpires = new Timestamp(System.currentTimeMillis()
				+ Long.valueOf(rd.expires_in) * 1000);
	}

	private void updateSql() throws SQLException {
		String UPDATE_SQL = "UPDATE dirtApiAuth SET `token`=?, `expires`=?, `refresh`=? WHERE `keyId`=?;";

		Connection con = DriverManager.getConnection(
				config.getDbConnectionString(), config.getDbUser(),
				config.getDbPass());
		PreparedStatement stmt = con.prepareStatement(UPDATE_SQL);
		stmt.setString(1, authToken);
		stmt.setTimestamp(2, tokenExpires);
		stmt.setString(3, refreshToken);
		stmt.setInt(4, keyId);
		stmt.execute();

		Utils.closeQuietly(stmt);
		Utils.closeQuietly(con);
	}

	private class RefreshData {
		public String access_token;
		public String token_type;
		public String expires_in;
		public String refresh_token;
	}

}

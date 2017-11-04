package atsb.eve.dirt;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;

import com.google.gson.Gson;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author austin
 */
public class OAuthUser {

	private static final int EXPIRES_WITHIN = 5000; // milliseconds

	private int keyId = -1;

	private String authToken;
	private Timestamp tokenExpires;
	private String refreshToken;

	private OAuthUser() {
	}

	public static OAuthUser getApiAuth(int keyId) throws Exception {
		OAuthUser oau = new OAuthUser();
		oau.loadFromSql(keyId);
		return oau;
	}

	public String getAuthToken() throws Exception {
		if (checkExpired()) {
			doRefresh();
			updateSql();
		}
		return authToken;
	}

	private void loadFromSql(int keyId) throws Exception {
		String SELECT_SQL = "SELECT `token`,`expires`,`refresh` FROM dirtApiAuth WHERE `keyId`=?;";

		Config cfg = Config.getInstance();
		Connection con = DriverManager.getConnection(cfg.getDbConnectionString(), cfg.getDbUser(), cfg.getDbPass());
		PreparedStatement stmt = con.prepareStatement(SELECT_SQL);
		stmt.setInt(1, keyId);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			authToken = rs.getString("token");
			tokenExpires = rs.getTimestamp("expires");
			refreshToken = rs.getString("refresh");
			this.keyId = keyId;
		} else {
			rs.close();
			stmt.close();
			con.close();
			throw new Exception("key not found");
		}

		rs.close();
		stmt.close();
		con.close();
	}

	private boolean checkExpired() {
		return tokenExpires.before(new Timestamp(System.currentTimeMillis() + EXPIRES_WITHIN));
	}

	private void doRefresh() throws Exception {

		Config cfg = Config.getInstance();

		URL url = new URL("https://login.eveonline.com/oauth/token");
		String creds = cfg.getSSOClientId() + ":" + cfg.getSSOSecretKey();
		String auth = "Basic " + new String(Base64.getEncoder().encode(creds.getBytes()));
		String data = "grant_type=refresh_token&refresh_token=" + refreshToken;

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
			throw new Exception(
					"Failed to do refresh (" + con.getResponseCode() + " " + con.getResponseMessage() + ")");
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
		this.refreshToken = rd.refresh_token;
		this.tokenExpires = new Timestamp(System.currentTimeMillis() + Long.valueOf(rd.expires_in)*1000);
	}

	private void updateSql() throws SQLException {
		String UPDATE_SQL = "UPDATE dirtApiAuth SET `token`=?, `expires`=?, `refresh`=? WHERE `keyId`=?;";

		Config cfg = Config.getInstance();
		Connection con = DriverManager.getConnection(cfg.getDbConnectionString(), cfg.getDbUser(), cfg.getDbPass());
		PreparedStatement stmt = con.prepareStatement(UPDATE_SQL);
		stmt.setString(1, authToken);
		stmt.setTimestamp(2, tokenExpires);
		stmt.setString(3, refreshToken);
		stmt.setInt(4, keyId);
		stmt.execute();

		stmt.close();
		con.close();
	}

	private class RefreshData {
		public String access_token;
		public String token_type;
		public String expires_in;
		public String refresh_token;
	}

	public static void main(String[] args) {
		try {
			OAuthUser o = OAuthUser.getApiAuth(1);
			System.out.println(o.authToken);
			System.out.println(o.refreshToken);
			System.out.println(o.tokenExpires);
			o.getAuthToken();
			System.out.println(o.authToken);
			System.out.println(o.refreshToken);
			System.out.println(o.tokenExpires);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

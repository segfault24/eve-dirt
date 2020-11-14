package atsb.eve.dirt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

import atsb.eve.util.DbInfo;
import atsb.eve.util.DbPool;

public class PriceCompare {

	private boolean debug = true;
	private DecimalFormat fmt = new DecimalFormat("#,###");

	private ArrayList<Route> routes = new ArrayList<Route>();
	private ArrayList<LineItem> items = new ArrayList<LineItem>();
	private long destLocationId;
	private String destLocationName;

	private String sql;

	private Connection db;

	public static void main(String[] args) {
		try {
			PriceCompare pc = new PriceCompare();
			pc.parseInputFile(new File("C:\\Users\\austin\\Desktop\\input.txt"));
			pc.run();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			DbPool pool = new DbPool(new DbInfo());
			db = pool.acquire();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		resolveIds();
		printInputs();
		buildMegaSql();
		try {
			runQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		calcResults();
		printResults();
	}

	private void parseInputFile(File f) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(f));
		try {
			while (true) {
				// grab a line, clean it
				String tmp = br.readLine();
				if (tmp == null) {
					break;
				}
				tmp = tmp.trim();

				// ignore comments
				if (tmp.startsWith("#")) {
					continue;
				}

				// split on whitespace
				String[] line = tmp.split("\\s+");
				if (line.length < 2) {
					continue;
				}

				if (line[0].equalsIgnoreCase("route")) {
					Route r = new Route();
					r.startLocationId = Long.parseLong(line[1]);
					r.endLocationId = Long.parseLong(line[2]);
					r.freightRate = Long.parseLong(line[3]);
					r.collatRate = Double.parseDouble(line[4]);
					routes.add(r);
					debug("found route: " + r.startLocationId + ">" +r.endLocationId + "@" + r.freightRate + "+" + r.collatRate + "%");
				} else if (line[0].equalsIgnoreCase("destination")) {
					destLocationId = Long.parseLong(line[1]);
					debug("found dest: " + destLocationId);
				} else {
					String[] line2 = tmp.split("\\s+(?=\\S*+$)");
					LineItem i = new LineItem();
					i.typeName = line2[0];
					i.quantity = Long.parseLong(line2[1]);
					items.add(i);
					debug("found item: " + i.quantity + " "+ i.typeName);
				}
			}
			debug("");
			debug("");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// clear out non-applicable routes
		// swap start/end if necessary
		ArrayList<Route> routes2 = new ArrayList<Route>();
		for (Route r : routes) {
			if (r.startLocationId == destLocationId) {
				// swap and add
				long tmp = r.startLocationId;
				r.startLocationId = r.endLocationId;
				r.startLocationId = tmp;
				routes2.add(r);
			} else if (r.endLocationId == destLocationId) {
				// add ad-is
				routes2.add(r);
			}
		}
		routes = routes2;

		// consolidate duplicate line items
		for (int i = 0; i < items.size(); i++) {
			for (int j = i + 1; j < items.size(); j++) {
				if (items.get(i).quantity > 0 && items.get(i).typeName.equalsIgnoreCase(items.get(j).typeName)) {
					items.get(i).quantity += items.get(j).quantity;
					items.get(j).quantity = 0;
				}
			}
		}
		ArrayList<LineItem> items2 = new ArrayList<LineItem>();
		for (LineItem i : items) {
			if (i.quantity > 0) {
				items2.add(i);
			}
		}
		items = items2;
		items.sort(new Comparator<LineItem>() {
			@Override
			public int compare(LineItem o1, LineItem o2) {
				return o1.typeName.compareTo(o2.typeName);
			}
		});
	}

	private void printInputs() {
		System.out.println("Destination: " + destLocationName + " (" + destLocationId + ")");
		System.out.println("Routes" + "(" + routes.size() + "):");
		for (Route r : routes) {
			System.out.println("    " + r.startLocationName + " (" + r.startLocationId + ") @ " + r.freightRate + " isk/m3 + " + r.collatRate * 100 + "% collateral");
		}
		System.out.println("Items" + "(" + items.size() + "):");
		for (LineItem i : items) {
			System.out.println("    " + i.typeName + "   id:" + i.typeId + " vol:" + i.volume + " qt:" + i.quantity);
		}
		System.out.println("");
	}

	private void printResults() {
		for (int j = 0; j < routes.size(); j++) {
			Route r = routes.get(j);
			double extPrice = 0;
			double frieght = 0;
			for (LineItem i : r.results) {
				extPrice += i.prices.get(j) * i.quantity;
				frieght += i.volume * i.quantity * r.freightRate + extPrice * r.collatRate;
			}
			System.out.println("############");
			System.out.println("# " + r.startLocationName + " (" + r.startLocationId + ")");
			System.out.println("#   extPrice: " + fmt.format(extPrice));
			System.out.println("#    frieght: " + fmt.format(frieght));
			System.out.println("############");
			for (LineItem i : r.results) {
				System.out.println(i.typeName + "\t" + i.quantity);
			}
			System.out.println("");
		}
	}

	private void resolveIds() {
		// check that locations exist and get name
		String locationSql = "SELECT structName AS name FROM structure WHERE structId=?";
		locationSql += " UNION SELECT stationName AS name FROM station WHERE stationId=?";
		try {
			PreparedStatement stmt = db.prepareStatement(locationSql);
			stmt.setLong(1, destLocationId);
			stmt.setLong(2, destLocationId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				destLocationName = rs.getString(1);
			} else {
				System.err.println("Failed to find locationId: " + destLocationId);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (Route r : routes) {
			try {
				PreparedStatement stmt = db.prepareStatement(locationSql);
				stmt.setLong(1, r.startLocationId);
				stmt.setLong(2, r.startLocationId);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					r.startLocationName = rs.getString(1);
				} else {
					System.err.println("Failed to find locationId: " + r.startLocationId);
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// check that items exist and get ids
		String typeSql = "SELECT typeId, volume FROM invType WHERE typeName=?";
		for (LineItem i : items) {
			try {
				PreparedStatement stmt = db.prepareStatement(typeSql);
				stmt.setString(1, i.typeName);
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					i.typeId = rs.getLong(1);
					i.volume = rs.getDouble(2);
				} else {
					System.err.println("Failed to find typeName: " + i.typeName);
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// filter unknowns
		ArrayList<LineItem> items2 = new ArrayList<LineItem>();
		for (LineItem i : items) {
			if (i.typeId != 0) {
				items2.add(i);
			}
		}
		items = items2;
	}

	private void buildMegaSql() {
		String sql = "SELECT i.typeId";
		for (int i = 0; i < routes.size(); i++) {
			sql += ", a" + i + ".best AS a" + i;
		}
		sql += " FROM invType AS i";
		for (int i = 0; i < routes.size(); i++) {
			sql += " LEFT JOIN (";
			sql += " SELECT typeId, MIN(price) AS best FROM marketorder";
			sql += " WHERE locationId=" + routes.get(i).startLocationId + " AND isBuyOrder=0";
			sql += " GROUP BY typeId, locationId";
			sql += " ) AS a" + i + " ON a" + i + ".typeId=i.typeId";
		}
		sql += " WHERE i.typeId IN ( " + items.get(0).typeId;
		for (int i = 1; i < items.size(); i++) {
			sql += ", " + items.get(i).typeId;
		}
		sql += " )";
		this.sql = sql;
		debug(sql);
	}

	private void runQuery() throws SQLException {
		PreparedStatement stmt = db.prepareStatement(sql);
		ResultSet rs = stmt.executeQuery();

		while (rs.next()) {
			long t = rs.getLong(1);
			// find the type
			for (LineItem i : items) {
				if (t == i.typeId) {
					// add the prices
					for (int j = 0; j < routes.size(); j++) {
						i.prices.add(rs.getDouble(2 + j));
					}
				}
			}
		}

		rs.close();
		stmt.close();
	}

	private void calcResults() {
		for (LineItem i : items) {
			double bestPrice = Double.MAX_VALUE;
			long lid = 0;
			for (int j = 0; j < routes.size(); j++) {
				Route r = routes.get(j);
				double price = i.prices.get(j);
				double extPrice = price * i.quantity;
				double frieght = i.volume * i.quantity * r.freightRate + extPrice * r.collatRate;
				double totalPrice = extPrice + frieght;
				debug(i.typeName + "\t" + r.startLocationName + "\t" + frieght + "\t" + totalPrice);
				if (price != 0 && totalPrice < bestPrice) {
					bestPrice = totalPrice;
					lid = r.startLocationId;
				}
			}
			for (Route r : routes) {
				if (r.startLocationId == lid) {
					debug(i.typeName + "\t" + r.startLocationName);
					r.results.add(i);
				}
			}
		}
		debug("");
	}

	private void debug(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	private static class Route {
		public long startLocationId;
		public String startLocationName;
		public long endLocationId;
		public long freightRate; // isk/m3
		public double collatRate; // decimal percent
		public ArrayList<LineItem> results = new ArrayList<LineItem>();
	}

	private static class LineItem {
		public String typeName;
		public long typeId;
		public long quantity;
		public double volume;
		public ArrayList<Double> prices = new ArrayList<Double>();
	}

}

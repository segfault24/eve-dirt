package atsb.eve.dirt;

public class Stats {

	public static long started = System.currentTimeMillis();
	public static long esiCalls = 0;
	public static long esiErrors = 0;
	public static long ssoCalls = 0;
	public static long ssoErrors = 0;

	public static String uptime() {
		long now = System.currentTimeMillis();
		long diff = (now - Stats.started) / 1000; // secs
		long days = diff / (60 * 60 * 24);
		long hours = (diff % (60 * 60 * 24)) / (60 * 60);
		long minutes = (diff % (60 * 60)) / ( 60);
		long seconds = (diff % 60);
		return days + "d" + hours + "h" + minutes + "m" + seconds + "s";
	}

	public static void reset() {
		esiCalls = 0;
		esiErrors = 0;
		ssoCalls = 0;
		ssoErrors = 0;
	}

}

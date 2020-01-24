package atsb.eve.dirt;

public class Stats {

	public static long esiCalls = 0;
	public static long esiErrors = 0;
	public static long ssoCalls = 0;
	public static long ssoErrors = 0;

	public static void reset() {
		esiCalls = 0;
		esiErrors = 0;
		ssoCalls = 0;
		ssoErrors = 0;
	}

}

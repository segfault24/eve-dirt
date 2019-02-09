package atsb.eve.dirt.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Pinger {

	private static final int TIMEOUT = 5000;
	
	public static void main(String[] args) {
		pingsock("www.eveonline.com", 443);
		pingsock("forums.eveonline.com", 443);
		pingsock("secure.eveonline.com", 443);
		pingsock("support.eveonline.com", 443);
		pingsock("community.eveonline.com", 443);
		pingsock("updates.eveonline.com", 443);
		pingsock("developers.eveonline.com", 443);
		pingsock("launcher.eveonline.com", 443);
		pingsock("binaries.eveonline.com", 443);
		pingsock("resources.eveonline.com", 443);
		pingsock("imageserver.eveonline.com", 443);
		pingsock("webimg.ccpgamescdn.com", 443);
		pingsock("tranquility.chat.eveonline.com",5222);
		pingsock("engine.extccp.com", 443);
		pingsock("cdn1.eveonline.com", 443);
		pingsock("esi.evetech.net", 443);
	}

	private static void pingsock(String addr, int port) {
		try {
			InetSocketAddress saddr = new InetSocketAddress(addr, port);
			System.out.print(addr + " : " + saddr.getAddress().getHostAddress() + " : ");
			Socket s = new Socket();
			s.connect(saddr, TIMEOUT);
			if (s.isConnected()) {
				System.out.println("up");
			} else {
				System.out.println("down");
			}
			s.close();
		} catch (IOException e) {
			System.out.println("error");
		}
	}
	
}

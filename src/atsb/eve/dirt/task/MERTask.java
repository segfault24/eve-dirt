package atsb.eve.dirt.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import atsb.eve.dirt.mer.MERLoader;
import atsb.eve.dirt.task.DirtTask;
import atsb.eve.util.Utils;

public class MERTask extends DirtTask {

	private static Logger log = LogManager.getLogger();

	private boolean manual = false;
	private int year;
	private int month;

	public MERTask() {
	}

	public MERTask(int year, int month) {
		this.manual = true;
		this.year = year;
		this.month = month;
	}

	@Override
	public String getTaskName() {
		return "mer";
	}

	@Override
	protected void runTask() {
		LocalDate d;
		
		if (manual) {
			d = LocalDate.now().withYear(year).withMonth(month);
		} else {
			d = LocalDate.now().minusDays(35);
			String merLastSuccess = Utils.getKV(getDb(), "mer-last");
			String targetMer = Integer.toString(d.getYear() * 100 + d.getMonthValue());
			if (merLastSuccess != null && targetMer.equalsIgnoreCase(merLastSuccess)) {
				// quit if we already got this one
				log.debug("Skipped execution, already got this MER");
				return;
			}
		}

		// try to get the zip
		List<String> urls = genUrls(d);
		File zip = getZip(urls);
		if (zip == null) {
			return;
		}

		// unzip and parse
		List<File> files = unzip(zip);
		for (File f : files) {
			parse(d, f);
		}

		// clean up
		zip.delete();
		for (File f : files) {
			f.delete();
		}

		// Set last retrieved mer
		String targetMer = Integer.toString(d.getYear() * 100 + d.getMonthValue());
		Utils.putKV(getDb(), "mer-last", targetMer);

	}

	private List<String> genUrls(LocalDate d) {
		List<String> urls = new ArrayList<String>();

		DateTimeFormatter MMMyyyy = DateTimeFormatter.ofPattern("MMMyyyy");
		urls.add("https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_" + d.format(MMMyyyy) + "b.zip");
		urls.add("https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_" + d.format(MMMyyyy) + ".zip");
		urls.add("http://web.ccpgamescdn.com/newssystem/media/73619/1/EVEOnline_MER_" + d.format(MMMyyyy) + ".zip");
		return urls;
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Jan2020.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Dec2019b.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Nov2019.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_May2019.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Apr2019.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Mar2019.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Feb2019.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Jan2019.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Dec2018.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Nov2018.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Oct2018.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Sep2018.zip
		// https://web.ccpgamescdn.com/aws/community/EVEOnline_MER_Aug2018.zip
		// http://web.ccpgamescdn.com/newssystem/media/73619/1/EVEOnline_MER_Jul2018.zip
		// http://web.ccpgamescdn.com/newssystem/media/73619/1/EVEOnline_MER_May2018.zip
		// http://content.eveonline.com/www/newssystem/media/73592/1/EVEOnline_MER_Apr2018.zip
		// http://web.ccpgamescdn.com/newssystem/media/73619/1/EVEOnline_MER_Mar2018.zip
		// http://web.ccpgamescdn.com/newssystem/media/73619/1/EVEOnline_MER_Feb2018.zip
		// http://web.ccpgamescdn.com/newssystem/media/73619/1/EVEOnline_MER_Jan2018.zip
		// https://content.eveonline.com/www/newssystem/media/73589/1/EVEOnline_MER_Dec2017.zip
		// https://content.eveonline.com/www/newssystem/media/73542/1/EVEOnline_MER_Nov2017.zip
		// https://content.eveonline.com/www/newssystem/media/73479/1/EVEOnline_MER_Oct2017.zip
		// https://cdn1.eveonline.com/community/MER/EVEOnline_MER_Sep2017.zip
		// https://cdn1.eveonline.com/community/MER/EVEOnline_MER_Aug17.zip
		// https://cdn1.eveonline.com/community/MER/EVEOnline_MER_Jul2017.zip
	}

	private File getZip(List<String> urls) {
		for (String url : urls) {
			try {
				URL u = new URL(url);
				HttpURLConnection http = (HttpURLConnection) u.openConnection();
				if (http.getResponseCode() != HttpURLConnection.HTTP_OK) {
					log.debug("Received " + http.getResponseCode() + " for " + url);
					continue;
				}

				String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
				InputStream is = http.getInputStream();
				File f = createTempFile(filename);
				f.delete();
				FileOutputStream fos = new FileOutputStream(f);
				log.debug("Attempting to download " + filename + " to " + f.getPath());

				int bytesRead = -1;
				byte[] buffer = new byte[4096];
				while ((bytesRead = is.read(buffer)) != -1) {
					fos.write(buffer, 0, bytesRead);
				}

				fos.close();
				is.close();

				return f;
			} catch (IOException e) {
				log.warn("Failed to download file from " + url + ": " + e.getLocalizedMessage());
				log.debug(e);
			}
		}
		return null;
	}

	private List<File> unzip(File zip) {
		List<File> files = new ArrayList<File>();
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				File f = createTempFile(entry.getName());
				log.debug("Attempting to extract " + entry.getName() + " to " + f.getPath());
				FileOutputStream fos = new FileOutputStream(f);
				int len;
				byte[] buf = new byte[4096];
				while ((len = zis.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
				fos.close();
				files.add(f);
			}
			zis.close();
		} catch (IOException e) {
			log.warn("Failed to extract archive: " + e.getLocalizedMessage());
			log.debug(e);
		}
		return files;
	}

	private void parse(LocalDate d, File f) {
		String fname = f.getName().toLowerCase();
		String cfg = "";
		if (!fname.contains("csv")) {
			// skip non-csv files
			return;
		} else if (fname.contains("regionalstats")) {
			cfg = "cfg/mer/regstat.config";
		} else if (fname.contains("produceddestroyedmined")) {
			cfg = "cfg/mer/pdm.config";
		} else if (fname.contains("iskvolume")) {
			cfg = "cfg/mer/iskvolume.config";
		} else if (fname.contains("moneysupply")) {
			cfg = "cfg/mer/moneysupply.config";
		} else if (fname.contains("sinksfaucets.csv")) {
			cfg = "cfg/mer/sinksfaucets.config";
		} else if (fname.contains("topsinksfaucets")) {
			cfg = "cfg/mer/topsinksfaucets.config";
		} else {
			// unrecognized
			log.debug("Unsupported CSV file: " + fname);
			return;
		}
		try {
			MERLoader mer = new MERLoader(d, cfg);
			mer.doImport(getDb(), f);
		} catch (Exception e) {
			log.warn(e);
		}
	}

	private static File createTempFile(String filename) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File f = new File(tmpdir + File.separator + filename);
		f.delete();
		return f;
	}

}

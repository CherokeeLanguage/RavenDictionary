package com.cherokeelessons.raven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class App extends Thread {

	@Override
	public void run() {
		final String DIR = "/home/mjoyner/Sync/Cherokee/CherokeeReferenceMaterial/Raven-Dictionary-Output/";
		File in = new File(DIR + "raven-cherokee-dictionary-tlw-2015-11-29.lyx");
		File destfile = new File(DIR + "raven-rock-cherokee-dictionary-DO-NOT-EDIT.lyx");
		ParseDictionary parseDictionary = new ParseDictionary(in);
		App.info("parsing...");
		parseDictionary.run();
		App.info("creating new lyx file...");
		List<IEntry> entries = parseDictionary.getEntries();
		LyxExportFile lyxExportFile = new LyxExportFile(entries, destfile.getAbsolutePath());
		lyxExportFile.setDocorpus(false);
		lyxExportFile.run();
		try {
			FileUtils.writeLines(new File(DIR+"raven-possible-duplications.odt"), lyxExportFile.maybe_dupe);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		App.info("creating new csv file...");
		entries = parseDictionary.getEntries();
		Iterator<IEntry> iterator = entries.iterator();
		List<String> csvlist = new ArrayList<>();
		while (iterator.hasNext()) {
			IEntry next = iterator.next();
			String def = next.getDef();
			def = def.replace("He is ", "");
			def = def.replace("She is ", "");
			List<String> syll = next.getSyllabary();
			String main = syll.get(0);
			for (String s : syll) {
				if (!s.matches(".*[Ꭰ-Ᏼ].*")) {
					continue;
				}
				csvlist.add(StringEscapeUtils.escapeCsv(s) + ","
						+ StringEscapeUtils.escapeCsv(def + " (" + main + ") [raven]"));
			}
		}

		try {
			FileUtils.writeLines(new File(DIR + "dictionary.csv"), csvlist);
		} catch (IOException e) {
			e.printStackTrace();
		}

		App.info("done.");
	}

	public App(String[] args) {
	}

	public static void info() {
		info("\n");
	}

	public static void info(String... info) {
		StringBuilder sb = new StringBuilder();
		for (String i : info) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(i);
		}
		info(sb.toString());
	}

	public static void info(String info) {
		System.out.println(info);
		System.out.flush();
	}

	public static void err(String err) {
		System.err.println(err);
		System.err.flush();
	}

	public static void err() {
		err("\n");
	}
}

package com.cherokeelessons.raven;

import java.io.File;

public class App extends Thread {

	@Override
	public void run() {
		File in = new File("/home/mjoyner/Sync/Cherokee/CherokeeReferenceMaterial/Raven-Dictionary-Output/raven-cherokee-dictionary.lyx");
		new ParseDictionary(in).run();
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

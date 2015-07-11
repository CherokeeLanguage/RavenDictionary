package com.cherokeelessons.raven;

import java.awt.EventQueue;
import java.io.File;

import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;

public class Main {
	public static void main(String[] args) {
		Config config = new Config() {
			@Override
			public File getReportPathFile() {
				File reportFolder = new File("reports");
				if (!reportFolder.isDirectory()) {
					reportFolder.mkdirs();
				}
				return reportFolder;
			}
			
			@Override
			public String getApptitle() {
				return "Raven Dictionary Rebuilder";
			}
			
			@Override
			public Thread getApp(String... args) throws Exception {
				return new App(args);
			}
		};
		EventQueue.invokeLater(new MainWindow(config, args));
	}
}
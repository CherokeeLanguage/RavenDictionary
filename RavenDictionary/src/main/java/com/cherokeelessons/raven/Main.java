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
				return "Researcher Insitute Loader";
			}
			
			@Override
			public Thread getApp(String... args) throws Exception {
				return new Application(args);
			}
		};
		EventQueue.invokeLater(new MainWindow(config, args));
	}
}
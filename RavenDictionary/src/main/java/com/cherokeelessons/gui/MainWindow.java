package com.cherokeelessons.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class MainWindow implements Runnable {

	public static interface Config {
		public Thread getApp(String... args) throws Exception;

		public File getReportPathFile();

		public String getApptitle();
	}

	private JFrame frame;
	private boolean headless;
	private final Config config;
	private String[] args;

	/**
	 * Create the application. Use: EventQueue.invokeLater(new
	 * MainWindow(config));
	 * 
	 * @param args
	 */
	public MainWindow(Config config, String... args) {
		if (args != null) {
			this.args = args;
		} else {
			this.args = new String[0];
		}
		this.config = config;
		initialize();
	}

	public static PrintStream logfile;

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void initialize() {
		Calendar cal = Calendar.getInstance();
		Date today = cal.getTime();
		SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd-HHmm");

		headless = false;
		JScrollPane scrollPane = new JScrollPane();
		JTextPane txtpnStartup = new JTextPane();
		try {
			frame = new JFrame();
			frame.setVisible(true);
		} catch (HeadlessException e1) {
			headless = true;
		}

		try {
			logfile = new PrintStream(new File(config.getReportPathFile(),
					date_format.format(today) + "-log.odt"), "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logfile = System.err;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logfile = System.err;

		}
		if (!headless) {
			GraphicsDevice gd = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int screen_width = gd.getDisplayMode().getWidth();
			int width = screen_width * 75 / 100;
			int screen_height = gd.getDisplayMode().getHeight();
			int height = screen_height * 75 / 100;
			System.out.println("display size: " + screen_width + "x"
					+ screen_height);
			System.out.println("frame size: " + width + "x" + height);

			frame.setTitle(config.getApptitle());
			frame.setBounds((screen_width - width) / 2,
					(screen_height - height) / 2, width, height);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
			scrollPane.setViewportView(txtpnStartup);
		}
		TeeStream tee_stdout = new TeeStream(System.out, logfile);
		TeeStream tee_stderr = new TeeStream(System.err, logfile);
		if (!headless) {
			MessageConsole mc = new MessageConsole(txtpnStartup);
			mc.redirectOut(Color.BLUE, tee_stdout);
			mc.redirectErr(Color.RED, tee_stderr);
		}
		System.err.println("");
		System.err.println("= " + config.getApptitle());
		System.err.println("");
		System.err
				.println("- Master project is in Eclipse as the Gradle project: '"
						+ config.getApptitle() + "'");
		System.err.println("");
		System.err
				.println("- Don't forget to keep the git repository synchronized via commit/push/pull when making changes.");
		System.err.println("");
		System.err.println("");
		System.err.flush();
	}

	@Override
	public void run() {
		Thread app;
		try {
			app = config.getApp(args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			System.out.flush();
			System.err.flush();
		}
		app.start();
	}
}
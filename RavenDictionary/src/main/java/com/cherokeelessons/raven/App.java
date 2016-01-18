package com.cherokeelessons.raven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class App extends Thread {

	private static final String DICTIONARY_SRC_LYX = "raven-cherokee-dictionary-tlw.lyx";

	@Override
	public void run() {
		try {
			_run();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}
	
	public void _run() throws IOException {
		final String DIR = "/home/mjoyner/Sync/Cherokee/CherokeeReferenceMaterial/Raven-Dictionary-Output/";
		File in = new File(DIR + DICTIONARY_SRC_LYX);
		File destfile = new File(DIR + "raven-rock-cherokee-dictionary-output.lyx");
		ParseDictionary parseDictionary = new ParseDictionary(in);
		App.info("parsing...");
		parseDictionary.run();
		App.info("creating new lyx file...");
		List<IEntry> entries = parseDictionary.getEntries();
		LyxExportFile lyxExportFile = new LyxExportFile(entries, destfile.getAbsolutePath());
		lyxExportFile.setDocorpus(false);
		lyxExportFile.setDoWordForms(false);
		try {
			String preface = FileUtils.readFileToString(new File(DIR+"includes/preface.lyx"));
			lyxExportFile.setPreface(StringUtils.substringBetween(preface, "\\begin_body", "\\end_body"));
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		try {
			String appendix = FileUtils.readFileToString(new File(DIR+"includes/appendix.lyx"));
			lyxExportFile.setAppendix(StringUtils.substringBetween(appendix, "\\begin_body", "\\end_body"));
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		try {
			String intro = FileUtils.readFileToString(new File(DIR+"includes/introduction.lyx"));
			lyxExportFile.setIntroduction(StringUtils.substringBetween(intro, "\\begin_body", "\\end_body"));
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		try {
			String grammar = FileUtils.readFileToString(new File(DIR+"grammar.lyx"));
			lyxExportFile.setGrammar(StringUtils.substringBetween(grammar, "\\begin_body", "\\end_body"));
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		
		String revision;
		try {
			revision = FileUtils.readFileToString(new File(DIR + DICTIONARY_SRC_LYX));
			revision = StringUtils.substringBetween(revision, "$Revision:", "$");
			revision=StringUtils.strip(revision);
			lyxExportFile.setRevision("Revision: "+revision);
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		
		String dateModified;
		FastDateFormat fdf = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss z", TimeZone.getTimeZone("EST5EDT")); 
		//FastDateFormat ftf = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, TimeZone.getTimeZone("EST5EDT"));
		dateModified = "Last modified: "+fdf.format(new Date());//+", "+ftf.format(new Date());
		lyxExportFile.setDateModified(dateModified);
		
		lyxExportFile.setAuthor("Michael Joyner, TommyLee Whitlock");
		lyxExportFile.setIsbn("978-1-329-78831-2");
		
		lyxExportFile.process();
		try {
			FileUtils.writeLines(new File(DIR+"raven-possible-duplications.odt"), lyxExportFile.maybe_dupe);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		App.info("creating new csv file for use by analyzer project ...");
		List<String> csvlist = new ArrayList<>();
		entries = parseDictionary.getEntries();
		entries.forEach(entry->{
			String def = entry.getDef();
			def = def.replace("He is ", "");
			def = def.replace("She is ", "");
			List<String> syll = entry.getSyllabary();
			String main = syll.get(0);
			for (String s : syll) {
				if (!s.matches(".*[Ꭰ-Ᏼ].*")) {
					continue;
				}
				csvlist.add(StringEscapeUtils.escapeCsv(s) + ","
						+ StringEscapeUtils.escapeCsv(def + " (" + main + ") [raven]"));
			}
		});

		try {
			FileUtils.writeLines(new File(DIR + "dictionary.csv"), csvlist);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		App.info("creating new csv file for use by example hunter script ...");
		csvlist.clear();
		entries = parseDictionary.getEntries();
		entries.stream().filter(entry->entry.getNotes().size()==0).forEach(entry->{
			String def = entry.getDef();
			def = def.replace("He is ", "");
			def = def.replace("She is ", "");
			List<String> syll = entry.getSyllabary();
			String main = syll.get(0);
			for (String s : syll) {
				if (!s.matches(".*[Ꭰ-Ᏼ].*")) {
					continue;
				}
				csvlist.add(StringEscapeUtils.escapeCsv(s) + ","
						+ StringEscapeUtils.escapeCsv(def + " (" + main + ") [raven]"));
			}
		});

		try {
			FileUtils.writeLines(new File(DIR + "needs-examples.csv"), csvlist);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		csvlist.clear();
		App.info("creating new csv file for use by cherokeedictionary.net ...");
		List<String> columns = new ArrayList<>();
		columns.add("entry");
		columns.add("pronounce");
		columns.add("pos");
		columns.add("c");
		columns.add("definitions");
		
		columns.add("pres_syl_1st");
		columns.add("pres_pro_1st");
		
		columns.add("past_syl");
		columns.add("past_pro");
		
		columns.add("hab_syl");
		columns.add("hab_pro");
		
		columns.add("imm_syl");
		columns.add("imm_pro");
		
		columns.add("dvb_syl");
		columns.add("dvb_pro");
		
		int verb = columns.size();
		
		columns.add("syl_3rd_plural");
		columns.add("pro_3rd_plural");
		columns.add("syl_1st");
		columns.add("pro_1st");
		columns.add("syl_1st_plural");
		columns.add("pro_1st_plural");
		
		int example = columns.size();
		
		columns.add("example_syl");
		columns.add("example_eng");
		columns.add("example_note");
		
		columns.add("notes");
		
		csvlist.add(StringUtils.join(columns, ","));
		
		entries.forEach(entry->{
			columns.clear();
			
			int c = entry.getSyllabary().size();
			Iterator<String> isyl = entry.getSyllabary().iterator();
			Iterator<String> ipro = entry.getPronunciations().iterator();
			String def = entry.formattedDefinition(); 
			def=def.replaceAll("\n*\\\\emph on\n*", "<em>");
			def=def.replaceAll("\n*\\\\emph default\n*", "</em>");
			def=def.replaceAll("\n*\\\\([a-z][A-Z]+) ([a-z][A-Z]+)\n*", "<span class='$1_$2' />");
			String part = entry.getType();
			List<String> notes = entry.getNotes();
			String genus = entry.getGenus();
			
			columns.add(StringEscapeUtils.escapeCsv(isyl.next()));
			columns.add(StringEscapeUtils.escapeCsv(ipro.next()));
			columns.add(StringEscapeUtils.escapeCsv(part));
			columns.add(StringEscapeUtils.escapeCsv(c+""));
			columns.add(StringEscapeUtils.escapeCsv(def));
			
			if (c!=6) {
				while (columns.size()<verb) {
					columns.add("");
				}
			}
			
			if (c==2) {
				String s = isyl.next();
				String p = ipro.next();
				if (!s.matches("[ᏓᏕᏗᏙᏚᏛᏤᏦᏧᏨ].*")){
					columns.add("");
					columns.add("");
				}
				columns.add(StringEscapeUtils.escapeCsv(s));
				columns.add(StringEscapeUtils.escapeCsv(p));
			}
			
			while (isyl.hasNext()) {
				columns.add(StringEscapeUtils.escapeCsv(isyl.next()));
				columns.add(StringEscapeUtils.escapeCsv(ipro.next()));
			}
			
			while (columns.size()<example) {
				columns.add("");
			}
			
			if (!notes.isEmpty()&&notes.get(0).contains("[")) {
				String note = notes.remove(0);
				note=unlatexFormat(note);
				Iterator<String> inote = Arrays.asList(StringUtils.split(note, "\n")).iterator();
				columns.add(StringEscapeUtils.escapeCsv(inote.next()));
				if (inote.hasNext()) {
					columns.add(StringEscapeUtils.escapeCsv(inote.next()));
				} else {
					columns.add("");
				}
				if (inote.hasNext()) {
					columns.add(StringEscapeUtils.escapeCsv(StringUtils.join(inote, "<br/>")));
				} else {
					columns.add("");
				}
			} else {
				columns.add("");
				columns.add("");
				columns.add("");
			}
			if (!notes.isEmpty()) {
				String note = StringUtils.join(notes,"<br/>");
				note=unlatexFormat(note);
				columns.add(StringEscapeUtils.escapeCsv(note));
			} else {
				columns.add("");
			}
			csvlist.add(StringUtils.join(columns, ","));
		});
		
		int withExamples=
		entries.stream().mapToInt(e->{
			List<String> n = e.getNotes();
			if (n.size()==0) {
				return 0;
			}
			if (!n.get(0).contains("[")){
				return 0;
			}
			return 1;
		}).sum();
		
		System.out.println("\t\tFound "+withExamples+" entries with examples.");
		
		try {
			FileUtils.writeLines(new File(DIR + "raven-cherokeedictionary-net.csv"), csvlist);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		App.info("done.");
	}
	
	public String unlatexFormat(String note) {
//		note=note.replace("\n\\size larger\n", "<span class='cap'>");
//		note=note.replace("\n\\size default\n", "</span>");
		note=note.replace("\n\\size larger\n", "");
		note=note.replace("\n\\size default\n", "");
		note=note.replace("\n\\size footnotesize\n", "");
		note=note.replace("\n\\series bold\n", "<strong>");
		note=note.replace("\n\\series default\n", "</strong>");
		note=note.replace("\n\\emph on\n", "<em>");
		note=note.replace("\n\\emph default\n", "</em>");
		note=note.replace("\n\\noun on\n", "<em>");
		note=note.replace("\n\\noun default\n", "</em>");
		note=note.replace("\n\\bar under\n", "<u>");
		note=note.replace("\n\\bar default\n", "</u>");
		note=note.replace("\\SpecialChar \\-", "");
		note=note.replace("\n\\begin_inset Newline newline\n\\end_inset\n", "\n");
		note=note.replaceAll("\n+", "\n");
		if (note.contains("\\")){
			System.err.println("*** WARNING - BACKSLASH REMAINS: "+note);
		}
		
		/*
		 * simple uncross any easy fix crossed-up spans
		 */
		note=note.replace("<em></u>", "</u><em>");
		note=note.replace("<u></em>", "</em><u>");
		note=note.replaceAll("(\\s+)</em>", "</em>$1");
		
		
		if (note.contains("<u>")&&!note.contains("</u>")){
			note+="</u>";
		}
		if (note.contains("<strong>")&&!note.contains("</strong>")){
			note+="</strong>";
		}
		if (note.contains("<em>")&&!note.contains("</em>")){
			note+="</em>";
		}
		
		/*
		 * assume 'u' should always be inside 'emph' or 'strong', etc.
		 */
		note=note.replace("<u><em>", "<u><em>");
		note=note.replace("</em></u>", "</u></em>");
		note=note.replace("<u><strong>", "<u><strong>");
		note=note.replace("</strong></u>", "</u></strong>");
		
		
		/*
		 * fix bogus regions
		 */
		note=note.replaceAll("<em>(\\s*)…</em>", "$1…");
		/*
		 * remove empty regions
		 */
		note=note.replace("<em></em>", "");
		note=note.replace("<u></u>", "");
		if (note.contains("[") && !note.contains("<u>")){
			System.err.println("MISSING <u>: "+note.replace("\n", " -> "));
		}
		u_balance: if (note.contains("[")){
			String[] lines = StringUtils.split(note, "\n");
			if (lines.length!=3) {
				break u_balance;
			}
			int u1 = StringUtils.countMatches(lines[0], "<u>");
			int u2 = StringUtils.countMatches(lines[0], "</u>");
			if (u1!=u2) {
				System.err.println("UNBALANCED <u>: "+note.replace("\n", " -> "));
				break u_balance;
			}
			int u3 = StringUtils.countMatches(lines[1], "<u>");
			int u4 = StringUtils.countMatches(lines[1], "</u>");
			if (u3!=u4) {
				System.err.println("UNBALANCED <u>: "+note.replace("\n", " -> "));
				break u_balance;
			}
			if (u1!=u3 && (u1==0 || u3==0)) {
				System.err.println("MISSING MATCHING <u>: "+note.replace("\n", " -> "));
				break u_balance;
			}
		}
		return note;
	}
	
	/*
	 * definitiond
	 * entrya
	 * 
	 * source
	 */

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

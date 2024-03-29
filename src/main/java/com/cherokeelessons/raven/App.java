package com.cherokeelessons.raven;

import com.cherokeelessons.gui.AbstractApp;
import com.cherokeelessons.gui.MainWindow.Config;
import com.cherokeelessons.log.Log;
import com.cherokeelessons.raven.RavenEntry.SpreadsheetEntry;
import com.cherokeelessons.raven.RavenEntry.SpreadsheetEntry.SpreadsheetRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.logging.Logger;

public class App extends AbstractApp {

    private static final String HTML_U_END = "</u>";
    private static final String HTML_U = "<u>";
    private static final String HTML_EM_END = "</em>";
    private static final String HTML_EM = "<em>";
    private static final String HTML_STRONG_END = "</strong>";
    private static final String HTML_STRONG = "<strong>";
    private static final String LF = "\n";
    private static final String LYX_EM = "\n\\emph on\n";
    private static final String LYX_UNU = "\n\\bar default\n";
    private static final String LYX_U = "\n\\bar under\n";
    private static final String LYX_UNEM = "\n\\emph default\n";
    private static final String LYX_UNBOLD = "\n\\series default\n";
    private static final String LYX_BOLD = "\n\\series bold\n";
    private static final String LYX_NEWLINE = "\n\\begin_inset Newline newline\n\\end_inset\n";
    private static final String PROPERTIES_FILE = "RavenDictionaryJava.properties";
    private static final String DICTIONARY_SRC_LYX = "raven-cherokee-dictionary-tlw.lyx";
    private static final String SUBDIR = "Documents/ᏣᎳᎩ/Lessons/Raven-Cherokee-English-Dictionary";
    private final Logger log = Log.getLogger(this);
    private String csvLink;
    public App(Config config, String[] args) {
        super(config, args);
        // TODO Auto-generated constructor stub
    }

    private static String simpleLatexFormat(String text) {
        // must do '\n' conversion first
        text = text.replace(LF, LYX_NEWLINE);
        text = text.replace(HTML_STRONG, LYX_BOLD);
        text = text.replace(HTML_STRONG_END, LYX_UNBOLD);
        text = text.replace(HTML_EM, LYX_EM);
        text = text.replace(HTML_EM_END, LYX_UNEM);
        text = text.replace(HTML_U, LYX_U);
        text = text.replace(HTML_U_END, LYX_UNU);
        return text;
    }

    public static String unlatexFormat(String text) {
        // note=note.replace("\n\\size larger\n", "<span class='cap'>");
        // note=note.replace("\n\\size default\n", "</span>");
        text = text.replace("\n\\size larger\n", "");
        text = text.replace("\n\\size default\n", "");
        text = text.replace("\n\\size footnotesize\n", "");
        text = text.replace(LYX_BOLD, HTML_STRONG);
        text = text.replace(LYX_UNBOLD, HTML_STRONG_END);
        text = text.replace(LYX_EM, HTML_EM);
        text = text.replace(LYX_UNEM, HTML_EM_END);
        text = text.replace("\n\\noun on\n", HTML_EM);
        text = text.replace("\n\\noun default\n", HTML_EM_END);
        text = text.replace(LYX_U, HTML_U);
        text = text.replace(LYX_UNU, HTML_U_END);
        text = text.replace("\\SpecialChar \\-", "");
        text = text.replace(LYX_NEWLINE, LF);
        text = text.replaceAll("\n+", LF);
        if (text.contains("\\")) {
            System.err.println("*** WARNING - BACKSLASH REMAINS: " + text);
        }

        /*
         * simple uncross any easy fix crossed-up spans
         */
        text = text.replace("<em></u>", "</u><em>");
        text = text.replace("<u></em>", "</em><u>");
        text = text.replaceAll("(\\s+)</em>", "</em>$1");

        if (text.contains(HTML_U) && !text.contains(HTML_U_END)) {
            text += HTML_U_END;
        }
        if (text.contains(HTML_STRONG) && !text.contains(HTML_STRONG_END)) {
            text += HTML_STRONG_END;
        }
        if (text.contains(HTML_EM) && !text.contains(HTML_EM_END)) {
            text += HTML_EM_END;
        }

        /*
         * assume 'u' should always be inside 'emph' or 'strong', etc.
         */
        // text = text.replace("<u><em>", "<u><em>");
        text = text.replace("</em></u>", "</u></em>");
        // text = text.replace("<u><strong>", "<u><strong>");
        text = text.replace("</strong></u>", "</u></strong>");

        /*
         * fix bogus regions
         */
        text = text.replaceAll("<em>(\\s*)…</em>", "$1…");
        /*
         * remove empty regions
         */
        text = text.replace("<em></em>", "");
        text = text.replace("<u></u>", "");
        if (text.contains("[") && !text.contains(HTML_U)) {
            System.err.println("MISSING <u>: " + text.replace(LF, " -> "));
        }
        u_balance:
        if (text.contains("[")) {
            String[] lines = StringUtils.split(text, LF);
            if (lines.length != 3) {
                break u_balance;
            }
            int u1 = StringUtils.countMatches(lines[0], HTML_U);
            int u2 = StringUtils.countMatches(lines[0], HTML_U_END);
            if (u1 != u2) {
                System.err.println("UNBALANCED <u>: " + text.replace(LF, " -> "));
                break u_balance;
            }
            int u3 = StringUtils.countMatches(lines[1], HTML_U);
            int u4 = StringUtils.countMatches(lines[1], HTML_U_END);
            if (u3 != u4) {
                System.err.println("UNBALANCED <u>: " + text.replace(LF, " -> "));
                break u_balance;
            }
            if (u1 != u3 && (u1 == 0 || u3 == 0)) {
                System.err.println("MISSING MATCHING <u>: " + text.replace(LF, " -> "));
                break u_balance;
            }
        }
        /*
         * strip out leading and trailing space on full lines of text
         */
        text = text.replaceAll("(?s)\\s*\n\\s*", LF);
        return text;
    }

    public static void info() {
        info(LF);
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
        err(LF);
    }

    @Override
    public void execute() throws IOException {
        File DIR = new File(FileUtils.getUserDirectory(), SUBDIR);
        log.info("loading config...");
        loadConfiguration();

//        log.info("parsing lyx file...");
//        File lyxSrcFile = new File(DIR, DICTIONARY_SRC_LYX);

//        String destCsvFile = "raven-dictionary-edit-file-from-lyx.csv";

        File csvDir = new File(DIR, "csv-files");
        csvDir.mkdirs();

//        File editFile = new File(DIR, destCsvFile);
//        writeCsvEditFile(editFile, extractEntriesFromLyxFile(lyxSrcFile));

        List<Entry> entries = extractEntriesFromGoogleCsvFile();
        validateEntries(entries);

        String destGoogleCsvFile = "raven-dictionary-edit-file-from-google.csv";
        File googleEditFile = new File(DIR, destGoogleCsvFile);
        writeCsvEditFile(googleEditFile, entries);

        File destfile = new File(DIR, "raven-rock-cherokee-dictionary-output.lyx");
        List<Entry> forLyx = new ArrayList<>();
        entries.forEach(e -> {
            RavenEntry entry = new RavenEntry(e);
            forLyx.add(entry);
            entry.setDef(simpleLatexFormat(entry.getDef().replace("\n", "")));
            ListIterator<String> inotes = entry.getNotes().listIterator();
            while (inotes.hasNext()) {
                String note = inotes.next();
                inotes.set(simpleLatexFormat(note));
            }
        });
        List<String> maybeDupes = writeLyxPrintFile(destfile, forLyx);

        File dupesCheckFile = new File(csvDir, "raven-possible-duplications.");
        writeDupesCheckFile(dupesCheckFile, maybeDupes);

        File analizerCsvFile = new File(csvDir, "dictionary.csv");
        writeAnalyzerCsvFile(analizerCsvFile, entries);
        writeLemmaLookupTabFile(new File(csvDir, "raven-lemma-lookup.tab"), entries);

        File needExamplesCsvFile = new File(csvDir, "needs-examples.csv");
        writeNeedExamplesCsvFile(needExamplesCsvFile, entries);

        File cherokeedictionaryCsvFile = new File(csvDir, "raven-cherokeedictionary-net.csv");
        writeCherokeedictionaryCsvFile(cherokeedictionaryCsvFile, entries);

        log.info("done.");
    }

    private void validateEntries(List<Entry> entries) {
        int badCount = 0;
        for (Entry entry : entries) {
            String firstEntry = entry.getSyllabary().get(0) + " (" + StringEscapeUtils.escapeJava(entry.getDef()) + ")";
            if (entry.getSyllabary().size() != entry.getPronunciations().size()) {
                badCount++;
                System.err.println("MISMATCHED SYLLABARY to PRONUNCIATIONS FOR: " + firstEntry);
                System.err.println(" - " + entry.getSyllabary().toString());
                System.err.println(" - " + entry.getPronunciations().toString());
            }

            Iterator<String> iSyllabary = entry.getSyllabary().iterator();
            Iterator<String> iPronounce = entry.getPronunciations().iterator();

            entry_scan: while (iSyllabary.hasNext()) {
                String syllabary = iSyllabary.next();
                String pronounce = Normalizer.normalize(iPronounce.next(), Form.NFC);
                if (syllabary.equals("-")) {
                    if (!pronounce.isEmpty()) {
                        badCount++;
                        System.err.println("UNEXPECTED PRONUNCIATION ENTRY FOR: " + firstEntry);
                        System.err.println("  " + syllabary + " [" + pronounce + "]");
                        continue;
                    }
                    continue;
                }
                if (!syllabary.matches("[,\\sᎠ-Ᏼ\\-]+")) {
                    badCount++;
                    System.err.println("BAD SYLLABARY ENTRY FOR: " + firstEntry);
                    System.err.println("  " + syllabary + " [" + pronounce + "]");
                    continue;
                }
                String pronounce_match_string = Normalizer.normalize("[*\u02d0:ʔ ,aeiouváéíóúv́àèìòùv̀ǎěǐǒǔv̌âêîôûv̂a̋e̋i̋őűv̋cdghjklmnstwy]*[aeiouváéíóúv́àèìòùv̀ǎěǐǒǔv̌âêîôûv̂a̋e̋i̋őűv̋cdghjklmnstwy][\u02d0:]?", Form.NFC);

                String[] pronounceEntries = pronounce.split(";\\s*");
                if (pronounceEntries.length>0) {
                    for (String subentry: pronounceEntries) {
                        if (!subentry.matches(pronounce_match_string)) {
                            badCount++;
                            System.err.println("BAD PRONUNCIATION ENTRY FOR: " + firstEntry + "<"+subentry+">");
                            System.err.println("  " + syllabary + " [" + pronounce + "]");
                            continue entry_scan;
                        }
                    }
                }

                if (syllabary.contains(",")) {
                    if (!syllabary.contains(", ") || !pronounce.contains(", ")) {
                        badCount++;
                        System.err.println("BAD COMMAND IN ENTRY FOR: " + firstEntry);
                        System.err.println("  " + syllabary + " [" + pronounce + "]");
                        continue;
                    }
                    if (StringUtils.countMatches(syllabary, ",") != StringUtils.countMatches(pronounce, ",")) {
                        badCount++;
                        System.err.println("SYLLABARY AND PRONOUNCE SUB-ENTRY COMMAS DON'T MATCH: " + firstEntry);
                        System.err.println("  " + syllabary + " [" + pronounce + "]");
                        continue;
                    }
                }

            }
        }
        if (badCount > 0) {
            throw new RuntimeException(badCount + " BAD ENTRIES IN SPREADSHEET!");
        }
    }

    private static String easternFormat(String pronounce) {
        return pronounce //
                .replace("j", "ts") //
                .replace("ch", "tsh") //
                .replace("J", "Ts") //
                .replace("Ch", "Tsh") //
                .replace("CH", "Tsh");
    }


    private List<Entry> extractEntriesFromGoogleCsvFile() throws IOException {
        if (csvLink == null) {
            return null;
        }
        File DIR = new File(FileUtils.getUserDirectory(), SUBDIR);
        File localCopyOfCsv = new File(DIR, "raven-dictionary-google-download.csv");
        List<Entry> entries = new ArrayList<>();

        log.info("Retrieving Google CSV file...");
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        URL csvFile = new URL(csvLink);
        URLConnection openConnection = csvFile.openConnection();
        openConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        String csvString = IOUtils.toString(csvFile, StandardCharsets.UTF_8);
        log.info("Retrieved Google CSV file...");
        FileUtils.write(localCopyOfCsv, csvString, StandardCharsets.UTF_8);
        log.info("Saving Google CSV file... " + localCopyOfCsv.getPath());
        Entry entry = null;
        try (CSVParser parser = new CSVParser(new StringReader(csvString), CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames())) {
            Iterator<CSVRecord> irec = parser.iterator();
            while (irec.hasNext()) {
                CSVRecord next = irec.next();
                String entryMark = StringUtils.strip(next.get(0));
                if (entry == null && !entryMark.equals("ENTRY")) {
                    continue;
                }
                String syllabaryOrNote = StringUtils.strip(next.get(1));
                String pronounce = StringUtils.strip(next.get(2));
                pronounce = pronounce.replace(":", "\u02d0");
                pronounce = easternFormat(pronounce);

                String pos = StringUtils.strip(next.get(3));
                String def = StringUtils.strip(next.get(4));
                if (entryMark.equalsIgnoreCase("ENTRY")) {
                    entry = new RavenEntry();
                    entries.add(entry);
                    entry.addSyllabary(syllabaryOrNote);
                    entry.addPronunciation(pronounce);
                    entry.setDef(def);
                    entry.setType(pos);
                    continue;
                }
                if (entryMark.isEmpty() && syllabaryOrNote.isEmpty()) {
                    continue;
                }
                if (entryMark.isEmpty() && !syllabaryOrNote.isEmpty()) {
                    if (entry != null) {
                        entry.addSyllabary(syllabaryOrNote);
                        entry.addPronunciation(pronounce);
                    }
                    continue;
                }
                if (entryMark.equalsIgnoreCase("note")) {
                    if (entry != null) {
                        entry.addNote(syllabaryOrNote);
                    }
                    continue;
                }
                if (entryMark.equalsIgnoreCase("cf")) {
                    if (entry != null) {
                        entry.addCf(syllabaryOrNote);
                    }
                    continue;
                }
                if (entryMark.equalsIgnoreCase("label")) {
                    if (entry != null) {
                        if (entry.getLabel() != null) {
                            System.err.println("Already assigned a label: " + entry.getSyllabary() + " " + entry.getDef() + " " + entry.getLabel());
                            throw new RuntimeException("Already assigned a label: " + entry.getSyllabary() + " " + entry.getDef() + " " + entry.getLabel());
                        }
                        entry.addCf(syllabaryOrNote);
                    }
                    continue;
                }
                if (entryMark.equalsIgnoreCase("TLW to here")) {
                    continue;
                }
                System.err.println("ENTRY TYPE NOT HANDLED: " + entryMark);
                throw new RuntimeException("ENTRY TYPE NOT HANDLED: " + entryMark);
            }
        }
        return entries;
    }

    private void loadConfiguration() throws IOException {
        File DIR = new File(FileUtils.getUserDirectory(), SUBDIR);
        File propertiesFile = new File(DIR, PROPERTIES_FILE);
        if (DIR.isDirectory()) {
            if (!propertiesFile.exists()) {
                FileUtils.touch(propertiesFile);
            }
        }
        if (!propertiesFile.exists()) {
            return;
        }
        Properties prop = new Properties();
        prop.load(new FileInputStream(propertiesFile));
        csvLink = prop.getProperty("google-csv-url");
    }

    private void writeCherokeedictionaryCsvFile(File cherokeedictionaryCsvFile, List<Entry> entries) {
        List<String> csvlist = new ArrayList<>();
        log.info("creating new csv file for use by cherokeedictionary.net ...");
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

        entries.forEach(entry -> {
            columns.clear();

            int c = entry.getSyllabary().size();
            Iterator<String> isyl = entry.getSyllabary().iterator();
            Iterator<String> ipro = entry.getPronunciations().iterator();
            String def = entry.formattedDefinition();
            def = def.replaceAll("\n*\\\\emph on\n*", HTML_EM);
            def = def.replaceAll("\n*\\\\emph default\n*", HTML_EM_END);
            def = def.replaceAll("\n*\\\\([a-z][A-Z]+) ([a-z][A-Z]+)\n*", "<span class='$1_$2' />");
            String part = entry.getType();
            List<String> notes = entry.getNotes();

            columns.add(StringEscapeUtils.escapeCsv(isyl.next()));
            columns.add(StringEscapeUtils.escapeCsv(ipro.next()));
            columns.add(StringEscapeUtils.escapeCsv(part));
            columns.add(StringEscapeUtils.escapeCsv(c + ""));
            columns.add(StringEscapeUtils.escapeCsv(def));

            if (c != 6) {
                while (columns.size() < verb) {
                    columns.add("");
                }
            }

            if (c == 2) {
                String s = isyl.next();
                String p = ipro.next();
                if (!s.matches("[ᏓᏕᏗᏙᏚᏛᏤᏦᏧᏨ].*")) {
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

            while (columns.size() < example) {
                columns.add("");
            }

            if (!notes.isEmpty() && notes.get(0).contains("[")) {
                String note = notes.remove(0);
                note = unlatexFormat(note);
                Iterator<String> inote = Arrays.asList(StringUtils.split(note, LF)).iterator();
                columns.add(StringUtils.strip(StringEscapeUtils.escapeCsv(inote.next())));
                if (inote.hasNext()) {
                    columns.add(StringUtils.strip(StringEscapeUtils.escapeCsv(inote.next())));
                } else {
                    columns.add("");
                }
                if (inote.hasNext()) {
                    columns.add(StringUtils.strip(StringEscapeUtils.escapeCsv(StringUtils.join(inote, "<br/>"))));
                } else {
                    columns.add("");
                }
            } else {
                columns.add("");
                columns.add("");
                columns.add("");
            }
            if (!notes.isEmpty()) {
                String note = StringUtils.join(notes, "<br/>");
                note = unlatexFormat(note);
                note = StringUtils.strip(note);
                columns.add(StringEscapeUtils.escapeCsv(note));
            } else {
                columns.add("");
            }
            csvlist.add(StringUtils.join(columns, ","));
        });

        try {
            FileUtils.writeLines(cherokeedictionaryCsvFile, csvlist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * definitiond entrya
     *
     * source
     */

    private void writeNeedExamplesCsvFile(File needsExamplesCsvFile, List<Entry> entries) {
        log.info("creating new csv file for use by example hunter script ...");
        List<String> csvlist = new ArrayList<>();
        entries.stream().filter(entry -> entry.getNotes().size() == 0).forEach(entry -> {
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
            FileUtils.writeLines(needsExamplesCsvFile, csvlist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAnalyzerCsvFile(File analizerCsvFile, List<Entry> entries) {
        log.info("creating new csv file for use by analyzer project ...");
        List<String> csvlist = new ArrayList<>();
        entries.forEach(entry -> {
            String def = entry.getDef();
            def = def.replace("He is ", "");
            def = def.replace("She is ", "");
            List<String> syll = entry.getSyllabary();
            String main = syll.get(0);
            for (String s : syll) {
                if (!s.matches(".*[Ꭰ-Ᏼ].*")) {
                    continue;
                }
                csvlist.add(StringEscapeUtils.escapeCsv(s).replace("\n", "\\n") + ","
                        + StringEscapeUtils.escapeCsv(def + " (" + main + ") [raven]").replace("\n", "\\n"));
            }
        });

        try {
            FileUtils.writeLines(analizerCsvFile, csvlist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLemmaLookupTabFile(File analizerCsvFile, List<Entry> entries) {
        log.info("creating new csv file for use by analyzer project ...");
        List<String> csvlist = new ArrayList<>();
        entries.forEach(entry -> {
            String def = entry.getDef();
            def = def.replace("He is ", "");
            def = def.replace("She is ", "");
            List<String> syll = entry.getSyllabary();
            String main = syll.get(0);
            for (String s : syll) {
                if (!s.matches(".*[Ꭰ-Ᏼ].*")) {
                    continue;
                }
                String surfaceForm = StringEscapeUtils.escapeCsv(s).replace("\n", " ");
                String lemmaForm = StringEscapeUtils.escapeCsv(main).replace("\n", " ");
                String details = StringEscapeUtils.escapeCsv(def + " [raven]").replace("\n", " ");
                csvlist.add(surfaceForm + "\t" + lemmaForm + "\t" + details);
            }
        });

        try {
            FileUtils.writeLines(analizerCsvFile, csvlist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDupesCheckFile(File dupesCheckFile, List<String> maybeDupes) {
        try {
            FileUtils.writeLines(dupesCheckFile, maybeDupes);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private List<String> writeLyxPrintFile(File destfile, List<Entry> entries) throws IOException {
        File DIR = new File(FileUtils.getUserDirectory(), SUBDIR);
        log.info("creating new lyx file...");
        LyxExportFile lyxExportFile = new LyxExportFile(entries, destfile.getAbsolutePath());
        lyxExportFile.setDocorpus(false);
        lyxExportFile.setDoWordForms(false);
        try {
            String preface = FileUtils.readFileToString(new File(DIR, "includes/preface.lyx"), StandardCharsets.UTF_8);
            lyxExportFile.setPreface(StringUtils.substringBetween(preface, "\\begin_body", "\\end_body"));
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
        try {
            String appendix = FileUtils.readFileToString(new File(DIR, "includes/appendix.lyx"),
                    StandardCharsets.UTF_8);
            lyxExportFile.setAppendix(StringUtils.substringBetween(appendix, "\\begin_body", "\\end_body"));
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
        try {
            String intro = FileUtils.readFileToString(new File(DIR, "includes/introduction.lyx"),
                    StandardCharsets.UTF_8);
            lyxExportFile.setIntroduction(StringUtils.substringBetween(intro, "\\begin_body", "\\end_body"));
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
        try {
            String grammar = FileUtils.readFileToString(new File(DIR, "grammar.lyx"), StandardCharsets.UTF_8);
            lyxExportFile.setGrammar(StringUtils.substringBetween(grammar, "\\begin_body", "\\end_body"));
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }

        String revision;
        try {
            revision = FileUtils.readFileToString(new File(DIR, DICTIONARY_SRC_LYX), StandardCharsets.UTF_8);
            revision = StringUtils.substringBetween(revision, "$Revision:", "$");
            revision = StringUtils.strip(revision);
            lyxExportFile.setRevision("Revision: " + revision);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }

        String dateModified;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss z", TimeZone.getTimeZone("EST5EDT"));
        // FastDateFormat ftf =
        // FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM,
        // TimeZone.getTimeZone("EST5EDT"));
        dateModified = "Last modified: " + fdf.format(new Date());// +",
        // "+ftf.format(new
        // Date());
        lyxExportFile.setDateModified(dateModified);

        lyxExportFile.setAuthor("Michael Conrad, TommyLee Whitlock");
        lyxExportFile.setIsbn("978-1-329-78831-2");

        lyxExportFile.process();
        return lyxExportFile.maybe_dupe;
    }

    private void writeCsvEditFile(File editFile, List<Entry> entries) throws IOException {
        log.info("writing csv edit file from lyx file...");
        FileUtils.deleteQuietly(editFile);
        editFile.getParentFile().mkdirs();
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(editFile), StandardCharsets.UTF_8);
        CSVFormat withHeader = CSVFormat.Builder.create().setHeader("ENTRY_MARK", "SYLLABARY", "PRONUNCIATION", "POS",
                "DEFINITIONS").build();
        List<Entry> copy = new ArrayList<>(entries);
        Collections.sort(copy);
        try (CSVPrinter printer = withHeader.print(os)) {
            for (Entry entry : copy) {
                SpreadsheetEntry records = entry.spreadsheetEntry();
                for (SpreadsheetRow record : records.rows) {
                    while (record.fields.size() < 5) {
                        record.fields.add("");
                    }
                    for (String field : record.fields) {
                        String tmp = unlatexFormat(field);
                        tmp = tmp.replace("<tag:emph:on>", HTML_EM);
                        tmp = tmp.replace("<tag:emph:default>", HTML_EM_END);
                        printer.print(tmp);
                    }
                    printer.println();
                }
                printer.println();
            }
        }
    }

    @Override
    protected void parseArgs(Iterator<String> iargs) {
        // TODO Auto-generated method stub

    }
}

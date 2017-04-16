package com.cherokeelessons.raven;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import net.cherokeedictionary.lyx.CrossReference;
import net.cherokeedictionary.lyx.DefinitionLine;
import net.cherokeedictionary.lyx.EnglishCherokee;
import net.cherokeedictionary.lyx.ExampleEntry;
import net.cherokeedictionary.lyx.LyxEntry;
import net.cherokeedictionary.lyx.LyxEntry.HasStemmedForms;
import net.cherokeedictionary.lyx.MultiEntry;
import net.cherokeedictionary.lyx.Reference;
import net.cherokeedictionary.lyx.VerbEntry;
import net.cherokeedictionary.lyx.WordForm;
import net.cherokeedictionary.shared.StemEntry;
import net.cherokeedictionary.shared.StemType;

public class LyxExportFile {
	private static final String sloppy_begin = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status collapsed\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "begin{sloppy}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n\n";

	private static final String sloppy_end = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status collapsed\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "end{sloppy}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n\n";

	private static final String columnsep_large = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status open\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "setlength{\n" //
			+ "\\backslash\n" //
			+ "columnsep}{20pt}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n" //
			+ "\n";
	private static final String columnsep_normal = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status open\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "setlength{\n" //
			+ "\\backslash\n" //
			+ "columnsep}{10pt}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n" //
			+ "\n";
	private static final String seprule_on = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status open\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "setlength{\n" //
			+ "\\backslash\n" //
			+ "columnseprule}{0.5pt}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n";
	private static final String seprule_off = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status open\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "setlength{\n" //
			+ "\\backslash\n" //
			+ "columnseprule}{0pt}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n";
	private static final String MULTICOLS_END = "\\begin_layout Standard\n" //
			+ "\\begin_inset ERT\n" //
			+ "status collapsed\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "end{multicols}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n";
	private static final String MULTICOLS_BEGIN = "\\begin_layout Standard\n" //
			+ "\n" //
			+ "\\lang english\n" //
			+ "\\begin_inset ERT\n" //
			+ "status collapsed\n" //
			+ "\n" //
			+ "\\begin_layout Plain Layout\n" //
			+ "\n" //
			+ "\n" //
			+ "\\backslash\n" //
			+ "begin{multicols}{2}\n" //
			+ "\\end_layout\n" //
			+ "\n" //
			+ "\\end_inset\n" //
			+ "\n" //
			+ "\n" //
			+ "\\end_layout\n";
	private static final String Chapter_Dictionary = "\\begin_layout Chapter\n" //
			+ "Dictionary\n" //
			+ "\\end_layout\n";
	private static final String Chapter_WordForms = "\\begin_layout Chapter\n" //
			+ "Word Form Lookup\n" //
			+ "\\end_layout\n";
	private static final String Chapter_English = "\\begin_layout Chapter\n" //
			+ "English to Cherokee Lookup\n" //
			+ "\\end_layout\n";

	private final List<Entry> entries;

	private boolean doWordForms = true;

	public boolean isDoWordForms() {
		return doWordForms;
	}

	public void setDoWordForms(boolean doWordForms) {
		this.doWordForms = doWordForms;
	}

	private boolean docorpus = false;

	public boolean isDocorpus() {
		return docorpus;
	}

	public void setDocorpus(boolean docorpus) {
		this.docorpus = docorpus;
	}

	public LyxExportFile(List<Entry> entries, String destfile) {
		this.entries = entries;
		this.lyxfile = destfile;
	}

	public final List<String> maybe_dupe = new ArrayList<>();

	public void process() throws IOException {
		_run();
	}

	private String preface = "";
	private String grammar = "";

	private String introduction = "";

	private String appendix = "";

	public String getAppendix() {
		return appendix;
	}

	public void setAppendix(String appendix) {
		this.appendix = appendix;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public void _run() throws IOException {
		final List<LyxEntry> definitions = new ArrayList<LyxEntry>();

		String start = IOUtils
				.toString(getClass().getResourceAsStream("/net/cherokeedictionary/lyx/LyxDocumentStart.txt"));
		start = start.replace("__preface__", preface);
		start = start.replace("__introduction__", introduction);
		start = start.replace("__REVISION__", revisionNumber);
		start = start.replace("__DATE__", dateModified);
		start = start.replace("ISBN: 978-x-xxx-xxxxx-x", "ISBN: " + formattedIsbn);
		start = start.replace("__AUTHOR__", author);

		String end = IOUtils.toString(getClass().getResourceAsStream("/net/cherokeedictionary/lyx/LyxDocumentEnd.txt"));
		end = end.replace("__grammar__", grammar);
		end = end.replace("__appendix__", appendix);

		for (Entry entry : entries) {
			if (entry.getType().startsWith("v")) {
				VerbEntry v = new VerbEntry();
				v.definition = entry.formattedDefinition();
				v.pos = "v";
				/*
				 * it is ASSUMED that vbst generated entries have six forms
				 * (five stems) and that they are in proper order
				 */
				v.present3rd = new DefinitionLine();
				v.present1st = new DefinitionLine();
				v.remotepast = new DefinitionLine();
				v.habitual = new DefinitionLine();
				v.imperative = new DefinitionLine();
				v.infinitive = new DefinitionLine();

				Iterator<String> ilatin = entry.getPronunciations().iterator();
				v.present3rd.pronounce = ilatin.next();
				v.present1st.pronounce = ilatin.next();
				v.remotepast.pronounce = ilatin.next();
				v.habitual.pronounce = ilatin.next();
				v.imperative.pronounce = ilatin.next();
				v.infinitive.pronounce = ilatin.next();
				Iterator<String> isyll = entry.getSyllabary().iterator();
				v.present3rd.syllabary = isyll.next();
				v.present1st.syllabary = isyll.next();
				v.remotepast.syllabary = isyll.next();
				v.habitual.syllabary = isyll.next();
				v.imperative.syllabary = isyll.next();
				v.infinitive.syllabary = isyll.next();

				if (entry.getNotes().size() != 0) {
					entry.getNotes().stream().forEach(note -> v.addNote(note));
				}

				definitions.add(v);
				continue;
			}
			MultiEntry multi = new MultiEntry();
			multi.definition = entry.formattedDefinition();
			multi.pos = entry.getType();
			Iterator<String> ilatin = entry.getPronunciations().iterator();
			Iterator<String> isyll = entry.getSyllabary().iterator();
			while (ilatin.hasNext() && isyll.hasNext()) {
				DefinitionLine def = new DefinitionLine();
				def.pronounce = ilatin.next();
				def.syllabary = isyll.next();
				multi.addDefinition(def);
			}
			if (entry.getNotes().size() != 0) {
				entry.getNotes().stream().forEach(note -> multi.addNote(note));
			}
			definitions.add(multi);
		}

		int iid = 0;
		for (LyxEntry e : definitions) {
			e.id = ++iid;
		}

		Collections.sort(definitions);

		/*
		 * Any duplicates?
		 */
		Iterator<LyxEntry> ientry = definitions.iterator();
		LyxEntry d1 = ientry.next();
		while (ientry.hasNext()) {
			LyxEntry d2 = ientry.next();
			if (!d1.getSyllabary().get(0).equals(d2.getSyllabary().get(0))) {
				d1 = d2;
				continue;
			}
			if (!d1.definition.equals(d2.definition)) {
				maybe_dupe.add("Possible Duplicate: " + d2.getSyllabary().get(0) + " | " + d1.definition + " | "
						+ d2.definition);
				d1 = d2;
				continue;
			}
			if (!d1.getPronunciations().get(0).equals(d2.getPronunciations().get(0))) {
				// App.info("Likely Duplicate: " + d2.getSyllabary().get(0) + "
				// " + d2.definition);
				d1 = d2;
				continue;
			}
			// App.info("Likely Duplicate: " + d2.getSyllabary().get(0) + " " +
			// d2.definition);
			ientry.remove();
		}
		for (String e : maybe_dupe) {
			// App.info(e);
		}

		/*
		 * Extract English Definitions
		 */
		List<EnglishCherokee> english = new ArrayList<>();
		Iterator<LyxEntry> idef = definitions.iterator();
		while (idef.hasNext()) {
			LyxEntry next = idef.next();
			String syllabary = next.getSyllabary().get(0);
			String pronounce = next.getPronunciations().get(0);
			if (syllabary.contains(",")) {
				syllabary = StringUtils.substringBefore(syllabary, ",");
				syllabary = StringUtils.strip(syllabary);
			}
			if (pronounce.contains(",")) {
				pronounce = StringUtils.substringBefore(pronounce, ",");
				pronounce = StringUtils.strip(pronounce);
			}
			String def = next.definition;
			int forLabel = next.id;
			EnglishCherokee ec = new EnglishCherokee();
			ec.setEnglish(def);
			ec.refs.add(new Reference(syllabary, pronounce, forLabel));
			english.addAll(getSplitsFor(ec));
		}
		Collections.sort(english);
		NumberFormat nf = NumberFormat.getInstance();
		App.info("Pre-combined English to Cherokee entries: " + nf.format(english.size()));
		for (int ix = 1; ix < english.size(); ix++) {
			if (english.get(ix - 1).equals(english.get(ix))) {
				english.remove(ix);
				ix--;
			}
		}
		App.info("Deduped English to Cherokee entries: " + nf.format(english.size()));
		for (int ix = 1; ix < english.size(); ix++) {
			EnglishCherokee e1 = english.get(ix - 1);
			EnglishCherokee e2 = english.get(ix);
			if (e1.getDefinition().equals(e2.getDefinition())) {
				e1.refs.addAll(e2.refs);
				english.remove(ix);
				ix--;
				continue;
			}
		}
		App.info("Post-combined English to Cherokee entries: " + nf.format(english.size()));

		/**
		 * Blacklisted entries.
		 */
		english.removeIf(entry -> entry.getDefinition().startsWith("All Varieties"));

		/*
		 * Build up word forms reference
		 */
		List<WordForm> wordforms = new ArrayList<>();
		if (doWordForms) {
			idef = definitions.iterator();
			while (idef.hasNext()) {
				LyxEntry next = idef.next();
				List<String> list = next.getSyllabary();
				String primary_entry = list.get(0);
				if (primary_entry.contains(",")) {
					primary_entry = StringUtils.substringBefore(primary_entry, ",");
				}
				primary_entry = StringUtils.strip(primary_entry);
				if (next instanceof HasStemmedForms) {
					List<StemEntry> stems = (((HasStemmedForms) next).getStems());
					if (stems.size() != 0) {
						list.clear();
					}
					/*
					 * add stems directly to wordforms list
					 */
					for (StemEntry entry : stems) {
						if (StringUtils.isBlank(entry.syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""))) {
							continue;
						}
						WordForm wf = new WordForm();
						wf.references.add(new Reference(primary_entry, "", next.id));
						wf.stemEntry = new StemEntry(entry);
						wordforms.add(wf);
					}
					/*
					 * continue with next def entry
					 */
					continue;
				}
				/*
				 * no stemmed entries found, just add raw definition entries
				 * instead...
				 */
				Iterator<String> isyl = list.iterator();
				while (isyl.hasNext()) {
					for (String syllabary : StringUtils.split(isyl.next(), ",")) {
						syllabary = StringUtils.strip(syllabary);
						if (StringUtils.isBlank(syllabary.replaceAll("[^Ꭰ-Ᏼ]", ""))) {
							continue;
						}
						WordForm wf = new WordForm();
						wf.stemEntry.syllabary = syllabary;
						wf.references.add(new Reference(primary_entry, "", next.id));
						wf.stemEntry = new StemEntry(syllabary, StemType.Other);
						wordforms.add(wf);
					}
				}
			}
			Collections.sort(wordforms);
			App.info("Pre-combined and pre-deduped Wordform entries: " + nf.format(wordforms.size()));
			for (int ix = 1; ix < wordforms.size(); ix++) {
				WordForm e1 = wordforms.get(ix - 1);
				WordForm e2 = wordforms.get(ix);
				if (e1.stemEntry.syllabary.equals(e2.stemEntry.syllabary)) {
					e2.references.removeAll(e1.references);
					e1.references.addAll(e2.references);
					WordForm.dedupeBySyllabary(e1.references);
					wordforms.remove(ix);
					ix--;
					continue;
				}
			}
			App.info("Post-combined Wordform entries: " + nf.format(wordforms.size()));
		}

		StringBuilder sb = new StringBuilder();

		/*
		 * Start of Book including all front matter
		 */
		sb.append(start);
		/*
		 * Cherokee Dictionary
		 */
		sb.append(Chapter_Dictionary + columnsep_large + seprule_on + MULTICOLS_BEGIN + sloppy_begin);
		String prevSection = "";
		int sectionCounter = 0;
		int maxPerSection = 40;
		for (LyxEntry entry : definitions) {
			String syll = StringUtils.left(entry.getSyllabary().get(0).replaceAll("[^Ꭰ-Ᏼ]", ""), 1);
			sectionCounter++;
			if (!syll.equals(prevSection)) {
				prevSection = syll;
				sb.append("\n\\begin_layout Section\n");
				sb.append("\n\\noun on\n");
				// sb.append(entry.getSyllabary().get(0));
				sb.append(syll);
				sb.append("\n\\noun default\n");
				// sb.append(syll);
				sb.append("\n\\end_layout\n");
				sectionCounter = 0;
			}
			if (sectionCounter >= maxPerSection) {
				sb.append("\n\\begin_layout Section\n");
				sb.append("\n\\noun on\n");
				sb.append(entry.getSyllabary().get(0));
				sb.append("\n\\noun default\n");
				sb.append("\n\\end_layout\n");
				sectionCounter = 0;
			}
			// sb.append(entry.getLyxCode().replace("\\n", " "));
			{
				sb.append(insetBoxFramelessStart());
				sb.append(entry.getLyxCode());
				if (entry.examples.size() != 0) {
					sb.append("\\begin_deeper\n");
					for (ExampleEntry ee : entry.examples) {
						sb.append(ee.getLyxCode());
					}
					sb.append("\\end_deeper\n");
				}
				Iterator<CrossReference> icross = entry.crossrefs.iterator();
				if (icross.hasNext()) {
					sb.append("\\begin_deeper\n");
					StringBuilder sb1 = new StringBuilder();
					sb1.append("\\begin_layout Standard\n");
					sb1.append("\\noindent\n");
					sb1.append("\\align left\n");
					sb1.append("\\emph on\n");
					sb1.append("cf: ");
					sb1.append("\\emph default\n");
					// sb1.append("\\end_layout\n");
					sb.append(sb1.toString());
					sb.append(icross.next().getLyxCode(true));
					while (icross.hasNext()) {
						sb.append(", " + icross.next().getLyxCode(true));
					}
					sb.append("\\end_deeper\n");
				}
				sb.append(insetBoxFramelessEnd());
			}
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off + columnsep_normal);

		/*
		 * English to Cherokee
		 */
		sb.append(Chapter_English + columnsep_large + seprule_on + MULTICOLS_BEGIN + sloppy_begin);
		prevSection = "";
		sectionCounter = 0;
		maxPerSection = 50;
		for (EnglishCherokee entry : english) {
			String eng = StringUtils.left(entry.getDefinition(), 1).toUpperCase();
			sectionCounter++;
			if (!eng.equals(prevSection)) {
				prevSection = eng;
				sb.append("\n\\begin_layout Section\n");
				sb.append("\n\\noun on\n");
				sb.append(eng.toUpperCase());
				// String sectionName =
				// StringUtils.substringBefore(entry.getDefinition(),"(");
				// sectionName=StringUtils.strip(sectionName);
				// sb.append(sectionName);
				sb.append("\n\\noun default\n");
				sb.append("\n\\end_layout\n");
				sectionCounter = 0;
			}
			if (sectionCounter >= maxPerSection) {
				sb.append("\n\\begin_layout Section\n");
				sb.append("\n\\noun on\n");
				String sectionName = StringUtils.substringBefore(entry.getDefinition(), "(");
				sectionName = StringUtils.strip(sectionName);
				sb.append(sectionName);
				sb.append("\n\\noun default\n");
				sb.append("\n\\end_layout\n");
				sectionCounter = 0;
			}
			sb.append(entry.getLyxCode(true));
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off + columnsep_normal);

		/*
		 * Wordform Lookup
		 */
		if (doWordForms) {
			sb.append(Chapter_WordForms + columnsep_large + seprule_on + MULTICOLS_BEGIN + sloppy_begin);
			prevSection = "";
			for (WordForm entry : wordforms) {
				if (entry.stemEntry.syllabary.contains(" ")) {
					continue;
				}
				String syll = StringUtils.left(entry.stemEntry.syllabary, 1);
				if (!syll.equals(prevSection)) {
					prevSection = syll;
					sb.append("\n\\begin_layout Section\n");
					sb.append(syll);
					sb.append("\n\\end_layout\n");
				}
				sb.append(entry.getLyxCode());
			}
			sb.append(sloppy_end + MULTICOLS_END + seprule_off + columnsep_normal);
		}

		sb.append(end);
		FileUtils.writeStringToFile(new File(lyxfile), sb.toString(), "UTF-8", false);

		if (docorpus) {
			corpusWriter(definitions);
		}

		/*
		 * Save out wordforms+defs into a special lookup file for use by other
		 * softwares.
		 */
		Map<Integer, LyxEntry> defmap = new HashMap<>();
		for (LyxEntry entry : definitions) {
			defmap.put(entry.id, entry);
		}
		Map<Integer, EnglishCherokee> engmap = new HashMap<>();
		for (EnglishCherokee entry : english) {
			for (Reference ref : entry.refs) {
				engmap.put(ref.toLabel, entry);
			}
		}
		StringBuilder sbwf = new StringBuilder();
		for (WordForm wordform : wordforms) {
			if (wordform.stemEntry.syllabary.contains(" ")) {
				continue;
			}
			sbwf.append(wordform.stemEntry.syllabary);
			sbwf.append("\t");
			sbwf.append(wordform.stemEntry.stemtype.name());
			for (int ix = 0; ix < wordform.references.size(); ix++) {
				sbwf.append("\t");
				Reference ref = wordform.references.get(ix);
				sbwf.append(ref.syllabary);
				EnglishCherokee eng = engmap.get(ref.toLabel);
				if (eng != null) {
					sbwf.append(":");
					sbwf.append(eng.getDefinition());
				}
			}
			sbwf.append("\n");
		}
		FileUtils.writeStringToFile(new File("output/wordforms.txt"), sbwf.toString(), "UTF-8");
		System.out.println("Wrote wordforms.txt");
	}

	private String insetBoxFramelessStart() {
		return "\n\\begin_layout Standard\n" //
				+ "\\begin_inset Box Frameless\n" //
				+ "position \"t\"\n" //
				+ "hor_pos \"c\"\n" //
				+ "has_inner_box 1\n" //
				+ "inner_pos \"t\"\n" //
				+ "use_parbox 0\n" //
				+ "use_makebox 0\n" //
				+ "width \"100col%\"\n" //
				+ "special \"none\"\n" //
				+ "height \"1in\"\n" //
				+ "height_special \"totalheight\"\n" //
				+ "status open\n";
	}

	private String insetBoxFramelessEnd() {
		return "\\end_inset\n" //
				+ "\n" //
				+ "\n" //
				+ "\\end_layout\n";
	}

	private void corpusWriter(final List<LyxEntry> definitions) throws IOException {
		/*
		 * CORPUS WRITER FOR MAT
		 */
		System.out.println();
		System.out.println("Started CORPUS text.");
		JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
		StringBuilder corpus_eng = new StringBuilder();
		StringBuilder corpus_chr = new StringBuilder();
		List<String> mdef = new ArrayList<>();
		Set<String> already = new HashSet<>();
		already.clear();
		int count = definitions.size();
		int percent = -1;
		int counter = 0;
		for (LyxEntry entry : definitions) {
			counter++;
			if (counter * 100 / count != percent) {
				percent = counter * 100 / count;
				System.out.print(percent + "% ");
			}
			mdef.clear();
			mdef.addAll(Arrays.asList(StringUtils.split(entry.definition, ";")));
			ListIterator<String> ldef = mdef.listIterator();
			while (ldef.hasNext()) {
				String subdef = StringUtils.strip(ldef.next());
				if (subdef.startsWith("Genus:")) {
					ldef.remove();
					continue;
				}
				if (subdef.startsWith("Family:")) {
					ldef.remove();
					continue;
				}
			}
			Collections.sort(mdef);
			ldef = mdef.listIterator();
			while (ldef.hasNext()) {
				String subdef = StringUtils.strip(ldef.next());
				if (subdef.startsWith("It is (")) {
					subdef = subdef.replaceAll("It is (\\(.*?\\))\\s*(.*)", "$2 $1");
				}
				subdef = subdef.replace("She is ", "");
				subdef = subdef.replace("He is ", "");
				subdef = subdef.replace("It is ", "");
				subdef = subdef.replace("They are ", "");
				subdef = subdef.replace("His ", "");
				subdef = subdef.replace("Her ", "");
				/*
				 * hide classifiers
				 */
				subdef = subdef.replace("something solid", "it");
				subdef = subdef.replace("something flexible", "it");
				subdef = subdef.replace("something alive", "it");
				subdef = subdef.replace("something liquid", "it");
				subdef = subdef.replace("something long", "it");
				subdef = subdef.replace("something in", "it in");
				subdef = subdef.replace("something thrown or chased", "it");
				subdef = subdef.replace("something for", "it for");
				Iterator<String> isyl = entry.getSyllabary().iterator();
				/*
				 * pos 1 = 3rd person continous, pos 2 = 1st person continuous,
				 * pos 2 = remote past, pos 3 = habitual, pos 4 = imperative,
				 * pos 5 = deverbal
				 */
				int pos = 0;
				while (isyl.hasNext()) {
					String tmp = subdef;
					List<DefSyl> tmp_def = new ArrayList<>();
					String str_syl = StringUtils.strip(isyl.next());
					pos++;
					if (pos == 2 && !entry.pos.startsWith("v")) {
						break;
					}
					if (str_syl.startsWith("-") || StringUtils.isBlank(str_syl)) {
						continue;
					}
					if (!str_syl.matches("^[Ꭰ-Ᏼ ]+$")) {
						continue;
					}
					switch (pos) {
					case 1:// 3rd continous
						tmp_def.add(new DefSyl(str_syl, tmp));
						if (!str_syl.matches("^[ᎤᏚ].*")) {
							tmp_def.addAll(pronouns_intransitive_a(str_syl, subdef));
							tmp_def.addAll(pronouns_transitive_a(str_syl, subdef));
						}
						break;
					case 2:// 1st person
							// tmp = subdef.replaceAll("^(He|She) is ", "I am
							// ");
							// tmp_def.add(new DefSyl(str_syl, tmp));
						break;
					case 3:// remote past
						tmp_def.addAll(pronouns_intransitive_b(str_syl, subdef));
						tmp_def.addAll(pronouns_transitive_b(str_syl, subdef));

						String rpast = subdef.replaceAll("^([a-zA-Z]+)ing\\b", "$1ed");
						if (rpast.equals(subdef)) {
							rpast = "was " + subdef;
						}
						tmp_def.add(new DefSyl(str_syl, rpast));
						tmp_def.add(new DefSyl(str_syl.replaceAll("Ꭲ$", ""), rpast));

						tmp_def.add(new DefSyl("Ꮒ-" + str_syl + "-ᎣᎢ", "had already " + rpast));
						tmp_def.add(new DefSyl("Ꮒ-" + str_syl + "-ᎥᎾ", "without " + subdef));
						tmp_def.add(new DefSyl("Ꮒ-" + str_syl + "-ᎥᎾ ᎨᏎ", "was without " + subdef));
						tmp_def.add(new DefSyl("Ꮒ-" + str_syl + "-ᎥᎾ ᎨᏎᏍᏗ", "will be without " + subdef));

						tmp_def.add(new DefSyl(str_syl.replaceAll("Ꭲ$", "²Ꭲ"), "later let be " + subdef));

						// AGAIN
						tmp_def.add(new DefSyl(str_syl + "-ᎢᏏᎭ", "again is " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎢᏌ", "let again be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎢᏌ²", "just now " + rpast));
						tmp_def.add(new DefSyl(str_syl + "-ᎢᏏᏍᎪᎢ", "often is again " + subdef));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1did again $2$3");
						tmp_def.add(new DefSyl(str_syl + "-ᎢᏌᏅᎢ", rpast + " again"));
						tmp_def.add(new DefSyl("Ꮣ-" + str_syl + "-ᎢᏌᏂ", "will be " + subdef + " again"));
						tmp_def.add(new DefSyl(str_syl + "-ᎢᏐᏗ", "for " + subdef + " again"));

						// BENEFACTIVE
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎭ", subdef + " for another"));
						tmp_def.add(new DefSyl(str_syl + "-Ꮟ", "let be " + subdef + " for another"));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎵ²", "just now " + rpast + " for another"));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎰᎢ", "usually " + subdef + " for another"));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎸᎢ", rpast + " for another"));
						tmp_def.add(new DefSyl("Ꮣ-" + str_syl + "-ᎡᎵ", "will be " + subdef + " for another"));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᏗ", "for " + subdef + " for another"));
						// going to do
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎦ", "coming to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎦ", "going to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎤᎦ", "let come to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎤᎦ", "let go to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎤᎦ²", "just now came to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎤᎦ²", "just now went to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎪᎢ", "comes to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎪᎢ", "goes to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎥᏒᎢ", "came to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎥᏒᎢ", "went to be " + subdef));
						tmp_def.add(new DefSyl("Ꮣ-" + str_syl + "-ᎡᏏ", "will come to be " + subdef));
						tmp_def.add(new DefSyl("Ꮣ-" + str_syl + "-ᎡᏏ", "will go to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎥᏍᏗ", "to come to be " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎥᏍᏗ", "to go to be " + subdef));
						// going and doing
						tmp_def.add(new DefSyl("Ᏹ-" + str_syl + "-ᎡᎾ", "when goes he is " + subdef));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎾ", "went and " + rpast));
						tmp_def.add(new DefSyl(str_syl + "-ᎡᎾ*", "will go and will be " + subdef));
						break;
					case 4:
						String rhabit = subdef.replaceAll("^([a-zA-Z]+)ing\\b", "$1es");
						tmp_def.add(new DefSyl(str_syl, "often " + subdef));
						tmp_def.add(new DefSyl(str_syl.replaceAll("Ꭲ$", ""), "often " + subdef));
						tmp_def.add(new DefSyl(str_syl, "usually " + subdef));
						if (!rhabit.equals(subdef)) {
							tmp_def.add(new DefSyl(str_syl, rhabit));
							tmp_def.add(new DefSyl(str_syl.replaceAll("Ꭲ$", ""), rhabit));
						}
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+ing)\\b", "will already have been $1");
						tmp_def.add(new DefSyl("Ꮒ-" + str_syl + "-ᎡᏍᏗ", "will already have been " + subdef));
						break;
					case 5:
						tmp_def.add(new DefSyl(str_syl + "²", "just was " + subdef));
						tmp_def.add(new DefSyl(str_syl + "²", "recently was " + subdef));
						tmp_def.add(new DefSyl(str_syl, "let be " + subdef));
						break;
					case 6:
						tmp_def.add(new DefSyl(str_syl, "to be " + subdef));
						tmp_def.add(new DefSyl(str_syl, "for " + subdef));
						break;
					}
					for (DefSyl def : tmp_def) {
						List<RuleMatch> lt = langTool.check(def.def);
						for (RuleMatch match : lt) {
							int from = match.getFromPos();
							int to = match.getToPos();
							if (match.getSuggestedReplacements().size() > 0) {
								def.def = StringUtils.left(def.def, from) + match.getSuggestedReplacements().get(0)
										+ StringUtils.substring(def.def, to);
							}
						}
						if (already.contains(def.syl + def.def)) {
							continue;
						}
						already.add(def.syl + def.def);
						corpus_chr.append(def.syl);
						corpus_chr.append("\n");
						corpus_eng.append(def.def);
						corpus_eng.append("\n");
					}
				}
			}
		}
		System.out.println();
		FileUtils.writeStringToFile(new File("output/corpus.chr3"), corpus_chr.toString());
		FileUtils.writeStringToFile(new File("output/corpus.en"), corpus_eng.toString());
		corpus_chr.setLength(0);
		corpus_eng.setLength(0);
		System.out.println("Finished CORPUS text.");
		System.out.println();
	}

	private Collection<? extends DefSyl> pronouns_transitive_a(String str_syl, String subdef) {
		return new ArrayList<DefSyl>();
	}

	private Collection<? extends DefSyl> pronouns_intransitive_a(String str_syl, String subdef) {
		return new ArrayList<DefSyl>();
	}

	private Collection<? extends DefSyl> pronouns_transitive_b(String str_syl, String subdef) {
		return new ArrayList<DefSyl>();
	}

	private Collection<? extends DefSyl> pronouns_intransitive_b(String str_syl, String subdef) {
		return new ArrayList<DefSyl>();
	}

	private final String lyxfile;

	private String dateModified;

	private String revisionNumber;

	private String formattedIsbn;

	private String author;

	/**
	 * 
	 * is this a "splittable" definition?
	 */
	private List<EnglishCherokee> getSplitsFor(EnglishCherokee ec) {
		List<EnglishCherokee> list = new ArrayList<>();
		splitAndAdd: {
			// keep this one first
			String definition = ec.getDefinition();
			if (definition.contains(";") || StringUtils.containsAny(definition, Consts.definitionMarkers)) {
				String defs[] = definition.split(Consts.splitRegex);
				for (String adef : defs) {
					if (StringUtils.isBlank(adef)) {
						continue;
					}
					adef = StringUtils.strip(adef);
					EnglishCherokee ec_split = new EnglishCherokee(ec);
					ec_split.setEnglish(adef);
					list.addAll(getSplitsFor(ec_split));
				}
				break splitAndAdd;
			}
			list.add(ec);
		}
		list.forEach(entry -> {
			String english = entry.getEnglish();
			if (english.startsWith("Biol. ")) {
				english = StringUtils.substringAfter(english, "Biol. ");
				entry.setItalic(true);
			}
			if (english.startsWith("biol. ")) {
				english = StringUtils.substringAfter(english, "biol. ");
				entry.setItalic(true);
			}
			english = english.replaceAll("\\\\[a-zA-Z]+ [a-zA-Z]+", "");
			entry.setEnglish(english);
		});
		return list;
	}

	public static class DefSyl {
		public String syl;
		public String def;

		public DefSyl() {
		}

		public DefSyl(String syl, String def) {
			this.syl = syl;
			this.def = def;
		}
	}

	public void setPreface(String preface_raw_lyx) {
		this.preface = preface_raw_lyx;
	}

	public void setGrammar(String grammar_raw_lyx) {
		this.grammar = grammar_raw_lyx;
	}

	public void setRevision(String revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}

	public void setIsbn(String formattedIsbn) {
		this.formattedIsbn = formattedIsbn;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}

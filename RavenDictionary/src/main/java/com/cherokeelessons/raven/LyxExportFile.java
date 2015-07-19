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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

public class LyxExportFile extends Thread {
	private static final String sloppy_begin = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status collapsed\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "begin{sloppy}\n" + "\\end_layout\n" + "\n" + "\\end_inset\n"
			+ "\n" + "\n" + "\\end_layout\n\n";

	private static final String sloppy_end = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status collapsed\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "end{sloppy}\n" + "\\end_layout\n" + "\n" + "\\end_inset\n"
			+ "\n" + "\n" + "\\end_layout\n\n";

	private static final String columnsep_large = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnsep}{20pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n" + "\n";
	private static final String columnsep_normal = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnsep}{10pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n" + "\n";
	private static final String seprule_on = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnseprule}{0.5pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n";
	private static final String seprule_off = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status open\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "setlength{\n" + "\\backslash\n" + "columnseprule}{0pt}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n";
	private static final String MULTICOLS_END = "\\begin_layout Standard\n"
			+ "\\begin_inset ERT\n" + "status collapsed\n" + "\n"
			+ "\\begin_layout Plain Layout\n" + "\n" + "\n" + "\\backslash\n"
			+ "end{multicols}\n" + "\\end_layout\n" + "\n" + "\\end_inset\n"
			+ "\n" + "\n" + "\\end_layout\n";
	private static final String MULTICOLS_BEGIN = "\\begin_layout Standard\n"
			+ "\n" + "\\lang english\n" + "\\begin_inset ERT\n"
			+ "status collapsed\n" + "\n" + "\\begin_layout Plain Layout\n"
			+ "\n" + "\n" + "\\backslash\n" + "begin{multicols}{2}\n"
			+ "\\end_layout\n" + "\n" + "\\end_inset\n" + "\n" + "\n"
			+ "\\end_layout\n";
	private static final String Chapter_Dictionary = "\\begin_layout Chapter\n"
			+ "Dictionary\n" + "\\end_layout\n";
	private static final String Chapter_WordForms = "\\begin_layout Chapter\n"
			+ "Word Form Lookup\n" + "\\end_layout\n";
	private static final String Chapter_English = "\\begin_layout Chapter\n"
			+ "English to Cherokee Lookup\n" + "\\end_layout\n";

	private final List<IEntry> entries;

	private boolean doWordForms = true;

	public LyxExportFile(List<IEntry> entries, String destfile) {
		this.entries = entries;
		this.lyxfile = destfile;
	}

	@Override
	public void run() {
		try {
			_run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void _run() throws IOException {
		final List<LyxEntry> definitions = new ArrayList<LyxEntry>();

		String start = IOUtils.toString(getClass().getResourceAsStream(
				"/net/cherokeedictionary/lyx/LyxDocumentStart.txt"));

		String end = IOUtils.toString(getClass().getResourceAsStream(
				"/net/cherokeedictionary/lyx/LyxDocumentEnd.txt"));

		for (IEntry entry : entries) {
			if (entry.getType().equals("v")) {
				VerbEntry v = new VerbEntry();
				v.definition = entry.formattedDefinition();
				v.pos = "v";
				/*
				 * it is ASSUMED that vbst generated entries have five forms and
				 * that they are in proper order
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
		List<String> maybe_dupe = new ArrayList<>();
		Iterator<LyxEntry> ientry = definitions.iterator();
		LyxEntry d1 = ientry.next();
		while (ientry.hasNext()) {
			LyxEntry d2 = ientry.next();
			if (!d1.getSyllabary().get(0).equals(d2.getSyllabary().get(0))) {
				d1 = d2;
				continue;
			}
			if (!d1.definition.equals(d2.definition)) {
				maybe_dupe.add("Possible Duplicate: "
						+ d2.getSyllabary().get(0) + " | " + d1.definition
						+ " | " + d2.definition);
				d1 = d2;
				continue;
			}
			if (!d1.getPronunciations().get(0)
					.equals(d2.getPronunciations().get(0))) {
				App.info("Likely Duplicate: " + d2.getSyllabary().get(0) + " "
						+ d2.definition);
				d1 = d2;
				continue;
			}
			App.info("Likely Duplicate: " + d2.getSyllabary().get(0) + " "
					+ d2.definition);
			ientry.remove();
		}
		for (String e : maybe_dupe) {
			App.info(e);
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
		App.info("Pre-combined English to Cherokee entries: "
				+ nf.format(english.size()));
		for (int ix = 1; ix < english.size(); ix++) {
			if (english.get(ix - 1).equals(english.get(ix))) {
				english.remove(ix);
				ix--;
			}
		}
		App.info("Deduped English to Cherokee entries: "
				+ nf.format(english.size()));
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
		App.info("Post-combined English to Cherokee entries: "
				+ nf.format(english.size()));

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
					primary_entry = StringUtils.substringBefore(primary_entry,
							",");
				}
				primary_entry = StringUtils.strip(primary_entry);
				if (next instanceof HasStemmedForms) {
					List<StemEntry> stems = (((HasStemmedForms) next)
							.getStems());
					if (stems.size() != 0) {
						list.clear();
					}
					/*
					 * add stems directly to wordforms list
					 */
					for (StemEntry entry : stems) {
						if (StringUtils.isBlank(entry.syllabary.replaceAll(
								"[^Ꭰ-Ᏼ]", ""))) {
							continue;
						}
						WordForm wf = new WordForm();
						wf.references.add(new Reference(primary_entry, "",
								next.id));
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
						if (StringUtils.isBlank(syllabary.replaceAll("[^Ꭰ-Ᏼ]",
								""))) {
							continue;
						}
						WordForm wf = new WordForm();
						wf.stemEntry.syllabary = syllabary;
						wf.references.add(new Reference(primary_entry, "",
								next.id));
						wf.stemEntry = new StemEntry(syllabary, StemType.Other);
						wordforms.add(wf);
					}
				}
			}
			Collections.sort(wordforms);
			App.info("Pre-combined and pre-deduped Wordform entries: "
					+ nf.format(wordforms.size()));
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
			App.info("Post-combined Wordform entries: "
					+ nf.format(wordforms.size()));
		}

		StringBuilder sb = new StringBuilder();

		/*
		 * Start of Book including all front matter
		 */
		sb.append(start);
		/*
		 * Cherokee Dictionary
		 */
		sb.append(Chapter_Dictionary + columnsep_large + seprule_on
				+ MULTICOLS_BEGIN + sloppy_begin);
		String prevSection = "";
		for (LyxEntry entry : definitions) {
			String syll = StringUtils.left(
					entry.getLyxCode().replaceAll("[^Ꭰ-Ᏼ]", ""), 1);
			if (!syll.equals(prevSection)) {
				prevSection = syll;
				sb.append("\\begin_layout Section\n");
				sb.append(syll);
				sb.append("\\end_layout\n");
			}
			sb.append(entry.getLyxCode().replace("\\n", " "));
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
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off + columnsep_normal);

		/*
		 * English to Cherokee
		 */
		sb.append(Chapter_English + columnsep_large + seprule_on
				+ MULTICOLS_BEGIN + sloppy_begin);
		prevSection = "";
		for (EnglishCherokee entry : english) {
			String eng = StringUtils.left(entry.getDefinition(), 1)
					.toUpperCase();
			if (!eng.equals(prevSection)) {
				prevSection = eng;
				sb.append("\\begin_layout Section\n");
				sb.append(eng.toUpperCase());
				sb.append("\\end_layout\n");
			}
			sb.append(entry.getLyxCode(true));
		}
		sb.append(sloppy_end + MULTICOLS_END + seprule_off + columnsep_normal);

		/*
		 * Wordform Lookup
		 */
		if (doWordForms) {
			sb.append(Chapter_WordForms + columnsep_large + seprule_on
					+ MULTICOLS_BEGIN + sloppy_begin);
			prevSection = "";
			for (WordForm entry : wordforms) {
				if (entry.stemEntry.syllabary.contains(" ")) {
					continue;
				}
				String syll = StringUtils.left(entry.stemEntry.syllabary, 1);
				if (!syll.equals(prevSection)) {
					prevSection = syll;
					sb.append("\\begin_layout Section\n");
					sb.append(syll);
					sb.append("\\end_layout\n");
				}
				sb.append(entry.getLyxCode());
			}
			sb.append(sloppy_end + MULTICOLS_END + seprule_off
					+ columnsep_normal);
		}

		sb.append(end);
		FileUtils.writeStringToFile(new File(lyxfile), sb.toString(), "UTF-8",
				false);

		corpusWriter(definitions);
		
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
		FileUtils.writeStringToFile(new File("output/wordforms.txt"),
				sbwf.toString(), "UTF-8");
		System.out.println("Wrote wordforms.txt");
	}

	private void corpusWriter(final List<LyxEntry> definitions)
			throws IOException {
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
		int counter=0;
		for (LyxEntry entry : definitions) {
			counter++;
			if (counter*100/count != percent) {
				percent = counter*100/count;
				System.out.print(percent+"% ");
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
				if (subdef.startsWith("He ")) {
					ldef.add(subdef.replace("He ", "She "));
					ldef.previous();
				}
				if (subdef.matches(".* him[^a-zA-Z]*?$")) {
					ldef.add(subdef
							.replaceAll("( him)([^a-zA-Z]*?)$", " her$2"));
					ldef.previous();
				}
			}
			Collections.sort(mdef);
			ldef = mdef.listIterator();
			while (ldef.hasNext()) {
				String subdef = StringUtils.strip(ldef.next());
				if (subdef.startsWith("It is (")){
					subdef = subdef.replaceAll("It is (\\(.*?\\))\\s*(.*)", "It is $2 $1");
				}
				Iterator<String> isyl = entry.getSyllabary().iterator();
				/*
				 * pos 1 = 3rd person continous, pos 2 = 1st person continuous,
				 * pos 2 = remote past, pos 3 = habitual, pos 4 = imperative,
				 * pos 5 = deverbal
				 */
				int pos = 0;
				while (isyl.hasNext()) {
					List<DefSyl> tmp_def=new ArrayList<>();
					String tmp = subdef;
					String str_syl = StringUtils.strip(isyl.next());
					pos++;
					if (pos == 2 && !entry.pos.startsWith("v")) {
						break;
					}
					if (str_syl.startsWith("-") || StringUtils.isBlank(str_syl)) {
						continue;
					}
					if (!str_syl.matches("^[Ꭰ-Ᏼ ]+$")){
						continue;
					}
					switch (pos) {
					case 1:// 3rd continous
						tmp_def.add(new DefSyl(str_syl, tmp));
						if (!str_syl.matches("^[ᎤᏚ].*")){
							tmp_def.addAll(pronouns_intransitive_a(str_syl, subdef));
							tmp_def.addAll(pronouns_transitive_a(str_syl, subdef));
						}
						break;
					case 2:// 1st person
						tmp = subdef.replaceAll("^(He|She) is ", "I am ");
						tmp_def.add(new DefSyl(str_syl, tmp));
						break;
					case 3://remote past
						tmp_def.addAll(pronouns_intransitive_b(str_syl, subdef));
						tmp_def.addAll(pronouns_transitive_b(str_syl, subdef));
						
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+)ing\\b", "$1ed");
						tmp_def.add(new DefSyl(str_syl, tmp));
						tmp_def.add(new DefSyl(str_syl.replaceAll("Ꭲ$", ""), tmp));
						
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl, tmp+" while next to"));
						
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+)(ing)?\\b", "had already $1ed");
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl+" -ᎣᎢ", tmp));
						
						tmp = subdef.replaceAll("^.*?is ([a-zA-Z]+\\b)(.*?)", "Without $1$2");
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl+" -ᎥᎾ", tmp));
						
						tmp = subdef.replaceAll("^.*?is ([a-zA-Z]+\\b)(.*?)", "Was without $1$2");
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl+" -ᎥᎾ ᎨᏎ", tmp));
						
						tmp = subdef.replaceAll("^.*?is ([a-zA-Z]+\\b)(.*?)", "Did without $1$2");
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl+" -ᎥᎾ ᎨᏎ", tmp));
						
						tmp = subdef.replaceAll("^.*?is ([a-zA-Z]+\\b)(.*?)", "Will be without $1$2");
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl+" -ᎥᎾ ᎨᏎᏍᏗ", tmp));
						
						tmp = subdef.replaceAll("^.*?is ([a-zA-Z]+)\\b(.*?)", "$1$2");
						tmp_def.add(new DefSyl(str_syl+" -Ꭵ⁴Ꭲ", tmp));
						
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "Later let $1be $2$3");
						tmp_def.add(new DefSyl(str_syl+" -Ꭵ²Ꭲ", tmp));
						
						//AGAIN
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is again $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎢᏏᎭ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "Let $1be again $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎢᏌ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is just now again $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎢᏌ²", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1often is again $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎢᏏᏍᎪᎢ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1did again $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎢᏌᏅᎢ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1will do again $2$3");
						tmp_def.add(new DefSyl("Ꮣ- " + str_syl+" -ᎢᏌᏂ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "For $1to do again $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎢᏐᏗ", tmp));
						//BENEFACTIVE
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is $2$3 for another");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᎭ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "let $1be $2$3 for another");
						tmp_def.add(new DefSyl(str_syl+" -Ꮟ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is just now $2$3 for another");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᎵ²", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is often $2$3 for another");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᎰᎢ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1did $2$3 for another");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᎸᎢ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1will be $2$3 for another");
						tmp_def.add(new DefSyl("Ꮣ- " + str_syl+" -ᎡᎵ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "For $1to do $2$3 for another");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᏗ", tmp));
						//going to do
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is going there to be $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᎦ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "Let $1be going there to be $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎤᎦ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is just now going there to be $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎤᎦ²", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1is often going there to be $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᎪᎢ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1went there to $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎥᏒᎢ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "$1will go there to $2$3");
						tmp_def.add(new DefSyl("Ꮣ- " + str_syl+" -ᎡᏏ", tmp));
						tmp = subdef.replaceAll("^(.*?)is ([a-zA-Z]+ing)\\b(.*?)", "For $1to go there to $2$3");
						tmp_def.add(new DefSyl(str_syl+" -ᎥᏍᏗ", tmp));
						//going and doing
						tmp = subdef.replaceAll("^(.*?) is ([a-zA-Z]+ing)\\b(.*?)", "When $1 goes there, $1 $2$3");
						tmp_def.add(new DefSyl("Ᏹ- " + str_syl+" -ᎡᎾ", tmp));
						tmp = subdef.replaceAll("^(.*?) is ([a-zA-Z]+ing)\\b(.*?)", "$1 went there and did $2$3");
						tmp_def.add(new DefSyl("Ᏹ- " + str_syl+" -ᎡᎾ", tmp));
						tmp = subdef.replaceAll("^(.*?) is ([a-zA-Z]+ing)\\b(.*?)", "$1 will go there and will be $2$3");
						tmp_def.add(new DefSyl(" [*] " + str_syl+" -ᎡᎾ", tmp));
						break;
					case 4:
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+ing)\\b", "often is $1");
						tmp_def.add(new DefSyl(str_syl, tmp));
						tmp_def.add(new DefSyl(str_syl.replaceAll("Ꭲ$", ""), tmp));
						
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl, tmp+" while next to"));
						
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+ing)\\b", "was $1");
						tmp_def.add(new DefSyl(str_syl+" -ᎥᎢ", tmp));
						
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+ing)\\b", "will be $1");
						tmp_def.add(new DefSyl(str_syl+" -ᎡᏍᏗ", tmp));
						
						tmp = subdef.replaceAll("\\bis ([a-zA-Z]+ing)\\b", "will already have been $1");
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl+" -ᎡᏍᏗ", tmp));
						break;
					case 5:
						//just now
						if (subdef.startsWith("it is")){
							tmp = subdef.replaceAll("^It is ([a-zA-Z]+ing)\\b", "It just was $1");
						} else {
							tmp = subdef.replaceAll("^(He|She) is ([a-zA-Z]+ing)\\b", "$1 just was $2");
						}
						tmp_def.add(new DefSyl(str_syl+"²", tmp));
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl, tmp+" while next to"));
						//let be
						if (subdef.startsWith("it is")){
							tmp = subdef.replaceAll("^It is ([a-zA-Z]+ing)\\b", "Let it be $1");
						} else {
							tmp = subdef.replaceAll("^(He|She) is ([a-zA-Z]+ing)\\b", "Let $1 be $2");
						}
						tmp_def.add(new DefSyl(str_syl, tmp));
						tmp_def.add(new DefSyl("Ꮒ- "+str_syl, tmp+" while next to"));
						break;
					case 6:
						if (subdef.startsWith("it is")){
							tmp = subdef.replaceAll("^It is\\b", "to be");
						} else {
							tmp = subdef.replaceAll("^(He|She) is\\b", "to be");
						}
						tmp_def.add(new DefSyl(str_syl, tmp));
						if (subdef.startsWith("it is")){
							tmp = subdef.replaceAll("^It is\\b", "It needs to be");
						} else {
							tmp = subdef.replaceAll("^(He|She) is\\b", "$1 needs to be");
						}
						tmp_def.add(new DefSyl(str_syl, tmp));
						if (subdef.startsWith("it is")){
							tmp = subdef.replaceAll("^It is\\b", "It must ");
						} else {
							tmp = subdef.replaceAll("^(He|She) is\\b", "$1 must ");
						}
						tmp_def.add(new DefSyl("ᎠᏎ "+str_syl, tmp));
						//Currently Able
						if (subdef.startsWith("it is")){
							tmp = subdef.replaceAll("^It is\\b", "able to");
						} else {
							tmp = subdef.replaceAll("^(He|She) is\\b", "able to");
						}
						tmp_def.add(new DefSyl("ᎬᎩ- "+str_syl, "I am "+tmp));
						tmp_def.add(new DefSyl("ᎦᏣ- "+str_syl, "You are "+tmp));
						tmp_def.add(new DefSyl("ᎦᏍᏗ- "+str_syl, "You two are "+tmp));
						tmp_def.add(new DefSyl("ᎦᎩᏂ- "+str_syl, "You and I are "+tmp));
						tmp_def.add(new DefSyl("ᎦᏥ- "+str_syl, "You all are "+tmp));
						tmp_def.add(new DefSyl("ᎦᏲᎩᏂ- "+str_syl, "He and I are "+tmp));
						tmp_def.add(new DefSyl("ᎦᏲᎩ- "+str_syl, "They and I are "+tmp));
						tmp_def.add(new DefSyl("ᎬᏩ- "+str_syl, "He is "+tmp));
						tmp_def.add(new DefSyl("ᎬᏩ- "+str_syl, "She is "+tmp));
						tmp_def.add(new DefSyl("ᎬᏩᏂ- "+str_syl, "They are "+tmp));
						break;
					}
					for (DefSyl def: tmp_def) {
						def.def=def.def.replace("Let He ", "Let him ");
						def.def=def.def.replace("Let She ", "Let her ");
						def.def=def.def.replace("Later let He ", "Later let him ");
						def.def=def.def.replace("Later let She ", "Later let her ");
						def.def=def.def.replace("For He to ", "For him to ");
						def.def=def.def.replace("For She to ", "For her to ");
						List<RuleMatch> lt = langTool.check(def.def);
						for (RuleMatch match:lt) {
							int from = match.getFromPos();
							int to = match.getToPos();
							if (match.getSuggestedReplacements().size()>0) {
								def.def = StringUtils.left(def.def, from)
										+ match.getSuggestedReplacements().get(0)
										+ StringUtils.substring(def.def, to);
							}
						}
						if (already.contains(def.syl + def.def)) {
							continue;
						}
						already.add(def.syl+def.def);
						corpus_chr.append(def.syl);
						corpus_chr.append("\n");
						corpus_eng.append(def.def);
						corpus_eng.append("\n");
					}
				}
			}
		}
		System.out.println();
		FileUtils.writeStringToFile(new File("output/corpus_chr.txt"),
				corpus_chr.toString());
		FileUtils.writeStringToFile(new File("output/corpus_en.txt"),
				corpus_eng.toString());
		corpus_chr.setLength(0);
		corpus_eng.setLength(0);
		System.out.println("Finished CORPUS text.");
		System.out.println();
	}

	private Collection<? extends DefSyl> pronouns_transitive_a(String str_syl,
			String subdef) {
		return new ArrayList<DefSyl>();
	}

	private Collection<? extends DefSyl> pronouns_intransitive_a(
			String str_syl, String subdef) {
		return new ArrayList<DefSyl>();
	}

	private Collection<? extends DefSyl> pronouns_transitive_b(String str_syl,
			String subdef) {
		return new ArrayList<DefSyl>();
	}

	private Collection<? extends DefSyl> pronouns_intransitive_b(
			String str_syl, String subdef) {
		return new ArrayList<DefSyl>();
	}

	private final String lyxfile;

	/*
	 * is this a "splittable" definition?
	 */
	private List<EnglishCherokee> getSplitsFor(EnglishCherokee ec) {
		List<EnglishCherokee> list = new ArrayList<>();
		splitAndAdd: {
			// keep this one first
			String definition = ec.getDefinition();
			if (definition.contains(";")) {
				String defs[] = definition.split(";");
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
		return list;
	}
	
	public static class DefSyl {
		public String syl;
		public String def;
		public DefSyl() {
		}
		public DefSyl(String syl, String def) {
			this.syl=syl;
			this.def=def;
		}
	}
}

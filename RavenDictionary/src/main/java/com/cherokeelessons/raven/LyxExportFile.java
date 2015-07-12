package com.cherokeelessons.raven;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
			multi.definition=entry.formattedDefinition();
			multi.pos=entry.getType();
			Iterator<String> ilatin = entry.getPronunciations().iterator();
			Iterator<String> isyll = entry.getSyllabary().iterator();
			while (ilatin.hasNext()&&isyll.hasNext()) {
				DefinitionLine def = new DefinitionLine();
				def.pronounce=ilatin.next();
				def.syllabary=isyll.next();
				multi.addDefinition(def);
			}
			definitions.add(multi);
		}
		
		int iid=0;
		for (LyxEntry e: definitions) {
			e.id=++iid;
		}

		Collections.sort(definitions);
		
		/*
		 * Any duplicates?
		 */
		Iterator<LyxEntry> ientry = definitions.iterator();
		LyxEntry d1=ientry.next();
		while (ientry.hasNext()) {
			LyxEntry d2=ientry.next();
			if (!d1.getSyllabary().get(0).equals(d2.getSyllabary().get(0))) {
				d1=d2;
				continue;
			}
			if (!d1.getPronunciations().get(0).equals(d2.getPronunciations().get(0))){
				d1=d2;
				continue;
			}
			if (!d1.definition.equals(d2.definition)){
				d1=d2;
				continue;
			}
			App.info("Removing Duplicate: "+d2.getSyllabary().get(0)+" "+d2.definition);
			ientry.remove();
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
					adef=StringUtils.strip(adef);
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
}

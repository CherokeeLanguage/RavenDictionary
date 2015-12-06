package com.cherokeelessons.raven;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class Entry implements IEntry {
	private static final boolean eastern_ts = true;
	private static final boolean show_transforms = false;
	private static final boolean show_deletes = false;

	private String def = "";
	@Override
	public String getDef() {
		return def;
	}
	@Override
	public void setDef(String def) {
		this.def = def;
	}

	private String type = "";
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private String genus = "";
	@Override
	public String getGenus() {
		return genus;
	}
	@Override
	public void setGenus(String genus) {
		this.genus = genus;
	}

	private final List<String> list = new ArrayList<>();

	public void add(String l) {
		list.add(l);
	}

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#size()
	 */
	@Override
	public int size() {
		return list.size();
	}

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#getPronunciations()
	 */
	@Override
	public List<String> getPronunciations() {
		List<String> tmp = new ArrayList<String>();
		for (String l : list) {
			if (!show_deletes) {
				l = l.replaceAll("[aeiouvạẹịọụṿ]"
						+ SyllabaryConverter.UpperMark, "");
				l = l.replace("dh", "t");
				l = l.replace("gh", "k");
			}
			if (show_transforms) {
				tmp.add(l);
				continue;
			}
			l = l.replace("s" + SyllabaryConverter.UpperMark, "s");
			l = l.replaceAll("¹h" + SyllabaryConverter.UpperMark, "¹");
			l = l.replaceAll("(?<=[hɂ])" + SyllabaryConverter.UpperMark, "");
			l = l.replaceAll("(?<=[aeiouvạẹịọụṿ])h(?=s)", "");
			if (!show_deletes && !show_transforms) {
				l = l.replace(SyllabaryConverter.UpperMark, "");
			}
			if (eastern_ts) {
				l = l.replace("j", "ts");
			}
			tmp.add(l);
		}
		return tmp;
	}

	private String _sortKey;

	@Override
	public String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			for (String l : getSyllabary()) {
				sb.append(l.replaceAll("[^Ꭰ-Ᏼ]", "") + "-");
			}
			for (String l : list) {
				sb.append(l + "-");
			}
			sb.append(list.size());
			_sortKey = sb.toString();
		}
		return _sortKey;
	}

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#compareTo(com.cherokeelessons.raven.Entry)
	 */
	@Override
	public int compareTo(IEntry o) {
		return sortKey().compareTo(o.sortKey());
	}

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IEntry)) {
			return false;
		}
		return compareTo((Entry) obj) == 0;
	}

	private boolean withSyllabary = true;

	public boolean isWithSyllabary() {
		return withSyllabary;
	}

	public void setWithSyllabary(boolean withSyllabary) {
		this.withSyllabary = withSyllabary;
	}

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#getSyllabary()
	 */
	@Override
	public List<String> getSyllabary() {
		List<String> list = new ArrayList<>();
		SyllabaryGuesser syll = new SyllabaryGuesser();
		Iterator<String> ilist = this.list.iterator();
		boolean invalid=false;
		while (ilist.hasNext()) {
			String pronunciation = ilist.next();
			if (StringUtils.isBlank(pronunciation)) {
				continue;
			}
			if (pronunciation.startsWith("-")) {
				list.add(pronunciation);
				continue;
			}
			if (pronunciation.startsWith("@")) {
				list.add(pronunciation);
				continue;
			}
			if (pronunciation.startsWith("IRR")) {
				list.add(pronunciation);
				continue;
			}
			String s = syll.get(pronunciation);
			invalid = invalid | s.contains("[");
			list.add(s);
		}
		if (invalid) {
			syll.reprocess(list);
		}
		return list;
	}

	@Override
	public String toString() {
		SyllabaryGuesser syll = new SyllabaryGuesser();
		StringBuilder sb = new StringBuilder();
		Iterator<String> ilist = list.iterator();
		if (ilist.hasNext()) {
			String pronunciation = ilist.next();
			reformat: {
				if (pronunciation.startsWith("-")) {
					sb.append(pronunciation);
					sb.append("\n");
					break reformat;
				}
				if (pronunciation.startsWith("@")) {
					sb.append(pronunciation);
					sb.append("\n");
					break reformat;
				}
				if (pronunciation.startsWith("IRR")) {
					sb.append(pronunciation);
					sb.append("\n");
					break reformat;
				}
				if (isWithSyllabary()) {
					sb.append(syll.get(pronunciation));
					sb.append(" [");
					sb.append(pronunciation);
					sb.append("]");
				} else {
					sb.append(pronunciation);
				}
			}
			sb.append(" - (" + type + ") ");
			sb.append(formattedDefinition());
			sb.append("\n");
			while (ilist.hasNext()) {
				pronunciation = ilist.next();
				if (StringUtils.isBlank(pronunciation)) {
					continue;
				}
				if (pronunciation.startsWith("-")) {
					sb.append(pronunciation);
					sb.append("\n");
					continue;
				}
				if (pronunciation.startsWith("@")) {
					sb.append(pronunciation);
					sb.append("\n");
					continue;
				}
				if (pronunciation.startsWith("IRR")) {
					sb.append(pronunciation);
					sb.append("\n");
					continue;
				}
				if (isWithSyllabary()) {
					sb.append(syll.get(pronunciation));
					sb.append(" [");
					sb.append(pronunciation);
					sb.append("]");
				} else {
					sb.append(pronunciation);
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#formattedDefinition()
	 */
	@Override
	public String formattedDefinition() {
		StringBuilder sb = new StringBuilder();
		String[] subdefs = StringUtils.split(def, ";");
		boolean additional = false;
		for (String subdef : subdefs) {
			if (StringUtils.isBlank(subdef)) {
				continue;
			}
			if (additional) {
				sb.append("; ");
			}
			subdef = StringUtils.normalizeSpace(subdef);
			subdef = StringUtils.strip(subdef);
			sb.append(StringUtils.left(subdef, 1).toUpperCase());
			sb.append(StringUtils.substring(subdef, 1));
			if (!StringUtils.endsWithAny(subdef, ".", "?", "!")) {
				sb.append(".");
			}
			additional = true;
		}
		// sb.append(def);
		if (!StringUtils.isBlank(genus)) {
			sb.append("; ");
			if (genus.startsWith("(")) {
				sb.append("Genus: ");
				String sub = StringUtils.substringBetween(genus, "(", ")");
				sub = WordUtils.capitalizeFully(sub);
				sb.append(sub);
				if (!StringUtils.endsWithAny(sub, ".", "?", "!")) {
					sb.append(".");
				}
			} else {
				sb.append(genus);
			}
		}
		return sb.toString();
	}
	
	private List<String> notes=new ArrayList<>();
	
	@Override
	public void addNote(String note) {
		notes.add(note);
	}
	@Override
	public void clearNotes() {
		notes.clear();
	}
	@Override
	public List<String> getNotes() {
		return new ArrayList<>(notes);
	}
}
package com.cherokeelessons.raven;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.cherokeelessons.log.Log;

public class Entry implements IEntry {
	
	private List<String> notes=new ArrayList<>();
	
	public void clearNotes(){
		notes.clear();
	}
	
	public void addNote(String note) {
		notes.add(note);
	}
	
	public List<String> getNotes(){
		return new ArrayList<>(notes);
	}

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

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#size()
	 */
	@Override
	public int size() {
		return syllabary.size();
	}

	private final List<String> pronunciations=new ArrayList<String>();
	public void addPronunciation(String pro) {
		if (pro == null) {
			App.err("BAD PRONUNCIATION FOR: "+getDef());
			throw new RuntimeException("BAD PRONUNCIATION FOR: "+getDef());
		}
		pronunciations.add(StringUtils.defaultString(pro));
	}
	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#getPronunciations()
	 */
	@Override
	public List<String> getPronunciations() {
		return new ArrayList<String>(pronunciations);
	}

	private String _sortKey;

	@Override
	public String sortKey() {
		if (StringUtils.isEmpty(_sortKey)) {
			StringBuilder sb = new StringBuilder();
			for (String l : getSyllabary()) {
				sb.append(l.replaceAll("[^Ꭰ-Ᏼ]", "") + "-");
			}
			for (String l : getPronunciations()) {
				sb.append(l + "-");
			}
			sb.append(size());
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

	/* (non-Javadoc)
	 * @see com.cherokeelessons.raven.IEntry#getSyllabary()
	 */
	@Override
	public List<String> getSyllabary() {
		return new ArrayList<String>(syllabary);
	}
	private final ArrayList<String> syllabary = new ArrayList<>();
	public void addSyllabary(String syllabary) {
		if (syllabary == null) {
			App.err("BAD SYLLABARY FOR: "+getDef());
		}
		this.syllabary.add(StringUtils.defaultString(syllabary));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<String> ilist = pronunciations.iterator();
		Iterator<String> slist = syllabary.iterator();
		if (ilist.hasNext()) {
			String pronunciation = ilist.next();
			String syllabary = slist.next();
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
					sb.append(syllabary);
					sb.append(" [");
					sb.append(pronunciation);
					sb.append("]");
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
				sb.append(syllabary);
				sb.append(" [");
				sb.append(pronunciation);
				sb.append("]");
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	@Override
	public String formattedDefinition() {
		StringBuilder sb = new StringBuilder();
		//String[] subdefs = StringUtils.split(def, ";");
		String[] subdefs = def.split(Consts.splitRegex);
		boolean additional = false;
		int count=0;
		for (String subdef : subdefs) {
			if (StringUtils.isBlank(subdef)) {
				continue;
			}
			if (additional) {
				//sb.append("; ");
				count++;
				sb.append(" ");
				sb.append(Consts.definitionMarkers[count % Consts.definitionMarkers.length]);
				sb.append(" ");
			}
			subdef = StringUtils.normalizeSpace(subdef);
			subdef = StringUtils.strip(subdef);
			if (!StringUtils.endsWithAny(subdef.replaceAll("<.*?>", ""), ".", "?", "!")) {
				subdef=subdef+".";
			}
			subdef = subdef.replaceAll("<tag:([a-zA-Z]+):([a-zA-Z]+)>", "\n\\\\$1 $2\n");
			if (!subdef.startsWith("biol.")){
				sb.append(StringUtils.left(subdef, 1).toUpperCase());
			} else {
				sb.append(StringUtils.left(subdef, 1));
			}
			sb.append(StringUtils.substring(subdef, 1));
			additional = true;
		}
		// sb.append(def);
		if (!StringUtils.isBlank(genus)) {
			//sb.append("; ");
			count++;
			sb.append(" ");
			sb.append(Consts.definitionMarkers[count % Consts.definitionMarkers.length]);
			sb.append(" ");
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
		if (count>0) {
			sb.insert(0, " ");
			sb.insert(0, Consts.definitionMarkers[0]);
		}
		return StringUtils.strip(sb.toString());
	}
}
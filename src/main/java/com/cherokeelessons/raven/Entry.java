
package com.cherokeelessons.raven;

import java.util.List;

import com.cherokeelessons.raven.RavenEntry.SpreadsheetEntry;

public interface Entry extends Comparable<Entry> {
	void addNote(String note);

	void addPronunciation(String pronounciation);

	void addSyllabary(String syllabary);

	void clearNotes();

	@Override
	int compareTo(Entry o);

	@Override
	boolean equals(Object obj);

	String formattedDefinition();

	String getDef();

	List<String> getNotes();

	List<String> getPronunciations();

	List<String> getSyllabary();

	String getType();

	void setDef(String def);

	void setType(String type);

	int size();

	String sortKey();

	SpreadsheetEntry spreadsheetEntry();

	List<String> getCf();

	void setCf(List<String> cf);

	void addCf(String cfEntry);

	String getLabel();

	void setLabel(String label);
}

package com.cherokeelessons.raven;

import java.util.List;

import com.cherokeelessons.raven.RavenEntry.SpreadsheetEntry;

public interface Entry extends Comparable<Entry> {
	void addNote(String note);
	void clearNotes();
	int compareTo(Entry o);
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
}

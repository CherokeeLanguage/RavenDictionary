package com.cherokeelessons.raven;

import java.util.List;

public interface IEntry extends Comparable<IEntry> {
	void addNote(String note);
	void clearNotes();
	int compareTo(IEntry o);
	boolean equals(Object obj);
	String formattedDefinition();
	String getDef();
	String getGenus();
	List<String> getNotes();
	List<String> getPronunciations();
	List<String> getSyllabary();
	String getType();
	void setDef(String def);
	void setGenus(String genus);
	void setType(String type);
	int size();
	String sortKey();
}

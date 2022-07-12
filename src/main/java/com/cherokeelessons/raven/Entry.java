package com.cherokeelessons.raven;

import com.cherokeelessons.raven.RavenEntry.SpreadsheetEntry;

import java.util.List;

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

    void setDef(String def);

    List<String> getNotes();

    List<String> getPronunciations();

    List<String> getSyllabary();

    String getType();

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

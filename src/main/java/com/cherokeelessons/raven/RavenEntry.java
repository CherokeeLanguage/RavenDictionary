package com.cherokeelessons.raven;

import com.cherokeelessons.raven.RavenEntry.SpreadsheetEntry.SpreadsheetRow;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RavenEntry implements Entry {

    protected final List<String> notes = new ArrayList<>();
    protected final List<String> pronunciations = new ArrayList<>();
    protected final List<String> syllabary = new ArrayList<>();
    protected String def;
    protected String type;
    protected List<String> cf = new ArrayList<>();
    protected String label;

    public RavenEntry() {
    }

    public RavenEntry(Entry copy) {
        if (copy == null) {
            return;
        }
        def = copy.getDef();
        if (copy.getNotes() != null) {
            notes.addAll(copy.getNotes());
        }
        if (copy.getPronunciations() != null) {
            pronunciations.addAll(copy.getPronunciations());
        }
        if (copy.getSyllabary() != null) {
            syllabary.addAll(copy.getSyllabary());
        }
        type = copy.getType();
    }

    @Override
    public List<String> getCf() {
        return cf;
    }

    @Override
    public void setCf(List<String> cf) {
        this.cf = cf;
    }

    @Override
    public void addCf(String cfEntry) {
        this.cf.add(cfEntry);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public SpreadsheetEntry spreadsheetEntry() {
        SpreadsheetEntry entry = new SpreadsheetEntry();
        SpreadsheetRow row;
        Iterator<String> isyll = syllabary.iterator();
        Iterator<String> ipron = pronunciations.iterator();
        Iterator<String> inotes = notes.iterator();
        /**
         * primary entry: flagged in column 1 and starts in column 2
         */
        row = new SpreadsheetRow();
        entry.rows.add(row);
        row.fields.add("ENTRY");
        if (isyll.hasNext()) {
            row.fields.add(isyll.next());
        } else {
            row.fields.add("");
        }
        if (ipron.hasNext()) {
            row.fields.add(ipron.next());
        } else {
            row.fields.add("");
        }
        row.fields.add(type == null ? "" : type);
        String tmpDef = def == null ? "" : def;
        tmpDef = tmpDef.trim();
        tmpDef = tmpDef.replace(";", ";\n").trim();
        for (String dm : Consts.DEFINITION_MARKERS) {
            tmpDef = tmpDef.replace(dm, ";\n").trim();
        }
        tmpDef = tmpDef.replaceAll("\\s*\n\\s*", "\n");
        row.fields.add(tmpDef);

        /**
         * all sub entries start in column 2
         */
        while (isyll.hasNext() || ipron.hasNext()) {
            row = new SpreadsheetRow();
            entry.rows.add(row);
            row.fields.add("");
            if (isyll.hasNext()) {
                row.fields.add(isyll.next());
            } else {
                row.fields.add("");
            }
            if (ipron.hasNext()) {
                row.fields.add(ipron.next());
            } else {
                row.fields.add("");
            }
        }

        /**
         * all note entries start in column 2
         */
        while (inotes.hasNext()) {
            row = new SpreadsheetRow();
            entry.rows.add(row);
            row.fields.add("note");
            row.fields.add(inotes.next());
        }
        return entry;
    }

    @Override
    public void addNote(String note) {
        notes.add(note);
    }

    @Override
    public void addPronunciation(String pro) {
        if (pro == null) {
            throw new RuntimeException("BAD PRONUNCIATION FOR: " + getDef());
        }
        pronunciations.add(StringUtils.defaultString(pro));
    }

    @Override
    public void addSyllabary(String _syllabary) {
        if (_syllabary == null) {
            throw new RuntimeException("BAD SYLLABARY FOR: " + getDef());
        }
        this.syllabary.add(StringUtils.defaultString(_syllabary));
    }

    @Override
    public void clearNotes() {
        notes.clear();
    }

    @Override
    public int compareTo(Entry o) {
        return sortKey().compareTo(o.sortKey());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pronunciations == null) ? 0 : pronunciations.hashCode());
        result = prime * result + ((syllabary == null) ? 0 : syllabary.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Entry)) {
            return false;
        }
        return compareTo((RavenEntry) obj) == 0;
    }

    @Override
    public String formattedDefinition() {
        StringBuilder sb = new StringBuilder();
        String[] subdefs = def.split(Consts.SPLIT_REGEX);
        boolean additional = false;
        int count = 0;
        for (String subdef : subdefs) {
            if (StringUtils.isBlank(subdef)) {
                continue;
            }
            if (additional) {
                // sb.append("; ");
                count++;
                sb.append(" ");
                sb.append(Consts.DEFINITION_MARKERS[count % Consts.DEFINITION_MARKERS.length]);
                sb.append(" ");
            }
            subdef = StringUtils.normalizeSpace(subdef);
            subdef = StringUtils.strip(subdef);
            if (!StringUtils.endsWithAny(subdef.replaceAll("<.*?>", ""), ".", "?", "!")) {
                subdef = subdef + ".";
            }
            subdef = subdef.replaceAll("<tag:([a-zA-Z]+):([a-zA-Z]+)>", "\n\\\\$1 $2\n");
            if (!subdef.startsWith("biol.")) {
                sb.append(StringUtils.left(subdef, 1).toUpperCase());
            } else {
                sb.append(StringUtils.left(subdef, 1));
            }
            sb.append(StringUtils.substring(subdef, 1));
            additional = true;
        }
        if (count > 0) {
            sb.insert(0, " ");
            sb.insert(0, Consts.DEFINITION_MARKERS[0]);
        }
        return StringUtils.strip(sb.toString());
    }

    @Override
    public String getDef() {
        return def;
    }

    @Override
    public void setDef(String def) {
        this.def = def;
    }

    @Override
    public List<String> getNotes() {
        return notes;
    }

    @Override
    public List<String> getPronunciations() {
        return pronunciations;
    }

    @Override
    public List<String> getSyllabary() {
        return syllabary;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int size() {
        return syllabary.size();
    }

    @Override
    public String sortKey() {
        StringBuilder sb = new StringBuilder();
        for (String l : getSyllabary()) {
            sb.append(l.replaceAll("[^Ꭰ-Ᏼ]", "") + "-");
        }
        for (String l : getPronunciations()) {
            sb.append(l + "-");
        }
        sb.append(size());
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> ilist = pronunciations.iterator();
        Iterator<String> slist = syllabary.iterator();
        if (ilist.hasNext()) {
            String pronunciation = ilist.next();
            String _syllabary = slist.next();
            reformat:
            {
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
                sb.append(_syllabary);
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
                sb.append(_syllabary);
                sb.append(" [");
                sb.append(pronunciation);
                sb.append("]");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static class SpreadsheetEntry {
        public List<SpreadsheetRow> rows = new ArrayList<>();

        public static class SpreadsheetRow {
            public List<String> fields = new ArrayList<>();
        }
    }
}

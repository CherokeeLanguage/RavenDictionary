package com.cherokeelessons.raven;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class SyllabaryConverter {

    public static final String UpperMark = "\u0332";//combining underline
    public static final String Overline = "\u203e";
    private static final Map<String, String> lat2chr;
    private static final List<String> latin_keys = new ArrayList<>();
    private final static String ldquo = "\u201c";
    private final static String rdquo = "\u201d";
    private final static String squo = "'";
    private final static String dsquo = squo + squo;
    private final static String dquo = "\"";
    private final static String lsquo = "\u2018";
    private final static String rsquo = "\u2019";
    private static final Comparator<String> descending = new Comparator<String>() {

        @Override
        public int compare(String arg0, String arg1) {
            int cmp = arg0.length() - arg1.length();
            if (cmp != 0) {
                return -cmp;
            }
            return arg0.compareTo(arg1);
        }
    };

    static {
        lat2chr = lat2chr();
        latin_keys.addAll(lat2chr.keySet());
        Collections.sort(latin_keys, descending);
    }

    /*
     * LAZY Horribly bad slow code here. :)
     */
    public static String transform2chr(String text) {
        String result = StringUtils.strip(text);
        for (String key : latin_keys) {
            result = result.replaceAll("-" + key, lat2chr.get(key));
            result = result.replaceAll(key, lat2chr.get(key));
            result = result.replaceAll("~([Ꭰ-Ᏼ]" + UpperMark + ")",
                    Overline + UpperMark + "$1");
            result = result.replaceAll("~([Ꭰ-Ᏼ])", Overline + "$1");
        }
        result = fancyQuotes(result);
        result = StringUtils.normalizeSpace(result);
        if (result.matches(".*[a-zA-Z].*")) {
            throw new RuntimeException("Incomplete transformation! " + result);
        }
        return result;
    }

    public static Map<String, String> chr2lat() {
        Map<String, String> syl2lat = new HashMap<String, String>();
        Map<String, String> lat2chr = lat2chr();

        Iterator<String> ikey = lat2chr.keySet().iterator();
        while (ikey.hasNext()) {
            String key = ikey.next();
            if (key.startsWith("hl")) {
                continue;
            }
            if (key.startsWith("ts")) {
                continue;
            }
            if (key.startsWith("ch")) {
                continue;
            }
            if (key.startsWith("qu")) {
                continue;
            }
            if (key.startsWith("k") && !key.startsWith("ka")) {
                continue;
            }
            syl2lat.put(lat2chr.get(key), key);
        }
        return syl2lat;
    }

    public static Map<String, String> lat2chr() {
        int ix = 0;
        String letter;
        String prefix;
        char chrStart = 'Ꭰ';
        String[] vowels = new String[6];

        Map<String, String> latin2syllabary = new HashMap<String, String>();

        vowels[0] = "a";
        vowels[1] = "e";
        vowels[2] = "i";
        vowels[3] = "o";
        vowels[4] = "u";
        vowels[5] = "v";

        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(vowels[ix], letter);
        }

        latin2syllabary.put("ga", "Ꭶ");

        latin2syllabary.put("ka", "Ꭷ");

        prefix = "g";
        chrStart = 'Ꭸ';
        for (ix = 1; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix - 1));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "k";
        chrStart = 'Ꭸ';
        for (ix = 1; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix - 1));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "h";
        chrStart = 'Ꭽ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "l";
        chrStart = 'Ꮃ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "m";
        chrStart = 'Ꮉ';
        for (ix = 0; ix < 5; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        latin2syllabary.put("na", "Ꮎ");
        latin2syllabary.put("hna", "Ꮏ");
        latin2syllabary.put("nah", "Ꮐ");

        prefix = "n";
        chrStart = 'Ꮑ';
        for (ix = 1; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix - 1));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "qu";
        chrStart = 'Ꮖ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "gw";
        chrStart = 'Ꮖ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        latin2syllabary.put("sa", "Ꮜ");
        latin2syllabary.put("s", "Ꮝ");

        prefix = "s";
        chrStart = 'Ꮞ';
        for (ix = 1; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix - 1));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        latin2syllabary.put("da", "Ꮣ");
        latin2syllabary.put("ta", "Ꮤ");
        latin2syllabary.put("de", "Ꮥ");
        latin2syllabary.put("te", "Ꮦ");
        latin2syllabary.put("di", "Ꮧ");
        latin2syllabary.put("ti", "Ꮨ");
        latin2syllabary.put("do", "Ꮩ");
        latin2syllabary.put("to", "Ꮩ");
        latin2syllabary.put("du", "Ꮪ");
        latin2syllabary.put("tu", "Ꮪ");
        latin2syllabary.put("dv", "Ꮫ");
        latin2syllabary.put("tv", "Ꮫ");
        latin2syllabary.put("dla", "Ꮬ");

        prefix = "hl";
        chrStart = 'Ꮭ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "tl";
        chrStart = 'Ꮭ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "j";
        chrStart = 'Ꮳ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "ts";
        chrStart = 'Ꮳ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "ch";
        chrStart = 'Ꮳ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "w";
        chrStart = 'Ꮹ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }

        prefix = "y";
        chrStart = 'Ꮿ';
        for (ix = 0; ix < 6; ix++) {
            letter = Character.toString((char) (chrStart + ix));
            latin2syllabary.put(prefix + vowels[ix], letter);
        }
        return latin2syllabary;
    }

    private static String fancyQuotes(String text) {
        // double single tick: into ldquo
        final String r1 = "(?<=[\\s{\\[<>(]|^)" + Pattern.quote(dsquo);
        // double single tick: into rdquo
        final String r5 = Pattern.quote(dsquo);
        text = text.replaceAll(r1, ldquo).replaceAll(r5, rdquo);

        // single double quote: into ldquo
        final String r3 = "(?<=[\\s{\\[<>(]|^)" + Pattern.quote(dquo);
        // single double quote: into rdquo
        final String r4 = Pattern.quote(dquo);
        text = text.replaceAll(r3, ldquo).replaceAll(r4, rdquo);

        // single tick: into lsquo
        final String r2 = "(?<=[\\s{\\[<>(]|^)" + Pattern.quote(squo);
        // single tick: into rsquo
        final String r6 = Pattern.quote(squo);
        text = text.replaceAll(r2, lsquo).replaceAll(r6, rsquo);

        return text;
    }
}

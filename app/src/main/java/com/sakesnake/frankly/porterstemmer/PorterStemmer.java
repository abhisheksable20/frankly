package com.sakesnake.frankly.porterstemmer;

import androidx.annotation.NonNull;

/*
This class is used to find root word of the given word
This will return single word at a time
**/
public class PorterStemmer {
    private static final char[] VOWELS = new char[]{'a', 'e', 'i', 'o', 'u'};
    private static final String[][] MORPHOLOGY_ONE = new String[][]{
            {"ational","ate"},
            {"tional","tion"},
            {"enci","ence"},
            {"anci","ance"},
            {"izer","ize"},
            {"iser","ize"},
            {"abli","able"},
            {"alli","al"},
            {"entli","ent"},
            {"eli","e"},
            {"ousli","ous"},
            {"ization","ize"},
            {"isation","ize"},
            {"ation","ate"},
            {"ator","ate"},
            {"alism","al"},
            {"iveness","ive"},
            {"fulness","ful"},
            {"ousness","ous"},
            {"aliti","al"},
            {"iviti","ive"},
            {"biliti","ble"}};

    private static final String[][] MORPHOLOGY_TWO = new String[][]{
            {"icate", "ic"},
            {"ative", ""},
            {"alize", "al"},
            {"alise", "al"},
            {"iciti", "ic"},
            {"ical",  "ic"},
            { "ful",   "" },
            { "ness",  "" }
    };

    public static final String[] MORPHOLOGY_THREE = new String[]{"al", "ance", "ence", "er", "ic", "able", "ible", "ant", "ement", "ment", "ent", "sion", "tion",
            "ou", "ism", "ate", "iti", "ous", "ive", "ize", "ise"};

    public String getRootWord(@NonNull final String word) {
        StringBuilder stem = new StringBuilder(word.toLowerCase());
        // Step 1a
        if (stem.toString().endsWith("sses"))
            stem.replace(stem.lastIndexOf("sses"), stem.length(), "ss");
        else if (stem.toString().endsWith("ies"))
            stem.replace(stem.lastIndexOf("ies"), stem.length(), "i");
        else if (stem.toString().endsWith("ss"))
            stem.replace(stem.lastIndexOf("ss"), stem.length(), "ss");
        else if (stem.toString().endsWith("s"))
            stem.delete(stem.length() - 1, stem.length());


        // Step 1b
        final int vcCombinations = vcCombinations(stem.toString());
        if (vcCombinations > 0) {
            if (stem.lastIndexOf("eed") != -1  &&  stem.toString().endsWith("eed")) {
                stem.replace(stem.lastIndexOf("eed"), stem.length(), "ee");
            }
        }

        if (vcCombinations > 0) {
            if ((stem.lastIndexOf("ed") != -1)  &&  stem.toString().endsWith("ed")) {
                stem.delete(stem.lastIndexOf("ed"), stem.length());
            }
            else if (stem.lastIndexOf("ing") != -1  &&  stem.toString().endsWith("ing")) {
                stem.delete(stem.lastIndexOf("ing"), stem.length());
            }

            // Clean up process
            if ((stem.lastIndexOf("at") != -1  &&  stem.toString().endsWith("at"))  ||  (stem.lastIndexOf("bl") != -1)  &&  stem.toString().endsWith("bl")){
                if (stem.lastIndexOf("at") != -1)
                    stem.replace(stem.lastIndexOf("at"),stem.length(),"ate");
                else
                    stem.replace(stem.lastIndexOf("bl"),stem.length(),"ble");

            }
            else if (endsWithDoubleConsonant(stem.toString())  &&  !((endWithLetter(stem.toString(),'l')) ||
                    (endWithLetter(stem.toString(),'s')) || (endWithLetter(stem.toString(),'z')))) {
                stem.delete(stem.length()-1,stem.length());
            }
            else if (vcCombinations == 1  &&  (isCVCCombination(stem.toString()))){
                stem.append('e');
            }

            String morphologyWord = stepTwo(stem.toString(),vcCombinations);
            if (!(morphologyWord.equals(stem.toString())))
                stem.replace(0,stem.length(),morphologyWord);

            morphologyWord = stepThree(stem.toString(),vcCombinations);
            if (!(morphologyWord).equals(stem.toString()))
                /*return*/ stem.replace(0,stem.length(),morphologyWord);

            morphologyWord = stepFour(stem.toString(),vcCombinations);
            if (!(morphologyWord).equals(stem.toString()))
                stem.replace(0,stem.length(),morphologyWord);

            if (vcCombinations > 1){
                if (stem.charAt(stem.length() - 1) == 'e') {
                    stem.delete(stem.length() - 1, stem.length());
                    return stem.toString();
                }
            }

            if ((vcCombinations > 1)  &&  (endsWithDoubleConsonant(stem.toString()))  &&  endWithLetter(stem.toString(),'l')){
                stem.delete(stem.length() - 1, stem.length());
                return stem.toString();
            }
        }


        return stem.toString();
    }


    private String stepTwo(final String word,final int vcCombinations){
        StringBuilder builder = new StringBuilder(word);
        if (vcCombinations > 0) {
            for (String[] strings : MORPHOLOGY_ONE) {
                if (builder.lastIndexOf(strings[0]) != -1 && builder.toString().endsWith(strings[0])) {
                    builder.replace(builder.lastIndexOf(strings[0]), builder.length(), strings[1]);
                    break;
                }
            }
        }
        return builder.toString();
    }

    public String stepThree(final String word,final int vcCombinations){
        StringBuilder builder = new StringBuilder(word);
        if (vcCombinations > 0) {
            for (String[] strings : MORPHOLOGY_TWO) {
                if (builder.lastIndexOf(strings[0]) != -1 && builder.toString().endsWith(strings[0])) {
                    builder.replace(builder.lastIndexOf(strings[0]), builder.length(), strings[1]);
                    break;
                }
            }
        }

        return builder.toString();
    }

    private String stepFour(final String word,final int vcCombinations){
        StringBuilder builder = new StringBuilder(word);
        if (vcCombinations > 1) {
            for (String s : MORPHOLOGY_THREE) {
                if (builder.lastIndexOf(s) != -1 && builder.toString().endsWith(s)) {
                    builder.delete(builder.lastIndexOf(s), builder.length());
                    break;
                }
            }
        }
        return builder.toString();
    }


    // Getting (VC) combinations
    public int vcCombinations(final String word) {
        StringBuilder builder = new StringBuilder(word);
        if (word.endsWith("ed"))
            builder.delete(builder.lastIndexOf("ed"), builder.length());

        else if (word.endsWith("ing"))
            builder.delete(builder.lastIndexOf("ing"), builder.length());

        int vc = 0;
        // When v becomes true followed by c then vc combination will be incremented
        boolean isV = false;
        for (int i = 0; i < builder.length(); i++) {
            if (isV) {
                if (!(isVowel(builder.charAt(i)))) {
                    isV = false;
                    vc++;
                    continue;
                }
            }

            if (isVowel(builder.charAt(i))) {
                isV = true;
            }
        }
        return vc;
    }

    // Checking is CVC combination but second C is not equal to W,X or Y
    public boolean isCVCCombination(String word){
        StringBuilder builder = new StringBuilder(word);
        if (word.endsWith("ed"))
            builder.delete(builder.lastIndexOf("ed"), builder.length());

        else if (word.endsWith("ing"))
            builder.delete(builder.lastIndexOf("ing"), builder.length());

        if (builder.length() >= 3){
            char thirdLastChar = builder.charAt(builder.length() - 3);
            char secondLastChar = builder.charAt(builder.length() - 2);
            char lastChar = builder.charAt(builder.length() - 1);
            if (lastChar=='w' || lastChar=='x' || lastChar=='y')
                return false;
            else
                return !(isVowel(thirdLastChar)) && (isVowel(secondLastChar)) && !(isVowel(lastChar));
        }
        else
            return false;
    }

    // Checking is given char is vowel
    private boolean isVowel(final char letter) {
        for (char vowel : VOWELS) {
            if (letter == vowel) {
                return true;
            }
        }
        return false;
    }

    // Checking is given word is double consonant(TT, BB etc)
    private boolean endsWithDoubleConsonant(String word){
        if (word.length() >= 2){
            char one = word.charAt(word.length() - 2);
            char two = word.charAt(word.length() - 1);
            if (!(isVowel(one))  &&  !(isVowel(two))){
                return one == two;
            }else{
                return false;
            }
        }
        return false;
    }

    // Checking is given word is ending with specific letter
    private boolean endWithLetter(final String word, final char letter){
        return (word.charAt(word.length() -1) == letter);
    }
}

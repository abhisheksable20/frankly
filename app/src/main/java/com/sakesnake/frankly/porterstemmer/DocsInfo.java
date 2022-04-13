package com.sakesnake.frankly.porterstemmer;

import androidx.annotation.NonNull;

import com.sakesnake.frankly.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// This class is used to get total words, sentences etc
public class DocsInfo{

    private String sentence;

    private List<String> paras = new ArrayList<>();

    private final List<String> stopWords = new ArrayList<>();

    private static final String[] stopWord = new String[]{"i", "me", "my", "myself", "we", "our", "ours", "ourself", "you",
            "your", "yours", "yourself", "he", "him", "his", "himself", "she", "her", "hers", "herself",
            "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom",
            "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have",
            "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because",
            "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through",
            "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "down", "in", "out", "on",
            "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why",
            "how", "all", "any", "both", "each", "few", "more", "other", "some", "such", "no", "nor", "not",
            "only", "owe", "same", "so", "than", "too", "very", "can", "will", "just", "done", "should", "noe",
            "p","ul","li","div","span","strong","b","em","cite","dfn","i","big"
    };

    // Counting total words and root words in sentence
    public static void totalWords(@NonNull final String text, @NonNull final DocInfoCallback docInfoCallback){
        App.getCachedExecutorService().execute(()->{
            final List<String> words = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            char[] totalChar = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
            boolean isWord = false;
            boolean backIsLetter = true;
            int totalWords = 0;
            String para = text.toLowerCase().trim();
            for (int i=0; i<para.length(); i++){
                for (int j=0; j<totalChar.length; j++){
                    if (para.charAt(i) == totalChar[j]) {
                        builder.append(para.charAt(i));
                        if (i == para.length()-1){
                            isWord = true;
                            break;
                        }
                        backIsLetter = true;
                        break;
                    }
                    else{
                        if (j == totalChar.length-1){
                            if (i == 0){
                                break;
                            }
                            if (para.charAt(i-1) == para.charAt(i)){
                                break;
                            }
                            else{
                                if (!backIsLetter){
                                    break;
                                }
                                isWord = true;
                            }
                        }
                    }
                }
                if (isWord){
                    if (!(isStopWord(builder.toString()))) {
                        words.add(builder.toString());
                    }

                    builder.delete(0,builder.length());
                    backIsLetter = false;
                    isWord = false;
                    totalWords++;
                }
            }

            final int totalStemWords = totalWords;
            final StringBuilder stringBuilder = new StringBuilder();
            final PorterStemmer stemmer = new PorterStemmer();
            for (String s:words) {
                stringBuilder.append(stemmer.getRootWord(s)).append(", ");
            }

            App.getMainThread().post(() ->{
                docInfoCallback.getDocInfo(totalStemWords,stringBuilder.toString());
            });
        });
    }

    // Checking is word is stop word or not
    public static boolean isStopWord(final String word){
        for (String s:stopWord) {
            if(s.equals(word))
                return true;
        }
        return false;
    }

    // Getting how many times the given word is occurring
    public int getWordOccurred(final String doc,final String word){
        final String str = word.trim();
        int times = 0;
        int index = 0;
        if (str.equals("")){
            return times;
        }
        while ((index = doc.indexOf(word,index)) != -1){
            System.out.println(doc.indexOf(word,index));
            times++;
            index++;
        }
        return times;
    }


    // Do not use this method in android
    private void initStopWordsList(){
        File file = new File("Path of stop words list. This method is not in use here");
        String str;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((str = reader.readLine()) != null){
                stopWords.add(str);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    // getting total paragraphs a file contains
    private List<String> totalPara(){
        return paras;
    }


    // Generate total words from file
    private DocsInfo fromFile(String fileName) throws IOException {
        boolean previousWasSpace = false;
        StringBuilder builder = new StringBuilder();
        StringBuilder paragraphs = new StringBuilder();
        String str;
        File file = new File("File path");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((str=reader.readLine()) != null){
            if (str.equals("")){
                if (!previousWasSpace){
                    paras.add(paragraphs.toString());
                    paragraphs.delete(0,paragraphs.length());
                    previousWasSpace = true;
                }
            }else{
                paragraphs.append(str).append("\n");
                previousWasSpace = false;
            }
            builder.append(str).append("\n");
        }
        if (!(paragraphs.toString().equals("")))
            paras.add(paragraphs.toString());

        this.sentence = builder.toString().toLowerCase();
        return this;
    }


    // To get total number of paragraph in document
    private int sentenceSegmentation(){
        List<String> dotSeparation = Arrays.asList(sentence.split("\\."));
        List<String> exclamationSeparation = Arrays.asList(sentence.split("!"));
        List<String> questionSeparation = Arrays.asList(sentence.split("\\?"));
        List<String> sentences = new ArrayList<>();

        if (dotSeparation.size() >= 1){
            if (dotSeparation.size() == 1  &&  dotSeparation.get(0).contains("."))
                sentences.addAll(dotSeparation);
            else if (dotSeparation.size() > 1)
                sentences.addAll(dotSeparation);

        }

        if (exclamationSeparation.size() >= 1){
            if (exclamationSeparation.size() == 1  &&  exclamationSeparation.get(0).contains("!"))
                sentences.addAll(exclamationSeparation);
            else if (exclamationSeparation.size() > 1)
                sentences.addAll(exclamationSeparation);
        }

        if (questionSeparation.size() >= 1){
            if (questionSeparation.size() == 1  &&  questionSeparation.get(0).contains("?"))
                sentences.addAll(questionSeparation);
            else if (questionSeparation.size() > 1)
                sentences.addAll(questionSeparation);
        }

        System.out.println("Dot: "+dotSeparation.size()+" Exclamation: "+exclamationSeparation.size()+" Question: "+questionSeparation.size());

        return sentences.size();
    }
}

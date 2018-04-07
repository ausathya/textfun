package com.sat.textfun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.TypeTokenFilter;
import org.apache.lucene.analysis.en.EnglishMinimalStemmer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class HVTAnalyzer {

    private static Logger logger = LogManager.getLogger(HVTAnalyzer.class);

    private static final String[] EMPTY_ARRAY = new String[0];

    private static Set<String> stopWordSet = new TreeSet<>();

    private Map<String, LongAdder> termCountMap = new TreeMap<>();

    private String fileName = "";


    static{
        try {
            loadStopWords();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HVTAnalyzer(String fileName){
        this.fileName = fileName;
    }

    public void emitTokens(long threshold) throws Exception {
        logger.info("------------------------------------------------");
        Stream<String> lines = Util.loadFile(fileName);
        lines.forEach(line -> {
            try {
                processLine(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //printTokens(1, "ALL Tokens");
        //printTokens(2, "More than Once");
        printTokens(threshold, "At-least 20");
        lines.close();
        termCountMap.clear();
    }


    private void processLine(String line) throws IOException {
        if(line == null)
            return ;

        line = normalize(line);
        TokenStream tokenStream = tokenize(line);
        tokenStream.reset();
        CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            String token = cattr.toString();
            token = cleanUpToken(token);
            if(!isStopWord(token) && isMinimumLength(token)) {
                token = stem(token);
                tokenFound(token);
            }
        }
        tokenStream.end();
        tokenStream.close();
    }

    private static EnglishMinimalStemmer englishMinimalStemmer = new EnglishMinimalStemmer();
    private String stem(String token){
        return token.substring(0, englishMinimalStemmer.stem(token.toCharArray(), token.length()));
    }

    private boolean isStopWord(String token){
        return stopWordSet.contains(token);
    }

    private String normalize(String token){
        if(token == null)
            return token;

        token = token.trim();
        token = token.toLowerCase();
        return token;
    }

    private String cleanUpToken(String token){
        if(token == null)
            return token;
        token = token.trim();
        if(token.equalsIgnoreCase("it'"))
            System.out.println("match");
        if(token.endsWith("'"))
            token = token.substring(0, token.length()-1);
        return token;
    }

    private boolean isMinimumLength(String token){
        if(token.length() > 2)
            return true;
        else
            return false;
    }


    private String[] tokenizeByWhitespace(String line){
        if(line  == null)
            return EMPTY_ARRAY;

        return line.split("\\s+");
    }


    private static Set<String> stopTypes = new HashSet<>(); // Ex: <NUM>
    private TokenStream tokenize(String line){
        if(line  == null)
            return null;

        final StandardTokenizer input = new StandardTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
        input.setReader(new StringReader(line));
        return new TypeTokenFilter(input, stopTypes);
    }

    public void tokenFound(String token) {
        termCountMap.computeIfAbsent(token, (t) -> new LongAdder()).increment();
    }

    private static void loadStopWords() throws Exception {
        Stream<String> lines = Util.loadFile("stopwords.txt");
        lines.forEach(l -> {
            if(l != null && l.length() > 0) {
                stopWordSet.add(l.trim().toLowerCase());
            }
        });
        //stopWordSet.forEach(word -> System.out.println(word));
        logger.info("Total Stopwords loaded: " + stopWordSet.size());
    }


    protected void printTokens(long threshold, String headerText){
        logger.info("--------------------------------------");
        logger.info("				" + headerText + "			   ");
        logger.info("--------------------------------------");
        termCountMap.forEach((k,v) ->{
                    if(v.longValue() >= threshold) {
                        logger.info(String.format("| %-25s	|	%3s |", k, v));
                    }
                }
        );
        logger.info("--------------------------------------");
        logger.info("------------------------------------------------");
    }

}

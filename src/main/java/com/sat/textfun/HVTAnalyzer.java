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

    private static Set<String> stopWordSet = new TreeSet<>();

    private Map<String, LongAdder> termCountMap = new TreeMap<>();

    private Map<String, Set<Long>> termDocMap = new TreeMap<>();

    private String fileName = "";

    private long tokenCount = 0;
    private long lineCount = 0;


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

    public void emitTokens(double minTokenCount, double tokenThreshold, double tfidfThreshold, double ratioToTopTermCount) throws Exception {
        Stream<String> lines = Util.loadFile(fileName);
        lines.forEach(line -> {
            try {
                processLine(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //computeTokens(1, "ALL Tokens");
        //computeTokens(2, "More than Once");
        logger.info("Total number of tokens found: " + tokenCount);
        computeTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
        lines.close();
        termCountMap.clear();
    }


    private void processLine(String line) throws IOException {
        if(line == null)
            return ;

        line = normalize(line);
        if(line.length() < 1)
            return;
        lineCount++;
        TokenStream tokenStream = tokenize(line);
        tokenStream.reset();
        CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            tokenCount ++;
            String token = cattr.toString();
            token = cleanUpToken(token);
            if(!isStopWord(token) && isMinimumLength(token)) {
                token = stem(token);
                tokenFound(token, lineCount);
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
        if(token.endsWith("'") || token.endsWith("’"))
            token = token.substring(0, token.length()-1);
        return token;
    }

    private boolean isMinimumLength(String token){
        if(token.length() > 2)
            return true;
        else
            return false;
    }


    private static Set<String> stopTypes = new HashSet<>(); // Ex: <NUM>
    private TokenStream tokenize(String line){
        if(line  == null)
            return null;

        final StandardTokenizer input = new StandardTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
        input.setReader(new StringReader(line));
        return new TypeTokenFilter(input, stopTypes);
    }

    public void tokenFound(String token, long lineCount) {
        termCountMap.computeIfAbsent(token, (t) -> new LongAdder()).increment();
        if(!termDocMap.containsKey(token)){
            termDocMap.put(token, new TreeSet<>());
        }
        termDocMap.get(token).add(lineCount);
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


    protected Set<HVTToken> computeTokens(double minTokenCount, double tokenThreshold, double tfidfThreshold, double ratioToTopTermCount){
        double allCountCutoff = tokenCount * tokenThreshold;
        logger.info(termCountMap);
        logger.info(termDocMap);

        long highestTermCount = 0;
        for(String term : termCountMap.keySet()){
            if(termCountMap.get(term).longValue() > highestTermCount){
                highestTermCount = termCountMap.get(term).longValue();
            }
        }
        logger.info("Highest Term Count Value: " + highestTermCount);
        final double highestTermThreshold = highestTermCount * ratioToTopTermCount;
        Set<HVTToken> tokenSet = new TreeSet<>();
        termCountMap.forEach((k,v) ->{
                    String term = k;
                    long termCount = v.longValue();
                    long docCount = termDocMap.get(k).size();
                    double idf = Math.log(lineCount/docCount);  // per line basis
                    double tf  = (double)docCount/termCount;  // for entire file
                    double tfidf = 1/(tf * idf);
                    //  && tf > tfidfThreshold
                    boolean meetsMinCountCutoff = termCount > minTokenCount;
                    boolean meetsAllCountCutoff = termCount > allCountCutoff;
                    boolean meetsHighestTermCutoff = termCount > highestTermThreshold;
                    boolean meetsTfIDFThreshold = tfidf > tfidfThreshold;
                    if(meetsMinCountCutoff && meetsAllCountCutoff && meetsHighestTermCutoff && meetsTfIDFThreshold) {
                        tokenSet.add(new HVTToken(term, termCount, docCount, tf, idf, tfidf));
                    }

                }
        );
        printTokens(tokenSet);
        return tokenSet;
    }

    protected void printTokens(Set<HVTToken> tokenSet){

        System.out.println("------------------------------------------------------------------");
        System.out.println(String.format("| %-25s	|	%5s %5s %10s %10s|", "Token", "Count", "TF", "IDF", "tf-idf"));
        System.out.println("------------------------------------------------------------------");

        tokenSet.forEach((t) ->{
                System.out.println(String.format("| %-25s	|	%5s %8.4f %8.4f  %8.4f|", t.term, t.termCount, t.tfScore, t.idfScore, t.tfIdfScore));
            }
        );
        System.out.println("------------------------------------------------------------------");
    }


    private static class HVTToken implements Comparable<HVTToken>{

        private String term;
        private long termCount;
        private long docCount;
        private double tfScore;
        private double idfScore;
        private double tfIdfScore;

        public HVTToken(String term, long termCount, long docCount, double tfScore, double idfScore, double tfIdfScore) {
            this.term = term;
            this.termCount = termCount;
            this.docCount = docCount;
            this.tfScore = tfScore;
            this.idfScore = idfScore;
            this.tfIdfScore = tfIdfScore;
        }

        @Override
        public String toString() {
            return String.format("| %-18s	|	%5s %5s %8.4f %8.4f  %8.4f|", term, termCount, docCount, tfScore, idfScore, tfIdfScore);
        }

        @Override
        public int compareTo(HVTToken that) {
            if(this.term.equalsIgnoreCase(that.term))
                return 0;

            if(this.tfIdfScore <= that.tfIdfScore)
                return 1;

            return -1;
        }
    }
}

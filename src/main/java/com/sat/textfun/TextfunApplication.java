package com.sat.textfun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.TypeTokenFilter;
import org.apache.lucene.analysis.en.EnglishMinimalStemmer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

@SpringBootApplication
public class TextfunApplication {


	private static Logger logger = LogManager.getLogger(TextfunApplication.class);

	private static final String[] EMPTY_ARRAY = new String[0];

	private static Set<String> stopWordSet = new TreeSet<>();


	private Map<String, LongAdder> termCountMap = new TreeMap<>();
	private String fileName = "";


	public static void main(String[] args) throws Exception {
		SpringApplication.run(TextfunApplication.class, args);
		new HVTAnalyzer("mormon-5rj1te.txt").emitTokens(3);
		new HVTAnalyzer("keto-3adsan.txt").emitTokens(20);
	}




}

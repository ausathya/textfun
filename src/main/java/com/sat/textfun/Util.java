package com.sat.textfun;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Util {


    protected static Stream<String> loadFile(String fileName) throws Exception {
        Path path = Paths.get(Util.class.getClassLoader()
                .getResource(fileName).toURI());
        return Files.lines(path);
    }
}

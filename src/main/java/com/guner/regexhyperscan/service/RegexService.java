package com.guner.regexhyperscan.service;

import com.gliwka.hyperscan.wrapper.CompileErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RegexService {

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            patternFilter();
        } catch (CompileErrorException e) {
            throw new RuntimeException(e);
        }
    }

    private void patternFilter() throws CompileErrorException {
        List<Pattern> patterns = Arrays.asList(
                Pattern.compile("The number is ([0-9]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("The color is (blue|red|orange)"),
                Pattern.compile("The color is (yellow) Second color is (green)")
                // and thousands more
        );

        List<Pattern> allPatterns = new ArrayList<>();
        IntStream.range(0,10).forEach(i -> allPatterns.addAll(patterns));

        Instant t1= Instant.now();
        List<Matcher> matchers = new ArrayList<>();
        String searchedText1= "The number is 7 the NUMber is 27 The number is 37 The color is blue";
        String searchedText2= "The number is 9 the NUMber is 29 The number is 39 The color is red";
        String searchedText3= "The nmber is 6 the NMber is 26 The nmber is 36 The color is yellow Second color is green";
        String searchedText4= "The nmber is 8 the NMber is 28 The nmber is 38 The color is black";
        IntStream.range(0,50000).forEach(i-> {
                    allPatterns.forEach(p -> matchers.add(p.matcher(searchedText1)));
                    allPatterns.forEach(p -> matchers.add(p.matcher(searchedText2)));
                    allPatterns.forEach(p -> matchers.add(p.matcher(searchedText3)));
                    allPatterns.forEach(p -> matchers.add(p.matcher(searchedText4)));
                }
        );

        //now we use the regular java regex api to check for matches - this is not hyperscan specific
        StringBuffer sb= new StringBuffer();
        for (Matcher matcher : matchers) {
            while (matcher.find()) {
                // will print 7 27 37 blue
                // will print 9 29 39 red
                //System.out.printf("%s ",matcher.group(1));
                sb.append(matcher.group(1)).append(" ");
                if (matcher.groupCount()==2) { // Second color is (green) 2. group
                    sb.append(matcher.group(2)).append(" ");
                }
            }
        }
        //System.out.println(sb);
        Instant t2 = Instant.now();
        System.out.println("\nDuration for Regex : " + (t2.toEpochMilli() - t1.toEpochMilli()));
    }
}

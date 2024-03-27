package com.guner.regexhyperscan.service;

import com.gliwka.hyperscan.wrapper.CompileErrorException;
import com.gliwka.hyperscan.wrapper.Database;
import com.gliwka.hyperscan.wrapper.Expression;
import com.gliwka.hyperscan.wrapper.ExpressionFlag;
import com.gliwka.hyperscan.wrapper.Match;
import com.gliwka.hyperscan.wrapper.Scanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class HyperscanDirectUsageSimpleService {

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

        //we define a list containing all of our expressions
        LinkedList<Expression> expressions = new LinkedList<>();

        //the first argument in the constructor is the regular pattern, the latter one is a expression flag
        //make sure you read the original hyperscan documentation to learn more about flags
        //or browse the ExpressionFlag.java in this repo.
        expressions.add(new Expression("[0-9]{5}", EnumSet.of(ExpressionFlag.SOM_LEFTMOST)));
        expressions.add(new Expression("Test", ExpressionFlag.CASELESS));


        //we precompile the expression into a database.
        //you can compile single expression instances or lists of expressions

        //since we're interacting with native handles always use try-with-resources or call the close method after use
        try(Database db = Database.compile(expressions)) {
            //initialize scanner - one scanner per thread!
            //same here, always use try-with-resources or call the close method after use
            try(Scanner scanner = new Scanner())
            {
                //allocate scratch space matching the passed database
                scanner.allocScratch(db);


                //provide the database and the input string
                //returns a list with matches
                //synchronized method, only one execution at a time (use more scanner instances for multithreading)
                List<Match> matches = scanner.scan(db, "12345 test string");

                //matches always contain the expression causing the match and the end position of the match
                //the start position and the matches string it self is only part of a matach if the
                //SOM_LEFTMOST is set (for more details refer to the original hyperscan documentation)
            }

            // Save the database to the file system for later use
            try(OutputStream out = new FileOutputStream("db")) {
                db.save(out);
            }

            // Later, load the database back in. This is useful for large databases that take a long time to compile.
            // You can compile them offline, save them to a file, and then quickly load them in at runtime.
            // The load has to happen on the same type of platform as the save.
            try (InputStream in = new FileInputStream("db");
                 Database loadedDb = Database.load(in)) {
                // Use the loadedDb as before.
            }
        }
        catch (CompileErrorException ce) {
            //gets thrown during  compile in case something with the expression is wrong
            //you can retrieve the expression causing the exception like this:
            Expression failedExpression = ce.getFailedExpression();
        }
        catch(IOException ie) {
            //IO during serializing / deserializing failed
        }
    }
}

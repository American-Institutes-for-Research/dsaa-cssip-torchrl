package gov.census.torch;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jline.console.ConsoleReader;

/**
 * A minimal application for running scripts or starting a repl.
 */
public class ScriptRunner {

    /**
     * Run a script or start a repl. If <code>args[0]</code> exists, treats it as a file name and
     * attempts to evaluate it. Otherwise starts a repl.
     */
    public static void main(String[] args) {

        java.net.URL tv4 =
            ScriptRunner.class.getClassLoader()
            .getResource("gov/census/torch/script/tv4.js");

        java.net.URL baseScript =
            ScriptRunner.class.getClassLoader()
            .getResource("gov/census/torch/script/torch.js");

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("rhino");

        try {
            InputStreamReader reader = 
                new InputStreamReader(tv4.openStream());
            engine.put(ScriptEngine.FILENAME, tv4.toString());
            engine.eval(reader);

            reader = 
                new InputStreamReader(baseScript.openStream());
            engine.put(ScriptEngine.FILENAME, baseScript.toString());
            engine.eval(reader);

            if (args.length > 0) {
                engine.put(ScriptEngine.FILENAME, args[0]);
                engine.eval(new FileReader(args[0]));
            }
            else {
                engine.put(ScriptEngine.FILENAME, "User Input");
                repl(engine);
            }
        }
        catch(IOException ioe) {
            System.err.println("There was a problem reading the script file");
            System.err.println(ioe);
            System.exit(1);
        } catch(ScriptException se) {
            System.err.println("There was a problem executing the script");
            System.err.println(se);
            System.exit(1);
        }
    }

    /**
     * Start a repl that evaluates incoming lines using <code>engine</code>.
     */
    protected static void repl(ScriptEngine engine) 
        throws IOException, ScriptException
    {
        ConsoleReader reader = new ConsoleReader();
        PrintWriter writer = new PrintWriter(reader.getOutput());

        reader.setPrompt("> ");

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                Object result = engine.eval(line);
                if (result != null) {
                    writer.println(result);
                    writer.flush();
                }
            }
            catch(ScriptException e) {
                writer.println(e);
                writer.flush();
            }

            if (line.equalsIgnoreCase("quit"))
                break;
        }
    }
}

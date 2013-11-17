package gov.census.torch;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jline.console.ConsoleReader;

public class ScriptRunner {

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
            engine.eval(reader);

            reader = 
                new InputStreamReader(baseScript.openStream());
            engine.eval(reader);

            if (args.length > 0)
                engine.eval(new FileReader(args[0]));
            else
                repl(engine);
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

    public static void repl(ScriptEngine engine) 
        throws IOException, ScriptException
    {
        ConsoleReader reader = new ConsoleReader();
        PrintWriter writer = new PrintWriter(reader.getOutput());

        reader.setPrompt("> ");

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                Object result = engine.eval(line);
                writer.println(result);
                writer.flush();
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

package gov.census.torch;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ScriptRunner {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar torch.jar SRIPT_FILE");
            System.exit(1);
        }

        java.net.URL baseScript =
            ScriptRunner.class.getClassLoader()
            .getResource("gov/census/torch/torch.js");

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("rhino");

        try {
            InputStreamReader reader = 
                new InputStreamReader(baseScript.openStream());
            engine.eval(reader);
            engine.eval(new FileReader(args[0]));
        }
        catch(IOException ioe) {
            System.err.println("There was a problem reading the script file:");
            System.err.println(ioe);
            System.exit(1);
        } catch(ScriptException se) {
            System.err.println("There was a problem executing the script:");
            System.err.println(se);
            se.printStackTrace();
            System.exit(1);
        }
    }
}

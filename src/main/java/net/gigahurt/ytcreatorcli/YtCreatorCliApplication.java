package net.gigahurt.ytcreatorcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YtCreatorCliApplication {

    public static void main(String[] args) {
        SpringApplication.run(YtCreatorCliApplication.class, quoteArgsForSpringShell(args));
    }

    /**
     * Spring Shell's NonInteractiveShellRunner joins all JVM args with spaces into
     * a single string and re-tokenizes it. This means option values that contain
     * single quotes (e.g. "Let's Play") or that start with '-' (e.g. video IDs like
     * "-9T8lPvnfnc") are misparse by the tokenizer.
     *
     * This method wraps each option value in double quotes so that the re-joined
     * command string is correctly tokenized by Spring Shell.
     */
    static String[] quoteArgsForSpringShell(String[] args) {
        String[] result = new String[args.length];
        boolean nextIsValue = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (nextIsValue) {
                result[i] = "\"" + arg.replace("\"", "\\\"") + "\"";
                nextIsValue = false;
            } else if (arg.startsWith("--") && arg.contains("=")) {
                // --option=value: quote the value part
                int eq = arg.indexOf('=');
                String value = arg.substring(eq + 1);
                result[i] = arg.substring(0, eq + 1) + "\"" + value.replace("\"", "\\\"") + "\"";
                nextIsValue = false;
            } else if (arg.startsWith("--")) {
                // --option with value as the next arg
                result[i] = arg;
                nextIsValue = true;
            } else {
                // command name or positional arg
                result[i] = arg;
                nextIsValue = false;
            }
        }
        return result;
    }
}

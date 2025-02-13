import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static final Pattern CMD_PATTERN = Pattern.compile("([^/]*)(//.*)?");

    private final Scanner scanner;
    private String currentLine;

    CommandType currentCommand;
    int commandNumber = 0;
    String commandSource;
    String arg1;
    int arg2;

    public Parser(Path inputFile) throws IOException {
        this.scanner = new Scanner(inputFile);
    }

    public boolean hasMoreCommands() {
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine().strip();
            if (!currentLine.startsWith("/") && !currentLine.isEmpty()) {
                return true;
            }
        }
        currentLine = null;
        return false;
    }

    public void advance() {
        if (currentLine == null) {
            return;
        }

        Matcher matcher = Parser.CMD_PATTERN.matcher(currentLine);
        if (matcher.find()) {
            commandSource = matcher.group(1).strip();
            commandNumber++;
            String[] parts = commandSource.split(" ");
            if (ArithmeticOp.parse(parts[0]) != null) {
                currentCommand = CommandType.C_ARITHMETIC;
                arg1 = parts[0];
            } else {
                switch (parts[0]) {
                    case "push":
                        currentCommand = CommandType.C_PUSH;
                        arg1 = parts[1];
                        arg2 = Integer.parseInt(parts[2]);
                        break;
                    case "pop":
                        currentCommand = CommandType.C_POP;
                        arg1 = parts[1];
                        arg2 = Integer.parseInt(parts[2]);
                        break;
                    case "label":
                        currentCommand = CommandType.C_LABEL;
                        arg1 = parts[1];
                        break;
                    case "goto":
                        currentCommand = CommandType.C_GOTO;
                        arg1 = parts[1];
                        break;
                    case "if-goto":
                        currentCommand = CommandType.C_IF;
                        arg1 = parts[1];
                        break;
                    case "function":
                        currentCommand = CommandType.C_FUNCTION;
                        arg1 = parts[1];
                        arg2 = Integer.parseInt(parts[2]);
                        break;
                    case "call":
                        currentCommand = CommandType.C_CALL;
                        arg1 = parts[1];
                        arg2 = Integer.parseInt(parts[2]);
                        break;
                    case "return":
                        currentCommand = CommandType.C_RETURN;
                }
            }
        }
    }

    public String currentCommand() {
        return currentCommand == null ? null : currentCommand.name();
    }

    public String arg1() {
        return arg1;
    }

    public int arg2() {
        return arg2;
    }

    public void close() throws IOException {
        scanner.close();
    }
}

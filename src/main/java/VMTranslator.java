import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VMTranslator {
    public static void main(String[] args) throws IOException {
        String inputName = args[0];
        Path inputPath = Paths.get(inputName);
        Path outputPath;
        List<Path> filesToParse;
        if (Files.isDirectory(inputPath)) {
            outputPath = Paths.get(inputName + ".asm");
            filesToParse = listVmFiles(inputPath);
        } else {
            int extIndex = inputName.lastIndexOf('.');
            outputPath = Paths.get(inputName.substring(0, extIndex) + ".asm");
            filesToParse = List.of(inputPath);
        }

        try (CodeWriter writer = new CodeWriter(outputPath)) {
            for (Path file : filesToParse) {
                String fileName = file.getFileName().toString();
                writer.setFileName(fileName.substring(0, fileName.lastIndexOf('.')));
                writer.writeNewFile();
                Parser parser = new Parser(file);
                try {
                    while (parser.hasMoreCommands()) {
                        parser.advance();
                        switch (parser.currentCommand) {
                            case C_ARITHMETIC:
                                writer.writeArithmetic(parser.arg1, parser.commandSource, parser.commandNumber);
                                break;
                            case C_POP:
                            case C_PUSH:
                                writer.writePushPop(parser.currentCommand, parser.commandSource,
                                        parser.arg1, parser.arg2, parser.commandNumber);
                        }
                    }
                } finally {
                    parser.close();
                }
            }

        }
    }

    private static List<Path> listVmFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(file -> !Files.isDirectory(file) && file.getFileName().endsWith(".vm"))
                    .collect(Collectors.toList());
        }
    }
}

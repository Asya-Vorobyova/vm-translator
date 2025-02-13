import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParserTest {
    @Test
    public void testPattern() {
        String line = "push constant 7 //";
        Matcher matcher = Parser.CMD_PATTERN.matcher(line);
        int a = 12 / 2 / 3;
        if (matcher.find()) {
            String cmd = matcher.group(1);
            assertNotNull(cmd);
        }
    }
}

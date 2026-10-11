package com.michiganecodata.beestinction.sim;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scans the sim's main sources for code that breaks the hard rules in CLAUDE.md A.5 (comments and string
 * literals are ignored). If this fails, read the message: it names the file, the line, and the rule.
 */
class HardRulesTest {

    private static final String[][] FORBIDDEN = {
            {"\\b(float|double|Float|Double)\\b", "A.5 rule 2: integer-only simulation (use percent, basis points, or milli-units)"},
            {"Math\\s*\\.\\s*random|\\bjava\\.util\\.Random\\b|\\bnew\\s+Random\\s*\\(|\\bThreadLocalRandom\\b|\\bSecureRandom\\b",
                    "A.5 rule 3: randomness only through GameRandom"},
            {"currentTimeMillis|nanoTime|\\bjava\\.time\\b|\\bInstant\\b|\\bLocalDate(Time)?\\b|\\bnew\\s+Date\\s*\\(",
                    "A.5 rule 5: time inside the sim is the tick counter only"},
            {"\\b(HashMap|HashSet|Hashtable)\\b", "A.5 rule 4: deterministic iteration (use ArrayList, LinkedHashMap, TreeMap, EnumMap)"},
            {"\\bThread\\b|\\bjava\\.util\\.concurrent\\b|\\bsynchronized\\b|\\bExecutorService\\b",
                    "A.5 rule 7: no threads"},
            {"\\bcom\\.badlogic\\b", "A.5 rule 1: no libGDX in sim"},
            {"\\bjava\\.lang\\.reflect\\b|Class\\s*\\.\\s*forName|getDeclaredField|\\bGson\\b|\\bObjectMapper\\b|\\bKryo\\b",
                    "A.5 rule 6: no reflection-based serialization"},
    };

    @Test
    void simSourcesFollowTheHardRules() throws IOException {
        Path root = Paths.get(System.getProperty("beestinction.simSourceDir", "src/main/java"));
        assertTrue(Files.isDirectory(root), "sim source folder not found: " + root.toAbsolutePath());
        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
        assertFalse(files.isEmpty());
        List<String> problems = new ArrayList<>();
        for (Path file : files) {
            String code = stripCommentsAndStrings(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
            String[] lines = code.split("\n", -1);
            for (String[] rule : FORBIDDEN) {
                Pattern p = Pattern.compile(rule[0]);
                for (int i = 0; i < lines.length; i++) {
                    Matcher m = p.matcher(lines[i]);
                    if (m.find()) {
                        problems.add(root.relativize(file) + ":" + (i + 1) + " uses '" + m.group() + "' -- " + rule[1]);
                    }
                }
            }
        }
        assertTrue(problems.isEmpty(), "Hard-rule violations in sim:\n  " + String.join("\n  ", problems));
    }

    @Test
    void stripperRemovesCommentsAndStringsButKeepsLines() {
        String code = "int a; // double\n/* float\n */ String s = \"Thread\"; char c = 'x';\nlong b;";
        String stripped = stripCommentsAndStrings(code);
        assertFalse(stripped.contains("double") || stripped.contains("float") || stripped.contains("Thread"));
        assertTrue(stripped.contains("long b;"));
        assertTrue(stripped.split("\n", -1).length == 4);
    }

    /** Replaces comments and string/char literals with spaces, keeping newlines so line numbers stay right. */
    static String stripCommentsAndStrings(String src) {
        StringBuilder out = new StringBuilder(src.length());
        int i = 0;
        int n = src.length();
        while (i < n) {
            char c = src.charAt(i);
            char next = i + 1 < n ? src.charAt(i + 1) : '\0';
            if (c == '/' && next == '/') {
                while (i < n && src.charAt(i) != '\n') {
                    out.append(' ');
                    i++;
                }
            } else if (c == '/' && next == '*') {
                out.append("  ");
                i += 2;
                while (i < n && !(src.charAt(i) == '*' && i + 1 < n && src.charAt(i + 1) == '/')) {
                    out.append(src.charAt(i) == '\n' ? '\n' : ' ');
                    i++;
                }
                out.append("  ");
                i += 2;
            } else if (c == '"' || c == '\'') {
                char quote = c;
                boolean textBlock = c == '"' && src.startsWith("\"\"\"", i);
                if (textBlock) {
                    out.append("   ");
                    i += 3;
                    while (i < n && !src.startsWith("\"\"\"", i)) {
                        out.append(src.charAt(i) == '\n' ? '\n' : ' ');
                        i++;
                    }
                    out.append("   ");
                    i += 3;
                    continue;
                }
                out.append(' ');
                i++;
                while (i < n && src.charAt(i) != quote) {
                    if (src.charAt(i) == '\\') {
                        out.append(' ');
                        i++;
                    }
                    out.append(' ');
                    i++;
                }
                out.append(' ');
                i++;
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}

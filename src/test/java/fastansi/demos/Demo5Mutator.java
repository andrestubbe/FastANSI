package fastansi.demos;

import fastansi.FastANSI;
import fastterminal.FastTerminal;

/**
 * Demo 5: FastANSI Mutator — centered via FastTerminal.getWindowSize()
 *
 * The word "FastANSI" mutates live through every ANSI effect the library provides.
 * Terminal size is queried via FastTerminal JNI to place the animation exactly
 * in the center of the screen — regardless of window size.
 */
public class Demo5Mutator {

    static final String[] LETTERS = {"F", "a", "s", "t", "A", "N", "S", "I"};
    static final int LEN = LETTERS.length;

    static final int[][] RAINBOW = {
        {255,  50,  50},
        {255, 150,  50},
        {255, 230,  50},
        { 80, 230,  80},
        { 50, 200, 255},
        { 80,  80, 255},
        {180,  80, 255},
        {255,  80, 200},
    };

    static final char[] GLITCH_CHARS = "!@#$%^&*<>?/\\|~`[]{}".toCharArray();
    static final int TEXT_WIDTH = LEN * 2 - 1;

    public static void main(String[] args) throws InterruptedException {
        int[] size = FastTerminal.getWindowSize(120, 30);
        int cols = size[0];
        int rows = size[1];

        int row      = rows / 2;
        int colStart = (cols - TEXT_WIDTH) / 2;

        System.out.print(FastANSI.ALT_BUFFER_ON);
        System.out.print(FastANSI.CURSOR_HIDE);
        System.out.print(FastANSI.CLEAR_SCREEN);
        System.out.flush();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.RESET);
            System.out.print(FastANSI.CURSOR_SHOW);
            System.out.print(FastANSI.ALT_BUFFER_OFF);
            System.out.flush();
        }));

        // ── Phase 1: Appear ────────────────────────────────────────────────
        label(row - 2, "Phase 1 · Appear", cols);
        for (int i = 0; i < LEN; i++) {
            at(row, colStart + i * 2, FastANSI.FG_BRIGHT_WHITE + LETTERS[i] + FastANSI.RESET);
            Thread.sleep(120);
        }
        Thread.sleep(600);

        // ── Phase 2: Rainbow ───────────────────────────────────────────────
        label(row - 2, "Phase 2 · Rainbow", cols);
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int shift = 0; shift < LEN; shift++) {
                for (int i = 0; i < LEN; i++) {
                    int[] c = RAINBOW[(i + shift) % LEN];
                    at(row, colStart + i * 2, FastANSI.fg(c[0], c[1], c[2]) + LETTERS[i] + FastANSI.RESET);
                }
                Thread.sleep(90);
            }
        }
        Thread.sleep(400);

        // ── Phase 3: Style sweep ───────────────────────────────────────────
        String[][] styles = {
            {FastANSI.BOLD,          FastANSI.BOLD_OFF,          "Phase 3 · Bold         "},
            {FastANSI.ITALIC,        FastANSI.ITALIC_OFF,        "Phase 3 · Italic       "},
            {FastANSI.UNDERLINE,     FastANSI.UNDERLINE_OFF,     "Phase 3 · Underline    "},
            {FastANSI.STRIKETHROUGH, FastANSI.STRIKETHROUGH_OFF, "Phase 3 · Strikethrough"},
        };
        for (String[] style : styles) {
            label(row - 2, style[2], cols);
            for (int i = 0; i < LEN; i++) {
                int[] c = RAINBOW[i];
                at(row, colStart + i * 2,
                    FastANSI.fg(c[0], c[1], c[2]) + style[0] + LETTERS[i] + style[1] + FastANSI.RESET);
                Thread.sleep(80);
            }
            Thread.sleep(500);
        }

        // ── Phase 4: Glitch ────────────────────────────────────────────────
        label(row - 2, "Phase 4 · Glitch       ", cols);
        java.util.Random rnd = new java.util.Random(42);
        for (int round = 0; round < 20; round++) {
            for (int i = 0; i < LEN; i++) {
                boolean glitch = rnd.nextInt(3) == 0;
                int[] c = RAINBOW[i];
                String ch    = glitch ? String.valueOf(GLITCH_CHARS[rnd.nextInt(GLITCH_CHARS.length)]) : LETTERS[i];
                String color = glitch ? FastANSI.fg(255, 255, 80) : FastANSI.fg(c[0], c[1], c[2]);
                at(row, colStart + i * 2, color + ch + FastANSI.RESET);
            }
            Thread.sleep(60);
        }
        for (int i = 0; i < LEN; i++) {
            at(row, colStart + i * 2, FastANSI.fg(RAINBOW[i][0], RAINBOW[i][1], RAINBOW[i][2]) + LETTERS[i] + FastANSI.RESET);
        }
        Thread.sleep(400);

        // ── Phase 5: Invert pulse ──────────────────────────────────────────
        label(row - 2, "Phase 5 · Invert       ", cols);
        for (int pulse = 0; pulse < 6; pulse++) {
            boolean inv = (pulse % 2 == 0);
            for (int i = 0; i < LEN; i++) {
                int[] c = RAINBOW[i];
                String s = inv
                    ? FastANSI.fg(c[0], c[1], c[2]) + FastANSI.INVERT + LETTERS[i] + FastANSI.INVERT_OFF + FastANSI.RESET
                    : FastANSI.fg(c[0], c[1], c[2]) + LETTERS[i] + FastANSI.RESET;
                at(row, colStart + i * 2, s);
            }
            Thread.sleep(200);
        }
        Thread.sleep(300);

        // ── Phase 6: Fade to white ─────────────────────────────────────────
        label(row - 2, "Phase 6 · Calm         ", cols);
        for (int step = 0; step <= 20; step++) {
            float t = step / 20f;
            for (int i = 0; i < LEN; i++) {
                int[] c = RAINBOW[i];
                int r = (int)(c[0] + (255 - c[0]) * t);
                int g = (int)(c[1] + (255 - c[1]) * t);
                int b = (int)(c[2] + (255 - c[2]) * t);
                at(row, colStart + i * 2, FastANSI.fg(r, g, b) + LETTERS[i] + FastANSI.RESET);
            }
            Thread.sleep(50);
        }

        // ── Final ──────────────────────────────────────────────────────────
        label(row - 2, "                       ", cols);
        for (int i = 0; i < LEN; i++) {
            at(row, colStart + i * 2, FastANSI.FG_BRIGHT_WHITE + FastANSI.BOLD + LETTERS[i] + FastANSI.RESET);
        }
        String link = "github.com/andrestubbe/FastANSI";
        at(row + 2, (cols - link.length()) / 2, FastANSI.fg(100, 100, 100) + link + FastANSI.RESET);
        Thread.sleep(3000);
    }

    static void at(int row, int col, String text) {
        System.out.print(FastANSI.cursorTo(row, col) + text);
        System.out.flush();
    }

    static void label(int row, String text, int cols) {
        int col = (cols - text.length()) / 2;
        System.out.print(FastANSI.cursorTo(row, col) + FastANSI.fg(80, 80, 80) + text + FastANSI.RESET);
        System.out.flush();
    }
}

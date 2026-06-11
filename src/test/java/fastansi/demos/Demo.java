package fastansi.demos;

import fastansi.FastANSI;
import fastansi.FastAnsiImage;
import fastansi.FastAnsiImage.Mode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * FastAnsiImage Demo — full-terminal image & video player.
 *
 * Usage:
 *   java Demo                      → show test pattern (all modes)
 *   java Demo image.png            → show image in all 4 modes (3s each)
 *   java Demo video.mp4            → play video at full terminal size
 *   java Demo video.mp4 RAMP       → force a specific mode
 *   java Demo anim.gif             → play animated GIF (no ffmpeg needed)
 *
 * Modes: HALF_BLOCK (default for video), FULL_BLOCK, RAMP, HYBRID
 *
 * HOW VIDEO CONVERSION WORKS:
 *   1. ffmpeg spawned as subprocess: reads video, outputs raw RGB24 frames
 *   2. ffmpeg pre-scales to exact terminal size × 2 rows (for HALF_BLOCK)
 *   3. Java reads raw bytes, converts each pair of rows → ▀ half-block cell
 *   4. ANSI escape codes assembled and printed to stdout with cursor-home
 *   No intermediate file. No pre-conversion. Just: java Demo myvideo.mp4
 *
 * Requires: ffmpeg on PATH (for .mp4 / .mkv / .avi / .webm / .mov)
 *           GIF works natively without ffmpeg.
 */
public class Demo {

    private static final String CURSOR_HOME = "\033[H";  // fast cursor-to-1,1 (no clear)

    public static void main(String[] args) throws Exception {

        // ── Parse arguments ──────────────────────────────────────────────────
        String filePath = null;
        Mode forceMode = null;
        boolean loopMode = false;
        boolean pingPong = false;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--loop")) loopMode = true;
            else if (arg.equalsIgnoreCase("--pingpong")) pingPong = true;
            else if (filePath == null) filePath = arg;
            else forceMode = Mode.valueOf(arg.toUpperCase());
        }

        // ── Windows True Color / Terminal size ───────────────────────────────
        int cols, rows;
        try {
            fastterminal.FastTerminal.setAnsiRawMode(true);
            int[] size = getTerminalSize();
            cols = Math.min(size[0], 120);
            // We want max 30 rows for the video.
            rows = Math.min(size[1], 30); 
        } catch (Throwable ignored) {
            int[] size = getTerminalSize();
            cols = Math.min(size[0], 120);
            rows = Math.min(size[1], 30);
        }

        // ── Alt buffer + hide cursor ─────────────────────────────────────────
        System.out.print(FastANSI.ALT_BUFFER_ON + FastANSI.CURSOR_HIDE + CURSOR_HOME + "\033[2J");
        System.out.flush();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.RESET + FastANSI.CURSOR_SHOW + FastANSI.ALT_BUFFER_OFF);
            System.out.flush();
        }));

        // ── Dispatch ─────────────────────────────────────────────────────────
        if (filePath == null) {
            runTestPattern(cols, rows);
            return;
        }

        String ext = filePath.toLowerCase().replaceAll(".*\\.", "");
        try {
            File f = new File(filePath);
            if (f.getName().toLowerCase().endsWith(".gif")) {
                playGif(f, cols, rows, forceMode != null ? forceMode : Mode.HALF_BLOCK);
            } else if (java.util.Set.of("png","jpg","jpeg","bmp","wbmp").contains(ext)) {
                showImage(f, cols, rows);
            } else {
                playVideo(f, cols, rows, forceMode != null ? forceMode : Mode.HALF_BLOCK, loopMode, pingPong);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // VIDEO PLAYBACK
    // =========================================================================

    private static void playVideo(File file, int cols, int rows, Mode mode, boolean loopMode, boolean pingPong) throws Exception {
        System.err.println("[FastAnsiImage] Opening: " + file.getName() +
                           "  " + cols + "x" + rows + " " + mode + " mode" + 
                           (pingPong ? " [PING PONG PRE-LOAD]" : (loopMode ? " [LOOP PRE-LOAD]" : "")));

        // ── LOOP/PING PONG MODE (Pre-load to memory, max FPS) ─────────────────
        if (loopMode || pingPong) {
            java.io.File cacheFile = new java.io.File(file.getAbsolutePath() + "_" + cols + "x" + rows + "_" + mode + ".ansicache.gz");
            java.util.List<String> frames = null;

            if (cacheFile.exists()) {
                System.out.println("Found cached frames! Bypassing ffmpeg and loading directly from disk...");
                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.util.zip.GZIPInputStream(new java.io.FileInputStream(cacheFile)))) {
                    frames = (java.util.List<String>) ois.readObject();
                } catch (Exception e) {
                    System.err.println("Failed to read cache, falling back to ffmpeg.");
                    frames = null;
                }
            }

            if (frames == null) {
                if (!FastAnsiImage.ffmpegAvailable() && !FastAnsiImage.jcodecAvailable()) {
                    System.err.println("No video backend found (neither ffmpeg nor JCodec).");
                    System.exit(1);
                }
                System.out.println("Pre-loading video frames to memory... please wait.");
                try (FastAnsiImage.FrameSource src = FastAnsiImage.fromVideoFile(file, cols, rows, mode)) {
                    frames = FastAnsiImage.preRenderToStrings(src, cols, rows, mode);
                }
                
                System.out.println("Saving " + frames.size() + " frames to compressed cache...");
                try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.util.zip.GZIPOutputStream(new java.io.FileOutputStream(cacheFile)))) {
                    oos.writeObject(frames);
                } catch (Exception e) {
                    System.err.println("Failed to write cache: " + e.getMessage());
                }
            }

            System.out.println("Loaded " + frames.size() + " frames. Starting " + (pingPong ? "Ping-Pong" : "Loop") + " playback!");
            Thread.sleep(1000);
            
            System.out.print(FastANSI.CLEAR_SCREEN);
            long t0 = System.nanoTime();
            long framesPlayed = 0;
            int idx = 0;
            int dir = 1;
            // Target 60 FPS for loop/pingpong
            long frameNs = 1_000_000_000L / 60; 
            
            while (true) {
                long ft = System.nanoTime();
                
                // Combine into single print to prevent tearing/flickering
                // Centered FastTerminal-style HUD (permanent)
                double liveFps = framesPlayed / ((System.nanoTime() - t0) / 1e9);
                String line1 = " [ FastANSI True-Color ] ";
                String line2 = String.format("  MODE: %s (%s)  ", mode, pingPong ? "PingPong" : "Loop");
                String line3 = String.format("  %dx%d  %.1f FPS  FRAME %d  ", cols, rows, liveFps, framesPlayed);
                
                int centerY = rows / 2;
                String hud = FastANSI.cursorTo(centerY - 1, (cols - line1.length()) / 2 + 1) + FastANSI.bg(245, 158, 11) + FastANSI.fg(0, 0, 0) + line1 + FastANSI.RESET
                           + FastANSI.cursorTo(centerY,     (cols - line2.length()) / 2 + 1) + FastANSI.bg(7, 7, 15)     + FastANSI.fg(255, 255, 255) + line2 + FastANSI.RESET
                           + FastANSI.cursorTo(centerY + 1, (cols - line3.length()) / 2 + 1) + FastANSI.bg(7, 7, 15)     + FastANSI.fg(245, 158, 11) + line3 + FastANSI.RESET;
                
                String syncStart = "\033[?2026h";
                String syncEnd = "\033[?2026l";
                String frame = syncStart + CURSOR_HOME + frames.get(idx) + hud + syncEnd;
                System.out.write(frame.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                System.out.flush();
                
                framesPlayed++;
                
                if (pingPong) {
                    idx += dir;
                    if (idx >= frames.size() - 1 || idx <= 0) dir *= -1; // Bounce
                } else {
                    idx++;
                    if (idx >= frames.size()) idx = 0; // Loop forward
                }
                
                long sleep = (frameNs - (System.nanoTime() - ft)) / 1_000_000;
                if (sleep > 0) Thread.sleep(sleep);
            }
        }

        // ── STREAMING MODE (Live decoding) ─────────────────────────────
        if (!FastAnsiImage.ffmpegAvailable() && !FastAnsiImage.jcodecAvailable()) {
            System.err.println("No video backend found (neither ffmpeg nor JCodec).");
            System.exit(1);
        }

        try (FastAnsiImage.FrameSource src = FastAnsiImage.fromVideoFile(file, cols, rows, mode)) {
            long   frameCount = 0;
            long   t0         = System.nanoTime();
            double fps        = src.getFps();
            long   frameNs    = fps > 0 ? (long)(1_000_000_000.0 / fps) : 0;

            while (src.hasNext()) {
                long ft = System.nanoTime();
                java.awt.image.BufferedImage frame = src.nextFrame();
                if (frame == null) break;

                String ansi = FastAnsiImage.toString(frame, cols, rows, mode);
                // ── Live FPS overlay (Centered) ───────────────────
                frameCount++;
                double elapsed = (System.nanoTime() - t0) / 1e9;
                double liveFps = frameCount / elapsed;
                
                String line1 = " [ FastANSI True-Color ] ";
                String line2 = String.format("  MODE: %s (Streaming)  ", mode);
                String line3 = String.format("  %dx%d  %.1f FPS  FRAME %d  ", cols, rows, liveFps, frameCount);
                
                int centerY = rows / 2;
                String hud = FastANSI.cursorTo(centerY - 1, (cols - line1.length()) / 2 + 1) + FastANSI.bg(245, 158, 11) + FastANSI.fg(0, 0, 0) + line1 + FastANSI.RESET
                           + FastANSI.cursorTo(centerY,     (cols - line2.length()) / 2 + 1) + FastANSI.bg(7, 7, 15)     + FastANSI.fg(255, 255, 255) + line2 + FastANSI.RESET
                           + FastANSI.cursorTo(centerY + 1, (cols - line3.length()) / 2 + 1) + FastANSI.bg(7, 7, 15)     + FastANSI.fg(245, 158, 11) + line3 + FastANSI.RESET;
                
                String syncStart = "\033[?2026h";
                String syncEnd = "\033[?2026l";
                String fullFrame = syncStart + CURSOR_HOME + ansi + hud + syncEnd;
                System.out.write(fullFrame.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                System.out.flush();

                // ── Frame pacing ───────────────────────────────────────────
                if (frameNs > 0) {
                    long sleep = (frameNs - (System.nanoTime() - ft)) / 1_000_000;
                    if (sleep > 0) Thread.sleep(sleep);
                }
            }
        }

        // Hold last frame
        Thread.sleep(Long.MAX_VALUE);
    }

    // =========================================================================
    // GIF PLAYBACK
    // =========================================================================

    private static void playGif(File file, int cols, int rows, Mode mode) throws Exception {
        System.err.println("[FastAnsiImage] GIF: " + file.getName() +
                           " → " + cols + "×" + rows + " " + mode);

        // GIFs are fully loaded into memory, loop forever
        while (true) {
            try (FastAnsiImage.FrameSource src = FastAnsiImage.fromGif(file)) {
                double fps     = src.getFps();
                long   frameNs = fps > 0 ? (long)(1_000_000_000.0 / fps) : 0;
                int    frame   = 0;

                while (src.hasNext()) {
                    long t = System.nanoTime();
                    BufferedImage img = src.nextFrame();
                    if (img == null) break;

                    System.out.print(CURSOR_HOME);
                    System.out.print(FastAnsiImage.toString(img, cols, rows, mode));
                    System.out.print(FastANSI.cursorTo(rows, 1));
                    System.out.print(FastANSI.bg(0,0,0) + FastANSI.fg(80,80,80)
                        + String.format(" GIF  %dx%d  %.1f fps  frame %d/%d ",
                            cols, rows, fps, ++frame, src.getFrameCount())
                        + FastANSI.RESET);
                    System.out.flush();

                    if (frameNs > 0) {
                        long sleep = (frameNs - (System.nanoTime() - t)) / 1_000_000;
                        if (sleep > 0) Thread.sleep(sleep);
                    }
                }
            }
        }
    }

    // =========================================================================
    // STILL IMAGE — cycle all 4 modes
    // =========================================================================

    private static void showImage(File file, int cols, int rows) throws Exception {
        BufferedImage src = ImageIO.read(file);
        if (src == null) { System.err.println("Cannot read image: " + file); return; }

        Mode[] modes  = Mode.values();
        String[] desc = {
            "HALF_BLOCK  ▀  2× vertical resolution  (recommended)",
            "FULL_BLOCK  █  1:1 cell pixels  (purest colour)",
            "RAMP        ., ASCII density chars",
            "HYBRID      ▓  density char over pixel background"
        };

        for (int i = 0; i < modes.length; i++) {
            System.out.print(CURSOR_HOME);
            System.out.print(FastAnsiImage.toString(src, cols, rows - 1, modes[i]));
            System.out.print(FastANSI.cursorTo(rows, 1));
            System.out.print(FastANSI.bg(0,0,0) + FastANSI.fg(180,180,180)
                + " " + (i+1) + "/" + modes.length + "  " + desc[i]
                + "  (next in 3s…) " + FastANSI.RESET);
            System.out.flush();
            Thread.sleep(3000);
        }

        // Loop back to HALF_BLOCK and hold
        System.out.print(CURSOR_HOME);
        System.out.print(FastAnsiImage.toString(src, cols, rows - 1, Mode.HALF_BLOCK));
        System.out.flush();
        Thread.sleep(Long.MAX_VALUE);
    }

    // =========================================================================
    // TEST PATTERN (no file arg)
    // =========================================================================

    private static void runTestPattern(int cols, int rows) throws Exception {
        // Build a rainbow gradient image in memory
        java.awt.image.BufferedImage test =
            new java.awt.image.BufferedImage(cols * 4, rows * 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < test.getHeight(); y++) {
            for (int x = 0; x < test.getWidth(); x++) {
                float hue = (float) x / test.getWidth();
                float val = 0.5f + 0.5f * (float) y / test.getHeight();
                test.setRGB(x, y, java.awt.Color.HSBtoRGB(hue, 1f, val));
            }
        }

        showImageObj(test, cols, rows);
    }

    private static void showImageObj(BufferedImage src, int cols, int rows) throws Exception {
        Mode[] modes = Mode.values();
        String[] desc = {
            "HALF_BLOCK", "FULL_BLOCK", "RAMP", "HYBRID"
        };
        for (int i = 0; i < modes.length; i++) {
            System.out.print(CURSOR_HOME);
            System.out.print(FastAnsiImage.toString(src, cols, rows - 1, modes[i]));
            System.out.print(FastANSI.cursorTo(rows, 1));
            System.out.print(FastANSI.bg(0,0,0) + FastANSI.fg(150,150,150)
                + " TEST PATTERN  mode " + (i+1) + "/4: " + desc[i] + FastANSI.RESET);
            System.out.flush();
            Thread.sleep(2500);
        }
        Thread.sleep(Long.MAX_VALUE);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static boolean isVideo(String ext) {
        return switch (ext) {
            case "mp4","mkv","avi","webm","mov","flv","wmv","ts","m4v" -> true;
            default -> false;
        };
    }

    private static int[] getTerminalSize() {
        // Try stty, fall back to sane defaults
        try {
            Process p = new ProcessBuilder("stty", "size").redirectInput(
                ProcessBuilder.Redirect.from(new File("/dev/tty"))).start();
            String out = new String(p.getInputStream().readAllBytes()).trim();
            if (!out.isEmpty()) {
                String[] parts = out.split("\\s+");
                if (parts.length >= 2) {
                    return new int[]{Integer.parseInt(parts[1]), Integer.parseInt(parts[0])};
                }
            }
        } catch (Exception ignored) {}
        // Windows / fallback
        try {
            String cols = System.getenv("COLUMNS");
            String lins = System.getenv("LINES");
            if (cols != null && lins != null)
                return new int[]{Integer.parseInt(cols), Integer.parseInt(lins)};
        } catch (Exception ignored) {}
        return new int[]{120, 40};
    }
}

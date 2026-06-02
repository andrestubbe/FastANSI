package fastansi.demos;

import fastansi.FastANSI;
import fastansi.FastAnsiImage;
import fastterminal.FastTerminal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Demo 6: FastAnsiImage — Image-to-ANSI Renderer
 *
 * Cycles through all three render modes of FastAnsiImage:
 *   1. RAMP    — Density chars + fg color
 *   2. BLOCK   — Background-colored spaces (purest pixel)
 *   3. HYBRID  — Both combined
 *
 * Usage:  run-demo.bat path/to/image.png
 */
public class Demo {

    public static void main(String[] args) throws Exception {
        String imagePath = args.length > 0 ? args[0] : "docs/screenshot.png";

        File file = new File(imagePath);
        if (!file.exists()) {
            System.err.println("[Demo6] Image not found: " + imagePath);
            return;
        }

        BufferedImage src = ImageIO.read(file);

        int[] size = FastTerminal.getWindowSize(120, 30);
        int cols = size[0];
        int rows = size[1] - 2;

        System.out.print(FastANSI.ALT_BUFFER_ON);
        System.out.print(FastANSI.CURSOR_HIDE);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(FastANSI.RESET);
            System.out.print(FastANSI.CURSOR_SHOW);
            System.out.print(FastANSI.ALT_BUFFER_OFF);
            System.out.flush();
        }));

        FastAnsiImage.Mode[] modes = FastAnsiImage.Mode.values();
        String[] labels = {"RAMP — Density chars + fg color", "BLOCK — Background colored space", "HYBRID — Both combined"};

        for (int i = 0; i < modes.length; i++) {
            System.out.print(FastANSI.CLEAR_SCREEN);
            System.out.print(FastANSI.cursorTo(1, 1));

            String frame = FastAnsiImage.render(src, cols, rows, modes[i]);
            System.out.print(frame);

            System.out.print(FastANSI.RESET);
            System.out.print(FastANSI.cursorTo(rows + 1, 1));
            System.out.print(FastANSI.fg(200, 200, 200) + " Mode " + (i + 1) + "/3: " + labels[i] + FastANSI.RESET);
            System.out.flush();

            Thread.sleep(3000);
        }

        // Loop back to HYBRID and hold
        System.out.print(FastANSI.CLEAR_SCREEN);
        System.out.print(FastANSI.cursorTo(1, 1));
        System.out.print(FastAnsiImage.render(src, cols, rows, FastAnsiImage.Mode.HYBRID));
        System.out.flush();
        Thread.sleep(Long.MAX_VALUE);
    }
}

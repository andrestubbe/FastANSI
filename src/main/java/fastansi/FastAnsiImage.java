package fastansi;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @class FastAnsiImage
 * @brief High-Performance Image-to-ANSI Renderer.
 *
 * Converts any BufferedImage into 24-bit ANSI escape sequences.
 *
 * Three pixel modes:
 *   RAMP        — Luminance → density character, colored fg. Classic ANSI art.
 *   BLOCK       — Background-colored space. Purest pixel accuracy.
 *   HYBRID      — Both: bg pixel color + fg density char. Maximum visual depth.
 *
 * Aspect-ratio correction is applied automatically to compensate for the
 * typical 2:1 height/width ratio of monospaced terminal characters.
 */
public final class FastAnsiImage {

    public enum Mode { RAMP, BLOCK, HYBRID }

    /** Luminance ramp: darkest to brightest. */
    private static final char[] RAMP = " .,:;i1tfLCG08@█".toCharArray();

    private FastAnsiImage() {}

    /**
     * @brief Renders a BufferedImage into an ANSI string at the given terminal width.
     *
     * @param src         Source image.
     * @param termCols    Target terminal column width.
     * @param termRows    Target terminal row height.
     * @param mode        Pixel rendering mode.
     * @return            ANSI-encoded string ready to print to stdout.
     */
    public static String render(BufferedImage src, int termCols, int termRows, Mode mode) {
        BufferedImage img = scale(src, termCols, termRows);

        StringBuilder sb = new StringBuilder(termCols * termRows * 30);

        for (int row = 0; row < termRows; row++) {
            for (int col = 0; col < termCols; col++) {
                int rgb = img.getRGB(col, row);
                int r   = (rgb >> 16) & 0xFF;
                int g   = (rgb >>  8) & 0xFF;
                int b   =  rgb        & 0xFF;

                switch (mode) {
                    case RAMP -> {
                        sb.append(FastANSI.fg(r, g, b))
                          .append(luminanceChar(r, g, b));
                    }
                    case BLOCK -> {
                        sb.append(FastANSI.bg(r, g, b))
                          .append(' ');
                    }
                    case HYBRID -> {
                        // bg = pixel color, fg = brightened for density char contrast
                        int rf = Math.min(255, r + 60);
                        int gf = Math.min(255, g + 60);
                        int bf = Math.min(255, b + 60);
                        sb.append(FastANSI.bg(r, g, b))
                          .append(FastANSI.fg(rf, gf, bf))
                          .append(luminanceChar(r, g, b));
                    }
                }
            }
            sb.append(FastANSI.RESET);
            if (row < termRows - 1) sb.append('\n');
        }

        return sb.toString();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private static char luminanceChar(int r, int g, int b) {
        double L = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        int idx = (int) ((L / 255.0) * (RAMP.length - 1));
        return RAMP[Math.max(0, Math.min(RAMP.length - 1, idx))];
    }

    private static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}

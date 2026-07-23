package fastansi.demos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Standalone demo showcasing TrueColor Half-Block Image Rendering in terminal.
 *
 * How Half-Block rendering works:
 * -------------------------------
 * Terminal character cells have a ~1:2 aspect ratio (twice as tall as wide).
 * Using the Unicode upper half-block character '▀' (\u2580):
 *   - Foreground color controls the TOP half of the cell (\u001b[38;2;R;G;Bm)
 *   - Background color controls the BOTTOM half of the cell (\u001b[48;2;R;G;Bm)
 *
 * This effectively doubles vertical resolution to 2 pixels per character cell!
 *
 * Usage:
 *   java fastansi.demos.HalfBlockImageDemo [optional-path-to-image.png]
 */
public class HalfBlockImageDemo {

    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        BufferedImage image;

        if (args.length > 0 && new File(args[0]).exists()) {
            out.println("Loading image: " + args[0]);
            image = ImageIO.read(new File(args[0]));
        } else {
            out.println("No image provided. Generating procedural test pattern image...");
            image = createProceduralTestImage(160, 80);
        }

        // Target terminal display size in columns and rows
        int termCols = 80;
        int termRows = 40; // 40 terminal rows = 80 vertical image pixels!

        // Scale image to target pixel dimensions (termCols x termRows * 2)
        BufferedImage scaled = scaleImage(image, termCols, termRows * 2);

        // Convert scaled image to ANSI half-block string
        String ansiOutput = renderToHalfBlockAnsi(scaled, termCols, termRows);

        // Print rendered result to terminal
        out.print(ansiOutput);
    }

    /**
     * Converts a BufferedImage into an ANSI half-block string.
     */
    public static String renderToHalfBlockAnsi(BufferedImage image, int cols, int rows) {
        StringBuilder sb = new StringBuilder(cols * rows * 30);

        for (int r = 0; r < rows; r++) {
            int topY = r * 2;
            int botY = r * 2 + 1;

            for (int c = 0; c < cols; c++) {
                // Get RGB values for top and bottom pixels
                int topRgb = image.getRGB(c, topY);
                int botRgb = (botY < image.getHeight()) ? image.getRGB(c, botY) : topRgb;

                int topR = (topRgb >> 16) & 0xFF;
                int topG = (topRgb >> 8) & 0xFF;
                int topB = topRgb & 0xFF;

                int botR = (botRgb >> 16) & 0xFF;
                int botG = (botRgb >> 8) & 0xFF;
                int botB = botRgb & 0xFF;

                // \u001b[38;2;R;G;Bm sets Foreground (top half of '▀')
                // \u001b[48;2;R;G;Bm sets Background (bottom half of '▀')
                sb.append(String.format("\u001b[38;2;%d;%d;%dm\u001b[48;2;%d;%d;%dm▀",
                        topR, topG, topB,
                        botR, botG, botB));
            }
            // Reset colors and break line
            sb.append("\u001b[0m\n");
        }

        return sb.toString();
    }

    /**
     * Scales an image to target width and height using smooth bilinear interpolation.
     */
    private static BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();
        return scaled;
    }

    /**
     * Creates a colorful procedural test image (rainbow gradient + circle) if no file is provided.
     */
    private static BufferedImage createProceduralTestImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Background rainbow gradient
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float hue = (float) x / width;
                float saturation = 0.85f;
                float brightness = (float) y / height;
                img.setRGB(x, y, Color.HSBtoRGB(hue, saturation, brightness));
            }
        }

        // Draw a sharp glowing circle in the center
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(255, 255, 255, 220));
        int diameter = Math.min(width, height) / 2;
        g.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);

        g.setColor(Color.BLACK);
        g.drawString("FAST ANSI IMAGE", (width / 2) - 45, (height / 2) + 5);

        g.dispose();
        return img;
    }
}

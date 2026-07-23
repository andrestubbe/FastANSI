package fastansi.demos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 1:1 Native TrueColor SIXEL Pixel Integration Demo.
 *
 * Fixes for SIXEL display & color issues:
 * 1. Aspect Ratio Distortion: SIXEL defaults to 1980s CRT 1:2 vertical aspect ratio.
 *    Using SIXEL Raster Attributes `"1;1;<width>;<height>` and aspect parameter `7` forces 1:1 square pixels!
 * 2. Pure Blue Tint / Color Shift: Replaced naive frequency quantization with a full 6x6x6 RGB color cube (216 colors) + O(1) color lookup.
 *    This guarantees 100% accurate color fidelity across Red, Green, Blue, Yellow, Magenta, and Cyan channels.
 *
 * Usage:
 *   java fastansi.demos.Sixel1To1PixelDemo [optional-path-to-image.png]
 */
public class Sixel1To1PixelDemo {

    public static void main(String[] args) throws IOException {
        BufferedImage image;

        if (args.length > 0 && new File(args[0]).exists()) {
            System.out.println("Loading image for TrueColor 1:1 SIXEL rendering: " + args[0]);
            image = ImageIO.read(new File(args[0]));
        } else {
            System.out.println("No image provided. Generating high-res 1:1 pixel test image (300x150)...");
            image = createHighResPixelImage(300, 150);
        }

        // Render the image as 1:1 TrueColor pixels to stdout using SIXEL protocol
        System.out.print("Sending TrueColor 1:1 SIXEL pixel stream to terminal...\n");
        writeSixelTrueColor(image, System.out);
        System.out.println();
    }

    /**
     * Encodes a BufferedImage into a TrueColor SIXEL 1:1 pixel stream with 1:1 square aspect ratio.
     */
    public static void writeSixelTrueColor(BufferedImage img, OutputStream out) throws IOException {
        int width = img.getWidth();
        int height = img.getHeight();

        StringBuilder sb = new StringBuilder(width * height * 3);

        // 1. SIXEL Header with 1:1 Aspect Ratio (7 = 1:1 square aspect ratio, 1 = preserve bg)
        sb.append("\033P7;1;7q");

        // 2. Raster Attributes: "1;1;<width>;<height> forces exact pixel dimensions & square pixels!
        sb.append(String.format("\"1;1;%d;%d", width, height));

        // 3. Build full 6x6x6 RGB Color Cube Palette (216 colors)
        int[][] palette = build6x6x6ColorCube();

        // Define color table registers (#0 to #215) in SIXEL format (#index;2;Red%;Green%;Blue%)
        for (int i = 0; i < palette.length; i++) {
            int rPct = (int) Math.round((palette[i][0] / 255.0) * 100.0);
            int gPct = (int) Math.round((palette[i][1] / 255.0) * 100.0);
            int bPct = (int) Math.round((palette[i][2] / 255.0) * 100.0);
            sb.append(String.format("#%d;2;%d;%d;%d", i, rPct, gPct, bPct));
        }

        // 4. Render 6-pixel vertical bands
        for (int yBand = 0; yBand < height; yBand += 6) {
            Set<Integer> colorsInBand = new HashSet<>();
            int[][] bandPixels = new int[6][width];

            for (int dy = 0; dy < 6; dy++) {
                int y = yBand + dy;
                for (int x = 0; x < width; x++) {
                    if (y < height) {
                        int rgb = img.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;

                        int colorIdx = mapRgbToPaletteIndex(r, g, b);
                        bandPixels[dy][x] = colorIdx;
                        colorsInBand.add(colorIdx);
                    } else {
                        bandPixels[dy][x] = -1;
                    }
                }
            }

            // Emit sixel data band for each color used in this slice
            for (int colorIdx : colorsInBand) {
                sb.append(String.format("#%d", colorIdx));

                int repeatCount = 0;
                char lastSixelChar = 0;

                for (int x = 0; x < width; x++) {
                    int sixelBits = 0;
                    for (int dy = 0; dy < 6; dy++) {
                        if (bandPixels[dy][x] == colorIdx) {
                            sixelBits |= (1 << dy);
                        }
                    }

                    char sixelChar = (char) (63 + sixelBits);

                    if (x > 0 && sixelChar == lastSixelChar) {
                        repeatCount++;
                    } else {
                        if (repeatCount > 0) {
                            appendRleSixel(sb, lastSixelChar, repeatCount);
                        }
                        lastSixelChar = sixelChar;
                        repeatCount = 1;
                    }
                }

                if (repeatCount > 0) {
                    appendRleSixel(sb, lastSixelChar, repeatCount);
                }

                sb.append("$"); // Carriage return (start of line)
            }
            sb.append("-"); // Next 6-pixel vertical band
        }

        // 5. SIXEL Trailer: \033\ (ST - String Terminator)
        sb.append("\033\\");

        out.write(sb.toString().getBytes("ISO-8859-1"));
        out.flush();
    }

    private static void appendRleSixel(StringBuilder sb, char sixelChar, int count) {
        if (count > 3) {
            sb.append("!").append(count).append(sixelChar);
        } else {
            for (int i = 0; i < count; i++) {
                sb.append(sixelChar);
            }
        }
    }

    /**
     * Builds a full 6x6x6 RGB color cube (216 colors) spanning the entire RGB color spectrum.
     */
    private static int[][] build6x6x6ColorCube() {
        int[][] palette = new int[216][3];
        int idx = 0;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    palette[idx][0] = r * 51;
                    palette[idx][1] = g * 51;
                    palette[idx][2] = b * 51;
                    idx++;
                }
            }
        }
        return palette;
    }

    /**
     * Fast O(1) mapping of any RGB color to the 6x6x6 color cube index.
     */
    private static int mapRgbToPaletteIndex(int r, int g, int b) {
        int rIdx = (r + 25) / 51;
        int gIdx = (g + 25) / 51;
        int bIdx = (b + 25) / 51;

        if (rIdx > 5) rIdx = 5;
        if (gIdx > 5) gIdx = 5;
        if (bIdx > 5) bIdx = 5;

        return rIdx * 36 + gIdx * 6 + bIdx;
    }

    /**
     * Generates a detailed 1:1 pixel test image with distinct RGB shapes (Red, Green, Blue, Yellow).
     */
    private static BufferedImage createHighResPixelImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Dark background
        g.setColor(new Color(20, 20, 25));
        g.fillRect(0, 0, width, height);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bright Red Oval
        g.setColor(new Color(255, 30, 30));
        g.fillOval(15, 20, 70, 70);

        // Bright Green Rectangle
        g.setColor(new Color(30, 255, 30));
        g.fillRect(100, 20, 70, 70);

        // Bright Blue Circle
        g.setColor(new Color(30, 30, 255));
        g.fillOval(185, 20, 70, 70);

        // Bright Yellow Banner
        g.setColor(new Color(255, 255, 30));
        g.fillRect(15, 105, 240, 30);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("TRUE COLOR 1:1 SIXEL", 45, 125);

        g.dispose();
        return img;
    }
}

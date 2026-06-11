package fastansi.demos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Generator {

    public static class CharDensity {
        public final char c;
        public final int pixels;
        public final float normalizedDensity;

        public CharDensity(char c, int pixels, float normalizedDensity) {
            this.c = c;
            this.pixels = pixels;
            this.normalizedDensity = normalizedDensity;
        }
    }

    public static void main(String[] args) {
        System.out.println("Generating FastGlyphDensity database...");
        
        String fontName = "Consolas";
        int fontSize = 24;
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        
        int width = 32;
        int height = 32;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        // Generate missing glyph signature
        char missingChar = '\uFFFF';
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(missingChar), (width - fm.charWidth(missingChar)) / 2, 
                    (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2));
        
        int[] missingSignature = new int[width * height];
        img.getRGB(0, 0, width, height, missingSignature, 0, width);
        int missingPixelCount = countPixels(img, width, height);

        List<CharDensity> rawList = new ArrayList<>();
        int maxPixels = 0;
        int validCount = 0;

        for (int i = 32; i <= 126; i++) { // Restrict to standard ASCII letters, numbers, and punctuation
            char c = (char) i;

            if (!font.canDisplay(c) || Character.isWhitespace(c) || Character.isISOControl(c)) {
                if (c != ' ') continue; // We do want space!
            }

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            int x = (width - fm.charWidth(c)) / 2;
            int y = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(String.valueOf(c), x, y);

            // Check if it exactly matches the missing signature
            boolean isMissing = true;
            int[] sig = new int[width * height];
            img.getRGB(0, 0, width, height, sig, 0, width);
            for (int p = 0; p < sig.length; p++) {
                if (sig[p] != missingSignature[p]) {
                    isMissing = false;
                    break;
                }
            }
            if (isMissing && c != ' ') continue;

            int count = countPixels(img, width, height);
            
            // Deduplicate visually identical characters (saves space)
            boolean duplicate = false;
            if (count > 0) {
                // Not strictly deduplicating here to save time, we will deduplicate in the final output by percentage
            }

            rawList.add(new CharDensity(c, count, 0));
            if (count > maxPixels) maxPixels = count;
            validCount++;
        }
        
        g.dispose();

        // 2. Normalize, Deduplicate & Sort
        List<CharDensity> sortedList = new ArrayList<>();
        for (CharDensity cd : rawList) {
            float norm = (float) cd.pixels / maxPixels;
            if (norm > 1.0f) norm = 1.0f;
            sortedList.add(new CharDensity(cd.c, cd.pixels, norm));
        }
        sortedList.sort(Comparator.comparingDouble(a -> a.normalizedDensity));

        // Deduplicate: Keep only the FIRST character for a given exact density percentage (rounded to 3 decimal places)
        List<CharDensity> uniqueList = new ArrayList<>();
        float lastNorm = -1.0f;
        for (CharDensity cd : sortedList) {
            float roundedNorm = Math.round(cd.normalizedDensity * 1000.0f) / 1000.0f;
            if (roundedNorm > lastNorm) {
                uniqueList.add(new CharDensity(cd.c, cd.pixels, roundedNorm));
                lastNorm = roundedNorm;
            }
        }

        // Print Source Code to File
        try {
            java.io.File outDir = new java.io.File("src/main/java/fastansi/density");
            outDir.mkdirs();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(new java.io.File(outDir, "FastGlyphDensity.java")), java.nio.charset.StandardCharsets.UTF_8))) {
                
                writer.println("package fastansi.density;");
                writer.println();
                writer.println("public final class FastGlyphDensity {");
                writer.println("    public static final String FONT = \"" + fontName + "\";");
                writer.println("    public static final int FONT_SIZE = " + fontSize + ";");
                writer.println("    public static final int VERSION = 1;");
                writer.println("    public static final int SCAN_RESOLUTION = " + width + ";");
                writer.println();
                
                writer.println("    private static final char[] GLYPHS = {");
                writer.print("        ");
                for (int i = 0; i < uniqueList.size(); i++) {
                    char c = uniqueList.get(i).c;
                    String esc = String.format("(char) 0x%04x", (int) c);
                    writer.print(esc + ", ");
                    if ((i + 1) % 15 == 0) writer.print("\n        ");
                }
                writer.println("\n    };");
                writer.println();
                
                writer.println("    private static final float[] DENSITY = {");
                writer.print("        ");
                for (int i = 0; i < uniqueList.size(); i++) {
                    writer.print(String.format(java.util.Locale.US, "%.3ff, ", uniqueList.get(i).normalizedDensity));
                    if ((i + 1) % 15 == 0) writer.print("\n        ");
                }
                writer.println("\n    };");
                
                writer.println();
                writer.println("    public static char getGlyphForOpacity(float opacity) {");
                writer.println("        if (opacity <= 0.0f) return GLYPHS[0];");
                writer.println("        if (opacity >= 1.0f) return GLYPHS[GLYPHS.length - 1];");
                writer.println("        // Binary search for closest density");
                writer.println("        int low = 0;");
                writer.println("        int high = DENSITY.length - 1;");
                writer.println("        while (low <= high) {");
                writer.println("            int mid = (low + high) >>> 1;");
                writer.println("            float midVal = DENSITY[mid];");
                writer.println("            if (midVal < opacity) low = mid + 1;");
                writer.println("            else if (midVal > opacity) high = mid - 1;");
                writer.println("            else return GLYPHS[mid];");
                writer.println("        }");
                writer.println("        // low is the insertion point. Find closest between low and low-1");
                writer.println("        if (low >= DENSITY.length) return GLYPHS[GLYPHS.length - 1];");
                writer.println("        if (low == 0) return GLYPHS[0];");
                writer.println("        float diffNext = DENSITY[low] - opacity;");
                writer.println("        float diffPrev = opacity - DENSITY[low - 1];");
                writer.println("        return diffNext < diffPrev ? GLYPHS[low] : GLYPHS[low - 1];");
                writer.println("    }");
                
                writer.println();
                writer.println("    public static float getOpacityForGlyph(char c) {");
                writer.println("        for (int i = 0; i < GLYPHS.length; i++) {");
                writer.println("            if (GLYPHS[i] == c) return DENSITY[i];");
                writer.println("        }");
                writer.println("        return 0.0f; // Unknown glyph");
                writer.println("    }");
                
                writer.println();
                writer.println("    public static String getPalette(int steps) {");
                writer.println("        StringBuilder sb = new StringBuilder(steps);");
                writer.println("        for (int i = 0; i < steps; i++) {");
                writer.println("            sb.append(getGlyphForOpacity((float) i / (steps - 1)));");
                writer.println("        }");
                writer.println("        return sb.toString();");
                writer.println("    }");
                
                writer.println("}");
            }
            System.out.println("Successfully generated src/main/java/fastansi/density/FastGlyphDensity.java!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static int countPixels(BufferedImage img, int w, int h) {
        int count = 0;
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (((img.getRGB(px, py) >> 16) & 0xFF) > 128) count++;
            }
        }
        return count;
    }
}

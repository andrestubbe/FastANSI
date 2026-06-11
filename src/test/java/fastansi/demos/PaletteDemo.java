package fastansi.demos;

import fastansi.FastANSI;
import java.awt.Font;

public class PaletteDemo {

    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println(" FastANSI Palette Demo  |  ASCII Density Calculator");
        System.out.println("=========================================================\n");

        System.out.println("Loading pre-computed pixel density database (FastGlyphDensity)...");
        String palette = fastansi.density.FastGlyphDensity.getPalette(24);

        System.out.println("\nLoaded " + palette.length() + "-character Density Palette (from 0% to 100%):");
        System.out.println(FastANSI.fg(0, 255, 128) + palette + FastANSI.RESET + "\n");

        System.out.println("Rendering 2D Gradient using Calculated Palette:\n");

        int width = 80;
        int height = 24;
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double maxDist = Math.sqrt(centerX * centerX + centerY * centerY);

        for (int y = 0; y < height; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < width; x++) {
                // Calculate distance from center (0.0 to 1.0)
                double dx = (x - centerX) / 1.5; // Aspect ratio correction
                double dy = y - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                // Invert distance so center is brightest (1.0), edges are darkest (0.0)
                double intensity = 1.0 - (dist / (height / 1.5));
                if (intensity < 0) intensity = 0;
                if (intensity > 1) intensity = 1;

                // Pick character from palette based on intensity
                int charIndex = (int) Math.round(intensity * (palette.length() - 1));
                
                // Use pure white foreground
                int r = 255;
                int g = 255;
                int b = 255;
                
                line.append(FastANSI.fg(r, g, b)).append(palette.charAt(charIndex));
            }
            System.out.println(line.toString() + FastANSI.RESET);
        }
        
        System.out.println("\nTo see the detailed JFrame GUI: run AsciiDensityCalculator directly.");
    }
}

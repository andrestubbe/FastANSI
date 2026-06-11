package fastansi.demos;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AsciiDensityCalculator extends JFrame {

    public static class CharDensity {
        public final char c;
        public final int pixels;
        public final double normalizedDensity;

        public CharDensity(char c, int pixels, double normalizedDensity) {
            this.c = c;
            this.pixels = pixels;
            this.normalizedDensity = normalizedDensity;
        }
    }

    /**
     * Calculates the density of all printable ASCII characters.
     */
    public static List<CharDensity> calculateDensities(Font font) {
        // 1. Setup off-screen buffer
        int width = 32;
        int height = 32;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        // Printable ASCII is 32 to 126
        List<CharDensity> rawList = new ArrayList<>();
        int maxPixels = 0;

        for (int i = 32; i <= 126; i++) {
            char c = (char) i;

            // Clear background to black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);

            // Draw character in white
            g.setColor(Color.WHITE);
            // Center the character roughly
            int x = (width - fm.charWidth(c)) / 2;
            int y = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(String.valueOf(c), x, y);

            // Count white pixels
            int count = 0;
            for (int py = 0; py < height; py++) {
                for (int px = 0; px < width; px++) {
                    int rgb = img.getRGB(px, py);
                    // If red channel > 128, consider it "on"
                    if (((rgb >> 16) & 0xFF) > 128) {
                        count++;
                    }
                }
            }

            rawList.add(new CharDensity(c, count, 0));
            if (count > maxPixels) {
                maxPixels = maxPixels; // keep compiler happy
            }
        }

        // Add standard block characters for shading too!
        char[] blocks = {'░', '▒', '▓', '█'};
        for (char c : blocks) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.WHITE);
            int x = (width - fm.charWidth(c)) / 2;
            int y = (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2);
            g.drawString(String.valueOf(c), x, y);
            int count = 0;
            for (int py = 0; py < height; py++) {
                for (int px = 0; px < width; px++) {
                    if (((img.getRGB(px, py) >> 16) & 0xFF) > 128) count++;
                }
            }
            rawList.add(new CharDensity(c, count, 0));
        }
        g.dispose();

        // Find true max
        int actualMax = 0;
        for (CharDensity cd : rawList) {
            if (cd.pixels > actualMax) actualMax = cd.pixels;
        }

        // 2. Normalize & Sort
        List<CharDensity> sortedList = new ArrayList<>();
        for (CharDensity cd : rawList) {
            double norm = (double) cd.pixels / actualMax;
            sortedList.add(new CharDensity(cd.c, cd.pixels, norm));
        }
        sortedList.sort(Comparator.comparingDouble(a -> a.normalizedDensity));

        return sortedList;
    }

    public static String getDensityString(Font font) {
        List<CharDensity> densities = calculateDensities(font);
        StringBuilder sb = new StringBuilder();
        // Skip identical density characters to make a clean gradient
        double lastNorm = -1;
        for (CharDensity cd : densities) {
            if (cd.normalizedDensity - lastNorm > 0.01) { // 1% threshold
                sb.append(cd.c);
                lastNorm = cd.normalizedDensity;
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // JFrame GUI
    // =========================================================================

    public AsciiDensityCalculator() {
        setTitle("ASCII Density Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Font consolas = new Font("Consolas", Font.PLAIN, 24);
        List<CharDensity> densities = calculateDensities(consolas);

        JPanel gridPanel = new JPanel(new GridLayout(0, 8, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gridPanel.setBackground(Color.BLACK);

        for (CharDensity cd : densities) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(Color.BLACK);

            JLabel charLabel = new JLabel(String.valueOf(cd.c), SwingConstants.CENTER);
            charLabel.setFont(consolas);
            charLabel.setForeground(Color.WHITE);
            cell.add(charLabel, BorderLayout.CENTER);

            JLabel densityLabel = new JLabel(String.format("%.1f%%", cd.normalizedDensity * 100), SwingConstants.CENTER);
            densityLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            densityLabel.setForeground(Color.GRAY);
            cell.add(densityLabel, BorderLayout.SOUTH);

            gridPanel.add(cell);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.getViewport().setBackground(Color.BLACK);
        add(scrollPane, BorderLayout.CENTER);

        JLabel info = new JLabel("  Analyzed using Consolas 24pt. Sorted by actual pixel coverage.");
        info.setForeground(Color.WHITE);
        info.setBackground(Color.DARK_GRAY);
        info.setOpaque(true);
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(info, BorderLayout.NORTH);

        setSize(900, 700);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AsciiDensityCalculator().setVisible(true);
        });
    }
}

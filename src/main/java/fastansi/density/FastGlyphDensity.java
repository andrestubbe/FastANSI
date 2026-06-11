package fastansi.density;

public final class FastGlyphDensity {
    public static final String FONT = "Consolas";
    public static final int FONT_SIZE = 24;
    public static final int VERSION = 1;
    public static final int SCAN_RESOLUTION = 32;

    private static final char[] GLYPHS = {
        (char) 0x0020, (char) 0x02be, (char) 0x02d7, (char) 0x034e, (char) 0x00b8, (char) 0x02d0, (char) 0x0060, (char) 0x02c8, (char) 0x02c6, (char) 0x002e, (char) 0x0359, (char) 0x002d, (char) 0x02ea, (char) 0x003a, (char) 0x036f, 
        (char) 0x0027, (char) 0x02dc, (char) 0x0336, (char) 0x02e3, (char) 0x02b3, (char) 0x034a, (char) 0x005e, (char) 0x02c1, (char) 0x005f, (char) 0x002c, (char) 0x02b5, (char) 0x02b2, (char) 0x0337, (char) 0x02b6, (char) 0x02c4, 
        (char) 0x0021, (char) 0x1d58, (char) 0x003b, (char) 0x0022, (char) 0x1d37, (char) 0x00a6, (char) 0x00b2, (char) 0x002f, (char) 0x1d3c, (char) 0x002a, (char) 0x003f, (char) 0x002b, (char) 0x0076, (char) 0x0131, (char) 0x0037, 
        (char) 0x007c, (char) 0x0028, (char) 0x0054, (char) 0x0264, (char) 0x004a, (char) 0x0059, (char) 0x00e7, (char) 0x0031, (char) 0x006c, (char) 0x0033, (char) 0x0046, (char) 0x0074, (char) 0x005b, (char) 0x0032, (char) 0x0049, 
        (char) 0x0035, (char) 0x0056, (char) 0x00c7, (char) 0x005a, (char) 0x0050, (char) 0x0132, (char) 0x0034, (char) 0x0036, (char) 0x00cc, (char) 0x0045, (char) 0x0058, (char) 0x0048, (char) 0x004f, (char) 0x0047, (char) 0x0052, 
        (char) 0x00c5, (char) 0x00c0, (char) 0x0044, (char) 0x00c8, (char) 0x00c2, (char) 0x0057, (char) 0x0042, (char) 0x00f5, (char) 0x0038, (char) 0x0030, (char) 0x00a9, (char) 0x004d, (char) 0x0023, (char) 0x014e, (char) 0x010e, 
        (char) 0x0150, (char) 0x0051, (char) 0x0143, (char) 0x01c1, (char) 0x014a, (char) 0x0025, (char) 0x019d, (char) 0x01e3, (char) 0x0246, (char) 0x0026, (char) 0x0067, (char) 0x01c6, (char) 0x00d1, (char) 0x01cc, (char) 0x0468, 
        (char) 0x01e5, (char) 0x1f86, (char) 0x0123, (char) 0x1f9e, (char) 0x0121, (char) 0x24f0, (char) 0x011d, (char) 0x01e7, (char) 0x01c4, (char) 0x052c, (char) 0x2528, (char) 0x011f, (char) 0x2547, (char) 0x1d83, (char) 0x2548, 
        (char) 0x052a, (char) 0x2523, (char) 0x263b, (char) 0x2542, (char) 0x1d7a, (char) 0x0040, (char) 0xfb16, (char) 0xfb04, (char) 0x01cb, (char) 0x2549, (char) 0x254b, (char) 0x258c, (char) 0x2580, (char) 0x2584, (char) 0x2593, 
        (char) 0x25d8, (char) 0x2588, 
    };

    private static final float[] DENSITY = {
        0.000f, 0.011f, 0.014f, 0.017f, 0.020f, 0.023f, 0.026f, 0.028f, 0.031f, 0.034f, 0.037f, 0.040f, 0.043f, 0.046f, 0.048f, 
        0.051f, 0.054f, 0.057f, 0.060f, 0.063f, 0.066f, 0.068f, 0.071f, 0.074f, 0.077f, 0.080f, 0.083f, 0.085f, 0.088f, 0.091f, 
        0.094f, 0.097f, 0.100f, 0.103f, 0.105f, 0.108f, 0.111f, 0.114f, 0.117f, 0.120f, 0.123f, 0.125f, 0.128f, 0.131f, 0.134f, 
        0.137f, 0.140f, 0.142f, 0.145f, 0.148f, 0.151f, 0.154f, 0.157f, 0.160f, 0.162f, 0.165f, 0.168f, 0.171f, 0.174f, 0.177f, 
        0.179f, 0.182f, 0.185f, 0.188f, 0.191f, 0.194f, 0.197f, 0.199f, 0.202f, 0.205f, 0.208f, 0.211f, 0.214f, 0.217f, 0.219f, 
        0.222f, 0.225f, 0.228f, 0.231f, 0.234f, 0.236f, 0.239f, 0.242f, 0.245f, 0.248f, 0.251f, 0.254f, 0.256f, 0.259f, 0.262f, 
        0.265f, 0.268f, 0.271f, 0.274f, 0.276f, 0.279f, 0.282f, 0.285f, 0.288f, 0.291f, 0.293f, 0.296f, 0.299f, 0.302f, 0.305f, 
        0.308f, 0.311f, 0.313f, 0.316f, 0.319f, 0.322f, 0.325f, 0.328f, 0.330f, 0.333f, 0.336f, 0.339f, 0.342f, 0.345f, 0.348f, 
        0.350f, 0.353f, 0.356f, 0.359f, 0.362f, 0.365f, 0.368f, 0.370f, 0.382f, 0.387f, 0.410f, 0.462f, 0.481f, 0.519f, 0.741f, 
        0.783f, 1.000f, 
    };

    public static char getGlyphForOpacity(float opacity) {
        if (opacity <= 0.0f) return GLYPHS[0];
        if (opacity >= 1.0f) return GLYPHS[GLYPHS.length - 1];
        // Binary search for closest density
        int low = 0;
        int high = DENSITY.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            float midVal = DENSITY[mid];
            if (midVal < opacity) low = mid + 1;
            else if (midVal > opacity) high = mid - 1;
            else return GLYPHS[mid];
        }
        // low is the insertion point. Find closest between low and low-1
        if (low >= DENSITY.length) return GLYPHS[GLYPHS.length - 1];
        if (low == 0) return GLYPHS[0];
        float diffNext = DENSITY[low] - opacity;
        float diffPrev = opacity - DENSITY[low - 1];
        return diffNext < diffPrev ? GLYPHS[low] : GLYPHS[low - 1];
    }

    public static float getOpacityForGlyph(char c) {
        for (int i = 0; i < GLYPHS.length; i++) {
            if (GLYPHS[i] == c) return DENSITY[i];
        }
        return 0.0f; // Unknown glyph
    }

    public static String getPalette(int steps) {
        StringBuilder sb = new StringBuilder(steps);
        for (int i = 0; i < steps; i++) {
            sb.append(getGlyphForOpacity((float) i / (steps - 1)));
        }
        return sb.toString();
    }
}

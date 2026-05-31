package fastansi.demos;

import fastansi.FastANSI;

public class Demo4RealtimeInterceptor {
    public static void main(String[] args) {
        // Setup Alternate Screen Buffer and hide cursor (like FastTerminal)
        System.out.print("\033[?1049h\033[?25l");
        
        long startTime = System.currentTimeMillis();
        StringBuilder rawStream = new StringBuilder();
        StringBuilder renderBuffer = new StringBuilder();
        
        // Ensure graceful exit and terminal reset
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print("\033[?1049l\033[?25h");
        }));

        while (true) {
            long t = (System.currentTimeMillis() - startTime) / 50; // 20 FPS ticks
            int mode = (int) ((System.currentTimeMillis() - startTime) / 4000) % 3; // Switch every 4 seconds

            // 1. GENERATOR: Create a massive, complex RGB Rainbow Plasma ANSI Stream
            // This simulates an external program outputting crazy amounts of color codes
            rawStream.setLength(0);
            rawStream.append("\033[H"); // Cursor home
            
            for (int y = 0; y < 30; y++) {
                rawStream.append("\033[").append(y + 1).append(";1H");
                for (int x = 0; x < 120; x++) {
                    int r = (int) (Math.sin(0.1 * x + t * 0.2) * 127 + 128);
                    int g = (int) (Math.sin(0.1 * y + t * 0.1) * 127 + 128);
                    int b = (int) (Math.sin(0.05 * (x + y) + t * 0.15) * 127 + 128);
                    
                    // Appending raw 24-bit True Color ANSI sequences
                    rawStream.append(FastANSI.fg(r, g, b)).append("█");
                }
            }

            // 2. INTERCEPTOR: Parse the raw stream with FastANSI in real-time
            renderBuffer.setLength(0);
            
            FastANSI.parse(rawStream.toString(), new BaseListener() {
                @Override
                public void onText(CharSequence text, int start, int end) {
                    renderBuffer.append(text, start, end);
                }

                @Override
                public void onCursorPosition(int row, int col) {
                    renderBuffer.append(FastANSI.cursorTo(row, col));
                }

                @Override
                public void onForegroundColor(int type, int r, int g, int b) {
                    if (mode == 0) {
                        // Pass-Through: Original Rainbow
                        renderBuffer.append(FastANSI.fg(r, g, b));
                    } else if (mode == 1) {
                        // Intercept: "Matrix Vision" Filter
                        int brightness = (r + g + b) / 3;
                        renderBuffer.append(FastANSI.fg(0, brightness, 0)); // Only green channel
                    } else if (mode == 2) {
                        // Intercept: "Thermal Vision" Filter
                        int brightness = (r + g + b) / 3;
                        if (brightness < 85) {
                            renderBuffer.append(FastANSI.fg(0, 0, brightness * 3)); // Blue
                        } else if (brightness < 170) {
                            renderBuffer.append(FastANSI.fg((brightness - 85) * 3, 0, 0)); // Red
                        } else {
                            renderBuffer.append(FastANSI.fg(255, (brightness - 170) * 3, 0)); // Yellow
                        }
                    }
                }
            });
            
            // Draw UI Overlay
            renderBuffer.append(FastANSI.cursorTo(1, 2));
            renderBuffer.append(FastANSI.fg(255, 255, 255)).append(FastANSI.BG_BLACK);
            if (mode == 0) renderBuffer.append(" [ MODE: ORIGINAL RAINBOW ] ");
            if (mode == 1) renderBuffer.append(" [ MODE: FASTANSI MATRIX FILTER ] ");
            if (mode == 2) renderBuffer.append(" [ MODE: FASTANSI THERMAL FILTER ] ");
            renderBuffer.append(FastANSI.BG_DEFAULT); // Reset background

            // 3. RENDER: Print the transformed stream
            System.out.print(renderBuffer.toString());

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
    }
}

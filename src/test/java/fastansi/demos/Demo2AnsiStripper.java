package fastansi.demos;

import fastansi.FastANSI;

public class Demo2AnsiStripper {
    public static void main(String[] args) {
        String noisyLogLine = "\033[2K\033[1;31m[CRITICAL]\033[0m \033[38;2;255;165;0mDatabase Connection Timeout\033[0m \033[3m(retrying in 5s)\033[0m";
        
        System.out.println("==================================================");
        System.out.println("FastANSI Demo 2: ANSI Stripper (Logging)");
        System.out.println("==================================================");
        System.out.println("Raw Input: " + noisyLogLine.replace("\033", "ESC"));
        System.out.println("\nExtracted Plain Text:");
        System.out.println("--------------------------------------------------");
        
        StringBuilder plainText = new StringBuilder();
        
        FastANSI.parse(noisyLogLine, new BaseListener() {
            @Override
            public void onText(CharSequence text, int start, int end) {
                // We only append the text, completely ignoring all styling and cursor callbacks
                plainText.append(text, start, end);
            }
        });
        
        System.out.println(plainText.toString());
        System.out.println("--------------------------------------------------");
    }
}

package fastansi.demos;

import fastansi.FastANSI;

public class Demo1MatrixInspector {
    public static void main(String[] args) {
        String input = "\033[1;31m[ERROR]\033[0m \033[33mDisk space low\033[0m at \033[4m/var/log\033[0m";
        
        System.out.println("==================================================");
        System.out.println("FastANSI Demo 1: Matrix Inspector (Visualizer)");
        System.out.println("==================================================");
        System.out.println("Raw Input: " + input.replace("\033", "ESC"));
        System.out.println("\nParsed Event Stream:");
        System.out.println("--------------------------------------------------");
        
        FastANSI.parse(input, new BaseListener() {
            @Override
            public void onText(CharSequence text, int start, int end) {
                System.out.println("[TEXT]  \"" + text.subSequence(start, end) + "\"");
            }
            
            @Override
            public void onReset() {
                System.out.println("[RESET] Styles reset");
            }
            
            @Override
            public void onBold(boolean enable) {
                System.out.println("[STYLE] Bold = " + enable);
            }
            
            @Override
            public void onUnderline(boolean enable) {
                System.out.println("[STYLE] Underline = " + enable);
            }
            
            @Override
            public void onForegroundColor(int type, int r, int g, int b) {
                if (type == FastANSI.COLOR_TYPE_4BIT) {
                    System.out.println("[COLOR] Foreground = 4-bit ANSI Index " + r);
                } else if (type == FastANSI.COLOR_TYPE_24BIT) {
                    System.out.println("[COLOR] Foreground = RGB(" + r + ", " + g + ", " + b + ")");
                }
            }
        });
        
        System.out.println("--------------------------------------------------");
    }
}

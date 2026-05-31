package fastansi.demos;

import fastansi.FastANSI;

public class Demo3HtmlConverter {
    public static void main(String[] args) {
        String consoleOutput = "\033[1;32mSUCCESS:\033[0m Build completed in \033[36m4.2s\033[0m";
        
        System.out.println("==================================================");
        System.out.println("FastANSI Demo 3: ANSI to HTML Converter");
        System.out.println("==================================================");
        System.out.println("Raw Input: " + consoleOutput.replace("\033", "ESC"));
        System.out.println("\nGenerated HTML Code:");
        System.out.println("--------------------------------------------------");
        
        StringBuilder html = new StringBuilder();
        
        FastANSI.parse(consoleOutput, new BaseListener() {
            private boolean isBold = false;
            private String currentSpan = "";
            
            private void openSpan(String style) {
                if (!currentSpan.isEmpty()) html.append("</span>");
                currentSpan = "<span style=\"" + style + "\">";
                html.append(currentSpan);
            }
            
            @Override
            public void onText(CharSequence text, int start, int end) {
                html.append(text, start, end);
            }
            
            @Override
            public void onReset() {
                if (!currentSpan.isEmpty()) html.append("</span>");
                currentSpan = "";
                isBold = false;
            }
            
            @Override
            public void onBold(boolean enable) {
                isBold = enable;
                if (enable) openSpan("font-weight:bold;");
            }
            
            @Override
            public void onForegroundColor(int type, int r, int g, int b) {
                if (type == FastANSI.COLOR_TYPE_4BIT) {
                    String[] colors = {"black", "red", "green", "yellow", "blue", "magenta", "cyan", "white"};
                    if (r >= 0 && r <= 7) {
                        String style = "color:" + colors[r] + ";";
                        if (isBold) style += "font-weight:bold;";
                        openSpan(style);
                    }
                }
            }
        });
        
        System.out.println(html.toString());
        System.out.println("--------------------------------------------------");
    }
}

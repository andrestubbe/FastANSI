package fastansi;

/**
 * High-Performance, Zero-Allocation ANSI and VT100/VT220/Xterm Escape Sequence Parser.
 * Processes characters procedurally without object instantiation, calling back listeners with primitive state values.
 */
public class FastANSI {

    // Color Type Constants
    public static final int COLOR_TYPE_4BIT = 0;
    public static final int COLOR_TYPE_8BIT = 1;
    public static final int COLOR_TYPE_24BIT = 2;

    // --- ANSI Control & Style Constants ---
    public static final String ESC = "\033";
    public static final String CSI = "\033[";
    
    // Formatting & Styles
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String BOLD_OFF = "\033[22m";
    public static final String ITALIC = "\033[3m";
    public static final String ITALIC_OFF = "\033[23m";
    public static final String UNDERLINE = "\033[4m";
    public static final String UNDERLINE_OFF = "\033[24m";
    public static final String BLINK = "\033[5m";
    public static final String BLINK_OFF = "\033[25m";
    public static final String INVERT = "\033[7m";
    public static final String INVERT_OFF = "\033[27m";
    public static final String HIDE = "\033[8m";
    public static final String HIDE_OFF = "\033[28m";
    public static final String STRIKETHROUGH = "\033[9m";
    public static final String STRIKETHROUGH_OFF = "\033[29m";

    // 4-bit Foreground Colors (Standard)
    public static final String FG_BLACK = "\033[30m";
    public static final String FG_RED = "\033[31m";
    public static final String FG_GREEN = "\033[32m";
    public static final String FG_YELLOW = "\033[33m";
    public static final String FG_BLUE = "\033[34m";
    public static final String FG_MAGENTA = "\033[35m";
    public static final String FG_CYAN = "\033[36m";
    public static final String FG_WHITE = "\033[37m";
    public static final String FG_DEFAULT = "\033[39m";

    // 4-bit Foreground Colors (Bright)
    public static final String FG_BRIGHT_BLACK = "\033[90m";
    public static final String FG_BRIGHT_RED = "\033[91m";
    public static final String FG_BRIGHT_GREEN = "\033[92m";
    public static final String FG_BRIGHT_YELLOW = "\033[93m";
    public static final String FG_BRIGHT_BLUE = "\033[94m";
    public static final String FG_BRIGHT_MAGENTA = "\033[95m";
    public static final String FG_BRIGHT_CYAN = "\033[96m";
    public static final String FG_BRIGHT_WHITE = "\033[97m";

    // 4-bit Background Colors (Standard)
    public static final String BG_BLACK = "\033[40m";
    public static final String BG_RED = "\033[41m";
    public static final String BG_GREEN = "\033[42m";
    public static final String BG_YELLOW = "\033[43m";
    public static final String BG_BLUE = "\033[44m";
    public static final String BG_MAGENTA = "\033[45m";
    public static final String BG_CYAN = "\033[46m";
    public static final String BG_WHITE = "\033[47m";
    public static final String BG_DEFAULT = "\033[49m";

    // 4-bit Background Colors (Bright)
    public static final String BG_BRIGHT_BLACK = "\033[100m";
    public static final String BG_BRIGHT_RED = "\033[101m";
    public static final String BG_BRIGHT_GREEN = "\033[102m";
    public static final String BG_BRIGHT_YELLOW = "\033[103m";
    public static final String BG_BRIGHT_BLUE = "\033[104m";
    public static final String BG_BRIGHT_MAGENTA = "\033[105m";
    public static final String BG_BRIGHT_CYAN = "\033[106m";
    public static final String BG_BRIGHT_WHITE = "\033[107m";

    // Common Control Operations
    public static final String ALT_BUFFER_ON = "\033[?1049h";
    public static final String ALT_BUFFER_OFF = "\033[?1049l";
    public static final String CURSOR_HIDE = "\033[?25l";
    public static final String CURSOR_SHOW = "\033[?25h";
    public static final String CLEAR_SCREEN = "\033[2J";
    public static final String CLEAR_LINE = "\033[2K";
    public static final String CURSOR_HOME = "\033[1;1H";

    // --- Fluent Builder / Generator Utilities ---

    /** Generates 24-bit True Color Foreground escape code */
    public static String fg(int r, int g, int b) {
        return "\033[38;2;" + r + ";" + g + ";" + b + "m";
    }

    /** Generates 24-bit True Color Background escape code */
    public static String bg(int r, int g, int b) {
        return "\033[48;2;" + r + ";" + g + ";" + b + "m";
    }

    /** Generates 8-bit index Foreground escape code */
    public static String fg(int index) {
        return "\033[38;5;" + index + "m";
    }

    /** Generates 8-bit index Background escape code */
    public static String bg(int index) {
        return "\033[48;5;" + index + "m";
    }

    /** Generates cursor positioning escape code */
    public static String cursorTo(int row, int col) {
        return "\033[" + row + ";" + col + "H";
    }

    /**
     * Interface to receive low-overhead callbacks for every parsed ANSI sequence.
     * Implementing classes can process telemetry state natively with zero GC impact.
     */
    public interface ANSIListener {
        // Plain Text Blocks
        void onText(CharSequence text, int start, int end);

        // Text Formatting (SGR - Select Graphic Rendition)
        void onReset();
        void onBold(boolean enable);
        void onItalic(boolean enable);
        void onUnderline(boolean enable);
        void onBlink(boolean enable);
        void onInvert(boolean enable);
        void onHide(boolean enable);
        void onStrikethrough(boolean enable);

        // Color Control (r, g, b are used for 24-bit, or raw index is passed in 'r' for 4/8-bit)
        void onForegroundColor(int colorType, int r, int g, int b);
        void onBackgroundColor(int colorType, int r, int g, int b);

        // Cursor Controls
        void onCursorPosition(int row, int col);
        void onCursorUp(int count);
        void onCursorDown(int count);
        void onCursorForward(int count);
        void onCursorBackward(int count);
        void onCursorNextLine(int count);
        void onCursorPrevLine(int count);
        void onCursorHorizontalAbsolute(int col);

        // Clearing and Erasing
        void onEraseInDisplay(int mode); // 0=cursor to end, 1=start to cursor, 2=entire screen, 3=scrollback
        void onEraseInLine(int mode);    // 0=cursor to end, 1=start to cursor, 2=entire line

        // Scrolling
        void onScrollUp(int count);
        void onScrollDown(int count);

        // Private Operating Modes (e.g. Alternate Buffer, Mouse tracking)
        void onPrivateMode(int mode, boolean enable); // mode (e.g. 1049=alt buffer, 25=cursor show)

        // Device Controls
        void onDeviceStatusReport(); // \033[6n
        
        // Window Title Control (OSC)
        void onWindowTitle(CharSequence title, int start, int end);

        // Fallback for debugging
        void onUnsupportedSequence(CharSequence raw, int start, int end);
    }

    /**
     * Zero-allocation, state-machine-driven parser for UTF-16 CharSequence.
     * Evaluates ESC, CSI, OSC, and private escape ranges purely using primitives.
     */
    public static void parse(CharSequence input, ANSIListener listener) {
        if (input == null || listener == null) return;

        int len = input.length();
        int textStart = 0;
        int i = 0;

        // Buffer array to hold parsed numerical parameters in CSI sequences (up to 16 parameters)
        int[] params = new int[16];
        int paramCount = 0;

        while (i < len) {
            char c = input.charAt(i);

            // Detect Escape character (\033 or \u001B)
            if (c == 27) {
                // If we accumulated normal text, flush it now
                if (i > textStart) {
                    listener.onText(input, textStart, i);
                }

                // Move past ESC
                i++;
                if (i >= len) {
                    textStart = len;
                    break;
                }

                char next = input.charAt(i);

                // 1. CSI - Control Sequence Introducer: ESC [
                if (next == '[') {
                    i++; // Move past '['
                    
                    boolean isPrivate = false;
                    if (i < len && input.charAt(i) == '?') {
                        isPrivate = true;
                        i++;
                    }

                    // Reset parameters
                    paramCount = 0;
                    int currentParam = -1;

                    // Parse numerical parameters
                    int seqStart = i;
                    while (i < len) {
                        char seqChar = input.charAt(i);

                        if (seqChar >= '0' && seqChar <= '9') {
                            if (currentParam == -1) {
                                currentParam = 0;
                            }
                            currentParam = currentParam * 10 + (seqChar - '0');
                            i++;
                        } else if (seqChar == ';') {
                            params[paramCount++] = (currentParam == -1) ? 0 : currentParam;
                            currentParam = -1;
                            if (paramCount >= params.length) break; // Overflow protection
                            i++;
                        } else {
                            // Non-numeric, non-separator character indicates the end of CSI sequence
                            if (currentParam != -1) {
                                params[paramCount++] = currentParam;
                            }
                            break;
                        }
                    }

                    if (i < len) {
                        char cmd = input.charAt(i);
                        i++; // Consume command character

                        if (isPrivate) {
                            // Private modes (e.g. ?25h, ?1049h, ?1049l)
                            int mode = (paramCount > 0) ? params[0] : 0;
                            if (cmd == 'h') {
                                listener.onPrivateMode(mode, true);
                            } else if (cmd == 'l') {
                                listener.onPrivateMode(mode, false);
                            } else {
                                listener.onUnsupportedSequence(input, seqStart - 3, i);
                            }
                        } else {
                            // Standard CSI Commands
                            switch (cmd) {
                                case 'm': // SGR (Select Graphic Rendition) - Colors & Styles
                                    if (paramCount == 0) {
                                        listener.onReset();
                                    } else {
                                        parseSGR(params, paramCount, listener);
                                    }
                                    break;
                                case 'H': // Cup - Cursor Position
                                case 'f':
                                    int row = (paramCount > 0) ? params[0] : 1;
                                    int col = (paramCount > 1) ? params[1] : 1;
                                    listener.onCursorPosition(row, col);
                                    break;
                                case 'A': // CUU - Cursor Up
                                    listener.onCursorUp((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'B': // CUD - Cursor Down
                                    listener.onCursorDown((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'C': // CUF - Cursor Forward
                                    listener.onCursorForward((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'D': // CUB - Cursor Backward
                                    listener.onCursorBackward((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'E': // CNL - Cursor Next Line
                                    listener.onCursorNextLine((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'F': // CPL - Cursor Preceding Line
                                    listener.onCursorPrevLine((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'G': // CHA - Cursor Horizontal Absolute
                                    listener.onCursorHorizontalAbsolute((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'J': // ED - Erase in Display
                                    listener.onEraseInDisplay((paramCount > 0) ? params[0] : 0);
                                    break;
                                case 'K': // EL - Erase in Line
                                    listener.onEraseInLine((paramCount > 0) ? params[0] : 0);
                                    break;
                                case 'S': // SU - Scroll Up
                                    listener.onScrollUp((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'T': // SD - Scroll Down
                                    listener.onScrollDown((paramCount > 0) ? params[0] : 1);
                                    break;
                                case 'n': // DSR - Device Status Report
                                    if (paramCount > 0 && params[0] == 6) {
                                        listener.onDeviceStatusReport();
                                    } else {
                                        listener.onUnsupportedSequence(input, seqStart - 2, i);
                                    }
                                    break;
                                default:
                                    listener.onUnsupportedSequence(input, seqStart - 2, i);
                                    break;
                            }
                        }
                    }
                    textStart = i;
                }
                // 2. OSC - Operating System Command: ESC ] (e.g., Set Window Title)
                else if (next == ']') {
                    i++; // Move past ']'
                    int oscStart = i;

                    // Read until BEL (\u0007) or ST (ESC \)
                    int contentEnd = -1;
                    while (i < len) {
                        char oscChar = input.charAt(i);
                        if (oscChar == 7) { // BEL
                            contentEnd = i;
                            i++;
                            break;
                        } else if (oscChar == 27 && i + 1 < len && input.charAt(i + 1) == '\\') { // ESC \ (ST)
                            contentEnd = i;
                            i += 2;
                            break;
                        }
                        i++;
                    }

                    if (contentEnd != -1) {
                        // Check if it sets window title (starts with '0;' or '2;')
                        if (contentEnd - oscStart >= 2 && 
                            (input.charAt(oscStart) == '0' || input.charAt(oscStart) == '2') && 
                            input.charAt(oscStart + 1) == ';') {
                            listener.onWindowTitle(input, oscStart + 2, contentEnd);
                        } else {
                            listener.onUnsupportedSequence(input, oscStart - 2, i);
                        }
                    }
                    textStart = i;
                }
                // 3. Fallback for standalone single-char ESC controls (like ESC M scroll backward, ESC D scroll forward)
                else {
                    if (next == 'M') {
                        listener.onScrollDown(1);
                    } else if (next == 'D') {
                        listener.onScrollUp(1);
                    } else {
                        listener.onUnsupportedSequence(input, i - 1, i + 1);
                    }
                    i++;
                    textStart = i;
                }
            } else {
                i++;
            }
        }

        // Flush any remaining trailing text
        if (i > textStart) {
            listener.onText(input, textStart, i);
        }
    }

    /**
     * Resolves the Select Graphic Rendition (SGR) parameter stack.
     * Correctly handles text styles, standard 4-bit indices, 8-bit index colors, and 24-bit RGB values.
     */
    private static void parseSGR(int[] params, int count, ANSIListener listener) {
        int idx = 0;
        while (idx < count) {
            int p = params[idx];

            if (p == 0) {
                listener.onReset();
                idx++;
            } else if (p == 1) {
                listener.onBold(true);
                idx++;
            } else if (p == 22) {
                listener.onBold(false);
                idx++;
            } else if (p == 3) {
                listener.onItalic(true);
                idx++;
            } else if (p == 23) {
                listener.onItalic(false);
                idx++;
            } else if (p == 4) {
                listener.onUnderline(true);
                idx++;
            } else if (p == 24) {
                listener.onUnderline(false);
                idx++;
            } else if (p == 5) {
                listener.onBlink(true);
                idx++;
            } else if (p == 25) {
                listener.onBlink(false);
                idx++;
            } else if (p == 7) {
                listener.onInvert(true);
                idx++;
            } else if (p == 27) {
                listener.onInvert(false);
                idx++;
            } else if (p == 8) {
                listener.onHide(true);
                idx++;
            } else if (p == 28) {
                listener.onHide(false);
                idx++;
            } else if (p == 9) {
                listener.onStrikethrough(true);
                idx++;
            } else if (p == 29) {
                listener.onStrikethrough(false);
                idx++;
            }
            // Standard 3-bit / 4-bit Foreground Colors
            else if (p >= 30 && p <= 37) {
                listener.onForegroundColor(COLOR_TYPE_4BIT, p - 30, 0, 0);
                idx++;
            } else if (p >= 90 && p <= 97) {
                listener.onForegroundColor(COLOR_TYPE_4BIT, p - 90 + 8, 0, 0); // Bright range
                idx++;
            } else if (p == 39) {
                listener.onForegroundColor(COLOR_TYPE_4BIT, -1, 0, 0); // Reset foreground to default
                idx++;
            }
            // Standard 3-bit / 4-bit Background Colors
            else if (p >= 40 && p <= 47) {
                listener.onBackgroundColor(COLOR_TYPE_4BIT, p - 40, 0, 0);
                idx++;
            } else if (p >= 100 && p <= 107) {
                listener.onBackgroundColor(COLOR_TYPE_4BIT, p - 100 + 8, 0, 0); // Bright range
                idx++;
            } else if (p == 49) {
                listener.onBackgroundColor(COLOR_TYPE_4BIT, -1, 0, 0); // Reset background to default
                idx++;
            }
            // Advanced Color Modes (8-bit or 24-bit True Color)
            else if (p == 38) { // Advanced Foreground
                if (idx + 2 < count && params[idx + 1] == 5) { // 8-bit index
                    int colorIdx = params[idx + 2];
                    listener.onForegroundColor(COLOR_TYPE_8BIT, colorIdx, 0, 0);
                    idx += 3;
                } else if (idx + 4 < count && params[idx + 1] == 2) { // 24-bit True RGB Color
                    int r = params[idx + 2];
                    int g = params[idx + 3];
                    int b = params[idx + 4];
                    listener.onForegroundColor(COLOR_TYPE_24BIT, r, g, b);
                    idx += 5;
                } else {
                    idx++; // Faulty sequence skip
                }
            } else if (p == 48) { // Advanced Background
                if (idx + 2 < count && params[idx + 1] == 5) { // 8-bit index
                    int colorIdx = params[idx + 2];
                    listener.onBackgroundColor(COLOR_TYPE_8BIT, colorIdx, 0, 0);
                    idx += 3;
                } else if (idx + 4 < count && params[idx + 1] == 2) { // 24-bit True RGB Color
                    int r = params[idx + 2];
                    int g = params[idx + 3];
                    int b = params[idx + 4];
                    listener.onBackgroundColor(COLOR_TYPE_24BIT, r, g, b);
                    idx += 5;
                } else {
                    idx++; // Faulty sequence skip
                }
            } else {
                idx++; // Unknown SGR param skip
            }
        }
    }
}

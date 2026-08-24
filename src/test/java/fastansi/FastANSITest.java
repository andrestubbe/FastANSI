package fastansi;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FastANSITest {

    private static class TestListener implements FastANSI.ANSIListener {
        final List<String> events = new ArrayList<>();

        @Override
        public void onText(CharSequence text, int start, int end) {
            events.add("TEXT:" + text.subSequence(start, end));
        }

        @Override
        public void onReset() {
            events.add("RESET");
        }

        @Override
        public void onBold(boolean enable) {
            events.add("BOLD:" + enable);
        }

        @Override
        public void onItalic(boolean enable) {
            events.add("ITALIC:" + enable);
        }

        @Override
        public void onUnderline(boolean enable) {
            events.add("UNDERLINE:" + enable);
        }

        @Override
        public void onBlink(boolean enable) {
            events.add("BLINK:" + enable);
        }

        @Override
        public void onInvert(boolean enable) {
            events.add("INVERT:" + enable);
        }

        @Override
        public void onHide(boolean enable) {
            events.add("HIDE:" + enable);
        }

        @Override
        public void onStrikethrough(boolean enable) {
            events.add("STRIKETHROUGH:" + enable);
        }

        @Override
        public void onForegroundColor(int colorType, int r, int g, int b) {
            events.add("FG:" + colorType + "," + r + "," + g + "," + b);
        }

        @Override
        public void onBackgroundColor(int colorType, int r, int g, int b) {
            events.add("BG:" + colorType + "," + r + "," + g + "," + b);
        }

        @Override
        public void onCursorPosition(int row, int col) {
            events.add("CURSOR_POS:" + row + "," + col);
        }

        @Override
        public void onCursorUp(int count) {
            events.add("CURSOR_UP:" + count);
        }

        @Override
        public void onCursorDown(int count) {
            events.add("CURSOR_DOWN:" + count);
        }

        @Override
        public void onCursorForward(int count) {
            events.add("CURSOR_FORWARD:" + count);
        }

        @Override
        public void onCursorBackward(int count) {
            events.add("CURSOR_BACKWARD:" + count);
        }

        @Override
        public void onCursorNextLine(int count) {
            events.add("CURSOR_NEXT:" + count);
        }

        @Override
        public void onCursorPrevLine(int count) {
            events.add("CURSOR_PREV:" + count);
        }

        @Override
        public void onCursorHorizontalAbsolute(int col) {
            events.add("CURSOR_X:" + col);
        }

        @Override
        public void onEraseInDisplay(int mode) {
            events.add("ERASE_DISPLAY:" + mode);
        }

        @Override
        public void onEraseInLine(int mode) {
            events.add("ERASE_LINE:" + mode);
        }

        @Override
        public void onScrollUp(int count) {
            events.add("SCROLL_UP:" + count);
        }

        @Override
        public void onScrollDown(int count) {
            events.add("SCROLL_DOWN:" + count);
        }

        @Override
        public void onPrivateMode(int mode, boolean enable) {
            events.add("PRIVATE:" + mode + "," + enable);
        }

        @Override
        public void onDeviceStatusReport() {
            events.add("DSR");
        }

        @Override
        public void onWindowTitle(CharSequence title, int start, int end) {
            events.add("TITLE:" + title.subSequence(start, end));
        }

        @Override
        public void onUnsupportedSequence(CharSequence raw, int start, int end) {
            events.add("UNSUPPORTED:" + raw.subSequence(start, end));
        }
    }

    @Test
    public void testTextAndReset() {
        TestListener listener = new TestListener();
        FastANSI.parse("Hello\033[0mWorld", listener);

        assertEquals(3, listener.events.size());
        assertEquals("TEXT:Hello", listener.events.get(0));
        assertEquals("RESET", listener.events.get(1));
        assertEquals("TEXT:World", listener.events.get(2));
    }

    @Test
    public void testStylesAnd4BitColors() {
        TestListener listener = new TestListener();
        FastANSI.parse("\033[1;31mRed Bold\033[22;39mNormal", listener);

        assertEquals(6, listener.events.size());
        assertEquals("BOLD:true", listener.events.get(0));
        assertEquals("FG:0,1,0,0", listener.events.get(1)); // 4-bit foreground color index 1 (Red)
        assertEquals("TEXT:Red Bold", listener.events.get(2));
        assertEquals("BOLD:false", listener.events.get(3));
        assertEquals("FG:0,-1,0,0", listener.events.get(4)); // Reset foreground index
        assertEquals("TEXT:Normal", listener.events.get(5));
    }

    @Test
    public void testTrueColorRGB() {
        TestListener listener = new TestListener();
        FastANSI.parse("\033[38;2;255;120;0mOrange Text", listener);

        assertEquals(2, listener.events.size());
        assertEquals("FG:2,255,120,0", listener.events.get(0)); // 24-bit RGB True Color
        assertEquals("TEXT:Orange Text", listener.events.get(1));
    }

    @Test
    public void testPrivateModes() {
        TestListener listener = new TestListener();
        FastANSI.parse("\033[?1049h\033[?25l", listener);

        assertEquals(2, listener.events.size());
        assertEquals("PRIVATE:1049,true", listener.events.get(0)); // Alt Screen buffer ON
        assertEquals("PRIVATE:25,false", listener.events.get(1));  // Cursor Hide OFF
    }

    @Test
    public void testCursorPositions() {
        TestListener listener = new TestListener();
        FastANSI.parse("\033[10;20H\033[A", listener);

        assertEquals(2, listener.events.size());
        assertEquals("CURSOR_POS:10,20", listener.events.get(0));
        assertEquals("CURSOR_UP:1", listener.events.get(1));
    }

    @Test
    public void testByteArrayNativeParsing() {
        TestListener listener = new TestListener();
        String seq = "Hello\033[38;2;255;120;0mOrange Text\033[0m\033[?1049h\033[10;20H";
        byte[] bytes = seq.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        FastANSI.parse(bytes, 0, bytes.length, listener);

        assertEquals(6, listener.events.size());
        assertEquals("TEXT:Hello", listener.events.get(0));
        assertEquals("FG:2,255,120,0", listener.events.get(1));
        assertEquals("TEXT:Orange Text", listener.events.get(2));
        assertEquals("RESET", listener.events.get(3));
        assertEquals("PRIVATE:1049,true", listener.events.get(4));
        assertEquals("CURSOR_POS:10,20", listener.events.get(5));
    }

    @Test
    public void testArgbGenerators() {
        int color = 0xFFFF7800; // Alpha 255, R 255, G 120, B 0
        assertEquals("\033[38;2;255;120;0m", FastANSI.fgArgb(color));
        assertEquals("\033[48;2;255;120;0m", FastANSI.bgArgb(color));
    }
}

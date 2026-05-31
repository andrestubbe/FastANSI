package fastansi.demos;

import fastansi.FastANSI;

public class BaseListener implements FastANSI.ANSIListener {
    @Override public void onText(CharSequence text, int start, int end) {}
    @Override public void onReset() {}
    @Override public void onBold(boolean enable) {}
    @Override public void onItalic(boolean enable) {}
    @Override public void onUnderline(boolean enable) {}
    @Override public void onBlink(boolean enable) {}
    @Override public void onInvert(boolean enable) {}
    @Override public void onHide(boolean enable) {}
    @Override public void onStrikethrough(boolean enable) {}
    @Override public void onForegroundColor(int colorType, int r, int g, int b) {}
    @Override public void onBackgroundColor(int colorType, int r, int g, int b) {}
    @Override public void onCursorPosition(int row, int col) {}
    @Override public void onCursorUp(int count) {}
    @Override public void onCursorDown(int count) {}
    @Override public void onCursorForward(int count) {}
    @Override public void onCursorBackward(int count) {}
    @Override public void onCursorNextLine(int count) {}
    @Override public void onCursorPrevLine(int count) {}
    @Override public void onCursorHorizontalAbsolute(int col) {}
    @Override public void onEraseInDisplay(int mode) {}
    @Override public void onEraseInLine(int mode) {}
    @Override public void onScrollUp(int count) {}
    @Override public void onScrollDown(int count) {}
    @Override public void onPrivateMode(int mode, boolean enable) {}
    @Override public void onDeviceStatusReport() {}
    @Override public void onWindowTitle(CharSequence title, int start, int end) {}
    @Override public void onUnsupportedSequence(CharSequence raw, int start, int end) {}
}

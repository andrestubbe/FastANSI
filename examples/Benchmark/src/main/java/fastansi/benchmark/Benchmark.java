package fastansi.benchmark;

import fastansi.FastANSI;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private String ansiSample;
    private FastANSI.ANSIListener noopListener;

    @Setup
    public void setup() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("\033[1;31mError \033[0m: \033[38;2;100;200;50mCustom color line ").append(i).append("\033[0m\n");
        }
        ansiSample = sb.toString();
        noopListener = new FastANSI.ANSIListener() {
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
        };
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmarkParseAnsiSequence() {
        FastANSI.parse(ansiSample, noopListener);
    }
}

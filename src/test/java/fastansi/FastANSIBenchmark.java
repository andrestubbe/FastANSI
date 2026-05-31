package fastansi;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(0)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
public class FastANSIBenchmark {

    private static final String ANSI_TEXT = "Hello \033[1;31mRed Bold\033[0m Text with \033[42mGreen Background\033[0m!";
    
    // Standard Regex used by many libraries to strip ANSI codes
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\e\\[[\\d;]*[^\\d;]");
    
    // A dummy listener that does nothing (to measure pure parsing overhead)
    private static final FastANSI.ANSIListener DUMMY_LISTENER = new fastansi.demos.BaseListener();

    @Benchmark
    public String benchmarkRegexStrip() {
        // Typical way developers strip ANSI: using Regex replacement.
        // This allocates a Matcher, performs string copying, and allocates a new String.
        return ANSI_PATTERN.matcher(ANSI_TEXT).replaceAll("");
    }

    @Benchmark
    public void benchmarkFastANSI() {
        // FastANSI procedural parsing. Zero-allocation state machine.
        FastANSI.parse(ANSI_TEXT, DUMMY_LISTENER);
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("Starting JMH Benchmark: FastANSI vs Regex...");
        
        Options opt = new OptionsBuilder()
                .include(FastANSIBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

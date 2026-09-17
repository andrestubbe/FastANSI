# FastANSI 0.1.2 — High-Performance ANSI & VT Escape Sequence Parser for Java

[![Status](https://img.shields.io/badge/status-0.1.2-brightgreen.svg)](https://github.com/andrestubbe/FastANSI/releases/tag/v0.1.2)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastANSI)

---

**⚡ A zero-dependency, zero-allocation UTF-16 ANSI and VT100/VT220/Xterm escape sequence parser for Java, engineered for
ultra-high-performance TUI layouts, terminal graphics, and console telemetry pipelines.**

FastANSI is the dedicated high-speed text processing substrate of the **FastJava** ecosystem. It introduces a highly
optimized, stack-free procedural state machine designed to parse raw terminal output streams containing styles, cursor
movements, and custom colors into structured cell representations at the physical hardware level.

To achieve a completely responsive, zero-latency desktop terminal experience, FastANSI is built to pair natively with the rendering module of the **FastJava** ecosystem:

* 🚀 **[FastTerminal](https://github.com/andrestubbe/FastTerminal)** — Direct, low-latency, hardware-accelerated 24-bit True Color terminal rendering engine.

By operating with absolutely **exactly zero object allocations** on the Java heap, FastANSI is 100%
garbage-collection-free and suited to run in demanding, high-throughput console-composing pipelines.

---

[**Watch Demo (Youtube)**](https://www.youtube.com/watch?v=mzIAnXfqXQs) | [**Watch JMH Benchmark (Youtube)**](https://www.youtube.com/watch?v=SEEYP7PdYNk)

[![FastANSI Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=mzIAnXfqXQs)

---

## Quick Start — Example

```java
import fastansi.FastANSI;

public class TerminalFormattingDemo {
    public static void main(String[] args) {
        // 1. Generate ANSI escape sequences for formatting
        String redBoldText = FastANSI.fg(255, 100, 100) + FastANSI.bold() + "Error!" + FastANSI.reset();
        String greenText = FastANSI.fg(100, 255, 100) + "Success" + FastANSI.reset();
        String cursorToStart = FastANSI.cursorTo(1, 1); // Move to top-left

        // 2. Parse existing ANSI streams
        String ansiInput = "\033[1;31mBold Red\033[0m Normal Text";
        FastANSI.parse(ansiInput, new FastANSI.ANSIListener() {
            @Override
            public void onText(CharSequence text, int start, int end) {
                System.out.println("Text: " + text.subSequence(start, end));
            }

            @Override
            public void onBold(boolean enable) {
                System.out.println("Bold: " + enable);
            }

            @Override
            public void onForegroundColor(int colorType, int r, int g, int b) {
                System.out.println("Color: RGB(" + r + "," + g + "," + b + ")");
            }

            // Implement other callbacks as needed
            @Override
            public void onReset() { System.out.println("Reset"); }
        });

        // 3. Strip ANSI codes from strings
        String cleanText = FastANSI.strip(ansiInput);
        System.out.println("Clean: " + cleanText); // "Bold Red Normal Text"
    }
}
```

---

## Table of Contents

- [Why FastANSI?](#why-fastansi)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastANSI?

The mission is to establish the fastest, most comprehensive escape sequence parser in the JVM universe. FastANSI enables
terminal viewports to consume raw external ANSI dumps dynamically, process global terminal styling, and support custom
24-bit True Color rendering with zero garbage collection overhead.

- **Eliminate Regex & String Splitting Bottlenecks**: Standard Java terminal formatters rely on regex patterns (`\u001B\\[[;\\d]*m`) and string slicing, causing catastrophic GC pauses during high-frequency terminal output.
- **Full VT100/VT220/Xterm Protocol Emulation**: Most Java libraries only parse basic 16-color foreground/background codes, failing on 24-bit TrueColor RGB, cursor coordinates, erase sweeps, alternate screens, and SIXEL graphics.
- **Zero-Allocation Stack-Free State Machine**: Operates directly on character slices with coordinate pointers (`start`, `end`) and primitive callbacks, processing tens of millions of characters per second without heap allocations.

| Feature | Jansi | JLine3 (AnsiMatcher) | FastANSI |
|:---|:---|:---|:---|
| **Parsing Model** | Object-heavy token stream | Java Regex / String parsing | **Stack-free procedural state machine** |
| **Allocation per Stream**| High (ANSI token objects) | High (Regex matcher & Strings) | **100% Zero-GC (Primitive pointers)** |
| **Protocol Support** | 4-bit / 8-bit basic colors | Standard SGR attributes | **TrueColor RGB + Cursor + OSC + SIXEL**|
| **Parsing Throughput** | ~5-10 MB/s | ~15-25 MB/s | **> 120 MB/s (> 100M chars/sec)** |

---



## Key Features

* **🚫 Zero Dependencies** — Completely standalone, lightweight, pure Java 17 library.
* **⚡ Zero Heap Allocation** — Renders cell properties purely using coordinate pointers (`start`, `end`) and primitives,
  avoiding all standard String splits or regex overhead.
* **🎨 Complete Color & Style Support** — Full parsing of standard SGR parameters (bold, italic, underlines, standard
  4-bit, 8-bit index, and 24-bit True Color RGB).
* **📏 Cursor & Erase Commands** — Recognizes all standard VT navigation codes (Cursor up/down/forward/backward, cursor
  absolute, display/line erasing).
* **📺 Private & OSC Operating Modes** — Detects alternate screen buffers (`?1049h`/`l`), cursor display toggles (`?25h`/
  `l`), and window title adjustments via Operating System Commands (OSC).
* **🖼️ Native 1:1 SIXEL Graphics** — Integrated SIXEL protocol encoder (`FastAnsiImage.Mode.SIXEL`, `toSixel()`, `writeSixel()`) for native 1:1 screen pixel rendering in modern terminals.

---

## Real-World Use Cases

- 🖥️ **Terminal Rendering**: Power 60+ FPS zero-latency ANSI rendering in [FastTerminal](https://github.com/andrestubbe/FastTerminal) without JVM Garbage Collection stalls.
- 📋 **Log Processing**: Parse and display ANSI-styled server logs, CI/CD outputs, and terminal sessions with proper formatting preserved.
- 🎮 **TUI Applications**: Enable rich terminal user interfaces with proper escape sequence handling for complex layouts and interactive elements.
- 🔌 **Terminal Emulators**: Parse external ANSI output from legacy applications, SSH sessions, and remote systems for accurate display.
- 📊 **Data Visualization**: Render colored charts, progress bars, and status indicators in terminal dashboards with proper ANSI formatting.
- 🌐 **Remote Shell Sessions**: Handle ANSI escape codes from remote connections, preserving formatting and cursor positioning from distant systems.

---

## Performance Benchmarks

FastANSI is rigorously profiled using **JMH** to guarantee zero overhead.
[**Watch the JMH Benchmark**](https://www.youtube.com/watch?v=SEEYP7PdYNk)

*Benchmark: Stripping ANSI escape codes from a text string.*

| Operation | Standard Regex (`replaceAll`) | FastANSI State Engine | Speedup | Allocations (GC) |
| :--- | :--- | :--- | :--- | :--- |
| **Strip ANSI String** | ~478 ns / op | **~99 ns / op** | **~4.8x** | **Zero** |

*Measured on Windows 11, Intel Core i5-1135G7 (Surface Pro 8), JDK 25.0.1. The engine bypasses `Thread.sleep` via `FastDWM` to guarantee zero-jitter native heartbeats even under GC pressure.*


---

## API Quick Reference

| Method | Return Type | Description | Docs |
|---|---|---|---|
| `FastANSI.parse(input, listener)` | `void` | Parses a text stream procedurally, triggering callbacks on the listener. | [Reference](docs/REFERENCE.md#fastansiparse) |
| `FastANSI.fg(r, g, b)` / `fg(idx)` | `String` | Generates 24-bit TrueColor or 8-bit indexed foreground ANSI escape sequences. | [Reference](docs/REFERENCE.md#color-generators) |
| `FastANSI.bg(r, g, b)` / `bg(idx)` | `String` | Generates 24-bit TrueColor or 8-bit indexed background ANSI escape sequences. | [Reference](docs/REFERENCE.md#color-generators) |
| `FastANSI.cursorTo(row, col)` | `String` | Generates cursor absolute positioning escape codes (`\033[row;colH`). | [Reference](docs/REFERENCE.md#cursor--display-controls) |
| `FastANSI.strip(input)` | `String` | High-speed, zero-allocation stripping of all ANSI escape sequences from text. | [Reference](docs/REFERENCE.md#core-api) |
| `FastAnsiImage.toString(src, cols, rows, mode)` | `String` | Converts `BufferedImage` to ANSI string with automatic aspect ratio handling. | [Reference](docs/REFERENCE.md#fastansiimagetostring) |
| `FastAnsiImage.toSixel(img)` | `String` | Encodes a `BufferedImage` into a 1:1 native SIXEL pixel escape sequence string. | [Reference](docs/REFERENCE.md#native-11-sixel-integration) |
| `FastAnsiImage.writeSixel(img, out)` | `void` | Streams 1:1 square-pixel SIXEL bytes directly into stdout or an `OutputStream`. | [Reference](docs/REFERENCE.md#native-11-sixel-integration) |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **Native 1:1 SIXEL Pixels** | [Sixel1To1PixelDemo.java](src/test/java/fastansi/demos/Sixel1To1PixelDemo.java) | `run-sixel.bat` | High-resolution 1:1 native screen pixel graphics using the SIXEL protocol with 6x6x6 color palette. |
| **Terminal Video & Animation Player** | [Demo.java](src/test/java/fastansi/demos/Demo.java) | `run-demo.bat` | 60 FPS full-terminal video and image playback utilizing TrueColor half-block resolution. |
| **Half-Block Image Viewer** | [HalfBlockImageDemo.java](src/test/java/fastansi/demos/HalfBlockImageDemo.java) | `run-halfblock.bat` | High-fidelity 24-bit TrueColor image rendering packing 2 vertical pixels per character cell (`▀`). |
| **CLI Video & Image Converter** | [Converter.java](src/main/java/fastansi/cli/Converter.java) | `run-converter.bat` | Headless conversion utility exporting images and video frames to `.ansi` text files and scripts. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastansi/benchmark/Benchmark.java) | `run-benchmark.bat` | OpenJDK JMH microbenchmarks measuring parser throughput and ANSI stripping execution latency. |

---

## Installation

FastANSI is pure-Java and has **zero external dependencies**.

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastANSI</artifactId>
        <version>0.1.2</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastANSI:0.1.2'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JAR directly to add it to your classpath:

1. 📦 **[fastansi-0.1.2.jar](https://github.com/andrestubbe/FastANSI/releases/download/v0.1.2/fastansi-0.1.2.jar)** (The Core Library)

---

## Documentation

* **[SIXEL.md](docs/SIXEL.md)**: SIXEL 1:1 native pixel graphics guide and protocol specification.
* **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive catalog of SGR styles, OSC window parameters, and callback contracts.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-allocation and low-overhead processing designs.
* **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestone features and performance extensions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.

---

## Platform Support

| Platform      | Status            |
|---------------|-------------------|
| Windows 10/11 | ✅ Fully Supported |
| Linux         | ✅ Fully Supported |
| macOS         | ✅ Fully Supported |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastTerminal](https://github.com/andrestubbe/FastTerminal)
- [FastANSI](https://github.com/andrestubbe/FastANSI)
- [FastEmojis](https://github.com/andrestubbe/FastEmojis)
- [FastUI](https://github.com/andrestubbe/FastUI)
- [FastGrid](https://github.com/andrestubbe/FastGrid)
- [FastProportion](https://github.com/andrestubbe/FastProportion)
- [FastTheme](https://github.com/andrestubbe/FastTheme)
- [FastCore](https://github.com/andrestubbe/FastCore)

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀

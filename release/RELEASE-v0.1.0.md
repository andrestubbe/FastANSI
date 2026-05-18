# FastANSI v0.1.0 — Initial Release 🚀

## 🎉 Version 0.1.0: Zero-Allocation ANSI Parser
**Release Date:** 2026-05-18  
**Tag:** `v0.1.0`

---

## ✨ Features

### ⚡ Zero-Allocation State Machine
- Sweeps character streams procedurally using primitive state tracking and coordinates.
- Guarantees **exactly zero heap allocations** during parsing, eliminating JVM Garbage Collection stutters in high-refresh TUI renderers.

### 🎨 Universal Style & Color Decoding
- Supports standard SGR formatting: bold, italics, underlines, blinking, and strikethroughs.
- Parsed colors are fully structured across standard 4-bit indices, 8-bit index palettes, and 24-bit True RGB Colors.

### 📺 Window Title (OSC) & private modes
- Accurately captures private operating modes (alternate screen buffers `?1049h`/`l`, cursor hiding `?25h`/`l`) and sets window titles via OSC commands.

---

## 📦 Installation (JitPack)

### Maven
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
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

---

## 🔧 Technical Details
- **Architecture:** Pure Java 17, 100% Platform-Independent.
- **Dependencies:** Zero external dependencies.
- **Performance Profile:** Fully compiled footprint of less than 15KB.

---

## 🙏 Credits
- Part of the **FastJava Ecosystem** — *Making the JVM faster.*

# 🖼️ SIXEL 1:1 Native Pixel Integration

This document outlines **SIXEL Graphics Protocol Integration** in `FastANSI` for rendering true, high-resolution **1:1 bitmap pixels** directly inside Windows Terminal, xterm, WezTerm, and all SIXEL-compatible terminal emulators.

---

## 💡 What is SIXEL?

Unlike ANSI character-block techniques (`▀`, `█`, or ASCII ramps), **SIXEL** is a native graphics terminal protocol created by DEC. 

When a terminal receives a SIXEL escape sequence:
- It switches from text-cell rendering to native bitmap graphics rendering.
- **1 image pixel = 1 native screen pixel** (no character block pixelation).
- Images, graphics, and UI charts render with sharp, full resolution inside the terminal window.

---

## ⚙️ How Sixel Protocol Encoding Works

SIXEL packages bitmap pixels into a stream of escape codes:

1. **Device Control Header**: `\033P7;1;7q`
   - Parameter `7` sets **1:1 square pixel aspect ratio** (prevents 1980s CRT vertical stretch distortion).
2. **Raster Attributes**: `"1;1;<width>;<height>`
   - Defines explicit pixel grid dimensions (`width x height`) and enforces 1:1 pixel scaling.
3. **Adaptive TrueColor Palette**: `#<index>;2;<R%>;<G%>;<B%>`
   - Dynamically samples the top 256 colors of the image for rich, true-color color reproduction.
4. **6-Pixel Vertical Bands**:
   - Bitmaps are split into 6-pixel high horizontal bands.
   - Each column in a band is encoded as an ASCII character between `?` (63) and `~` (126), where bits 0–5 represent 6 vertical pixels.
   - `$` resets position to the start of the band.
   - `-` moves down to the next 6-pixel row band.
5. **String Terminator (ST)**: `\033\`
   - Returns the terminal back to normal text grid rendering.

---

## 🚀 Quick Code Example

Below is a minimal Java snippet to encode and output a `BufferedImage` as a 1:1 SIXEL pixel stream:

```java
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import fastansi.demos.Sixel1To1PixelDemo;

public class SixelExample {
    public static void main(String[] args) throws Exception {
        BufferedImage image = ImageIO.read(new File("image.png"));
        
        // Write 1:1 SIXEL pixel stream to stdout
        Sixel1To1PixelDemo.writeSixel(image, System.out);
    }
}
```

---

## 🛠️ Testing & Demos

You can run the interactive SIXEL demo directly via the included script:

```cmd
# 1. Run demo with a 1:1 vector/gradient test pattern:
.\run-sixel.bat

# 2. Render any PNG/JPEG at 1:1 native screen pixel resolution:
.\run-sixel.bat test_image.jpg
```

---

## 🖥️ Terminal Compatibility

| Terminal | SIXEL Support | Notes |
| :--- | :--- | :--- |
| **Windows Terminal** | ✅ Native (v1.22+) | Default on modern Windows 11 / Windows 10 |
| **WezTerm** | ✅ Native | Windows / macOS / Linux |
| **Ghostty** | ✅ Native | macOS / Linux |
| **iTerm2** | ✅ Native | macOS |
| **Alacritty** | 🔶 Extension | Requires sixel build/patch |

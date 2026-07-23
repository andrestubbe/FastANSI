# ⚡ FastANSI Changelog

## v0.1.2 - Native SIXEL 1:1 Pixel Integration
- **Native SIXEL 1:1 Pixel Encoding**: Added `FastAnsiImage.Mode.SIXEL`, `FastAnsiImage.toSixel()`, and `FastAnsiImage.writeSixel()` for native 1:1 screen pixel rendering inside Windows Terminal, xterm, WezTerm, Ghostty, and iTerm2.
- **1:1 Square Pixel Aspect Ratio**: Added explicit SIXEL aspect ratio parameter `7` and raster attributes (`"1;1;width;height`) to eliminate vertical stretch distortion.
- **6x6x6 TrueColor Palette & O(1) Color Lookup**: Implemented full RGB color cube palette with \(O(1)\) pixel mapping for accurate, non-tinted color reproduction across all RGB channels.
- **SIXEL Demo & Script**: Included `Sixel1To1PixelDemo.java` and `run-sixel.bat` launcher.
- **Documentation**: Added comprehensive [SIXEL Protocol Guide](SIXEL.md) and updated API reference.

## v0.1.1 [ALPHA] - FastASCII Integration
- **FastASCII Integration**: Adopted `FastASCII` 0.1.0 as the core integer and glyph density dependency.
- **Glyph Density Extracted**: Extracted `FastGlyphDensity` and moved it into the core `FastASCII` library for ecosystem-wide availability.

## v0.1.0 [ALPHA] - Initial Release
- **Zero-Allocation Parser Kernel**: High-performance stack-free procedural loop.
- **Full 24-bit True Color Parsing**: Accurate resolution of advanced foreground and background RGB sequences.
- **Cursor & Buffer private modes**: Decodes cursor movement codes and alternate buffer toggles.
- **FastAnsiImage**: Introduced a high-performance image-to-ANSI renderer (supports Density chars, Block colored spaces, and Hybrid mode).
- **Standalone Converter**: Added CLI tool for exporting terminal animations to self-playing bash/batch scripts.
- **Instant Playback Caching**: Hardware-specific .ansicache.gz file optimization for immediate TrueColor video startup.
- **JMH Benchmarks**: Officially confirmed ~4.8x speedup over standard regex string stripping, proving the zero-allocation architecture.

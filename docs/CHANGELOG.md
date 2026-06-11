# ⚡ FastANSI Changelog

## v0.1.0 [ALPHA] - Initial Release
- **Zero-Allocation Parser Kernel**: High-performance stack-free procedural loop.
- **Full 24-bit True Color Parsing**: Accurate resolution of advanced foreground and background RGB sequences.
- **Cursor & Buffer private modes**: Decodes cursor movement codes and alternate buffer toggles.
- **FastAnsiImage**: Introduced a high-performance image-to-ANSI renderer (supports Density chars, Block colored spaces, and Hybrid mode).
- **Standalone Converter**: Added CLI tool for exporting terminal animations to self-playing bash/batch scripts.
- **Instant Playback Caching**: Hardware-specific .ansicache.gz file optimization for immediate TrueColor video startup.
- **JMH Benchmarks**: Officially confirmed ~4.8x speedup over standard regex string stripping, proving the zero-allocation architecture.

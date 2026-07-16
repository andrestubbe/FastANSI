# ⚡ FastANSI API Reference

## 🛠️ Core API

### `FastANSI.parse`
```java
public static void parse(CharSequence input, ANSIListener listener)
```
Parses a UTF-16 character stream procedurally, resolving all ANSI escape sequences and dispatching low-overhead primitive callbacks to the provided listener.
* **Arguments**:
  * `input`: The `CharSequence` (e.g. `String`, `StringBuilder`) containing the text stream.
  * `listener`: The `FastANSI.ANSIListener` implementation to receive parsing events.

---

## 🎨 ANSIListener Callback Specifications

### 1. Plain Text Block
```java
void onText(CharSequence text, int start, int end)
```
Dispatched when a segment of regular printable text is encountered.
* **Params**:
  * `text`: The original input stream.
  * `start`: Inclusive start character index of the text segment.
  * `end`: Exclusive end character index of the text segment.

### 2. SGR (Select Graphic Rendition) Styles
* `onReset()`: Resets all formatting, colors, and font attributes to defaults (`\033[0m`).
* `onBold(boolean enable)`: Controls bold style (`\033[1m` / `\033[22m`).
* `onItalic(boolean enable)`: Controls italic style (`\033[3m` / `\033[23m`).
* `onUnderline(boolean enable)`: Controls underline style (`\033[4m` / `\033[24m`).

### 3. SGR Advanced Color Controls
```java
void onForegroundColor(int colorType, int r, int g, int b)
void onBackgroundColor(int colorType, int r, int g, int b)
```
Dispatched when foreground or background colors change.
* **Params**:
  * `colorType`:
    * `COLOR_TYPE_4BIT (0)`: Standard 16 colors. Index is passed in parameter `r` (0 to 15, or -1 to reset to default).
    * `COLOR_TYPE_8BIT (1)`: 256 color index. Index is passed in parameter `r` (0 to 255).
    * `COLOR_TYPE_24BIT (2)`: Packed 24-bit True RGB Color. RGB color parameters are passed directly via `r`, `g`, and `b` (0 to 255).

---

## 🏗️ Generator Utilities (`FastANSI`)
FastANSI includes zero-allocation string builders to format ANSI strings on the fly.

### Color Generators
* `FastANSI.fg(int r, int g, int b)`: Generates 24-bit TrueColor foreground.
* `FastANSI.bg(int r, int g, int b)`: Generates 24-bit TrueColor background.
* `FastANSI.fg(int index)`: Generates 8-bit index foreground.
* `FastANSI.bg(int index)`: Generates 8-bit index background.

### Cursor & Display Controls
* `FastANSI.cursorTo(int row, int col)`: Generates absolute cursor positioning (`ESC[row;colH`).
* Built-in Constants: `FastANSI.CLEAR_SCREEN`, `FastANSI.CURSOR_HIDE`, `FastANSI.ALT_BUFFER_ON`, etc.

---

## 🖼️ FastAnsiImage
High-performance Image-to-ANSI renderer included natively in the library.

### `FastAnsiImage.toString`
```java
public static String toString(BufferedImage src, int termCols, int termRows, Mode mode)
```
Converts an image directly into an ANSI string with automatic aspect ratio correction for terminals.

**Modes:**
* `Mode.HALF_BLOCK`: Uses upper and lower half-block characters (`▀`) to pack 2 vertical pixels into a single terminal cell (Best quality, 2x vertical resolution).
* `Mode.FULL_BLOCK`: Background-colored spaces (`█`) (Purest color accuracy, 1:1 pixel look).
* `Mode.RAMP`: Luminance to density characters, with foreground color (Classic ANSI art).
* `Mode.HYBRID`: Background-colored spaces overlaid with brightened foreground density characters (Maximum depth).

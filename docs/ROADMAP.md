# ⚡ FastANSI Roadmap

## 🎯 Completed Milestones (v0.1.0)
* [x] **Zero-Allocation Parser Kernel**: High-performance stack-free procedural loop.
* [x] **Full 24-bit True Color Parsing**: Accurate resolution of advanced foreground and background RGB sequences.
* [x] **Cursor & Buffer private modes**: Decodes cursor movement codes and alternate buffer toggles.
* [x] **Unit Testing**: Robust JUnit coverage for colors, styles, splits, and custom OSC codes.

---

## 🚀 Active Milestones (v0.2.0)
* [ ] **FastTerminal Integration**:
  * [ ] Integrate `FastANSI` as a utility inside `FastTerminalScene` to allow writing styled ANSI string blocks natively.
* [x] **JMH Micro-Benchmarks**:
  * [x] Benchmark `FastANSI` against standard regex-based or split-based ANSI parsers to officially confirm speedups.

---

## 🔮 Future Explorations (v1.0.0)
* [ ] **Byte-Array Parser Engine**:
  * [ ] Implement a zero-allocation `byte[]` direct stream parser to process incoming raw JNI socket or standard input byte buffers directly without converting them to Java Strings first!

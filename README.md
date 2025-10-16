# 🌐 Markon Browser

**Markon Browser** is a modern, JavaFX-based desktop application designed to connect to and render Markdown content from **MCTP (Markdown Content Transfer Protocol)** servers. It functions as a minimalistic Markdown browser, providing tabbed browsing, real-time rendering, and smooth user experience without the weight of a traditional web browser.

---

## 🚀 Features

- 🧭 **Tabbed Interface** — Open multiple markdown pages in separate tabs, with a dynamic `+` button for new tabs.
- 🔗 **MCTP Protocol Support** — Connect directly to Markdown servers using the `mctp://` protocol.
- 📝 **Markdown Rendering** — Uses **flexmark-java** to parse and render markdown to clean HTML.
- ⚙️ **Smart URL Handling** — Automatically normalizes URLs and ports (default: 9196).
- 🧩 **Offline Welcome Page** — Displays a built-in welcome message describing MCTP and usage.
- 🧱 **JavaFX UI** — Clean, responsive, and modern interface with CSS styling.

---

## 🧰 Requirements

- **Java 17+**
- **JavaFX SDK**
- **Flexmark-Java** library for Markdown rendering

Ensure you have JavaFX properly linked to your classpath when running or building the project.

---

## 🗂️ Project Structure

```
MarkonBrowser/
├── src/
│   └── MarkonBrowser.java     # Main application
├── resources/
│   ├── fonts.css              # Custom font styles
│   └── (other optional assets)
└── README.md                  # Project documentation
```

---

## ⚙️ How It Works

1. Launches a **JavaFX Application** window.
2. Displays a **Welcome Tab** with formatted HTML explaining how to use the browser.
3. Allows users to enter an address in the format:

   ```
   mctp://<host>[:port]/<path>
   ```

   or simply:

   ```
   localhost/index.md
   ```

4. Fetches Markdown content from the given MCTP server via **TCP Socket**.
5. Parses the Markdown using **Flexmark** and displays it as HTML within a `WebView`.

---

## 🧩 Example Usage

### 1️⃣ Start an MCTP Server

If you already have an **MCTP Server** running (e.g., from the Node.js version):

```bash
node MCTPServer.js
```

### 2️⃣ Launch the Markon Browser

Compile and run the application:

```bash
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.web -cp .:flexmark-all.jar MarkonBrowser.java
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.web -cp .:flexmark-all.jar MarkonBrowser
```

🧭 Example input:

```
localhost:9090/hello
```

---

## 🖥️ User Interface Overview

| Component     | Description                                                            |
| ------------- | ---------------------------------------------------------------------- |
| **URL Field** | Accepts an MCTP address in format `host[:port]/path`.                  |
| **Go Button** | Fetches and displays the markdown content. Disabled if field is empty. |
| **Tab Pane**  | Supports multiple open markdown documents and dynamic tab creation.    |
| **WebView**   | Renders markdown content as HTML using Flexmark.                       |

---

## 🧠 Core Classes and Logic

### `fetchMarkdownFromMctpServer()`

Establishes a TCP connection to an MCTP server, sends a request like:

```
MCTP/1.0
Host: localhost
Request: GET /hello.md
```

Then reads the response body (Markdown) from the server.

### `convertMarkdownToHtml()`

Uses Flexmark’s `Parser` and `HtmlRenderer` to transform raw markdown into HTML for the WebView.

### `loadUrlInTab()`

Parses user input, validates MCTP URLs, normalizes missing protocol/port, fetches data, and updates tab content.

---

## 🎨 Customization

You can modify the appearance of the browser through `resources/fonts.css` or by editing WebView’s internal styles.

Example CSS snippet:

```css
#root {
  -fx-background-color: #f9f9f9;
}
#controls {
  -fx-background-color: #eaeaea;
}
```

---

## 🧭 Protocol Support

**Markon Browser** only supports the **MCTP protocol**, not `http://` or `https://`.

Example supported URLs:

```
localhost/index.md
mctp://127.0.0.1:9196/docs/readme.md
```

Unsupported URLs:

```
http://example.com
https://github.com
```

---

## Resources

[flexmark jar library](https://repo1.maven.org/maven2/com/vladsch/flexmark/flexmark-all/0.64.8/)

[javafx sdk](https://gluonhq.com/products/javafx/)

[json](https://search.maven.org/remotecontent?filepath=org/json/json/20250517/json-20250517.jar)

---

## 🧩 Future Enhancements

- [ ] Bookmark support for frequently accessed pages.
- [ ] Dark mode and theme customization.
- [ ] Markdown-to-HTML caching for faster reloads.
- [ ] Configurable default MCTP server and ports.
- [ ] Integrated connection log viewer.

---

## 👨‍💻 Author

Developed by **[Mark Wayne B. Menorca]**
📧 [innovations@marcuwynu.space](mailto:innovations@marcuwynu.space)
🌐 [https://github.com/marcuwynu23](https://github.com/marcuwynu23)

---

## 📜 License

MIT License © 2025 — Free to use, modify, and distribute.

---

> 💡 _Markon Browser bridges MCTP servers and markdown lovers — browse text, beautifully._

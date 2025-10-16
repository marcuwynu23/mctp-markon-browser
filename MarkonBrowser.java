
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

public class MarkonBrowser extends Application {

    private static final String RESOURCE_DIR = "resources";

    private final String welcomeString = """
                   <h1 style="color:#2c3e50;">Welcome to MCTP Browser</h1>

            <p style="font-size: 1.1em; color:#34495e; max-width:600px;">
              MCTP Browser is a lightweight, markdown-based browsing tool designed to seamlessly fetch and display content from <strong>MCTP (Markdown Content Transfer Protocol)</strong> servers.
            </p>

            <h2 style="color:#2980b9;">Features</h2>
            <ul style="font-size: 1em; color:#34495e; max-width:600px;">
              <li> Fetch and render markdown documents from MCTP servers.</li>
              <li> Multiple tabs with easy tab management.</li>
              <li> Support for custom URLs with optional port and paths.</li>
              <li> Fast, simple, and focused on markdown content.</li>
            </ul>

            <h2 style="color:#2980b9;">How to Use</h2>
            <ol style="font-size: 1em; color:#34495e; max-width:600px;">
              <li>Enter an address in the field above using the format <code>host[:port]/path</code>. <br> You can omit <code>mctp://</code> as it's added automatically.</li>
              <li>Click <strong>Go</strong> or press <strong>Enter</strong> to fetch the markdown content from the server.</li>
              <li>Content will be rendered as HTML in the tab's browser area.</li>
              <li>Open new tabs by clicking the <strong>+</strong> tab.</li>
            </ol>

            <p style="font-style: italic; color:#7f8c8d; max-width:600px;">
              Enjoy your markdown browsing experience with MCTP Browser!
            </p>
                    """;

    private TabPane tabPane;
    private BorderPane root;
    private Scene scene;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getTabs().add(createNewBrowserTab("Welcome", welcomeString));

        // Add the "+" tab for creating new tabs
        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        tabPane.getTabs().add(addTab);

        // Listen for tab selection to handle "+" tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == addTab) {
                Tab newBrowserTab = createNewBrowserTab("New Tab", welcomeString);
                tabPane.getTabs().add(tabPane.getTabs().size() - 1, newBrowserTab);
                tabPane.getSelectionModel().select(newBrowserTab);
            }
        });

        root = new BorderPane();
        root.setCenter(tabPane);
        root.setId("root");

        scene = new Scene(root, 900, 700);
        scene.getStylesheets()
                .add(getClass().getResource("/" + RESOURCE_DIR + "/fonts.css").toExternalForm());

        primaryStage.setTitle("Markon Browser");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Tab createNewBrowserTab(String title, String initialContent) {
        Tab tab = new Tab(title);

        TextField urlField = new TextField();
        urlField.setPromptText("mctp://<host>[:port]/<route> (e.g., localhost/index.md)");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button goButton = new Button("Go");
        goButton.setDisable(true); // Initially disabled because field is empty
        urlField.textProperty().addListener((obs, oldText, newText) -> {
            goButton.setDisable(newText.trim().isEmpty());
        });

        WebView webView = new WebView();
        webView.setId("webview");

        webView.getEngine().loadContent(initialContent);

        HBox controls = new HBox(10, urlField, goButton);
        controls.setPadding(new Insets(8));
        controls.setId("controls");

        VBox vbox = new VBox(controls, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        tab.setContent(vbox);

        goButton.setOnAction(e -> {
            if (!urlField.getText().trim().isEmpty()) {
                loadUrlInTab(urlField.getText(), webView, tab, urlField);
            }
        });

        urlField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !urlField.getText().trim().isEmpty()) {
                loadUrlInTab(urlField.getText(), webView, tab, urlField);
            }
        });

        tab.setOnCloseRequest(event -> {
            int browserTabsCount = (int) tabPane.getTabs().stream()
                    .filter(t -> t != tabPane.getTabs().get(tabPane.getTabs().size() - 1)) // exclude "+" tab
                    .count();

            if (browserTabsCount <= 1) {
                // Prevent closing last browser tab
                event.consume();
            }
        });

        return tab;
    }

    private void loadUrlInTab(String rawInput, WebView webView, Tab tab, TextField urlField) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            webView.getEngine().loadContent("<h2>Invalid URL</h2>");
            setTabTitle(tab, "Invalid URL");
            return;
        }

        String url = rawInput.trim();

        // Reject if it's a non-MCTP protocol
        if (url.contains("://") && !url.startsWith("mctp://")) {
            webView.getEngine().loadContent("""
                        <h2 style='color:darkred;'>Error</h2>
                        <p>Protocols like <code>http://</code> or <code>https://</code> are not supported.</p>
                        <p>Please use format like:</p>
                        <ul>
                            <li><code>localhost[:port]/path</code></li>
                            <li><code>mctp://localhost[:port]/path</code></li>
                        </ul>
                    """);
            setTabTitle(tab, "Invalid Protocol");
            return;
        }

        // Normalize input (add mctp:// if missing)
        if (!url.startsWith("mctp://")) {
            url = "mctp://" + url;
        }

        try {
            String withoutProtocol = url.substring("mctp://".length());

            String host;
            int port = 9196;
            String path = "/index.md";

            int slashIndex = withoutProtocol.indexOf('/');
            String hostPortPart = (slashIndex == -1) ? withoutProtocol : withoutProtocol.substring(0, slashIndex);
            path = (slashIndex == -1) ? "/" : withoutProtocol.substring(slashIndex);

            if (hostPortPart.contains(":")) {
                String[] hp = hostPortPart.split(":", 2);
                host = hp[0];
                if (hp[1].isEmpty()) {
                    throw new IllegalArgumentException("Port number is empty.");
                }
                port = Integer.parseInt(hp[1]);
            } else {
                host = hostPortPart;
            }

            if (path.equals("/")) {
                path = "/index.md";
            }

            String markdown = fetchMarkdownFromMctpServer(host, port, path);
            String html = convertMarkdownToHtml(markdown);
            webView.getEngine().loadContent(html);

            String pageTitle = extractTitleFromHtml(html);
            if (pageTitle == null || pageTitle.isEmpty()) {
                pageTitle = host + path;
            }
            setTabTitle(tab, pageTitle);

            // Update the text field, **removing** mctp:// prefix if present
            String cleanUrl = host + (port != 9196 ? (":" + port) : "") + path;
            urlField.setText(cleanUrl);

        } catch (Exception e) {
            e.printStackTrace();
            webView.getEngine().loadContent("<h2>Error: " + e.getMessage() + "</h2>");
            setTabTitle(tab, url);
        }
    }

    private void setTabTitle(Tab tab, String title) {
        tab.setText(title);
    }

    private String fetchMarkdownFromMctpServer(String host, int port, String path) {
        try (Socket socket = new Socket(host, port)) {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            String request = String.format("MCTP/1.0\nHost: %s\nRequest: GET %s\n\n", host, path);
            writer.write(request);
            writer.flush();

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Ignore headers
            }

            StringBuilder body = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                body.append((char) ch);
            }

            return body.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "# Error\nCould not fetch content from MCTP server at " + host + ":" + port;
        }
    }

    private String convertMarkdownToHtml(String markdown) {
        String headContent = "";
        String bodyMarkdown = markdown;

        String headRegex = "(?s)<head>(.*?)</head>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(headRegex,
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(markdown);

        if (matcher.find()) {
            headContent = matcher.group(1).trim();
            bodyMarkdown = matcher.replaceFirst("").trim();
        }

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String renderedBodyHtml = renderer.render(parser.parse(bodyMarkdown));

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                %s
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(headContent, renderedBodyHtml);
    }

    private String extractTitleFromHtml(String html) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "<title>(.*?)</title>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

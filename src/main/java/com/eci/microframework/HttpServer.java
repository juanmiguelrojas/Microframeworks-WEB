package com.eci.microframework;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.eci.microframework.utilities.URLParser;

public class HttpServer {

    private static final int PORT = 8080;
    private static final Map<String, Route> GET_SERVICES = new HashMap<>();
    private static String staticFilesPath = "/webroot";

    @FunctionalInterface
    public interface Route {
        String handle(Request req, Response res);
    }

    public static class Request {
        private final String path;
        private final Map<String, String> values;

        public Request(String path, Map<String, String> values) {
            this.path = path;
            this.values = values;
        }

        public String getPath() {
            return path;
        }

        public String getValues(String key) {
            return values.get(key);
        }
    }

    public static class Response {
        private String contentType = "text/plain; charset=UTF-8";

        public String getContentType() {
            return contentType;
        }

        public void type(String contentType) {
            this.contentType = contentType;
        }
    }

    public static void get(String path, Route route) {
        GET_SERVICES.put(path, route);
    }

    public static void staticfiles(String path) {
        String normalized = path == null ? "" : path.trim();
        if (!normalized.isEmpty() && !normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        staticFilesPath = normalized;
    }

    public static void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on http://localhost:" + PORT);
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handle(client);
                } catch (Exception e) {
                    System.err.println("Request error: " + e.getMessage());
                }
            }
        }
    }

    private static void handle(Socket client) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            return;
        }

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            // Ignore headers for this simple educational framework
        }

        String[] tokens = requestLine.split(" ");
        if (tokens.length < 2) {
            sendText(out, 400, "text/plain; charset=UTF-8", "Bad Request");
            return;
        }

        String method = tokens[0];
        String rawUri = tokens[1];

        if (!"GET".equalsIgnoreCase(method)) {
            sendText(out, 404, "text/plain; charset=UTF-8", "Not Found");
            return;
        }

        URLParser parser = new URLParser("http://localhost:" + PORT + rawUri);
        String path = parser.getPath();

        Route route = GET_SERVICES.get(path);
        if (route != null) {
            Request req = new Request(path, parser.getQueryParams());
            Response res = new Response();
            String body = route.handle(req, res);
            sendText(out, 200, res.getContentType(), body == null ? "" : body);
            return;
        }

        if (serveStatic(path, out)) {
            return;
        }

        sendText(out, 404, "text/plain; charset=UTF-8", "Not Found");
    }

    private static boolean serveStatic(String path, BufferedOutputStream out) throws IOException {
        if (staticFilesPath == null || staticFilesPath.isBlank()) {
            return false;
        }

        String filePath = "/".equals(path) ? "/index.html" : path;
        if (filePath.contains("..")) {
            sendText(out, 403, "text/plain; charset=UTF-8", "Forbidden");
            return true;
        }

        String resourcePath = staticFilesPath + filePath;
        try (InputStream file = HttpServer.class.getResourceAsStream(resourcePath)) {
            if (file == null) {
                return false;
            }

            byte[] data = file.readAllBytes();
            sendBytes(out, 200, detectContentType(resourcePath), data);
            return true;
        }
    }

    private static void sendText(BufferedOutputStream out, int status, String contentType, String body) throws IOException {
        sendBytes(out, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(BufferedOutputStream out, int status, String contentType, byte[] body) throws IOException {
        String statusText = switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            default -> "";
        };

        String headers = "HTTP/1.1 " + status + " " + statusText + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n\r\n";

        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private static String detectContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain; charset=UTF-8";
    }

    public static Map<String, String> parseQueryParams(String query) {
        return URLParser.parseQueryParams(query);
    }

    public static void main(String[] args) throws Exception {
        staticfiles("/webroot");

        get("/hello", (req, res) -> "Hello " + req.getValues("name"));

        get("/pi", (req, res) -> String.valueOf(Math.PI));

        start();
    }
}

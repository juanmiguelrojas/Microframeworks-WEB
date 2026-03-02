package com.eci.microframework;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class URLParser {
	private final String originalUrl;
	private final String protocol;
	private final String host;
	private final int port;
	private final String path;
	private final String query;
	private final Map<String, String> queryParams = new LinkedHashMap<>();

	public URLParser(String url) throws MalformedURLException {
		this.originalUrl = url;
		URL u = new URL(url);
		this.protocol = u.getProtocol();
		this.host = u.getHost();
		this.port = (u.getPort() == -1) ? u.getDefaultPort() : u.getPort();
		this.path = u.getPath();
		this.query = u.getQuery();
		if (this.query != null) {
			parseQuery(this.query, this.queryParams);
		}
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPath() {
		return path;
	}

	public String getQuery() {
		return query;
	}

	public Map<String, String> getQueryParams() {
		return new LinkedHashMap<>(queryParams);
	}

	private static void parseQuery(String query, Map<String, String> dest) {
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			if (pair.isEmpty()) continue;
			int idx = pair.indexOf('=');
			try {
				if (idx > 0) {
					String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
					String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
					dest.put(key, value);
				} else {
					String key = URLDecoder.decode(pair, "UTF-8");
					dest.put(key, "");
				}
			} catch (UnsupportedEncodingException e) {
				// UTF-8 is always supported; ignore silently
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Original: ").append(originalUrl).append('\n');
		sb.append("Protocol: ").append(protocol).append('\n');
		sb.append("Host: ").append(host).append('\n');
		sb.append("Port: ").append(port).append('\n');
		sb.append("Path: ").append(path).append('\n');
		sb.append("Query: ").append(query).append('\n');
		sb.append("Params: ").append(queryParams).append('\n');
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		String test = "http://example.com:8080/path/to/resource?foo=bar&name=John+Doe&empty";
		URLParser parser = new URLParser(test);
		System.out.println(parser);
	}
}


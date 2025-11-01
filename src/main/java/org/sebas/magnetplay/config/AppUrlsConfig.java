package org.sebas.magnetplay.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simple component that exposes configured application URLs (frontend, streaming, torrent api, backend).
 * These values are read from `application.properties` and can be overridden via environment variables
 * (for example by docker-compose env_file / env).
 */
@Component
public class AppUrlsConfig {

	@Value("${app.frontend.url:http://localhost:5173}")
	private String frontendUrl;

	@Value("${app.streaming.url:http://localhost:3000}")
	private String streamingUrl;

	@Value("${app.torrentapi.url:http://localhost:8009}")
	private String torrentApiUrl;

	@Value("${app.backend.url:http://localhost:8080}")
	private String backendUrl;

	public String getFrontendUrl() {
		return frontendUrl;
	}

	public String getStreamingUrl() {
		return streamingUrl;
	}

	public String getTorrentApiUrl() {
		return torrentApiUrl;
	}

	public String getBackendUrl() {
		return backendUrl;
	}

	@PostConstruct
	private void logConfiguredUrls() {
		// Avoid using a logging framework here to keep this class low-dependency during startup.
		System.out.println("Configured App URLs: frontend=" + frontendUrl + ", backend=" + backendUrl + ", streaming=" + streamingUrl + ", torrentApi=" + torrentApiUrl);
	}

}


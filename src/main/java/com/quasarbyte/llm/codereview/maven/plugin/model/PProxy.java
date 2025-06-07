package com.quasarbyte.llm.codereview.maven.plugin.model;

/**
 * Represents the configuration for a network proxy.
 * <p>
 * This class can be mapped to a {@link java.net.Proxy} instance.
 * </p>
 */
public class PProxy {

    /**
     * The type of the proxy.
     * <p>
     * Should be one of {@code DIRECT}, {@code HTTP}, or {@code SOCKS}.
     * </p>
     */
    private String type;

    /**
     * The hostname or IP address of the proxy server.
     * <p>
     * Not required for type {@code DIRECT}.
     * </p>
     */
    private String host;

    /**
     * The port number of the proxy server.
     * <p>
     * Not required for type {@code DIRECT}.
     * </p>
     */
    private Integer port;

    public String getType() {
        return type;
    }

    public PProxy setType(String type) {
        this.type = type;
        return this;
    }

    public String getHost() {
        return host;
    }

    public PProxy setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public PProxy setPort(Integer port) {
        this.port = port;
        return this;
    }
}

package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PProxy;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PProxyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Maps a Proxy configuration to a {@link java.net.Proxy} instance.
 */
public class PProxyMapperImpl implements PProxyMapper {

    private static final Logger logger = LoggerFactory.getLogger(PProxyMapperImpl.class);

    @Override
    public Proxy map(PProxy proxy) {
        if (proxy == null) {
            logger.warn("Provided Proxy is null, returning null.");
            return null;
        }

        logger.info("Mapping Proxy: {}", format(proxy));

        if (proxy.getType() == null) {
            logger.error("Proxy type cannot be null.");
            throw new ValidationException("Proxy type cannot be null.");
        }

        Proxy.Type proxyType;
        try {
            proxyType = Proxy.Type.valueOf(proxy.getType().toUpperCase());
            logger.debug("Parsed proxy type: {}", proxyType);
        } catch (IllegalArgumentException ex) {
            logger.error("Unknown proxy type: {}", proxy.getType(), ex);
            throw new ValidationException("Unknown proxy type: " + proxy.getType(), ex);
        }

        if (proxyType == Proxy.Type.DIRECT) {
            logger.info("Proxy type is DIRECT. No proxy will be used.");
            return null;
        }

        String host = proxy.getHost();
        Integer port = proxy.getPort();

        if (host == null || host.trim().isEmpty()) {
            logger.error("Proxy host cannot be null or blank.");
            throw new ValidationException("Proxy host cannot be null or blank.");
        }
        if (port == null) {
            logger.error("Proxy port cannot be null.");
            throw new ValidationException("Proxy port cannot be null.");
        }

        InetSocketAddress address = new InetSocketAddress(host, port);
        logger.info("Proxy created: type={}, host={}, port={}", proxyType, host, port);
        return new Proxy(proxyType, address);
    }

    private static String format(PProxy proxy) {
        return "PProxy{" +
                "type='" + proxy + '\'' +
                ", host='" + proxy + '\'' +
                ", port=" + proxy +
                '}';
    }
}

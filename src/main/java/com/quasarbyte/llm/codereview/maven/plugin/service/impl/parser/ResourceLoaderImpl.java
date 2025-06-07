package com.quasarbyte.llm.codereview.maven.plugin.service.impl.parser;

import com.quasarbyte.llm.codereview.maven.plugin.service.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceLoaderImpl implements ResourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLoaderImpl.class);

    private final ClassLoader classLoader;

    public ResourceLoaderImpl() {
        this.classLoader = getClass().getClassLoader();
        logger.debug("ResourceLoaderImpl initialized with default class loader.");
    }

    public ResourceLoaderImpl(ClassLoader classLoader) {
        this.classLoader = classLoader;
        logger.debug("ResourceLoaderImpl initialized with provided class loader: {}", classLoader);
    }

    @Override
    public String load(String location) throws IOException {
        return load(location, null);
    }

    @Override
    public String load(String location, String codePage) throws IOException {
        logger.info("Loading resource from location: '{}' with codePage: '{}'", location, codePage);
        if (location == null || location.isEmpty()) {
            logger.error("Resource location must not be null or empty.");
            throw new IllegalArgumentException("Resource location must not be null or empty");
        }

        Charset charset = (codePage != null && !codePage.isEmpty())
                ? Charset.forName(codePage)
                : StandardCharsets.UTF_8;
        logger.debug("Using charset: '{}'", charset);

        if (location.startsWith("classpath:")) {
            String path = location.substring("classpath:".length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            logger.debug("Loading classpath resource: '{}'", path);
            try (InputStream in = classLoader.getResourceAsStream(path)) {
                if (in == null) {
                    logger.error("Classpath resource not found: {}", path);
                    throw new IOException("Classpath resource not found: " + path);
                }
                byte[] bytes = readAllBytes(in);
                logger.debug("Read {} bytes from classpath resource '{}'", bytes.length, path);
                return new String(bytes, charset);
            }
        } else if (location.startsWith("file:")) {
            String path = location.substring("file:".length());
            logger.debug("Loading file resource: '{}'", path);
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            logger.debug("Read {} bytes from file '{}'", bytes.length, path);
            return new String(bytes, charset);
        } else {
            logger.debug("Loading file resource (default): '{}'", location);
            byte[] bytes = Files.readAllBytes(Paths.get(location));
            logger.debug("Read {} bytes from file '{}'", bytes.length, location);
            return new String(bytes, charset);
        }
    }

    private static byte[] readAllBytes(InputStream input) throws IOException {
        final int bufLen = 4096;
        byte[] buf = new byte[bufLen];
        int readLen;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int totalBytes = 0;
            while ((readLen = input.read(buf, 0, bufLen)) != -1) {
                output.write(buf, 0, readLen);
                totalBytes += readLen;
            }
            logger.trace("Total bytes read from InputStream: {}", totalBytes);
            return output.toByteArray();
        }
    }
}

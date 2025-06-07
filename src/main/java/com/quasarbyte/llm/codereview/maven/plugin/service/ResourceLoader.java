package com.quasarbyte.llm.codereview.maven.plugin.service;

import java.io.IOException;

/**
 * Loads the contents of a resource from the classpath or file system as a String.
 * <p>
 * Supported resource location formats:
 * <ul>
 *   <li><b>Classpath resource</b>: Use the prefix {@code classpath:}, e.g. {@code classpath:/my/script.js}</li>
 *   <li><b>File system path</b>: Use an absolute or relative path, e.g. {@code /tmp/file.txt} or {@code config/settings.json}</li>
 * </ul>
 * <p>
 * The default encoding for reading resources is UTF-8, unless otherwise specified.
 */
public interface ResourceLoader {
    /**
     * Loads a resource from the classpath or file system using UTF-8 encoding.
     *
     * @param location the resource location, e.g., {@code "classpath:/foo.txt"} or {@code "/tmp/bar.txt"}
     * @return the resource contents as a String
     * @throws IOException if the resource cannot be loaded or read
     */
    String load(String location) throws IOException;

    /**
     * Loads a resource from the classpath or file system using the specified code page (charset).
     *
     * @param location the resource location, e.g., {@code "classpath:/foo.txt"} or {@code "/tmp/bar.txt"}
     * @param codePage the character encoding to use, e.g., {@code "UTF-8"}, {@code "ISO-8859-1"}, {@code "Windows-1251"};
     *                 if {@code null} or empty, UTF-8 will be used by default
     * @return the resource contents as a String
     * @throws IOException if the resource cannot be loaded or read, or if the charset is not supported
     */
    String load(String location, String codePage) throws IOException;
}

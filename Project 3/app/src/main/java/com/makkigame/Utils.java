package com.makkigame;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for loading resources into direct ByteBuffers.
 */
public class Utils {
    /**
     * Loads a classpath resource into a direct ByteBuffer.
     * 
     * @param resource The path to the resource in the classpath.
     * @return A direct ByteBuffer containing the resource data.
     */
    public static ByteBuffer loadResource(String resource) {
        URL url = Utils.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }

        Path path;
        try {
            path = Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for resource: " + resource, e);
        }

        try {
            long fileSize = Files.size(path);
            // Allocate a direct buffer for STBImage
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) fileSize + 1);
            try (SeekableByteChannel channel = Files.newByteChannel(path)) {
                while (channel.read(buffer) != -1) {
                    // Read until EOF
                }
            }
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resource, e);
        }
    }
}

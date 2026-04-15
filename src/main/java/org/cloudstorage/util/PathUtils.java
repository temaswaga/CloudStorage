package org.cloudstorage.util;

public class PathUtils {

    public static String[] parsePathParts(String path) {
        String stripped = path.replaceAll("^/+|/+$", "");
        int lastSlash = stripped.lastIndexOf("/");

        String parentPath = (lastSlash >= 0) ? stripped.substring(0, lastSlash) : null;
        String name = (lastSlash >= 0) ? stripped.substring(lastSlash + 1) : stripped;

        return new String[]{parentPath, name};
    }
}
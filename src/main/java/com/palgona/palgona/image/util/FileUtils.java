package com.palgona.palgona.image.util;

import java.util.UUID;

public class FileUtils {

    public static String createFileName(String fileName) {
        FileExtension extension = FileExtension.from(fileName);
        return UUID.randomUUID().toString().concat(extension.getExtension());
    }
}

package org.example.finaloop.service;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImageStore {

    private static final String FOLDER = "images/courses";

    private ImageStore() {
    }

    public static String saveCourseImage(File source) {
        try {
            Path folder = Paths.get(FOLDER);
            Files.createDirectories(folder);

            String fileName = source.getName();
            String extension = fileName.contains(".")
                    ? fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
                    : ".png";

            Path target = folder.resolve("course-" + System.currentTimeMillis() + extension);
            Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return FOLDER + "/" + target.getFileName();

        } catch (IOException e) {
            System.err.println("SaveImage Error: " + e.getMessage());
            return null;
        }
    }

    public static Image load(String path, double width, double height) {
        if (path == null || path.isBlank()) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Image image = new Image(file.toURI().toString(), width * 2, height * 2, true, true, false);
        return image.isError() ? null : image;
    }
}

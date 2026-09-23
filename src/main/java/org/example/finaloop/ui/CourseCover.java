package org.example.finaloop.ui;

import org.example.finaloop.service.ImageStore;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class CourseCover {

    public static final String CARD = "cover";
    public static final String THUMB = "cover-thumb";
    public static final String PREVIEW = "cover-preview";

    private CourseCover() {
    }

    public static StackPane build(int courseId, String courseName, String imagePath,
                                  double width, double height, String kind) {
        StackPane cover = new StackPane();
        cover.setMinSize(width, height);
        cover.setPrefSize(width, height);
        cover.setMaxSize(width, height);
        cover.getStyleClass().add(kind);

        Image image = ImageStore.load(imagePath, width, height);
        if (image != null) {
            cover.getChildren().add(croppedImage(image, width, height, kind));
        } else {
            cover.getStyleClass().add("cover-auto");
            Label initials = new Label(Ui.initials(courseName));
            initials.getStyleClass().add("cover-text");
            cover.getChildren().add(initials);
        }
        return cover;
    }

    private static ImageView croppedImage(Image image, double width, double height, String kind) {
        double targetRatio = width / height;
        double imageRatio = image.getWidth() / image.getHeight();
        double viewWidth = image.getWidth();
        double viewHeight = image.getHeight();

        if (imageRatio > targetRatio) {
            viewWidth = image.getHeight() * targetRatio;
        } else {
            viewHeight = image.getWidth() / targetRatio;
        }
        double x = (image.getWidth() - viewWidth) / 2;
        double y = (image.getHeight() - viewHeight) / 2;

        ImageView view = new ImageView(image);
        view.setViewport(new Rectangle2D(x, y, viewWidth, viewHeight));
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setSmooth(true);

        double radius = CARD.equals(kind) ? 22 : (THUMB.equals(kind) ? 16 : 20);
        double clipHeight = CARD.equals(kind) ? height + radius : height;
        Rectangle clip = new Rectangle(width, clipHeight);
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        view.setClip(clip);
        return view;
    }
}

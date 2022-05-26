package javafx.ryhmat88_2;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class Root extends StackPane {
    private final Pane actualRoot;
    private final Rectangle blackScreen;

    public Pane getActualRoot() {
        return actualRoot;
    }

    public Root(Pane actualRoot) {
        this.actualRoot = actualRoot;
        blackScreen = new Rectangle();
        blackScreen.widthProperty().bind(actualRoot.widthProperty());
        blackScreen.heightProperty().bind(actualRoot.heightProperty());
        blackScreen.setVisible(false);
        this.getChildren().addAll(actualRoot, blackScreen);
    }

    public void setBlackScreen(boolean bool) {
        blackScreen.setVisible(bool);
    }
}

package javafx.ryhmat88_2;

import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class CardBox extends VBox {
    private final ToggleButton cardButton;
    private final Label cardLabel;

    public CardBox(ToggleButton cardButton, Label cardLabel) {
        this.cardButton = cardButton;
        this.cardLabel = cardLabel;
    }

    public void setAmount(int amount) {
        cardLabel.setText("x" + amount);
    }

    public boolean collectedCard() {
        return cardButton.isSelected();
    }

    public void resetCardButton() {
        cardButton.setSelected(false);
    }
}

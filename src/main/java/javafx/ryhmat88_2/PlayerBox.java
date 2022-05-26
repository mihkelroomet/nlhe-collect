package javafx.ryhmat88_2;

import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class PlayerBox extends StackPane {
    private final StackPane outerPlayerBox;
    private final ToggleButton knockButton;
    private final TextField playerName;
    private final CardBox[] cardBoxes;
    private final TextField knockBox;

    public PlayerBox(StackPane outerPlayerBox, ToggleButton knockButton, TextField playerName, CardBox[] cardBoxes, TextField knockBox) {
        this.outerPlayerBox = outerPlayerBox;
        this.knockButton = knockButton;
        this.playerName = playerName;
        this.cardBoxes = cardBoxes;
        this.knockBox = knockBox;
    }

    public boolean gotKnocked() {
        return knockButton.isSelected();
    }

    public void resetKnockedButton() {
        knockButton.setSelected(false);
    }

    public void showPlayerBox() {
        outerPlayerBox.setVisible(true);
    }

    public void hidePlayerBox() {
        outerPlayerBox.setVisible(false);
    }

    public void showKnockBox() {
        knockBox.setVisible(true);
    }

    public void hideKnockBox() {
        knockBox.setVisible(false);
    }

    public void updateKnockedFor(int knockedFor) {
        knockBox.setText("Knocked for " + knockedFor + " rounds");
    }

    public void setCardsCollected(int whichCard, int howMany) {
        cardBoxes[whichCard].setAmount(howMany);
    }

    public CardBox getCardBox(int whichBox) {
        return cardBoxes[whichBox];
    }

    public void setName(String name) {
        playerName.setText(name);
    }
}

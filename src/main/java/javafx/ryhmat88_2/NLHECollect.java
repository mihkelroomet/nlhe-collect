package javafx.ryhmat88_2;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NLHECollect extends Application {
    public static String[] VALUES = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};
    public static String[] SUITS = {"♥", "♦", "♣", "♠"};

    private final DoubleProperty windowWidth = new SimpleDoubleProperty(960);
    private final DoubleProperty windowHeight = new SimpleDoubleProperty(540);
    private final int maxPlayers = 7;
    private final List<Player> players = new ArrayList<>();
    private final PlayerBox[] playerBoxes = new PlayerBox[maxPlayers]; // viited vasaku pane'i elementidele
    private final String sisendfail = "nimed.dat";
    private Spinner<Integer> winSpinner;
    private Spinner<Integer> penaltySpinner;
    private TextField errorBox;
    private Label winnerText;
    private Stage stage = new Stage();
    private Root mainRoot;
    private Root helpRoot;
    private Root winnerRoot;

    // loeb kokku, mitu erinevat kaarti on valitud
    private int countDiffSelectedCards() {
        Set<Integer> diffSelectedCards = new HashSet<>();
        for (int i = 0; i < players.size(); i++) {
            PlayerBox playerBox = playerBoxes[i];
            for (int j = 0; j < 52; j++) {
                CardBox cardBox = playerBox.getCardBox(j);
                if (cardBox.collectedCard()) {
                    diffSelectedCards.add(j);
                }
            }
        }
        return diffSelectedCards.size();
    }

    // loeb kokku, mitu mängijat on see käsi knocked
    private int countPlayersKnocked() {
        int playersKnocked = 0;
        for (int i = 0; i < players.size(); i++) {
            PlayerBox playerBox = playerBoxes[i];
            if (playerBox.gotKnocked()) {
                playersKnocked++;
            }
        }
        return playersKnocked;
    }

    // näitab errorit
    private void displayError(String errorText) {
        errorBox.setText("Error: " + errorText);

        SequentialTransition st = new SequentialTransition();
        st.setCycleCount(1);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), errorBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.millis(3000));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), errorBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        st.getChildren().addAll(fadeIn, pause, fadeOut);
        st.playFromStart();
    }

    // teatab võitja nime
    private void declareWinner(String winnerName) {
        switchToRoot(winnerRoot);
        winnerText.setText(winnerName + " wins!");

        FadeTransition ft = new FadeTransition(Duration.millis(2000), winnerRoot.getActualRoot());
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.playFromStart();
    }

    // lisab uue mängija
    private void newPlayer() {
        if (players.size() < maxPlayers) {
            players.add(new Player(players.size() + 1));
            refreshLeftPane();
        }
    }

    // uus mäng
    private void newGame() {
        for (int i = 0; i < players.size(); i++) {
            PlayerBox playerBox = playerBoxes[i];
            Player player = players.get(i);

            playerBox.resetKnockedButton();
            playerBox.hideKnockBox();
            player.setKnockedFor(0);

            player.resetCardsCollected();

            for (int j = 0; j < 52; j++) {
                CardBox cardBox = playerBox.getCardBox(j);
                cardBox.resetCardButton();
            }
        }
        refreshLeftPane();
    }

    // järgmine käsi
    private void nextHand() {
        if (countDiffSelectedCards() > 5 || countDiffSelectedCards() > countPlayersKnocked() + 1) {
            displayError("Invalid Selection"); // näita errorit, kui valik vigane
            return;
        }

        // vähendame iga mängija knockedFor väärtust ühe võrra, kui see on suurem nullist
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.setKnockedFor(Math.max(0, player.getKnockedFor() - 1));
        }

        for (int i = 0; i < players.size(); i++) {
            PlayerBox playerBox = playerBoxes[i];
            Player player = players.get(i);

            // vaatame, kas mängija sai knocked
            if (playerBox.gotKnocked()) {
                player.setKnockedFor(penaltySpinner.getValue());
                playerBox.resetKnockedButton();
            }

            // vaatame, kas mängija sai kaarte juurde
            for (int j = 0; j < 52; j++) {
                CardBox cardBox = playerBox.getCardBox(j);
                if (cardBox.collectedCard()) {
                    player.collectCard(j);
                    cardBox.resetCardButton();
                    if (player.getCardsCollected(j) >= winSpinner.getValue()) {
                        declareWinner(player.getName());
                    }
                }
            }
        }
        refreshLeftPane();
    }

    // uuendab infot vasakus pane'is
    private void refreshLeftPane() {
        for (int i = 0; i < maxPlayers; i++) {
            playerBoxes[i].hidePlayerBox();
            playerBoxes[i].hideKnockBox();
        }
        for (int i = 0; i < players.size(); i++) {
            PlayerBox playerBox = playerBoxes[i];
            Player player = players.get(i);

            playerBox.setName(player.getName());
            playerBox.showPlayerBox();

            for (int j = 0; j < 52; j++) {
                playerBox.setCardsCollected(j, player.getCardsCollected(j));
            }

            playerBox.updateKnockedFor(player.getKnockedFor());
            if (player.getKnockedFor() > 0) {
                playerBox.showKnockBox();
            }
        }
    }

    // loeb failist esialgsed nimed
    private void loeFailistEsialgsedNimed(String sisendfail) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(sisendfail))) {
            int mitu = dis.readInt();
            for (int i = 0; i < Math.min(mitu, maxPlayers); i++) {
                players.add(new Player(dis.readUTF()));
            }
        }
    }

    // tekitab HBoxi, milles on antud tekstiga Label ja antud väärtustega Spinner<Integer>
    private HBox labelJaSpinner(String tekst, int spinnerMin, int spinnerMax, int spinnerInitial) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.spacingProperty().bind(box.widthProperty().multiply(0.05));

        Label label = new Label(tekst);
        label.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> spinner = new Spinner<>();
        spinner.prefWidthProperty().bind(box.widthProperty().multiply(0.45));
        spinner.prefHeightProperty().bind(box.heightProperty());
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(spinnerMin, spinnerMax, spinnerInitial));
        spinner.setEditable(true);

        box.getChildren().addAll(label, spinner);
        if (tekst.equals("Win @")) {
            winSpinner = spinner;
        }
        else if (tekst.equals("Penalty")) {
            penaltySpinner = spinner;
        }
        return box;
    }

    // seob Regioni teksti suuruse Pane'i suurusega
    private void bindFontSize(Region region, Pane pane, int divideBy) {
        region.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", pane.heightProperty().add(pane.widthProperty()).divide(divideBy).asString(), ";"
        ));
    }

    private Pane createLeftPane() {
        double playerBoxWidth = 0.94;
        double errorBoxWidth = 0.8;
        double errorBoxHeight = 0.07;
        double spacingAndPadding = 0.015;
        double playerBoxHeight = (1 - (maxPlayers + 2) * spacingAndPadding - errorBoxHeight) / maxPlayers;

        VBox pane = new VBox();
        pane.setId("button-pane");
        pane.prefHeightProperty().bind(windowHeight);
        pane.prefWidthProperty().bind(windowWidth.multiply(0.75)); // vasak pane moodustab 75% aknast
        pane.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(pane.heightProperty().multiply(spacingAndPadding).doubleValue()), pane.heightProperty()
        ));
        pane.spacingProperty().bind(pane.heightProperty().multiply(spacingAndPadding));
        pane.setAlignment(Pos.CENTER);

        // mängijad
        for (int i = 0; i < maxPlayers; i++) {
            int mitmes = i; // eventHandlerite jaoks
            StackPane outerPlayerBox = new StackPane();
            outerPlayerBox.maxWidthProperty().bind(pane.widthProperty().multiply(playerBoxWidth));
            outerPlayerBox.minWidthProperty().bind(pane.widthProperty().multiply(playerBoxWidth));
            outerPlayerBox.prefHeightProperty().bind(pane.heightProperty().multiply(playerBoxHeight));
            outerPlayerBox.getStyleClass().add("player-box");

            HBox innerPlayerBox = new HBox();
            innerPlayerBox.spacingProperty().bind(outerPlayerBox.widthProperty().multiply(0.01));
            innerPlayerBox.paddingProperty().bind(Bindings.createObjectBinding(
                    () -> new Insets(0, outerPlayerBox.widthProperty().multiply(0.01).doubleValue(),
                            0, outerPlayerBox.widthProperty().multiply(0.01).doubleValue()), outerPlayerBox.widthProperty()
            ));
            innerPlayerBox.setAlignment(Pos.CENTER);

            // knocki valimine
            ToggleButton knockButton = new ToggleButton("Knock");
            knockButton.prefWidthProperty().bind(outerPlayerBox.widthProperty().multiply(0.09));
            bindFontSize(knockButton, outerPlayerBox, 60);

            // mängija nimi
            TextField playerName = new TextField("Player " + (i + 1));
            playerName.prefWidthProperty().bind(outerPlayerBox.widthProperty().multiply(0.14));
            playerName.textProperty().addListener((observable, oldValue, newValue) -> players.get(mitmes).setName(newValue));
            bindFontSize(playerName, outerPlayerBox, 40);
            playerName.getStyleClass().add("player-name");

            // kaartide valimine
            GridPane cardPane = new GridPane();
            cardPane.prefWidthProperty().bind(outerPlayerBox.widthProperty().multiply(0.67));
            cardPane.setAlignment(Pos.CENTER);

            CardBox[] cardBoxes = new CardBox[52];
            for (int rida = 0; rida < 2; rida++) {
                for (int veerg = 0; veerg < 26; veerg++) {
                    VBox cardBox = new VBox();
                    cardBox.setAlignment(Pos.CENTER);

                    String cardName = VALUES[veerg % 13] + SUITS[(13 * rida + veerg) / 13];
                    ToggleButton cardButton = new ToggleButton(cardName);
                    bindFontSize(cardButton, outerPlayerBox, 115);
                    cardButton.getStyleClass().add("card-button");
                    if (rida == 0) {
                        cardButton.getStyleClass().add("red-text");
                    }

                    Label cardLabel = new Label("x0");
                    bindFontSize(cardLabel, outerPlayerBox, 100);
                    cardLabel.getStyleClass().add("black-text");

                    cardBox.getChildren().addAll(cardButton, cardLabel);

                    cardPane.add(cardBox, veerg, rida);

                    cardBoxes[26 * rida + veerg] = new CardBox(cardButton, cardLabel);
                }
            }

            // mängija kustutamine
            Button deleteButton = new Button("X");
            deleteButton.prefWidthProperty().bind(outerPlayerBox.widthProperty().multiply(0.05));
            deleteButton.setOnAction(event -> {
                players.remove(mitmes);
                refreshLeftPane();
            });
            bindFontSize(deleteButton, outerPlayerBox, 35);
            deleteButton.getStyleClass().add("delete-button");

            innerPlayerBox.getChildren().addAll(knockButton, playerName, cardPane, deleteButton);

            // teade selle kohta, kui mängija on knocked
            TextField knockBox = new TextField("Knocked for 10 rounds");
            knockBox.prefWidthProperty().bind(outerPlayerBox.widthProperty());
            knockBox.prefHeightProperty().bind(outerPlayerBox.heightProperty());
            knockBox.setAlignment(Pos.CENTER);
            knockBox.setEditable(false); // mittemuudetav
            knockBox.setDisable(true); // mittevalitav
            bindFontSize(knockBox, pane, 50);
            knockBox.getStyleClass().add("knock-box");
            knockBox.setVisible(false); // alguses nähtamatu

            outerPlayerBox.getChildren().addAll(innerPlayerBox, knockBox);
            outerPlayerBox.setVisible(false); // alguses nähtamatu

            pane.getChildren().add(outerPlayerBox);

            playerBoxes[i] = new PlayerBox(outerPlayerBox, knockButton, playerName, cardBoxes, knockBox);
        }

        // error box vigastest sisestustest teatamiseks
        TextField errorBox = new TextField("Error: Error message here");
        errorBox.maxWidthProperty().bind(pane.widthProperty().multiply(errorBoxWidth));
        errorBox.minWidthProperty().bind(pane.widthProperty().multiply(errorBoxWidth));
        errorBox.prefHeightProperty().bind(pane.heightProperty().multiply(errorBoxHeight));
        errorBox.setAlignment(Pos.CENTER_LEFT);
        errorBox.setEditable(false); // mittemuudetav
        errorBox.setDisable(true); // mittevalitav
        bindFontSize(errorBox, pane, 50);
        errorBox.setId("error-box");
        this.errorBox = errorBox;

        pane.getChildren().add(errorBox);

        return pane;
    }

    private Pane createRightPane() {
        VBox pane = new VBox();
        pane.setAlignment(Pos.TOP_CENTER);
        pane.prefWidthProperty().bind(windowWidth.multiply(0.25)); // parem pane moodustab 25% aknast
        pane.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(pane.heightProperty().multiply(0.05).doubleValue()), pane.heightProperty()
        ));
        pane.spacingProperty().bind(pane.heightProperty().multiply(0.05));

        // järgmine käsi
        Button nextHandButton = new Button("Next Hand");
        nextHandButton.setOnAction(event -> nextHand());

        // uus mängija
        Button newPlayerButton = new Button("New Player");
        newPlayerButton.setOnAction(event -> newPlayer());

        // ühesuguste kaartide arv võiduks
        HBox winAtBox = labelJaSpinner("Win @", 2, 9, 3);

        // käte arv, mis tuleb chipide otsasaamise korral väljas istuda
        HBox penaltyBox = labelJaSpinner("Penalty", 0, 99, 10);

        // täisekraan
        Button fullScreenButton = new Button("Full Screen");
        fullScreenButton.setOnAction(event -> stage.setFullScreen(!stage.isFullScreen()));

        // abi ja juhised
        Button helpButton = new Button("Help");
        helpButton.setOnAction(event -> switchToRoot(helpRoot));

        // väljumine
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(event -> Platform.exit());

        // lisame kõik objektid paremasse pane'i ja seome nende mõõtmed
        Region[] rightPaneRegions = {nextHandButton, newPlayerButton, winAtBox, penaltyBox, fullScreenButton, helpButton, quitButton};
        for (Region rightPaneRegion : rightPaneRegions) {
            pane.getChildren().add(rightPaneRegion);
            rightPaneRegion.prefWidthProperty().bind(pane.widthProperty().multiply(0.65));
            rightPaneRegion.prefHeightProperty().bind(pane.heightProperty().multiply(0.05));

            // teksti skaleerimine
            if (rightPaneRegion instanceof Button) {
                bindFontSize(rightPaneRegion, pane, 40);
            }
            else {
                bindFontSize(rightPaneRegion, pane, 50);
            }
        }

        return pane;
    }

    private Root createMainRoot() {
        BorderPane mainPane = new BorderPane();
        mainPane.setLeft(createLeftPane());
        mainPane.setRight(createRightPane());
        return new Root(mainPane);
    }

    private Root createHelpRoot() {
        VBox pane = new VBox();
        pane.setAlignment(Pos.CENTER);
        pane.prefWidthProperty().bind(windowWidth);
        pane.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(pane.heightProperty().multiply(0.05).doubleValue()), pane.heightProperty().multiply(0.05)
        ));
        pane.spacingProperty().bind(pane.heightProperty().multiply(0.05));

        ScrollPane sp = new ScrollPane();
        Text text = new Text(
                "This is a tool to track collected cards for NLHE Collect.\n\n" +
                        "At the end of every hand choose the collected cards and knocked players and click \"Next Hand\" or press Enter.\n\n\n" +
                        "Rules of NLHE Collect:\n\n" +
                        "You play like you would normal poker, but with a different goal in mind. " +
                        "The winner is not the player who has the most chips at the end, nor the one who knocks everybody else out. " +
                        "Instead you win by collecting cards. " +
                        "Once you have at least X (denoted by \"Win @\") of any single card, you win.\n\n" +
                        "You collect a card by winning a hand. " +
                        "The card you get is the first card of the flop. " +
                        "If the hand ends before the flop, the card that would have been the first card of the flop gets revealed for the winner to collect it. " +
                        "Note that you do not physically take possession of the card upon collecting it, it just gets added to the tracker.\n\n" +
                        "To keep players from being too aggressive, there is a penalty for losing your stack. " +
                        "Whenever you run out of chips, you will need to sit out X (denoted by \"Penalty\") hands before getting to play again. " +
                        "This also means that if a player is left in the game alone, they will be able to collect cards without competition.\n\n" +
                        "Additionally, you can collect multiple cards at once by knocking out other players. " +
                        "For every player you knock out in a hand you collect an additional community card, for up to five cards collected at once."
        );
        text.wrappingWidthProperty().bind(pane.widthProperty().multiply(0.82));
        text.setLineSpacing(1.5);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        text.setId("help-text");
        sp.setContent(text);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setId("help-text-area");

        bindFontSize(sp, pane, 70);
        sp.maxWidthProperty().bind(pane.widthProperty().multiply(0.87));
        sp.prefHeightProperty().bind(pane.heightProperty().multiply(0.65));

        Button backButton = new Button("Back");
        backButton.setAlignment(Pos.BOTTOM_CENTER);
        backButton.setOnAction(event -> switchToRoot(mainRoot));

        bindFontSize(backButton, pane, 40);
        backButton.prefWidthProperty().bind(pane.widthProperty().multiply(0.15));
        backButton.prefHeightProperty().bind(pane.heightProperty().multiply(0.05));

        pane.getChildren().addAll(sp, backButton);

        return new Root(pane);
    }

    private Root createWinnerRoot() {
        VBox pane = new VBox();
        pane.setAlignment(Pos.CENTER);
        pane.prefWidthProperty().bind(windowWidth);
        pane.spacingProperty().bind(windowWidth.multiply(0.1));
        pane.getStyleClass().add("black-background");

        Label winnerText = new Label("Winner wins!");
        bindFontSize(winnerText, pane, 20);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.prefWidthProperty().bind(windowWidth.multiply(0.5));
        buttonBox.spacingProperty().bind(windowWidth.multiply(0.1));

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> switchToRoot(mainRoot));
        bindFontSize(backButton, pane, 40);

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(event -> {
            newGame();
            switchToRoot(mainRoot);
        });
        bindFontSize(newGameButton, pane, 40);

        buttonBox.getChildren().addAll(backButton, newGameButton);

        pane.getChildren().addAll(winnerText, buttonBox);

        this.winnerText = winnerText;

        return new Root(pane);
    }

    private void switchToRoot(Parent sceneRoot) {
        stage.getScene().setRoot(sceneRoot);
    }

    // kirjutame hetkenimed faili
    @Override
    public void stop() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(sisendfail))) {
            dos.writeInt(players.size());
            for (Player player : players) {
                dos.writeUTF(player.getName());
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        loeFailistEsialgsedNimed(sisendfail);
        this.stage = stage;
        mainRoot = createMainRoot();
        helpRoot = createHelpRoot();
        winnerRoot = createWinnerRoot();
        Scene scene = new Scene(mainRoot, windowWidth.doubleValue(), windowHeight.doubleValue());
        scene.getStylesheets().add("javafx/ryhmat88_2/style.css");

        windowWidth.bind(scene.widthProperty());
        windowHeight.bind(scene.heightProperty());

        stage.setScene(scene);
        stage.setTitle("NLHE Collect");
        stage.show();

        // kui palju stage on suurem scene'ist
        // teist mõõdet polegi vaja leida, et ratiot hoida
        double stageSceneWidthDiff = stage.getWidth() - scene.getWidth();

        // constant ratio 16x9
        stage.minWidthProperty().bind(scene.heightProperty().multiply(16.0 / 9).add(stageSceneWidthDiff));
        stage.maxWidthProperty().bind(scene.heightProperty().multiply(16.0 / 9).add(stageSceneWidthDiff));

        // et ei virvendaks nii palju akna suuruse muutmisel
        stage.widthProperty().addListener(((observable, oldValue, newValue) -> new Thread(() -> {
            Root root = (Root) stage.getScene().getRoot();
            root.setBlackScreen(true);
            // raskem on teha akent väiksemaks, nii et sellega läheb kauem
            Üld.sleep((long) (1700 * Math.pow((1200 / newValue.doubleValue()), 2) * (Math.pow(oldValue.doubleValue(), 3) / Math.pow(newValue.doubleValue(), 3))));
            root.setBlackScreen(false);
        }).start()));

        // see peaks juhtuma teises threadis ja väikse delayga et ta ei virvendaks sisse alguses, aga millegipärast
        // ei saa cardLabeleid teises threadis uuendada (kõike muud saab)
        refreshLeftPane();
    }

    public static void main(String[] args) {
        launch();
    }
}
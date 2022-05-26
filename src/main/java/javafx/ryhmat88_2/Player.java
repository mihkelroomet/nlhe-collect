package javafx.ryhmat88_2;

public class Player {
    private String name;
    private int knockedFor;
    private int[] cardsCollected = new int[52];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKnockedFor() {
        return knockedFor;
    }

    public void setKnockedFor(int knockedFor) {
        this.knockedFor = knockedFor;
    }

    public int getCardsCollected(int cardNr) {
        return cardsCollected[cardNr];
    }

    public void resetCardsCollected() {
        cardsCollected = new int[52];
    }

    public Player(int nr) {
        this("Player " + nr);
    }

    public Player(String name) {
        this.name = name;
        knockedFor = 0;
    }

    public void collectCard(int cardNr) {
        cardsCollected[cardNr] = cardsCollected[cardNr] + 1;
    }
}

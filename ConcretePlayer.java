public class ConcretePlayer implements Player {
    private final boolean isDefender; // Indicates whether the player is a defender or attacker
    private int wins;
    public ConcretePlayer(boolean isDefender) {
        this.isDefender = isDefender;
        wins = 0;
    }
    @Override
    public boolean isPlayerOne() {
        return isDefender;
    }

    public void win() {
        wins++;
    }
    @Override
    public int getWins() {
        return wins;
    }

}

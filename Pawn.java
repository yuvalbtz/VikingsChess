public class Pawn extends ConcretePiece {
    private int kills;
    public Pawn(Player owner) {
        this.owner = owner;
        this.kills = 0;
        this.dist = 0;
    }
    public void kill() {
        kills++;
    }
    public void reduceKill() {
        kills--;
    }
    public int getKills() {
        return kills;
    }
    public String getKillsStr() {
        return kills + " kills";
    }

    @Override
    public String getType() {
        return (getOwner().isPlayerOne()) ? "♙" : "♟";
    }

    @Override
    public String toString() {

        return (getOwner().isPlayerOne() ? "D" : "A") + number + ": ";
    }
}

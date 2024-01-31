public class King extends ConcretePiece {
    public King(Player defender) {
        this.owner = defender;
        this.dist = 0;
    }

    public String getType() {
        return "♔";
    }

    public String toString() {

        return "K" + this.number + ": ";
    }
}

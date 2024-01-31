import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Position {
    private final int x;

    private final int y;

    private final List<Piece> pieces;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
        this.pieces = new LinkedList<>();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getPieceSize() {
        return (new HashSet(this.pieces)).size();
    }

    public void raisePiece(Piece piece) {
        this.pieces.add(piece);
    }

    public String piecesStr() {
        return "" + getPieceSize() + " pieces";
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    public void erasePiece(Piece piece) {
        this.pieces.remove(piece);
    }
}

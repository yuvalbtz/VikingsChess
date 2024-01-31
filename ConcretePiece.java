import java.util.ArrayList;
import java.util.List;

public abstract class ConcretePiece implements Piece {
    protected Player owner;

    private final List<Position> positionsHistory = new ArrayList<>();

    protected int dist;

    protected int number;

    public Player getOwner() {
        return this.owner;
    }

    public void update(Position newPosition) {
        setNewPosition(newPosition);
    }

    public int getDist() {
        return (this.dist == 0) ? calculateDist() : this.dist;
    }

    public String getDistStr() {
        return "" + getDist() + " squares";
    }

    private int calculateDist() {
        for (int i = 0; i < this.positionsHistory.size() - 1; i++) {
            int x = ((Position)this.positionsHistory.get(i)).getX(), y = ((Position)this.positionsHistory.get(i)).getY();
            int x1 = ((Position)this.positionsHistory.get(i + 1)).getX(), y1 = ((Position)this.positionsHistory.get(i + 1)).getY();
            this.dist += Math.abs(x - x1) + Math.abs(y - y1);
        }
        return this.dist;
    }

    public List<Position> getPositionHistory() {
        return this.positionsHistory;
    }

    public Position getLastPosition() {
        return this.positionsHistory.get(this.positionsHistory.size() - 1);
    }

    public void setNewPosition(Position position) {
        this.positionsHistory.add(position);
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    public abstract String getType();

    public abstract String toString();

    public void deleteLastStep() {
        this.positionsHistory.removeLast();
    }
}

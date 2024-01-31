import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameLogic implements PlayableLogic {
    public static int BOARD_SIZE = 11;
    private ConcretePiece[][] board;
    private Position[][] positions;
    private final ConcretePlayer attacker;
    private final ConcretePlayer defender;
    private boolean gameFinished;
    private boolean attackerTurn;
    private final List<ConcretePiece> pieces = new ArrayList<>();
    private Stack<Move> moveHistory; // Stack to store move history
    private record Move(Position to, List<ConcretePiece> capturedPieces) {} // Inner class to represent a move
    private List<ConcretePiece> capturedPieces;

    public GameLogic() {
        this.attackerTurn = true;
        this.attacker = new ConcretePlayer(false);
        this.defender = new ConcretePlayer(true);
        this.positions = new Position[BOARD_SIZE][BOARD_SIZE];
        reset();
    }
    @Override
    public boolean move(Position a, Position b) {
        // Check if the source piece is belong to the player how is playing now
        boolean attackerTurn = isSecondPlayerTurn();
        boolean defenderPosition = getPieceAtPosition(a).getOwner().isPlayerOne();
        if (illegalMove(a, b) || (attackerTurn && defenderPosition) || (!attackerTurn && !defenderPosition))
            return false;

        //System.out.print("("+a.getX()+", "+a.getY()+"), ("+b.getX()+", "+b.getY()+"), ");
        // Copy the current state of the bord
        capturedPieces = new ArrayList<>();

        // Move the piece
        int x = a.getX(), y = a.getY();
        ConcretePiece movingPiece = board[x][y];
        board[b.getX()][b.getY()] = movingPiece;

        // Update the piece history
        movingPiece.update(b);
        int xB = b.getX();
        int yB = b.getY();
        if (positions[xB][yB] == null)
            positions[xB][yB] = new Position(xB, yB);

        // Update the position history
        positions[xB][yB].raisePiece(movingPiece);
        board[x][y] = null;

        // Check for captured pieces or victory
        checkCaptureAndVictory(b);

        // Switch turn
        this.attackerTurn = !attackerTurn;

        // Add the move to history
        moveHistory.push(new Move(b, capturedPieces));

        return true;
    }
    private boolean illegalMove(Position a, Position b) {
        if (board[a.getX()][a.getY()] == null || // No piece in src
                board[b.getX()][b.getY()] != null || // Place is occupied
                (a.getX() != b.getX() && a.getY() != b.getY()) || // Not at the same line
                (isCorner(b) && !(board[a.getX()][a.getY()] instanceof King))) // Not a king on the Corner
            return true;

        // Scan for checking if jump over other piece
        int aX = a.getX(), bX = b.getX();
        int aY = a.getY(), bY = b.getY();
        boolean sameRow = aY == bY;
        if (sameRow) {
            int s = Math.min(aX, bX);
            int t = Math.max(aX, bX);
            for (int i = s+1; i < t; i++) {
                if (board[i][aY] != null)
                    // Jump not allowed
                    return true;
            }
        } else {
            int s = Math.min(aY, bY);
            int t = Math.max(aY, bY);
            for (int i = s+1; i < t; i++) {
                if (board[aX][i] != null)
                    // Jump not allowed
                    return true;
            }
        }
        return false;
    }
    private void checkCaptureAndVictory(Position newPosition) {
        if (isCorner(newPosition)) { // The King
            declareVictoryAndPrintStats(true);
            return;
        }

        int x = newPosition.getX(), y = newPosition.getY();
        if (board[x][y] instanceof Pawn) {
            int xLeft1 = x - 1, xLeft2 = x - 2;
            if (x != 0)
                if (xLeft1 == 0 || // The edge closes it
                        // Another player closes it
                        (board[xLeft2][y] != null && !(board[xLeft2][y] instanceof King) && (board[xLeft2][y].getOwner().isPlayerOne() == board[x][y].getOwner().isPlayerOne()) ||
                                // The corner closes it
                                (isCorner(new Position(xLeft2, y)))))
                    if (board[xLeft1][y] != null) // There is someone to capture
                        // They are not from the same team
                        if (board[xLeft1][y].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne())
                            if (board[xLeft1][y] instanceof Pawn) {
                                capturedPieces.add(board[xLeft1][y]);
                                board[xLeft1][y] = null; // Capture!
                                ((Pawn) board[x][y]).kill();
                            }
                            else {
                                if(isKingSurrounded(xLeft1,y)) {
                                    declareVictoryAndPrintStats(false);
                                    return;
                                }
                            }

            int xRight1 = x + 1, xRight2 = x + 2;

            if (xRight1 == board.length - 1 ||
                    (xRight2 < board.length && board[xRight2][y] != null && !(board[xRight2][y] instanceof King) && board[xRight2][y].getOwner().isPlayerOne() == board[x][y].getOwner().isPlayerOne()) ||
                    isCorner(new Position(xRight2, y))) {

                if (xRight1 < board.length && board[xRight1][y] != null &&
                        board[xRight1][y].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
                    if (board[xRight1][y] instanceof Pawn) {
                        capturedPieces.add(board[xRight1][y]);
                        board[xRight1][y] = null; // Capture!
                        ((Pawn) board[x][y]).kill();
                    }
                    else {
                        if (isKingSurrounded(xRight1,y)) {
                            declareVictoryAndPrintStats(false);
                            return;
                        }
                    }
                }
            }


            int yUp1 = y - 1, yUp2 = y - 2;
            if (y != 0)
                if (yUp1 == 0 ||
                        (board[x][yUp2] != null && !(board[x][yUp2] instanceof King) && board[x][yUp2].getOwner().isPlayerOne() == board[x][y].getOwner().isPlayerOne()) ||
                        isCorner(new Position(x, yUp2))) {

                    if (board[x][yUp1] != null &&
                            board[x][yUp1].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
                        if (board[x][yUp1] instanceof Pawn) {
                            capturedPieces.add(board[x][yUp1]);
                            board[x][yUp1] = null; // Capture!
                            ((Pawn) board[x][y]).kill();
                        }
                        else {
                            if (isKingSurrounded(x, yUp1)) {
                                declareVictoryAndPrintStats(false);
                                return;
                            }
                        }
                    }
                }

            int yDown1 = y + 1, yDown2 = y + 2;

            if (yDown1 == board[0].length - 1 ||
                    (yDown2 < board[0].length && board[x][yDown2] != null && !(board[x][yDown2] instanceof King) && board[x][yDown2].getOwner().isPlayerOne() == board[x][y].getOwner().isPlayerOne()) ||
                    isCorner(new Position(x, yDown2))) {

                if (yDown1 < board[0].length && board[x][yDown1] != null &&
                        board[x][yDown1].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
                    if (board[x][yDown1] instanceof Pawn) {
                        capturedPieces.add(board[x][yDown1]);
                        board[x][yDown1] = null; // Capture!
                        ((Pawn) board[x][y]).kill();
                    }
                    else {
                        if (isKingSurrounded(x, yDown1)) {
                            declareVictoryAndPrintStats(false);
                        }
                    }
                }
            }
        }
    }
    private void declareVictoryAndPrintStats(boolean isDefenderWon) {
        gameFinished = true;
        ConcretePlayer winner = isDefenderWon ? defender : attacker;
        winner.win();


        // First part
        pieces.sort((piece1, piece2) -> {
            int defenderComparison = Boolean.compare(piece1.getOwner().isPlayerOne(), piece2.getOwner().isPlayerOne());

            if (defenderComparison != 0) {
                return isDefenderWon ? -defenderComparison : defenderComparison;
            }

            int stepsComparison = Integer.compare(piece1.getPositionHistory().size(), piece2.getPositionHistory().size());

            if (stepsComparison != 0) {
                return stepsComparison;
            }

            return Integer.compare(piece1.getNumber(), piece2.getNumber());
        });

        printStats(pieces, (o)->o.getPositionHistory().size() > 1, ConcretePiece::getPositionHistory);


        // Second Part
        // Remove the king in order to get only pawn who can kill
        List<Pawn> pawns = pieces.stream()
                .filter(piece -> piece instanceof Pawn)
                .map(piece -> (Pawn) piece)
                .sorted((piece1, piece2) -> {
                    int killsC = Integer.compare(piece2.getKills(), piece1.getKills());
                    return compareByOrder(isDefenderWon, piece1, piece2, killsC);

                })
                .collect(Collectors.toList());

        printStats(pawns, (o) -> o.getKills() > 0, Pawn::getKillsStr);


        // Third part
        pieces.sort((piece1, piece2) -> {
            int ditComp = Integer.compare(piece2.getDist(), piece1.getDist());
            return compareByOrder(isDefenderWon, piece1, piece2, ditComp);

        });
        printStats(pieces, (o) -> o.getDist() > 0, ConcretePiece::getDistStr);


        // Forth part
        List<Position> positionList = Stream.of(positions)
                .flatMap(Stream::of) // Flatten the 2D array into a single stream
                .filter(Objects::nonNull) // Filter out null positions
                .sorted((position1, position2) -> {
                    int ditComp = Integer.compare(position2.getPieceSize(), position1.getPieceSize());
                    if (ditComp != 0)
                        return ditComp;

                    int xComp = Integer.compare(position1.getX(), position2.getX());
                    if (xComp != 0)
                        return xComp;

                    return Integer.compare(position1.getY(), position2.getY());
                })
                .toList();
        printStats(positionList, (position -> position.getPieceSize() > 1), Position::piecesStr);

        pieces.clear();
        positions = new Position[BOARD_SIZE][BOARD_SIZE];
    }
    private int compareByOrder(boolean isDefenderWon, ConcretePiece piece1, ConcretePiece piece2, int comp) {
        if (comp != 0)
            return comp;

        int numCom = Integer.compare(piece1.getNumber(), piece2.getNumber());
        if (numCom != 0)
            return numCom;

        int defenderComparison = Boolean.compare(piece1.getOwner().isPlayerOne(), piece2.getOwner().isPlayerOne());
        return isDefenderWon ? -defenderComparison : defenderComparison;
    }
    private <T, R> void printStats(Collection<T> collection, Function<T, Boolean> condition, Function<T, R> function) {
        for (T p: collection) {
            if (condition.apply(p)) {
                System.out.print(p);
                System.out.println(function.apply(p).toString());
            }
        }
        for (int i = 0; i < 75; i++) {
            System.out.print("*");
        }
        System.out.println();
    }
    private boolean isKingSurrounded(int x, int y) {
        int enemyCount = 0;

        // Check left
        if (x > 0 && board[x - 1][y] != null && board[x - 1][y].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
            enemyCount++;
        }

        // Check right
        if (x < board.length - 1 && board[x + 1][y] != null && board[x + 1][y].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
            enemyCount++;
        }

        // Check up
        if (y > 0 && board[x][y - 1] != null && board[x][y - 1].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
            enemyCount++;
        }

        // Check down
        if (y < board[0].length - 1 && board[x][y + 1] != null && board[x][y + 1].getOwner().isPlayerOne() != board[x][y].getOwner().isPlayerOne()) {
            enemyCount++;
        }

        // Check if the king is on the border
        if (x == 0 || x == board.length - 1 || y == 0 || y == board[0].length - 1) {
            return enemyCount >= 3;
        } else {
            return enemyCount == 4;
        }
    }
    private boolean isCorner(Position newPosition) {
        return (newPosition.getX() % 10 == 0 && newPosition.getY() % 10 == 0);
    }
    @Override
    public Piece getPieceAtPosition(Position position) {
        return board[position.getX()][position.getY()];
    }
    @Override
    public Player getSecondPlayer() {
        return attacker;
    }
    @Override
    public Player getFirstPlayer() {
        return defender;
    }
    @Override
    public boolean isGameFinished() {
        return gameFinished;
    }
    @Override
    public boolean isSecondPlayerTurn() {
        return this.attackerTurn;
    }
    @Override
    public void reset() {
        board = new ConcretePiece[BOARD_SIZE][BOARD_SIZE];
        gameFinished = false;
        attackerTurn = true;
        moveHistory = new Stack<>();

        // Starting Position attacker
        for (int i = 3; i <= 7; i++) {
            board[0][i] = new Pawn(attacker);
        }
        board[1][5] = new Pawn(attacker);
        for (int i = 3; i <= 7; i++) {
            board[i][0] = new Pawn(attacker);
        }
        board[5][1] = new Pawn(attacker);
        for (int i = 3; i <= 7; i++) {
            board[10][i] = new Pawn(attacker);
        }
        board[9][5] = new Pawn(attacker);
        for (int i = 3; i <= 7; i++) {
            board[i][10] = new Pawn(attacker);
        }
        board[5][9] = new Pawn(attacker);

        // Defender
        for (int i = 4; i <= 6; i++) {
            for (int j = 4; j <= 6 ; j++) {
                if (j != 5 || i != 5) {
                    board[i][j] = new Pawn(defender);
                } else
                    board[i][j] = new King(defender);
            }
        }
        board[5][3] = new Pawn(defender);
        board[3][5] = new Pawn(defender);
        board[5][7] = new Pawn(defender);
        board[7][5] = new Pawn(defender);

        int defNumbers = 1, atcNumber = 1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[j][i] != null) {
                    ConcretePiece concretePiece = board[j][i];
                    int numberToSet = concretePiece.getOwner().isPlayerOne() ? defNumbers++ : atcNumber++;

                    if (concretePiece instanceof Pawn)
                        concretePiece.setNumber(numberToSet);
                    else
                        concretePiece.setNumber(numberToSet);

                    concretePiece.setNewPosition(new Position(j, i));
                    positions[j][i] = new Position(j, i);
                    positions[j][i].raisePiece(concretePiece);
                }
            }
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (ConcretePiece p: board[i]) {
                if (p!=null)
                    pieces.add(p);
            }
        }

    }
    @Override
    public void undoLastMove() {
        //System.out.print("BACK ");
        if (moveHistory.isEmpty()) {
            return; // Nothing to undo
        }

        Move lastMove = moveHistory.pop();

        // Retrieve the data
        Position to = lastMove.to();
        List<ConcretePiece> capturedPieces = lastMove.capturedPieces;
        ConcretePiece movedPiece = board[to.getX()][to.getY()];

        // Update the piece and square history
        positions[to.getX()][to.getY()].erasePiece(movedPiece);
        movedPiece.deleteLastStep();
        if (movedPiece instanceof Pawn) {
            for (ConcretePiece p : capturedPieces) {
                ((Pawn) movedPiece).reduceKill();
            }
        }

        // Undo the move
        Position prevStep = movedPiece.getLastPosition();
        board[prevStep.getX()][prevStep.getY()] = movedPiece;
        board[to.getX()][to.getY()] = null;

        // Bring back the deaths
        for (ConcretePiece p : capturedPieces) {
            Position prevCapturedStep = p.getLastPosition();
            int x = prevCapturedStep.getX(), y = prevCapturedStep.getY();
            board[x][y] = p;
        }

        // Switch turn
        this.attackerTurn = !attackerTurn;
    }
    @Override
    public int getBoardSize() {
        return BOARD_SIZE;
    }

}

package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final Piece[][] squares;

    public Board() {
        squares = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
    }

    public Piece pieceAt(Position position) {
        return squares[position.file()][position.rank()];
    }

    public boolean isEmpty(Position position) {
        return pieceAt(position) == null;
    }

    public void place(Position position, Piece piece) {
        squares[position.file()][position.rank()] = piece;
    }

    public List<Position> positionsOf(Color color) {
        List<Position> positions = new ArrayList<>();

        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                Piece piece = squares[file][rank];

                if (piece != null && piece.color() == color) {
                    positions.add(new Position(file, rank));
                }
            }
        }

        return positions;
    }

    /**
     * The square holding {@code color}'s king, or {@code null} if that side
     * has no king on the board.
     *
     * <p>A real game always has both kings, but tests build partial positions
     * without them all the time, so {@code null} is a normal answer here, not
     * an error.
     */
    public Position kingPosition(Color color) {
        for (Position occupiedSquare : positionsOf(color)) {
            // Look at every square that holds one of this colour's pieces.
            Piece pieceFound = pieceAt(occupiedSquare);
            // Get the piece standing on that square.
            if (pieceFound.type() == PieceType.KING) {
                // If that piece is a king, we found it.
                return occupiedSquare;
                // Hand back the square the king is on.
            }
        }
        return null;
        // We looked at every piece and none was a king.
    }

    /**
     * Lifts the moving piece off {@code from} and sets it down on {@code to}.
     * Checks nothing: deciding whether a move is allowed is {@code Game}'s job.
     * On a promotion, the piece set down is a new piece of the promoted type.
     */
    public void apply(Move move) {
        Piece arriving = move.moved();
        if (move.isPromotion()) {
            arriving = createPromoted(move.promotesTo(), move.moved().color());
        }
        place(move.from(), null);
        place(move.to(), arriving);
    }

    /**
     * Takes a move back: the moving piece returns to {@code from}, and whatever
     * it captured (null for a quiet move) returns to {@code to}.
     */
    public void undo(Move move) {
        place(move.from(), move.moved());
        place(move.to(), move.captured());
    }

    /**
     * Builds the piece a pawn promotes into.
     *
     * <p>This is a second, private switch on PieceType; the first is in
     * PieceFactory. Calling PieceFactory.create here would make model depend on
     * factory, an arrow pointing the wrong way, so we accept duplicating four
     * lines instead. The cost: adding a new promotable piece means editing this
     * switch too, and the compiler will not remind us, because of the default.
     */
    private static Piece createPromoted(PieceType type, Color color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> throw new IllegalArgumentException("Cannot promote to " + type);
        };
    }

    @Override
    public String toString() {
        StringBuilder fen = new StringBuilder();

        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) {
            int emptyCount = 0;

            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                Piece piece = squares[file][rank];

                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }

                    fen.append(piece.symbol());
                }
            }

            if (emptyCount > 0) {
                fen.append(emptyCount);
            }

            if (rank > 0) {
                fen.append('/');
            }
        }

        return fen.toString();
    }
}

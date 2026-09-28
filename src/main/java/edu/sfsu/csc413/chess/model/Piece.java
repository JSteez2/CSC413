package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public Color color() {
        return color;
    }

    public PieceType type() {
        return type;
    }

    public char symbol() {
        if (color == Color.WHITE) {
            return type.symbol();
        } else {
            return Character.toLowerCase(type.symbol());
        }
    }

    /** Every move this piece could make from {@code from}, ignoring check. */
    public abstract List<Move> pseudoLegalMoves(Board board, Position from);

    /** True if this piece could capture an enemy standing on {@code target}. */
    public boolean attacks(Board board, Position from, Position target) {
        for (Move move : pseudoLegalMoves(board, from)) {
            if (move.to().equals(target)) {
                return true;
            }
        }
        return false;
    }

    /** Slides outward along each {file, rank} direction until blocked. */
    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {
        List<Move> moves = new ArrayList<>();
        for (int[] direction : directions) {
            Position to = from.offsetOrNull(direction[0], direction[1]);
            while (to != null) {
                Piece occupant = board.pieceAt(to);
                if (occupant == null) {
                    moves.add(Move.quiet(from, to, this));
                } else {
                    if (occupant.color() != color) {
                        moves.add(Move.capture(from, to, this, occupant));
                    }
                    break;   // stop after a capture, or before a friend
                }
                to = to.offsetOrNull(direction[0], direction[1]);
            }
        }
        return moves;
    }

    /** Steps to each {file, rank} offset that is on the board and not a friend. */
    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets) {
        List<Move> moves = new ArrayList<>();
        for (int[] offset : offsets) {
            Position to = from.offsetOrNull(offset[0], offset[1]);
            if (to == null) {
                continue;   // off the board
            }
            Piece occupant = board.pieceAt(to);
            if (occupant == null) {
                moves.add(Move.quiet(from, to, this));
            } else if (occupant.color() != color) {
                moves.add(Move.capture(from, to, this, occupant));
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}

package edu.sfsu.csc413.chess.model;

public record Position(int file, int rank) {

    public static final int BOARD_SIZE = 8;

    public Position {

        if (!isOnBoard(file, rank)) {

            throw new IllegalArgumentException(

                    "Position off board: file=" + file + ", rank=" + rank);

        }

    }

    public static boolean isOnBoard(int file, int rank) {

        return file >= 0 && file < BOARD_SIZE

                && rank >= 0 && rank < BOARD_SIZE;

    }

    public static Position parse(String square) {

        if (square == null || square.length() != 2) {

            throw new IllegalArgumentException("Invalid square: " + square);

        }

        int file = square.charAt(0) - 'a';

        int rank = square.charAt(1) - '1';

        return new Position(file, rank);

    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {

        int newFile = file + fileDelta;

        int newRank = rank + rankDelta;

        if (!isOnBoard(newFile, newRank)) {

            return null;

        }

        return new Position(newFile, newRank);

    }

    @Override

    public String toString() {

        return "" + (char) ('a' + file) + (char) ('1' + rank);

    }

}


package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A window for playing chess by clicking: click one of your pieces, then the
 * square to move it to. While a piece is selected, its square is yellow and
 * every square it can legally move to has a dot.
 *
 * <p>This class is the board panel and the view at once. It draws the board in
 * {@link #paintComponent} and turns a click's pixels into a square in
 * {@link #boardClicked}. It only reads the game: the dots come from
 * {@code game.legalMoves()}, and every move and every Undo goes to the
 * {@link GameController} as a {@link PlayerAction}.
 *
 * <p>{@link #start} shows the window and returns.
 */
public class SwingView extends JPanel implements BoardView {

    private static final int SQUARE_SIZE = 72;
    private static final Color LIGHT_SQUARE_COLOR = new Color(240, 217, 181);
    private static final Color DARK_SQUARE_COLOR = new Color(181, 136, 99);
    private static final Color SELECTED_SQUARE_COLOR = new Color(200, 162, 200);
    private static final Color LEGAL_MOVE_DOT_COLOR = new Color(40, 90, 40, 160);

    private final JFrame window = new JFrame("Chess");
    private final JLabel turnLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton undoButton = new JButton("Undo");

    private GameController controller;
    private Game game;
    private Position selectedSquare;
    private Position moveFromSquare;
    private Position moveToSquare;
    private boolean gameIsFinished;

    /** Builds the window. It is not shown until {@link #start}. */
    public SwingView() {
        setPreferredSize(new Dimension(8 * SQUARE_SIZE, 8 * SQUARE_SIZE));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                boardClicked(event.getX(), event.getY());
            }
        });

        undoButton.addActionListener(event -> controller.handle(new PlayerAction.Undo()));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageLabel, BorderLayout.CENTER);
        bottomPanel.add(undoButton, BorderLayout.EAST);

        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 16f));

        window.setLayout(new BorderLayout());
        window.add(turnLabel, BorderLayout.NORTH);
        window.add(this, BorderLayout.CENTER);
        window.add(bottomPanel, BorderLayout.SOUTH);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
    }

    @Override
    public void render(Game game) {
        this.game = game;
        SwingUtilities.invokeLater(this::updateScreen);
    }

    @Override
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message));
    }

    /** Shows the window and returns. From here on, clicks drive the game. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        this.game = controller.game();
        SwingUtilities.invokeLater(() -> {
            updateScreen();
            window.setVisible(true);
        });
    }

    /**
     * Asks which piece a pawn becomes, but only when the move just tried really
     * is a promotion. The controller also asks this for any four-letter move it
     * can't find, such as an illegal {@code e2e5}, and a dialog then would make
     * no sense. (Idea taken from m7-run-1.)
     */
    @Override
    public PieceType promotionChoice(Game game) {
        boolean moveIsPromotion = false;
        for (Move legalMove : game.legalMoves()) {
            if (legalMove.isPromotion() && legalMove.from().equals(moveFromSquare)
                    && legalMove.to().equals(moveToSquare)) {
                moveIsPromotion = true;
            }
        }
        if (!moveIsPromotion) {
            return PieceType.QUEEN;
        }

        PieceType[] pieceChoices = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        String[] buttonNames = {"Queen", "Rook", "Bishop", "Knight"};
        int pickedIndex = JOptionPane.showOptionDialog(window, "Promote the pawn to:", "Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttonNames, buttonNames[0]);
        if (pickedIndex < 0) {
            return PieceType.QUEEN;
        }
        return pieceChoices[pickedIndex];
    }

    /** The game is over or the player quit: stop taking clicks, keep the board on screen. */
    @Override
    public void close() {
        gameIsFinished = true;
        selectedSquare = null;
        SwingUtilities.invokeLater(() -> undoButton.setEnabled(false));
    }

    /** Called by Swing when the mouse is pressed on the board. */
    private void boardClicked(int x, int y) {
        if (gameIsFinished || game == null) {
            return;
        }
        int file = x / SQUARE_SIZE;
        int rank = 7 - y / SQUARE_SIZE;
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            return;
        }
        Position clickedSquare = new Position(file, rank);
        Piece clickedPiece = game.board().pieceAt(clickedSquare);
        boolean clickedOwnPiece = clickedPiece != null && clickedPiece.color() == game.sideToMove();

        if (selectedSquare == null) {
            if (clickedOwnPiece) {
                selectedSquare = clickedSquare;
                messageLabel.setText(" ");
            } else {
                messageLabel.setText("Click one of your own pieces first.");
            }
        } else if (clickedSquare.equals(selectedSquare)) {
            selectedSquare = null;
        } else if (clickedOwnPiece) {
            selectedSquare = clickedSquare;
        } else {
            moveFromSquare = selectedSquare;
            moveToSquare = clickedSquare;
            String moveNotation = moveFromSquare.toString() + moveToSquare.toString();
            selectedSquare = null;
            messageLabel.setText(" ");
            controller.handle(new PlayerAction.Move(moveNotation));
        }
        repaint();
    }

    /** The squares the selected piece can move to, read from the game's legal moves. */
    private List<Position> squaresSelectedPieceCanReach() {
        List<Position> reachableSquares = new ArrayList<>();
        if (selectedSquare == null || game == null) {
            return reachableSquares;
        }
        for (Move legalMove : game.legalMoves()) {
            if (legalMove.from().equals(selectedSquare) && !reachableSquares.contains(legalMove.to())) {
                reachableSquares.add(legalMove.to());
            }
        }
        return reachableSquares;
    }

    private void updateScreen() {
        String turnText = game.sideToMove() + " to move";
        if (game.status() == GameStatus.CHECK) {
            turnText = turnText + "  |  CHECK";
        } else if (game.status() == GameStatus.CHECKMATE) {
            turnText = turnText + "  |  CHECKMATE";
        } else if (game.status() == GameStatus.STALEMATE) {
            turnText = turnText + "  |  STALEMATE";
        }
        turnLabel.setText(turnText);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<Position> reachableSquares = squaresSelectedPieceCanReach();

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int squareX = file * SQUARE_SIZE;
                int squareY = (7 - rank) * SQUARE_SIZE;
                Position square = new Position(file, rank);
                boolean isLightSquare = (file + rank) % 2 == 1;

                if (square.equals(selectedSquare)) {
                    g.setColor(SELECTED_SQUARE_COLOR);
                } else if (isLightSquare) {
                    g.setColor(LIGHT_SQUARE_COLOR);
                } else {
                    g.setColor(DARK_SQUARE_COLOR);
                }
                g.fillRect(squareX, squareY, SQUARE_SIZE, SQUARE_SIZE);

                Piece piece = null;
                if (game != null) {
                    piece = game.board().pieceAt(square);
                }
                if (piece != null) {
                    drawPiece(g, piece, squareX, squareY);
                }
                if (reachableSquares.contains(square)) {
                    drawLegalMoveDot(g, squareX, squareY);
                }
            }
        }
    }

    /** A small dot in the middle of a square the selected piece can move to. */
    private void drawLegalMoveDot(Graphics2D g, int squareX, int squareY) {
        int dotSize = SQUARE_SIZE / 4;
        int dotX = squareX + (SQUARE_SIZE - dotSize) / 2;
        int dotY = squareY + (SQUARE_SIZE - dotSize) / 2;
        g.setColor(LEGAL_MOVE_DOT_COLOR);
        g.fillOval(dotX, dotY, dotSize, dotSize);
    }

    /** Draws a piece as a disc with its letter: a white disc for white, a black disc for black. */
    private void drawPiece(Graphics2D g, Piece piece, int squareX, int squareY) {
        boolean isWhitePiece = piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE;
        int margin = SQUARE_SIZE / 8;
        int diameter = SQUARE_SIZE - 2 * margin;

        if (isWhitePiece) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillOval(squareX + margin, squareY + margin, diameter, diameter);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(squareX + margin, squareY + margin, diameter, diameter);

        String letter = String.valueOf(piece.type().symbol());
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, SQUARE_SIZE / 2));
        FontMetrics metrics = g.getFontMetrics();
        int letterX = squareX + (SQUARE_SIZE - metrics.stringWidth(letter)) / 2;
        int letterY = squareY + (SQUARE_SIZE - metrics.getHeight()) / 2 + metrics.getAscent();
        if (isWhitePiece) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawString(letter, letterX, letterY);
    }
}

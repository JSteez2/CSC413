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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A window with an 8x8 board. Click one of your pieces, then the square to
 * move it to.
 *
 * <p>The board is a grid of 64 {@link SquarePanel}s. Each one knows its own
 * {@link Position}, so a click never has to be turned from pixels into a
 * square. The view only reads the game; every move and every Undo goes to the
 * {@link GameController} as a {@link PlayerAction}.
 *
 * <p>{@link #start} shows the window and returns. Swing calls
 * {@link #squareClicked} when a square is pressed.
 */
public class SwingView implements BoardView {

    private static final int SQUARE_SIZE = 72;
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_SQUARE = new Color(246, 246, 105);

    private final JFrame frame = new JFrame("Chess");
    private final SquarePanel[][] squares = new SquarePanel[8][8];
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton undoButton = new JButton("Undo");

    private GameController controller;
    private Game game;
    private Position selected;
    private Position triedFrom;
    private Position triedTo;
    private boolean closed;

    /** Builds the window. It is not shown until {@link #start}. */
    public SwingView() {
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int row = 0; row < 8; row++) {
            int rank = 7 - row;
            for (int file = 0; file < 8; file++) {
                SquarePanel square = new SquarePanel(new Position(file, rank));
                squares[file][rank] = square;
                boardPanel.add(square);
            }
        }

        JPanel rankLabels = new JPanel(new GridLayout(8, 1));
        for (int rank = 8; rank >= 1; rank--) {
            JLabel label = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(24, SQUARE_SIZE));
            rankLabels.add(label);
        }

        JPanel fileLabels = new JPanel(new GridLayout(1, 8));
        for (char file = 'a'; file <= 'h'; file++) {
            fileLabels.add(new JLabel(String.valueOf(file), SwingConstants.CENTER));
        }
        JPanel bottomOfBoard = new JPanel(new BorderLayout());
        JPanel corner = new JPanel();
        corner.setPreferredSize(new Dimension(24, 20));
        bottomOfBoard.add(corner, BorderLayout.WEST);
        bottomOfBoard.add(fileLabels, BorderLayout.CENTER);

        JPanel boardWithCoordinates = new JPanel(new BorderLayout());
        boardWithCoordinates.add(rankLabels, BorderLayout.WEST);
        boardWithCoordinates.add(boardPanel, BorderLayout.CENTER);
        boardWithCoordinates.add(bottomOfBoard, BorderLayout.SOUTH);

        undoButton.addActionListener(event -> controller.handle(new PlayerAction.Undo()));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageLabel, BorderLayout.CENTER);
        bottomPanel.add(undoButton, BorderLayout.EAST);

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));

        frame.setLayout(new BorderLayout());
        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(boardWithCoordinates, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void render(Game game) {
        this.game = game;
        SwingUtilities.invokeLater(this::refresh);
    }

    @Override
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message));
    }

    /** Shows the window and returns. The game then runs on Swing's clicks. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        this.game = controller.game();
        SwingUtilities.invokeLater(() -> {
            refresh();
            frame.setVisible(true);
        });
    }

    /**
     * Asks which piece a pawn becomes, but only if the move just tried really
     * is a promotion. The controller also asks this for any four-letter move
     * it can't find, such as an illegal {@code e2e5}, and a dialog then would
     * make no sense.
     */
    @Override
    public PieceType promotionChoice(Game game) {
        boolean isPromotion = false;
        for (Move move : game.legalMoves()) {
            if (move.isPromotion() && move.from().equals(triedFrom) && move.to().equals(triedTo)) {
                isPromotion = true;
            }
        }
        if (!isPromotion) {
            return PieceType.QUEEN;
        }

        PieceType[] pieces = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        String[] names = {"Queen", "Rook", "Bishop", "Knight"};
        int picked = JOptionPane.showOptionDialog(frame, "Promote the pawn to:", "Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
        if (picked < 0) {
            return PieceType.QUEEN;
        }
        return pieces[picked];
    }

    /** The game is over or the player quit: stop taking clicks, keep the board on screen. */
    @Override
    public void close() {
        closed = true;
        SwingUtilities.invokeLater(() -> undoButton.setEnabled(false));
    }

    /** Called by Swing when a square is pressed. */
    private void squareClicked(Position clicked) {
        if (closed || game == null) {
            return;
        }
        Piece piece = game.board().pieceAt(clicked);
        boolean ownPiece = piece != null && piece.color() == game.sideToMove();

        if (selected == null) {
            if (ownPiece) {
                selected = clicked;
                messageLabel.setText(" ");
            } else {
                messageLabel.setText("Click one of your own pieces first.");
            }
        } else if (clicked.equals(selected)) {
            selected = null;
        } else if (ownPiece) {
            selected = clicked;
        } else {
            triedFrom = selected;
            triedTo = clicked;
            selected = null;
            messageLabel.setText(" ");
            controller.handle(new PlayerAction.Move(triedFrom.toString() + triedTo.toString()));
        }
        frame.repaint();
    }

    private void refresh() {
        String status = game.sideToMove() + " to move";
        if (game.status() != GameStatus.IN_PROGRESS) {
            status = status + "  |  " + game.status();
        }
        statusLabel.setText(status);
        frame.repaint();
    }

    /** Draws a piece as a disc with its letter: white disc for white, black for black. */
    private static void drawPiece(Graphics2D g, Piece piece, int size) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean white = piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE;
        int margin = size / 8;
        int diameter = size - 2 * margin;

        g.setColor(white ? Color.WHITE : Color.BLACK);
        g.fillOval(margin, margin, diameter, diameter);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(margin, margin, diameter, diameter);

        String letter = String.valueOf(piece.type().symbol());
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 2));
        FontMetrics metrics = g.getFontMetrics();
        int x = (size - metrics.stringWidth(letter)) / 2;
        int y = (size - metrics.getHeight()) / 2 + metrics.getAscent();
        g.setColor(white ? Color.BLACK : Color.WHITE);
        g.drawString(letter, x, y);
    }

    /** One square of the board. It knows its own position, so clicks need no arithmetic. */
    private class SquarePanel extends JPanel {
        private final Position position;

        SquarePanel(Position position) {
            this.position = position;
            setPreferredSize(new Dimension(SQUARE_SIZE, SQUARE_SIZE));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    squareClicked(SquarePanel.this.position);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            boolean lightSquare = (position.file() + position.rank()) % 2 == 1;
            Color background = lightSquare ? LIGHT_SQUARE : DARK_SQUARE;
            if (position.equals(selected)) {
                background = SELECTED_SQUARE;
            }
            g.setColor(background);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (game != null) {
                Piece piece = game.board().pieceAt(position);
                if (piece != null) {
                    drawPiece((Graphics2D) g, piece, getWidth());
                }
            }
        }
    }
}

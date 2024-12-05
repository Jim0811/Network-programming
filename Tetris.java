import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public final class Tetris extends JPanel implements ActionListener, KeyListener {
    final int BOARD_WIDTH = 20;
    final int BOARD_HEIGHT = 20;
    final int TILE_SIZE = 30;
    Timer timer;
    boolean[][] board;
    int[][] currentPiece;
    int pieceRow, pieceCol;
    Random random;

    public Tetris() {
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        board = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
        random = new Random();
        spawnNewPiece();
        timer = new Timer(250, this);
        timer.start();
    }

    void spawnNewPiece() {
        pieceRow = 0;
        pieceCol = BOARD_WIDTH / 2 - 1;
        currentPiece = getRandomPiece();
    }

    int[][] getRandomPiece() {
        int[][][] pieces = {
                { { 1, 1, 1, 1, 1 } },

                { { 1, 1, 1 }, { 1, 0, 1 } },
                { { 1, 1, 1 }, { 1, 1, 0 } },
                { { 1, 1, 1 }, { 0, 1, 1 } },

                { { 1, 1, 1, 1 }, { 1, 0, 0, 0 } },
                { { 1, 1, 1, 1 }, { 0, 0, 0, 1 } },
                { { 1, 1, 1, 0 }, { 0, 0, 1, 1 } },
                { { 0, 1, 1, 1 }, { 1, 1, 0, 0 } },

                { { 1, 1, 1 }, { 0, 0, 1 }, { 0, 0, 1 } },
                { { 1, 1, 1 }, { 0, 1, 0 }, { 0, 1, 0 } },
                { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } },

                { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 0, 1 } },
                { { 0, 1, 0 }, { 1, 1, 1 }, { 1, 0, 0 } },

                { { 0, 0, 1 }, { 1, 1, 1 }, { 1, 0, 0 } },
                { { 1, 0, 0 }, { 1, 1, 1 }, { 0, 0, 1 } },

                { { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 1 } },

        };
        return pieces[random.nextInt(pieces.length)];
    }

    void rotatePiece() {
        int rows = currentPiece.length;
        int cols = currentPiece[0].length;
        int[][] rotated = new int[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = currentPiece[r][c];
            }
        }
        if (!collides(rotated, pieceRow, pieceCol)) {
            currentPiece = rotated;
        }
    }

    void hardDrop() {
        while (!collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
        }
        placePiece();
    }

    boolean collides(int[][] piece, int newRow, int newCol) {
        for (int r = 0; r < piece.length; r++) {
            for (int c = 0; c < piece[r].length; c++) {
                if (piece[r][c] == 1) {
                    int boardRow = newRow + r;
                    int boardCol = newCol + c;
                    if (boardRow < 0 || boardRow >= BOARD_HEIGHT || boardCol < 0 || boardCol >= BOARD_WIDTH
                            || board[boardRow][boardCol]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void placePiece() {
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
                if (currentPiece[r][c] == 1) {
                    board[pieceRow + r][pieceCol + c] = true;
                }
            }
        }
        clearRows();
        spawnNewPiece();
    }

    void clearRows() {
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            boolean fullRow = true;
            for (int c = 0; c < BOARD_WIDTH; c++) {
                if (!board[r][c]) {
                    fullRow = false;
                    break;
                }
            }
            if (fullRow) {
                for (int row = r; row > 0; row--) {
                    board[row] = board[row - 1];
                }
                board[0] = new boolean[BOARD_WIDTH];
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
        } else {
            placePiece();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the current piece
        g.setColor(Color.RED);
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
                if (currentPiece[r][c] == 1) {
                    g.fillRect((pieceCol + c) * TILE_SIZE, (pieceRow + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                if (!collides(currentPiece, pieceRow, pieceCol - 1)) {
                    pieceCol--;
                }
            }
            case KeyEvent.VK_RIGHT -> {
                if (!collides(currentPiece, pieceRow, pieceCol + 1)) {
                    pieceCol++;
                }
            }
            case KeyEvent.VK_DOWN -> {
                if (!collides(currentPiece, pieceRow + 1, pieceCol)) {
                    hardDrop();
                }
            }
            case KeyEvent.VK_UP -> rotatePiece();
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        Tetris game = new Tetris();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

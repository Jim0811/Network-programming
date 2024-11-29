import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Tetris extends JPanel implements ActionListener, KeyListener {
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int TILE_SIZE = 30;
    private Timer timer;
    private boolean[][] board;
    private int[][] currentPiece;
    private int pieceRow, pieceCol;
    private Random random;

    public Tetris() {
        setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        board = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
        random = new Random();
        spawnNewPiece();
        timer = new Timer(500, this);
        timer.start();
    }

    private void spawnNewPiece() {
        pieceRow = 0;
        pieceCol = BOARD_WIDTH / 2 - 1;
        currentPiece = getRandomPiece();
    }

    private int[][] getRandomPiece() {
        int[][][] pieces = {
                { { 1, 1, 1, 1 } }, // Line
                { { 1, 1 }, { 1, 1 } }, // Square
                { { 0, 1, 0 }, { 1, 1, 1 } }, // T
                { { 1, 1, 0 }, { 0, 1, 1 } }, // Z
                { { 0, 1, 1 }, { 1, 1, 0 } }, // S
        };
        return pieces[random.nextInt(pieces.length)];
    }

    private void rotatePiece() {
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

    private boolean collides(int[][] piece, int newRow, int newCol) {
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

    private void placePiece() {
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

    private void clearRows() {
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
        // Draw the board
        g.setColor(Color.GRAY);
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
                if (board[r][c]) {
                    g.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw the current piece
        g.setColor(Color.RED);
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
                if (currentPiece[r][c] == 1) {
                    g.fillRect((pieceCol + c) * TILE_SIZE, (pieceRow + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw grid lines
        g.setColor(Color.DARK_GRAY);
        for (int r = 0; r <= BOARD_HEIGHT; r++) {
            g.drawLine(0, r * TILE_SIZE, BOARD_WIDTH * TILE_SIZE, r * TILE_SIZE);
        }
        for (int c = 0; c <= BOARD_WIDTH; c++) {
            g.drawLine(c * TILE_SIZE, 0, c * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (!collides(currentPiece, pieceRow, pieceCol - 1)) {
                    pieceCol--;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (!collides(currentPiece, pieceRow, pieceCol + 1)) {
                    pieceCol++;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!collides(currentPiece, pieceRow + 1, pieceCol)) {
                    pieceRow++;
                }
                break;
            case KeyEvent.VK_UP:
                rotatePiece();
                break;
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import javax.swing.*;

public class client {
   int BOARD_WIDTH = 20;
   int BOARD_HEIGHT = 20;
   static int TILE_SIZE = 30;
   Timer timer;
   PlayerBoard player1;
   PlayerBoard player2;
   boolean[][] board;
   boolean isRunning; // Game state variable

   //
   static JLabel[] L;
   static ImageIcon I;

   //

   JPanel gamePanel;
   Socket socket;
   DataOutputStream outstream;
   DataInputStream instream;

   @SuppressWarnings("CallToPrintStackTrace")
   public client(String servername, int port) {
      isRunning = false; // Game starts paused

      gamePanel = new JPanel() {
         @Override
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isRunning) {
               g.setColor(Color.gray);
               g.setFont(new Font("Arial", Font.BOLD, 20));
               g.drawString("Press SPACE to Start", BOARD_WIDTH * TILE_SIZE / 4, BOARD_HEIGHT * TILE_SIZE / 2);
            } else {
               player1.draw(g, 0);
               player2.draw(g, 1);

            }

         }
      };

      gamePanel.setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE));
      gamePanel.setBackground(Color.white);
      gamePanel.setFocusable(true);

      gamePanel.addKeyListener(new KeyAdapter() {
         @Override
         @SuppressWarnings({ "UseSpecificCatch", "CallToPrintStackTrace" })
         public void keyPressed(KeyEvent e) {

            try {
               outstream.writeInt(e.getKeyCode());
            } catch (Exception e1) {
               e1.printStackTrace();
            }

         }
      });

      player1 = new PlayerBoard(0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S);
      player2 = new PlayerBoard(1, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN);

      timer = new Timer(250, e -> {
         if (isRunning) {
            player1.update();
            player2.update();
            gamePanel.repaint();
         }
      });
      timer.start();

      try {
         socket = new Socket(InetAddress.getByName(servername), port);
         outstream = new DataOutputStream(socket.getOutputStream());
         instream = new DataInputStream(socket.getInputStream());

         new Thread(() -> {
            while (true) {
               try {
                  int i = instream.readInt();
                  if (i == KeyEvent.VK_R) {
                     player1.resetBoard();
                     player2.resetBoard();

                  } else if (i == KeyEvent.VK_SPACE) {

                     isRunning = !isRunning;

                  } else if (i < pieces.length & i >= 0) {

                     currentp = i;

                  } else if (isRunning) {
                     player1.handleKey(i);
                     player2.handleKey(i);
                  }

                  gamePanel.repaint();
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }).start();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public JPanel getPanel() {
      return gamePanel;
   }

   public static void main(String[] args) {
      JFrame frame = new JFrame();
      client game = new client("localhost", 1234);
      frame.add(game.getPanel());
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);

      L = new JLabel[401];
      for (int i = 0; i <= 400; i++) {
         L[i] = new JLabel();
         L[i].setBounds((i % 20) * TILE_SIZE, i / 20 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
         frame.add(L[i]);
      }

   }

   int currentp;
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

   class PlayerBoard {

      int[][] currentPiece;
      int pieceRow, pieceCol;
      int offsetX;
      Random random;
      int leftKey;
      int rightKey;
      int rotateKey;
      int downKey;
      private Image blueImage;
      private Image violetImage;

      private Image redImage;


      public PlayerBoard(int offsetX, int leftKey, int rightKey, int rotateKey, int downKey) {
         this.offsetX = offsetX;
         this.leftKey = leftKey;
         this.rightKey = rightKey;
         this.rotateKey = rotateKey;
         this.downKey = downKey;
         blueImage = new ImageIcon("blue.png").getImage();
         redImage = new ImageIcon("red.png").getImage();
         violetImage = new ImageIcon("violet.png").getImage();

         board = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
         random = new Random();
         spawnNewPiece(this.offsetX);
      }

      @SuppressWarnings({ "CallToPrintStackTrace", "UseSpecificCatch" })
      final void spawnNewPiece(int offsetX) {

         pieceRow = 0;
         pieceCol = BOARD_WIDTH / 2 - 1;

         int rando = random.nextInt(pieces.length);
         try {
            currentPiece = pieces[currentp];
            outstream.writeInt(rando);

         } catch (Exception e1) {
            e1.printStackTrace();
         }
         
         if(collides(currentPiece, pieceRow + 1, pieceCol)){
            isRunning = false;
            player1.resetBoard();
            player2.resetBoard();
         }
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
         spawnNewPiece(offsetX);
      }

      void hardDrop() {
         while (!collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
         }
         placePiece();
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

      public void resetBoard() {
         for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
               board[r][c] = false;
            }
         }
         spawnNewPiece(offsetX);
      }

      public void update() {
         if (!collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
         } else {
            placePiece();
         }
      }

      public void draw(Graphics g, int color) {
         // 绘制已有的方块（以原样显示为图片）
         for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
               if (board[r][c]) {
                  
                  g.drawImage(violetImage, c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);

               }
            }
         }
   
         // 绘制当前的方块
         for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
               if (currentPiece[r][c] == 1) {
                  g.drawImage((color==0?blueImage:redImage), (pieceCol + c) * TILE_SIZE, (pieceRow + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
               }
            }
         }
      }

      public void handleKey(int keyCode) {
         if (keyCode == leftKey && !collides(currentPiece, pieceRow, pieceCol - 1)) {
            pieceCol--;
         } else if (keyCode == rightKey && !collides(currentPiece, pieceRow, pieceCol + 1)) {
            pieceCol++;
         } else if (keyCode == downKey && !collides(currentPiece, pieceRow + 1, pieceCol)) {
            hardDrop();
         } else if (keyCode == rotateKey) {
            rotatePiece();
         }
      }
   }
}

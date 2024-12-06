import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Vector;
import javax.swing.*;

public class client {
   int BOARD_WIDTH = 20;
   int BOARD_HEIGHT = 20;
   static int TILE_SIZE = 30;
   Timer timer;
   int delay=250;
   PlayerBoard player1;
   PlayerBoard player2;
   boolean[][] board;
   boolean isRunning; // Game state variable
   boolean ismain;
   boolean isGameOver;
   private int score=0;
   //
   static JLabel[] L;
   static ImageIcon I;

   JPanel gamePanel;
   JLabel speedLabel;

   Socket socket;
   DataOutputStream outstream;
   DataInputStream instream;

   @SuppressWarnings("CallToPrintStackTrace")
   public client(String servername, int port,JFrame frame) {
      isRunning = false;
      isGameOver = false;

      speedLabel = new JLabel("Speed: " + (1000 / delay) + " moves/sec");
      speedLabel.setFont(new Font("Arial", Font.PLAIN, 20));
      speedLabel.setForeground(Color.BLACK);
      frame.add(speedLabel, BorderLayout.NORTH); // 将标签放在窗口顶部

      gamePanel = new JPanel() {
         @Override
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isGameOver) {
               g.setColor(Color.red);
               g.setFont(new Font("Arial", Font.BOLD, 40));
       
               String gameOverText = "Game Over!";
               FontMetrics fm = g.getFontMetrics();
               int textWidth = fm.stringWidth(gameOverText);
               int x = (BOARD_WIDTH * TILE_SIZE - textWidth) / 2;
               int y = (BOARD_HEIGHT * TILE_SIZE) / 2 - 30;
               g.drawString(gameOverText, x, y);
       
               String scoreText = "Score: " + score;
               textWidth = fm.stringWidth(scoreText);
               x = (BOARD_WIDTH * TILE_SIZE - textWidth) / 2;
               y = (BOARD_HEIGHT * TILE_SIZE) / 2 + 30;
               g.drawString(scoreText, x, y);

               String resetString = "Press R to play again";
               textWidth = fm.stringWidth(resetString);
               x = (BOARD_WIDTH * TILE_SIZE - textWidth) / 2;
               y = (BOARD_HEIGHT * TILE_SIZE) / 2 + 120;
               g.drawString(resetString, x, y);
            } else if (!isRunning) {
               

               g.setColor(Color.gray);
               g.setFont(new Font("Arial", Font.BOLD, 20));
               player1.draw(g, 0);
               player2.draw(g, 1);
               g.drawString("Press P to Pause/Unpause", BOARD_WIDTH * TILE_SIZE / 4, BOARD_HEIGHT * TILE_SIZE / 2);
            } else {
               player1.draw(g, 0);
               player2.draw(g, 1);
               player1.drawNextPiece(g, 0, 0, 0);
               player2.drawNextPiece(g, BOARD_WIDTH - player2.nextPiece[0].length, 0, 1);

               g.setColor(Color.LIGHT_GRAY);
               g.setFont(new Font("Arial", Font.PLAIN, 30));
       
               String scoreText = "Score: " + score;
               FontMetrics fm = g.getFontMetrics();
               int textWidth = fm.stringWidth(scoreText);
               int x = (BOARD_WIDTH * TILE_SIZE - textWidth) / 2;
               int y = (BOARD_HEIGHT * TILE_SIZE) / 2;
       
               g.drawString(scoreText, x, y);
            }

            player1.drawNextPiece(g, 0, 0, 0); // Adjust (x, y) as needed for position
            player2.drawNextPiece(g, BOARD_WIDTH - player2.nextPiece[0].length, 0, 1); //

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
               int keyCode = e.getKeyCode();
               if (keyCode == KeyEvent.VK_R && isGameOver) {
                     player1.resetBoard();
                     player2.resetBoard();
                     isGameOver = false;
                     isRunning = false;
               } else if (keyCode == KeyEvent.VK_PLUS || keyCode == KeyEvent.VK_EQUALS) { // 增加速度
                     delay = Math.max(50, delay - 50); // 防止速度太快
                     timer.setDelay(delay);
                     speedLabel.setText("Speed: " + (1000 / delay) + " moves/sec");
               } else if (keyCode == KeyEvent.VK_MINUS) { // 减少速度
                     delay = Math.min(1000, delay + 50); // 防止速度太慢
                     timer.setDelay(delay);
                     speedLabel.setText("Speed: " + (1000 / delay) + " moves/sec");
               } else {
                     outstream.writeInt(keyCode);
               }
            } catch (Exception e1) {
               e1.printStackTrace();
            }
         }

         
      });

      player1 = new PlayerBoard(0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S);
      player2 = new PlayerBoard(1, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_DOWN);

      timer = new Timer(delay, e -> {
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
                  System.out.println(i);
                  if (i == KeyEvent.VK_R) {
                     player1.resetBoard();
                     player2.resetBoard();

                  } else if (i == KeyEvent.VK_P) {

                     isRunning = !isRunning;

                  } else if (i < pieces.length * 2 & i >= 0) {
                     if (i < pieces.length) {
                        player1.nextvec.add(i);
                     } else {
                        player2.nextvec.add(i - pieces.length);

                     }

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
      client game = new client("localhost", 1234,frame);
      frame.add(game.getPanel());
      frame.pack();
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setLayout(new BorderLayout());

      L = new JLabel[401];
      for (int i = 0; i <= 400; i++) {
         L[i] = new JLabel();
         L[i].setBounds((i % 20) * TILE_SIZE, i / 20 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
         frame.add(L[i]);
      }

   }

   int[][][] pieces = {
         { { 1, 1, 1, 1, 1 } }, { { 1, 1, 1 }, { 1, 0, 1 } }, { { 1, 1, 1 }, { 1, 1, 0 } },
         { { 1, 1, 1 }, { 0, 1, 1 } }, { { 1, 1, 1, 1 }, { 1, 0, 0, 0 } }, { { 1, 1, 1, 1 }, { 0, 0, 0, 1 } },
         { { 1, 1, 1, 0 }, { 0, 0, 1, 1 } }, { { 0, 1, 1, 1 }, { 1, 1, 0, 0 } },
         { { 1, 1, 1 }, { 0, 0, 1 }, { 0, 0, 1 } }, { { 1, 1, 1 }, { 0, 1, 0 }, { 0, 1, 0 } },
         { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } }, { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 0, 1 } },
         { { 0, 1, 0 }, { 1, 1, 1 }, { 1, 0, 0 } }, { { 0, 0, 1 }, { 1, 1, 1 }, { 1, 0, 0 } },
         { { 1, 0, 0 }, { 1, 1, 1 }, { 0, 0, 1 } }, { { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 1 } }, };

   class PlayerBoard {
      int[][] currentPiece;
      int pieceRow, pieceCol;
      int offsetX;
      Random random;
      int leftKey;
      int rightKey;
      int rotateKey;
      int downKey;
      Image blueImage;
      Image violetImage;
      Image redImage;

      Vector<Integer> nextvec;

      int[][] nextPiece; // Add next piece array

      Image I;

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

         nextvec = new Vector<>();
         nextvec.add(0);
         nextvec.add(0);
         nextvec.add(0);
         spawnNewPiece(this.offsetX);
      }

      @SuppressWarnings({ "CallToPrintStackTrace", "UseSpecificCatch" })
      final void spawnNewPiece(int offsetX) {
         pieceRow = 0;
         pieceCol = BOARD_WIDTH / 2 - 5 + offsetX * 6;
      
         int rando = random.nextInt(pieces.length) + pieces.length * offsetX;
         try {
            outstream.writeInt(rando);
         } catch (Exception e1) {
            e1.printStackTrace();
         }
      
         currentPiece = pieces[nextvec.get(0)];
         nextvec.remove(0);
         nextPiece = pieces[nextvec.get(0)];
      
         // 如果新方塊出現時就已經碰撞，遊戲結束
         if (collides(currentPiece, pieceRow, pieceCol)) {
            isGameOver = true;
            isRunning = false; // 停止遊戲
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

      private void clearRows() {
         int rowsCleared = 0;
      
         for (int r = BOARD_HEIGHT - 1; r >= 0; r--) {
            boolean fullRow = true;
      
            for (int c = 0; c < BOARD_WIDTH; c++) {
               if (!board[r][c]) {
                  fullRow = false;
                  break;
               }
            }
      
            if (fullRow) {
               rowsCleared++;
               // 將該行以上的方塊下移
               for (int r2 = r; r2 > 0; r2--) {
                  for (int c = 0; c < BOARD_WIDTH; c++) {
                     board[r2][c] = board[r2 - 1][c];
                  }
               }
               // 最上面的一行清空
               for (int c = 0; c < BOARD_WIDTH; c++) {
                  board[0][c] = false;
               }
               delay = Math.max(50, delay - 100);
               timer.setDelay(delay);
               r++;
            }
         }
      
         // 根據清除的行數增加分數
         score += rowsCleared;
         
      }
      

      public void resetBoard() {
         for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
               board[r][c] = false;
            }
         }
         spawnNewPiece(offsetX);
         isGameOver = false;
         isRunning = false;
         score = 0;
      }
      public void update() {

         if (!collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
         } else {
            placePiece();
         }
      }

      public void drawNextPiece(Graphics g, int x, int y, int color) {
         // Draw next piece at the specified position
         for (int r = 0; r < nextPiece.length; r++) {
            for (int c = 0; c < nextPiece[r].length; c++) {
               if (nextPiece[r][c] == 1) {
                  if (color == 0) {
                     I = new ImageIcon("bluei.png").getImage();
                  } else {
                     I = new ImageIcon("redi.png").getImage();
                  }
                  g.drawImage(I, (x + c) * TILE_SIZE, (y + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
               }
            }
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
         int ghostRow = pieceRow;
         while (!collides(currentPiece, ghostRow + 1, pieceCol)) {
            ghostRow++;
         }

         for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
               if (currentPiece[r][c] == 1) {
                  if (color == 0) {
                     I = new ImageIcon("bluei.png").getImage();
                  } else {
                     I = new ImageIcon("redi.png").getImage();
                  }

                  g.drawImage(I, (pieceCol + c) * TILE_SIZE, (ghostRow + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);

               }
            }
         }
         // 绘制当前的方块
         for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
               if (currentPiece[r][c] == 1) {
                  g.drawImage((color == 0 ? blueImage : redImage), (pieceCol + c) * TILE_SIZE,
                        (pieceRow + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
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

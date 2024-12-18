import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Vector;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class client {
   boolean dieeee;
   boolean firsts;
   boolean firsts0;

   int BOARD_WIDTH = 20;
   int BOARD_HEIGHT = 25;
   static int TILE_SIZE = 25;

   int speed = 500;
   Timer timer;
   PlayerBoard player1;
   PlayerBoard player2;
   boolean[][] board;
   boolean isRunning;
   static JLabel[] L;
   static ImageIcon I;
   static Image im;
   int counter = 0;

   JPanel gamePanel;
   Socket socket;
   static DataOutputStream outstream;
   DataInputStream instream;

   static Clip clip, clip2;

   @SuppressWarnings("CallToPrintStackTrace")
   public client(String servername, int port) {

      playSound("bg.wav");

      isRunning = false;
      firsts0 = true;
      firsts = true;
      dieeee = false;

      gamePanel = new JPanel() {
         @Override
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isRunning) {

               if (firsts) {
                  im = new ImageIcon("tetris.png").getImage();
                  g.drawImage(im, 0, 0, BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE, null);

                  im = new ImageIcon("bob.png").getImage();
                  g.drawImage(im, 4 * TILE_SIZE, (18 - (speed * 2 / 125)) * TILE_SIZE, 2 * TILE_SIZE, 2 * TILE_SIZE,
                        null);

                  im = new ImageIcon("c.png").getImage();
                  g.drawImage(im, 5 * TILE_SIZE, 5 * TILE_SIZE, 10 * TILE_SIZE, 3 * TILE_SIZE, null);

                  im = new ImageIcon("select.png").getImage();
                  g.drawImage(im, 5 * TILE_SIZE, 10 * TILE_SIZE, 10 * TILE_SIZE, 8 * TILE_SIZE + TILE_SIZE / 2, null);

               } else {

                  if (dieeee) {
                     im = new ImageIcon("tetris.png").getImage();
                     g.drawImage(im, 0, 0, BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE, null);

                     player1.draw(g, 0);
                     player2.draw(g, 1);
                     g.setColor(Color.white);
                     g.setFont(new Font("Arial", Font.BOLD, 20));
                     g.drawString("score: " + counter, TILE_SIZE * 8, TILE_SIZE * 13);

                     im = new ImageIcon("go.png").getImage();
                     g.drawImage(im, 5 * TILE_SIZE, 10 * TILE_SIZE, 10 * TILE_SIZE, 3 * TILE_SIZE, null);

                  } else {
                     im = new ImageIcon("tetris.png").getImage();
                     g.drawImage(im, 0, 0, BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE, null);

                     g.setColor(Color.white);
                     g.setFont(new Font("Arial", Font.BOLD, 20));
                     g.drawString("score: " + counter, TILE_SIZE * 8, TILE_SIZE);

                     player1.draw(g, 0);
                     player2.draw(g, 1);

                     player1.drawNextPiece(g, 0, 0, 0);
                     player2.drawNextPiece(g, BOARD_WIDTH - player2.nextPiece[0].length, 0, 1);

                     im = new ImageIcon("rule.png").getImage();
                     g.drawImage(im, 0 * TILE_SIZE, 10 * TILE_SIZE, 20 * TILE_SIZE, 10 * TILE_SIZE, null);
                  }
               }
            } else {

               im = new ImageIcon("tetris.png").getImage();
               g.drawImage(im, 0, 0, BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE, null);

               player1.draw(g, 0);
               player2.draw(g, 1);
               g.setColor(Color.white);
               g.setFont(new Font("Arial", Font.BOLD, 20));
               g.drawString("score: " + counter, TILE_SIZE * 8, TILE_SIZE);

               player1.drawNextPiece(g, 0, 0, 0);
               player2.drawNextPiece(g, BOARD_WIDTH - player2.nextPiece[0].length, 0, 1);

            }

         }
      };

      gamePanel.setPreferredSize(new Dimension(BOARD_WIDTH * TILE_SIZE, BOARD_HEIGHT * TILE_SIZE));
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

      player1 = new PlayerBoard(0, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_Q,
            KeyEvent.VK_E);
      player2 = new PlayerBoard(1, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_U,
            KeyEvent.VK_O);

      timer = new Timer(speed, e -> {
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
                  if (i == KeyEvent.VK_B) {

                     player1.resetBoard();
                     player2.resetBoard();
                     player1.spawnNewPiece();
                     player2.spawnNewPiece();
                     firsts = true;
                     isRunning = false;
                     dieeee = false;

                  } else if (i == KeyEvent.VK_C) {

                     player1.clearanyway();

                  } else if (i == KeyEvent.VK_V) {

                     counter -= 30 * (5 - speed / 125);
                     player1.nextvec = new Vector<>();
                     player1.nextvec.add(0);
                     player1.nextvec.add(0);
                     player1.nextvec.add(0);

                     player2.nextvec = new Vector<>();
                     player2.nextvec.add(0);
                     player2.nextvec.add(0);
                     player2.nextvec.add(0);

                     player1.spawnNewPiece();
                     player2.spawnNewPiece();

                  } else if (i == KeyEvent.VK_P) {

                     if (firsts0) {
                        player1.nextvec = new Vector<>();
                        player1.nextvec.add(5);
                        player1.nextvec.add(6);
                        player1.nextvec.add(7);
                        player1.nextvec.add(8);

                        player2.nextvec = new Vector<>();
                        player2.nextvec.add(8);
                        player2.nextvec.add(9);
                        player2.nextvec.add(10);
                        player2.nextvec.add(11);

                        player1.resetBoard();
                        player2.resetBoard();
                        player1.spawnNewPiece();
                        player2.spawnNewPiece();

                     }

                     if (dieeee) {
                        player1.resetBoard();
                        player2.resetBoard();
                        player1.spawnNewPiece();
                        player2.spawnNewPiece();
                        firsts = true;
                        isRunning = false;
                        dieeee = false;
                     }
                     firsts0 = false;

                     firsts = false;

                     isRunning = !isRunning;

                  } else if (i == KeyEvent.VK_EQUALS) {
                     speed = speed - 125;
                     if (speed < 125) {
                        speed = 125;
                     }
                     timer.setDelay(speed);

                  } else if (i == KeyEvent.VK_MINUS) {
                     speed = speed + 125;
                     if (speed > 500) {
                        speed = 500;
                     }
                     timer.setDelay(speed);

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

   public static void playSound(String filePath) {
      try {
         File soundFile = new File(filePath);
         AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
         clip = AudioSystem.getClip();
         clip.open(audioStream);
         clip.start();
         clip.loop(Clip.LOOP_CONTINUOUSLY);
      } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {
      }
   }

   public static void playSound2(String filePath) {
      try {
         File soundFile2 = new File(filePath);
         AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(soundFile2);
         clip2 = AudioSystem.getClip();
         clip2.open(audioStream2);
         clip2.start();
      } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignored) {
      }
   }

   public JPanel getPanel() {
      return gamePanel;
   }

   public static void main(String[] args) {
      JFrame frame = new JFrame();
      client game = new client("192.168.1.108", 1234);// internet

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

   int[][][] pieces = {

         { { 1 } },
         { { 1, 1 } },
         { { 1, 1, 1 } },
         { { 1, 1, 1, 1, 1 } },
         { { 1, 1 }, { 1, 0 } },
         { { 1, 1, 1 }, { 1, 0, 1 } },
         { { 1, 1, 1 }, { 1, 1, 0 } },
         { { 1, 1, 1 }, { 0, 1, 1 } },
         { { 1, 1, 1, 1 }, { 1, 0, 0, 0 } },
         { { 1, 1, 1, 1 }, { 0, 0, 0, 1 } },
         { { 1, 1, 1, 0 }, { 0, 0, 1, 1 } },
         { { 0, 1, 1, 1 }, { 1, 1, 0, 0 } },
         { { 1, 1, 1, 1 }, { 0, 1, 0, 0 } },
         { { 1, 1, 1, 1 }, { 0, 0, 1, 0 } },
         { { 1, 1, 1 }, { 0, 0, 1 }, { 0, 0, 1 } },
         { { 1, 1, 1 }, { 0, 1, 0 }, { 0, 1, 0 } },
         { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } },
         { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 0, 1 } },
         { { 0, 1, 0 }, { 1, 1, 1 }, { 1, 0, 0 } },
         { { 0, 0, 1 }, { 1, 1, 1 }, { 1, 0, 0 } },
         { { 1, 0, 0 }, { 1, 1, 1 }, { 0, 0, 1 } },
         { { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 1 } }, };

   class PlayerBoard {
      int[][] currentPiece;
      int pieceRow, pieceCol;
      int offsetX;
      Random random;
      int leftKey;
      int rightKey;
      int rotateKey;
      int downKey;
      int qkey, ekey;
      Image blueImage;
      Image violetImage;
      Image redImage;

      int nowp;
      boolean holded;

      Vector<Integer> nextvec;

      int[][] nextPiece;

      Image I;

      public PlayerBoard(int offsetX, int leftKey, int rightKey, int rotateKey, int downKey, int qkey, int ekey) {
         this.offsetX = offsetX;
         this.leftKey = leftKey;
         this.rightKey = rightKey;
         this.rotateKey = rotateKey;
         this.downKey = downKey;
         this.qkey = qkey;
         this.ekey = ekey;
         blueImage = new ImageIcon("blue.png").getImage();
         redImage = new ImageIcon("red.png").getImage();
         violetImage = new ImageIcon("violet.png").getImage();

         board = new boolean[BOARD_HEIGHT][BOARD_WIDTH];
         random = new Random();

         nextvec = new Vector<>();
         nextvec.add(0);
         nextvec.add(0);
         nextvec.add(0);
         spawnNewPiece();

      }

      @SuppressWarnings({ "CallToPrintStackTrace", "UseSpecificCatch" })
      final void spawnNewPiece() {
         holded = false;
         pieceRow = 0;
         pieceCol = BOARD_WIDTH / 2 - 5 + offsetX * 6;

         int rando = random.nextInt(pieces.length) + pieces.length * offsetX;
         try {
            outstream.writeInt(rando);
         } catch (Exception e1) {
            e1.printStackTrace();
         }

         currentPiece = pieces[nextvec.get(0)];
         nowp = nextvec.get(0);
         nextvec.remove(0);
         nextPiece = pieces[nextvec.get(0)];

         if (collides(currentPiece, pieceRow + 1, pieceCol)) {
            isRunning = false;
            dieeee = true;
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

         playSound2("stomp.wav");

         for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
               if (currentPiece[r][c] == 1) {
                  try {
                     if (pieceRow != 0)
                        board[pieceRow + r][pieceCol + c] = true;

                  } catch (Exception e) {
                  }
               }
            }
         }
         clearRows();
         spawnNewPiece();
      }

      void hardDrop() {
         counter += 1 * (5 - speed / 125);

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
               playSound2("win.wav");
               counter += 10 * (5 - speed / 125);

               for (int row = r; row > 0; row--) {
                  board[row] = board[row - 1];
               }
               board[0] = new boolean[BOARD_WIDTH];
            }
         }
      }

      public void clearanyway() {
         counter -= 10 * (5 - speed / 125);

         for (int row = BOARD_HEIGHT - 1; row > 0; row--) {
            board[row] = board[row - 1];
         }
         board[0] = new boolean[BOARD_WIDTH];
      }

      public void resetBoard() {
         counter = 0;
         for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
               board[r][c] = false;
            }
         }
      }

      public void update() {

         if (!collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
         } else {
            placePiece();
         }
      }

      public void drawNextPiece(Graphics g, int x, int y, int color) {

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
         for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[r].length; c++) {
               if (currentPiece[r][c] == 1) {
                  g.drawImage((color == 0 ? blueImage : redImage), (pieceCol + c) * TILE_SIZE,
                        (pieceRow + r) * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
               }
            }
         }
      }

      public void hold() {
         if (!holded) {
            nextPiece = currentPiece;
            currentPiece = pieces[nextvec.get(0)];
            nextvec.set(0, nowp);
            pieceRow = 0;
            pieceCol = BOARD_WIDTH / 2 - 5 + offsetX * 6;

         }
         holded = true;
      }

      public void handleKey(int keyCode) {
         if (keyCode == leftKey && !collides(currentPiece, pieceRow, pieceCol - 1)) {
            pieceCol--;
         } else if (keyCode == rightKey && !collides(currentPiece, pieceRow, pieceCol + 1)) {
            pieceCol++;
         } else if (keyCode == downKey && !collides(currentPiece, pieceRow + 1, pieceCol)) {
            pieceRow++;
         } else if (keyCode == rotateKey) {
            rotatePiece();
         } else if (keyCode == qkey) {
            hardDrop();

         } else if (keyCode == ekey) {
            hold();
         }
      }
   }
}

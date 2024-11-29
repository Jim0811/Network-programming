import java.io.*;
import java.net.*;
import java.util.*;

public class server {
     static ServerSocket SSocket;
     Hashtable ht = new Hashtable();
     Socket socket;

     public server() throws IOException {
          SSocket = new ServerSocket(1234);

          System.out.println("waiting");

          while (true) {
               socket = SSocket.accept();
               System.out.println("connected");
               DataOutputStream outstream = new DataOutputStream(socket.getOutputStream());
               ht.put(socket, outstream);
               Thread thread = new Thread(new ServerThread(socket, ht));
               thread.start();
          }

     }

     public static void main(String[] args) throws Exception {
          server ServerStart = new server();
     }
}

class ServerThread extends Thread implements Runnable {
     Socket socket;
     Hashtable ht;

     public ServerThread(Socket socket, Hashtable ht) {
          this.socket = socket;
          this.ht = ht;
     }

     @Override
     public void run() {
          DataInputStream instream;

          try {
               instream = new DataInputStream(socket.getInputStream());

               while (true) {
                    int message = instream.readInt();
                    System.out.println("Message: " + message);

                    synchronized (ht) {
                         for (Enumeration e = ht.elements(); e.hasMoreElements();) {
                              DataOutputStream outstream = (DataOutputStream) e.nextElement();

                              try {
                                   outstream.writeInt(message);
                              } catch (IOException ex) {
                                   ex.printStackTrace();
                              }
                         }
                    }
               }
          } catch (IOException ex) {
          } finally {
               synchronized (ht) {
                    System.out.println("Remove connection: " + socket);

                    ht.remove(socket);

                    try {
                         socket.close();
                    } catch (IOException ex) {
                    }
               }
          }
     }
}
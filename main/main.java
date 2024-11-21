package main;

import javax.swing.JFrame;

public class main {
    public static void main(String[] args) {
        JFrame window = new JFrame("title");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gp=new GamePanel();
        window.add(gp);
        window.pack();
        
        window.setVisible(true);
    }
}

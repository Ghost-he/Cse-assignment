import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimplePlatformer extends JPanel implements KeyListener, ActionListener {
    private final int TILE_SIZE = 60;
    private final int[][] level = {
            {0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,1,0,0,0,0,1,0},
            {0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,1,0,0,0,0},
            {1,1,1,1,1,1,0,0,0,1,1,1},
            {1,1,1,1,1,1,0,0,0,1,1,1},
            {1,1,1,1,1,1,0,0,0,1,1,1}
    };
    private int playerX = TILE_SIZE * 2, playerY = TILE_SIZE * 5;
    private int playerVelocityY = 0;
    private boolean leftPressed = false, rightPressed = false, isJumping = false;
    private boolean fullscreen = false;
    private Timer timer;
    private GraphicsDevice device;
    private JFrame frame;

    public SimplePlatformer(JFrame frame) {
        this.frame = frame;
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        setPreferredSize(new Dimension(800, 480));
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(20, this);
        timer.start();
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        frame.dispose();
        if (fullscreen) {
            frame.setUndecorated(true);
            device.setFullScreenWindow(frame);
        } else {
            frame.setUndecorated(false);
            device.setFullScreenWindow(null);
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // [Keep all your existing paintComponent code unchanged]
        // Sky
        g.setColor(new Color(135, 206, 250));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Sun
        g.setColor(Color.YELLOW);
        g.fillOval(650, 50, 80, 80);

        // Sun rays
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2 * i / 12;
            int x1 = 690 + (int)(50 * Math.cos(angle));
            int y1 = 90 + (int)(50 * Math.sin(angle));
            int x2 = 690 + (int)(70 * Math.cos(angle));
            int y2 = 90 + (int)(70 * Math.sin(angle));
            g.drawLine(x1, y1, x2, y2);
        }

        // Clouds
        g.setColor(Color.WHITE);
        g.fillOval(80, 60, 120, 60);
        g.fillOval(200, 80, 100, 40);
        g.fillOval(400, 50, 140, 60);
        g.fillOval(600, 90, 100, 40);

        // Draw level tiles
        for (int y = 0; y < level.length; y++) {
            for (int x = 0; x < level[y].length; x++) {
                if (level[y][x] == 1) {
                    // Ground block
                    g.setColor(new Color(110, 90, 60));
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(120, 200, 80));
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, 10);
                }
            }
        }

        // Trees on platforms (properly aligned)
        drawTree(g, TILE_SIZE * 5 + TILE_SIZE/2 - 10, TILE_SIZE * 2 - 30, 30); // Left platform tree
        drawTree(g, TILE_SIZE * 10 + TILE_SIZE/2 - 10, TILE_SIZE * 2 - 30, 30); // Right platform tree (same height as left)

        // Additional tree on the small middle platform
        drawTree(g, TILE_SIZE * 7 + TILE_SIZE/2 - 10, TILE_SIZE * 4 - 30, 25);

        // Left ground trees and sign
        drawTree(g, TILE_SIZE * 1, TILE_SIZE * 5 - 10, 40);
        drawTree(g, TILE_SIZE * 3, TILE_SIZE * 5 - 10, 40);
        drawSign(g, TILE_SIZE * 2, TILE_SIZE * 5 + 5, false);

        // Right ground tree and sign
        drawTree(g, TILE_SIZE * 9 + 20, TILE_SIZE * 5 - 10, 40);
        drawSign(g, TILE_SIZE * 10 + 10, TILE_SIZE * 5 + 5, true);
    }

    // [Keep all your other helper methods unchanged]
    private void drawTree(Graphics g, int x, int y, int height) {
        // Trunk
        g.setColor(new Color(120, 80, 40));
        g.fillRect(x - 4, y, 8, height);
        // Leaves
        g.setColor(new Color(120, 200, 80));
        g.fillOval(x - 18, y - 24, 36, 36);
        g.setColor(new Color(90, 170, 60));
        g.fillOval(x - 12, y - 12, 24, 24);
    }

    private void drawSign(Graphics g, int x, int y, boolean rightArrow) {
        // Pole
        g.setColor(new Color(150, 110, 60));
        g.fillRect(x + 7, y + 12, 6, 18);
        // Board
        g.setColor(new Color(220, 180, 120));
        g.fillRect(x, y, 20, 15);
        // Arrow
        g.setColor(new Color(120, 90, 40));
        int[] xs, ys;
        if (rightArrow) {
            xs = new int[]{x + 6, x + 14, x + 14, x + 18, x + 14, x + 14, x + 6};
            ys = new int[]{y + 5, y + 5, y + 2, y + 8, y + 13, y + 10, y + 10};
        } else {
            xs = new int[]{x + 14, x + 6, x + 6, x + 2, x + 6, x + 6, x + 14};
            ys = new int[]{y + 5, y + 5, y + 2, y + 8, y + 13, y + 10, y + 10};
        }
        g.fillPolygon(xs, ys, xs.length);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Gravity
        playerVelocityY += 1;
        playerY += playerVelocityY;

        // Horizontal movement
        if (leftPressed) playerX -= 5;
        if (rightPressed) playerX += 5;

        // Collision detection
        if (checkCollision(playerX, playerY + TILE_SIZE)) {
            playerY = (playerY / TILE_SIZE) * TILE_SIZE;
            playerVelocityY = 0;
            isJumping = false;
        }

        // Prevent falling out of screen
        if (playerY > getHeight()) {
            playerX = TILE_SIZE * 2;
            playerY = TILE_SIZE * 5;
            playerVelocityY = 0;
        }

        repaint();
    }

    private boolean checkCollision(int x, int y) {
        int tileX = x / TILE_SIZE;
        int tileY = y / TILE_SIZE;
        if (tileX < 0 || tileY < 0 || tileY >= level.length || tileX >= level[0].length) return false;
        return level[tileY][tileX] == 1;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftPressed = true; break;
            case KeyEvent.VK_RIGHT: rightPressed = true; break;
            case KeyEvent.VK_UP:
                if (!isJumping) {
                    playerVelocityY = -16;
                    isJumping = true;
                }
                break;
            case KeyEvent.VK_F11:
                toggleFullscreen();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: leftPressed = false; break;
            case KeyEvent.VK_RIGHT: rightPressed = false; break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Platformer Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(new SimplePlatformer(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
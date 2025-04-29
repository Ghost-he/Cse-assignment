import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SimplePlatformer extends JPanel implements KeyListener, ActionListener {
    // Tile size for level design
    private final int TILE_SIZE = 60;
    private final int PLAYER_WIDTH = 26, PLAYER_HEIGHT = 57;
    private final int[][] level = {
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,1,1,0,0,0},
            {0,0,0,0,0,0,0,1,0,0,0,0,0,0},
            {1,1,1,1,1,1,0,0,0,1,1,1,1,1},
            {1,1,1,1,1,1,0,0,0,1,1,1,1,1},
            {1,1,1,1,1,1,0,0,0,1,1,1,1,1}
    };

    private int playerX = TILE_SIZE * 2, playerY = TILE_SIZE * 5;
    private int playerVelocityY = 0;
    private boolean leftPressed = false, rightPressed = false, isJumping = false, wPressed = false;
    private boolean fullscreen = false;
    private Timer timer;
    private GraphicsDevice device;
    private JFrame frame;
    private Random random = new Random();
    private int enemyShootTimer = 0;
    private Image playerImage;
    private int hitCount = 0; // new variable for tracking hits
    private Image EnemiesImage;

    // Enemy positions (x, y)

    private int[][] enemies = {
            {TILE_SIZE * 6, TILE_SIZE * 4}, // Enemy 1
            {TILE_SIZE * 8, TILE_SIZE * 2}, // Enemy 2
            {TILE_SIZE * 4, TILE_SIZE * 3}, // Enemy 3
            {TILE_SIZE * 9, TILE_SIZE * 6}, // Enemy 4
            {TILE_SIZE * 11, TILE_SIZE * 4}  // Enemy 5
    };

    // Bullet lists
    private ArrayList<Rectangle> playerBullets = new ArrayList<>();
    private ArrayList<Rectangle> enemyBullets = new ArrayList<>();

    public SimplePlatformer(JFrame frame) {
        this.frame = frame;
        // keep the image is same folder as code
        playerImage = new ImageIcon("CH11.png").getImage();
        EnemiesImage = new ImageIcon("em11.png").getImage(); // the image
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
                    g.setColor(new Color(110, 90, 60));
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(120, 200, 80));
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, 10);
                }
            }
        }

        // Draw trees, signs, and enemies
        //drawTree(g, TILE_SIZE * 5 + TILE_SIZE/2 - 10, TILE_SIZE * 2 - 30, 30);
        //drawTree(g, TILE_SIZE * 10 + TILE_SIZE/2 - 10, TILE_SIZE * 2 - 30, 30);
        drawTree(g, TILE_SIZE * 7 + TILE_SIZE/2 - 10, TILE_SIZE * 4 - 30, 25);
        drawTree(g, TILE_SIZE * 1, TILE_SIZE * 5 - 10, 40);
        drawTree(g, TILE_SIZE * 3, TILE_SIZE * 5 - 10, 40);
        drawSign(g, TILE_SIZE * 2, TILE_SIZE * 5 + 5, false);
        drawTree(g, TILE_SIZE * 9 + 20, TILE_SIZE * 5 - 10, 40);
        drawSign(g, TILE_SIZE * 10 + 10, TILE_SIZE * 5 + 5, true);

        // Draw player
        drawPlayer(g, playerX, playerY);



        // Draw enemies
        for (int[] enemy : enemies) {
            drawEnemy(g, enemy[0], enemy[1]);
        }

        // Draw bullets
        drawBullets(g);

        // Draw health
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Health: " + (5 - hitCount), 20, 30);

        // If player is dead, show death message
        if (hitCount >= 5) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Next Time We meet Kaand krduga", 100, getHeight() / 2);
        }
    }

    private void drawPlayer(Graphics g, int x, int y) {
        g.drawImage(playerImage, x, y , 26, 57, null);
    }


    private void drawEnemy(Graphics g, int x, int y) {

        g.drawImage(EnemiesImage, x, y , TILE_SIZE - 20, TILE_SIZE - 20, null);
    }

    private void drawTree(Graphics g, int x, int y, int height) {
        g.setColor(new Color(120, 80, 40));
        g.fillRect(x - 4, y, 8, height);
        g.setColor(new Color(120, 200, 80));
        g.fillOval(x - 18, y - 24, 36, 36);
        g.setColor(new Color(90, 170, 60));
        g.fillOval(x - 12, y - 12, 24, 24);
    }

    private void drawSign(Graphics g, int x, int y, boolean rightArrow) {
        g.setColor(new Color(150, 110, 60));
        g.fillRect(x + 7, y + 12, 6, 18);
        g.setColor(new Color(220, 180, 120));
        g.fillRect(x, y, 20, 15);
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

    private void drawBullets(Graphics g) {
        g.setColor(Color.YELLOW);
        for (Rectangle bullet : playerBullets) {
            g.fillRect(bullet.x, bullet.y , 10, 5);
        }

        g.setColor(Color.RED);
        for (Rectangle bullet : enemyBullets) {
            g.fillRect(bullet.x, bullet.y, 10, 5);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Apply gravity
        playerVelocityY += 1;

        // Calculate new Y position
        int newPlayerY = playerY + playerVelocityY;

        // Horizontal movement
        if (leftPressed) playerX = Math.max(0, playerX - 5);
        if (rightPressed || wPressed) playerX = Math.min(getWidth() - PLAYER_WIDTH, playerX + 5);

        // Check for ground collision
        boolean onGround = false;
        Rectangle playerFeet = new Rectangle(playerX, playerY, 26,57);

        for (int y = 0; y < level.length; y++) {
            for (int x = 0; x < level[y].length; x++) {
                if (level[y][x] == 1) {
                    Rectangle tile = new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (playerFeet.intersects(tile)) {
                        // Snap player to the top of the tile
                        playerY = y * TILE_SIZE - PLAYER_HEIGHT;
                        playerVelocityY = 0;
                        isJumping = false;
                        onGround = true;
                        break;
                    }
                }
            }
            if (onGround) break;
        }

        // If not on ground, apply the new Y position
        if (!onGround) {
            playerY = newPlayerY;
        }

        // Check for ceiling collision
        if (playerVelocityY < 0) {
            Rectangle playerHead = new Rectangle(playerX , playerY , 26,57);
            for (int y = 0; y < level.length; y++) {
                for (int x = 0; x < level[y].length; x++) {
                    if (level[y][x] == 1) {
                        Rectangle tile = new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        if (playerHead.intersects(tile)) {
                            playerVelocityY = 0;
                            playerY = y * TILE_SIZE + TILE_SIZE;
                            break;
                        }
                    }
                }
            }
        }

        // Player bullets movement
        ArrayList<Rectangle> bulletsToRemove = new ArrayList<>();
        for (Rectangle bullet : playerBullets) {
            bullet.x += 10;
            if (bullet.x > getWidth()) {
                bulletsToRemove.add(bullet);
            }
        }
        playerBullets.removeAll(bulletsToRemove);
        // Check if player bullets hit enemies
        ArrayList<Rectangle> bulletsToRemoveOnHit = new ArrayList<>();
        ArrayList<int[]> enemiesToRemove = new ArrayList<>();

        for (Rectangle bullet : playerBullets) {
            for (int[] enemy : enemies) {
                Rectangle enemyRect = new Rectangle(enemy[0], enemy[1], TILE_SIZE - 20, TILE_SIZE - 20);
                if (bullet.intersects(enemyRect)) {
                    bulletsToRemoveOnHit.add(bullet);
                    enemiesToRemove.add(enemy);
                    break; // bullet hit something, no need to check other enemies
                }
            }
        }

// Remove bullets that hit
        playerBullets.removeAll(bulletsToRemoveOnHit);

// Actually remove the enemies that were hit
        for (int[] enemyToRemove : enemiesToRemove) {
            removeEnemyByPosition(enemyToRemove[0], enemyToRemove[1]);
        }

        // Enemy bullets movement and collision detection
        ArrayList<Rectangle> enemyBulletsToRemove = new ArrayList<>();
        for (Rectangle bullet : enemyBullets) {
            bullet.x -= 5;
            if (bullet.x < 0) {
                enemyBulletsToRemove.add(bullet);
            }
            if (bullet.intersects(new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT))) {
                enemyBulletsToRemove.add(bullet);
                hitCount++; // Increase hit count
                if (hitCount > 5) hitCount = 5; // Cap at 5
            }
        }
        enemyBullets.removeAll(enemyBulletsToRemove);

        // Enemy shooting
        enemyShootTimer++;
        if (enemyShootTimer >= 50) {
            enemyShootTimer = 0;
            for (int[] enemy : enemies) {
                if (random.nextInt(100) < 30) {
                    enemyShoot(enemy[0], enemy[1]);
                }
            }
        }

        repaint();
    }

    private void enemyShoot(int enemyX, int enemyY) {
        Rectangle bullet = new Rectangle(enemyX - 10, enemyY + (TILE_SIZE - 20) / 2, 10, 5);
        enemyBullets.add(bullet);
    }
    private void removeEnemyByPosition(int x, int y) {
        ArrayList<int[]> newEnemies = new ArrayList<>();
        for (int[] enemy : enemies) {
            if (!(enemy[0] == x && enemy[1] == y)) {
                newEnemies.add(enemy);
            }
        }
        enemies = newEnemies.toArray(new int[newEnemies.size()][]);
    }


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_W:
                wPressed = true;
                break;
            case KeyEvent.VK_UP:
                if (!isJumping) {
                    playerVelocityY = -15;
                    isJumping = true;
                }
                break;
            case KeyEvent.VK_F11:
                toggleFullscreen();
                break;
            case KeyEvent.VK_SPACE:
                shootBullet();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_W:
                wPressed = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used but required by KeyListener
    }

    private void shootBullet() {
        Rectangle bullet = new Rectangle(playerX + PLAYER_WIDTH, playerY + 30, 10, 5);
        playerBullets.add(bullet);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Simple Platformer");
        SimplePlatformer game = new SimplePlatformer(frame);
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

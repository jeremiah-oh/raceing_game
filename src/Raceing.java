import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import java.util.Random;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;

import javax.swing.*;
import java.awt.*;

import javax.sound.sampled.*;
import javax.swing.border.LineBorder;

public class Raceing extends JFrame {
    private static StartScreen startScreen;
    private static Image twisty_turn_main;
    private static Image twisty_turn_outline;
    private static Image finish_line;
    private static Image tire;

    public Raceing() {
        setup();
    }

    public static void setup() {
        appFrame = new JFrame("Raceing");

        //declaring variable values
        XOFFSET = 0;
        YOFFSET = -15;
        WINWIDTH = 1000;
        WINHEIGHT = 800;
        pi = 3.14159265358979;
        endgame = false;
        //player 1 values
        p1width = 25;
        p1height = 25;
        p1originalX = (double)XOFFSET + (((double)WINWIDTH) / 3) + (p1width * 22);
        p1originalY = (double)YOFFSET + (((double)WINHEIGHT) / 3) + (p1height * 12.5);
        //player 2 values
        p2width = 25;
        p2height = 25;
        p2originalX = (double)XOFFSET + (((double)WINWIDTH) /  3) + (p2width * 20.5);
        p2originalY = (double)YOFFSET + (((double)WINHEIGHT) / 3) + (p2height * 12.5);

        gamePanel = new MyPanel();

        //init start screen and music
        startScreen = new StartScreen(new StartGame(gamePanel), new QuitGame());
        MusicPlayer musicPlayer = new MusicPlayer("nooks_cranny.wav");
        musicPlayer.start();

        appFrame.add(startScreen);
        appFrame.setSize(WINWIDTH, WINHEIGHT);
        appFrame.setLocationRelativeTo(null);
        appFrame.setVisible(true);
    }

    public static class StartScreen extends JPanel {
        public static JButton startBut;
        public static JButton exitBut;

        public StartScreen(ActionListener startListener, ActionListener exitListener) {
            setBackground(GREENLIGHT);
            setLayout(new GridBagLayout());

            //init and styling for title and buttons
            JLabel title = new JLabel("raceing game", JLabel.CENTER);
            title.setFont(new Font("SansSerif Plain", Font.BOLD, 48));
            title.setForeground(Color.WHITE);

            startBut = new JButton("         start         ");
            exitBut = new JButton("         exit         ");

            startBut.setFont(new Font("SansSerif Plain", Font.PLAIN, 22));
            exitBut.setFont(new Font("SansSerif Plain", Font.PLAIN, 22));

            startBut.setForeground(SEASHELL);
            exitBut.setForeground(SEASHELL);

            startBut.setFocusPainted(false);
            exitBut.setFocusPainted(false);

            startBut.setBackground(SAGE_GREEN);
            exitBut.setBackground(SAGE_GREEN);

            //listening for when startgame button is pressed
            StartGame startGameListener = new StartGame(gamePanel);
            QuitGame quitGameListener = new QuitGame();

            startBut.addActionListener(startGameListener);
            exitBut.addActionListener(quitGameListener);

            //layout/coords for buttons
            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            buttonPanel.setBackground(GREENLIGHT);
            buttonPanel.add(startBut);
            buttonPanel.add(exitBut);

            GridBagConstraints titleGbc = new GridBagConstraints();
            titleGbc.gridx = 0;
            titleGbc.gridy = 0;
            titleGbc.weightx = 1.0;
            titleGbc.weighty = 1.0;
            titleGbc.insets = new Insets(0, 0, 300, 0);

            //constraints for buttons
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.CENTER;

            add(title, titleGbc);
            add(buttonPanel, gbc);
        }
    }

    private static class MyPanel extends JPanel implements KeyListener {
        private boolean startRace = false;
        private Image carModel;
        private Image carModel2;
        private double player1X;
        private double player1Y;
        private double playerRotation;
        private double player2X;
        private double player2Y;
        private double player2Rotation;
        private int p1Laps = 0;
        private int p2Laps = 0;
        private boolean player1OnFinish, player2OnFinish;
        
        public MyPanel() {
            twisty_turn_main = new ImageIcon("twist_and_turn_maintrack.png").getImage();
            twisty_turn_outline = new ImageIcon("twist_and_turn_outline.png").getImage();
            finish_line = loadAndResizeImage("finish_line.png", 59, 59);
            carModel = loadAndResizeImage("car1.png", 50, 80);
            carModel2 = loadAndResizeImage("car2.png", 50, 80);
            tire = loadAndResizeImage("tire.png", 40, 40);

            player1X = p1originalX;
            player1Y = p1originalY;
            player2X = p2originalX;
            player2Y = p2originalY;
            playerRotation = 0.0;
            player2Rotation = 0.0;

            setPreferredSize(new Dimension(WINWIDTH, WINHEIGHT));

            setFocusable(true);
            addKeyListener(this);
            pUp = false;
            pW = false;
        }

        //drawing everything onto the main screen
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (startRace) {
                Graphics2D g2d = (Graphics2D) g;
                double scaleX = (double) getWidth() / twisty_turn_main.getWidth(null);
                double scaleY = (double) getHeight() / twisty_turn_main.getHeight(null);

                double scaleX1 = (double) getWidth() / twisty_turn_outline.getWidth(null);
                double scaleY1 = (double) getHeight() / twisty_turn_outline.getHeight(null);

                AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
                g2d.drawImage(twisty_turn_main, scaleTransform, this);

                AffineTransform scaleTransform1 = AffineTransform.getScaleInstance(scaleX1, scaleY1);
                g2d.drawImage(twisty_turn_outline, scaleTransform1, this);

                Font font = new Font("SansSerif Plain", Font.PLAIN, 12);
                g2d.setFont(font);
                g2d.setColor(SEASHELL);
                g2d.drawString("P1 Speed: " + Math.round(p1velocity) * 60, 5, 20);
                g2d.drawString("P2 Speed: " + Math.round(p2velocity) * 60, 895, 20);

                Font font2 = new Font("SansSerif Plain", Font.PLAIN, 12);
                g2d.setFont(font2);
                g2d.setColor(SEASHELL);
                g2d.drawString("P1 Laps: " + p1Laps + " / 3", 5, 35);
                g2d.drawString("P2 Laps: " + p2Laps + " / 3", 895, 35);

                int finishX = 860;
                int finishY = 550;
                g2d.drawImage(finish_line, finishX, finishY, null);

                AffineTransform rotationTransform = AffineTransform.getRotateInstance(playerRotation,
                        player1X + carModel.getWidth(null) / 2, player1Y + carModel.getHeight(null) / 2);
                g2d.setTransform(rotationTransform);

                g2d.drawImage(carModel, (int) player1X, (int) player1Y, null);

                AffineTransform rotationTransform2 = AffineTransform.getRotateInstance(player2Rotation,
                    player2X + carModel2.getWidth(null) / 2, player2Y + carModel2.getHeight(null) / 2);
                g2d.setTransform(rotationTransform2);

                g2d.drawImage(carModel2, (int) player2X, (int) player2Y, null);

                g2d.drawImage(tire, 400, 500, null);
                g2d.drawImage(tire, 340, 440, null);
                g2d.drawImage(tire, 290, 510, null);
                g2d.drawImage(tire, 250, 410, null);
            }
        }

        //method for resizing car model
        private Image loadAndResizeImage(String filePath, int width, int height) {
            try {
                BufferedImage originalImage = ImageIO.read(new File(filePath));
                BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(originalImage, 0, 0, width, height, null);
                g.dispose();
                return resizedImage;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void startRace() {
            startRace = true;
            repaint();
        }

        public void setPlayerPosition(double x, double y) {
            x = Math.max(XOFFSET, Math.min(x, XOFFSET + WINWIDTH - 70));
            y = Math.max(YOFFSET, Math.min(y, YOFFSET + WINHEIGHT - 100));

            player1X = x;
            player1Y = y;
            //checkLapFinish();
            repaint();
        }

        public void setPlayer2Position(double x, double y) {
            x = Math.max(XOFFSET, Math.min(x, XOFFSET + WINWIDTH - 70));
            y = Math.max(YOFFSET, Math.min(y, YOFFSET + WINHEIGHT - 100));

            player2X = x;
            player2Y = y;
            //checkLapFinish();
            repaint();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            handleKey(e, true);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            handleKey(e, false);
        }

        @Override
        public void keyTyped(KeyEvent e) { }

        private void handleKey(KeyEvent e, boolean pressed) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_UP:
                    pUp = pressed;
                    break;
                case KeyEvent.VK_DOWN:
                    pDown = pressed;
                    break;
                case KeyEvent.VK_LEFT:
                    pLeft = pressed;
                    break;
                case KeyEvent.VK_RIGHT:
                    pRight = pressed;
                    break;
                case KeyEvent.VK_W:
                    pW = pressed;
                    break;
                case KeyEvent.VK_A:
                    pA = pressed;
                    break;
                case KeyEvent.VK_S:
                    pS = pressed;
                    break;
                case KeyEvent.VK_D:
                    pD = pressed;
                    break;
            }
        }

        public void setPlayerRotation(double angle) {
            playerRotation = angle;
            repaint();
        }

        public void setPlayer2Rotation(double angle) {
            player2Rotation = angle;
            repaint();
        }

        private Color getBackgroundColor(int x, int y) {
            BufferedImage newMap = toBufferedImage(twisty_turn_main);
            BufferedImage newMap2 = toBufferedImage(twisty_turn_outline);
            int rgb = newMap.getRGB(x, y);
            int rgb1 = newMap2.getRGB(x, y);
            return new Color(rgb, true);
        }

        private static BufferedImage toBufferedImage(Image image) {
            if (image == null) {
                // Handle the case when the image is not loaded properly
                System.err.println("Error: Unable to load image");
                return null;
            }
    
            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            }
    
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
    
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
    
            return bufferedImage;
        }

        private void checkLapFinish() {
            if (isTouchingFinish(player1X, player1Y)) {
                if (!player1OnFinish) {
                    player1OnFinish = true;
                    p1Laps++;
                    updateLapCounter();
                }
            } else {
                player1OnFinish = false;
            }
        
            if (isTouchingFinish(player2X, player2Y)) {
                if (!player2OnFinish) {
                    player2OnFinish = true;
                    p2Laps++;
                    updateLapCounter();
                }
            } else {
                player2OnFinish = false;
            }
        }

        private boolean isTouchingFinish(double x, double y) {
            int pixelX = (int) Math.round(x);
            int pixelY = (int) Math.round(y);
            Color pixelColor = getBackgroundColor(pixelX, pixelY);
        
            int finishLineRed = 255;
            int finishLineGreen = 255;
            int finishLineBlue = 255;
        
            int colorTolerance = 30;
        
            return Math.abs(pixelColor.getRed() - finishLineRed) < colorTolerance
                && Math.abs(pixelColor.getGreen() - finishLineGreen) < colorTolerance
                && Math.abs(pixelColor.getBlue() - finishLineBlue) < colorTolerance;
        }

        private void updateLapCounter() {
            if (p1Laps >= 3 || p2Laps >= 3) {
                endgame = true;
                System.exit(0);
            }
            
            SwingUtilities.invokeLater(() -> {
                repaint();
            });
        }
    }

    private static class StartGame implements ActionListener {
        private final MyPanel panel;
        private final JButton startBut;
        private final JButton exitBut;

        public StartGame(MyPanel panel) {
            this.panel = panel;
            this.startBut = StartScreen.startBut;
            this.exitBut = StartScreen.exitBut;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            appFrame.remove(startScreen);

            appFrame.add(gamePanel);
            appFrame.revalidate();
            appFrame.repaint();

            panel.setPlayerPosition(p1originalX, p1originalY);
            panel.setPlayer2Position(p2originalX, p2originalY);
            panel.startRace();

            startBut.setVisible(false);
            exitBut.setVisible(false);

            panel.requestFocusInWindow();

            PlayerMover playerMover = new PlayerMover(panel);
            new Thread(playerMover).start();

            panel.repaint();
        }
    }

    private static class QuitGame implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            endgame = true;
            System.exit(0);
        }
    }

    private static class PlayerMover implements Runnable {
        public PlayerMover(MyPanel panel) {
            this.panel = panel;
            rotateStep = 0.045; 
            rotateStep2 = 0.045;
            rotationAngle = 0.0;
            rotationAngle2 = 0.0;
            p1velocity = 0.0;
            accelerating = false;
        }
        public void run() {
            while (!endgame) {
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //player 1 movement
                if (pUp) {
                    isAccel = true;
                    isDecel = false;
                } else if (isAccel) {
                    isAccel = false;
                    isDecel = true;
                }
                if (isAccel) {
                    p1velocity = Math.min(p1velocity + accel, maxSpeed);
                    moveInDirection(rotationAngle, false);
                }
                if (isDecel) {
                    p1velocity = Math.max(p1velocity - deceleration, 0.0);
                    moveInDirection(rotationAngle, false);

                    if (p1velocity == 0.0) {
                        isDecel = false;
                    }
                }
                if (pDown) {
                    p1velocity = Math.max(p1velocity - decel, 0.0);
                    moveInDirection(rotationAngle + pi, false);
                }
                if (pLeft) {
                    accelerating = false;
                    rotationAngle -= rotateStep;
                    panel.setPlayerRotation(rotationAngle);
                }
                if (pRight) {
                    accelerating = false;
                    rotationAngle += rotateStep;
                    panel.setPlayerRotation(rotationAngle);
                }

                //player 2 movement
                if (pW) {
                    isAccel2 = true;
                    isDecel2 = false;
                } else if (isAccel2) {
                    isAccel2 = false;
                    isDecel2 = true;
                }
                if (isAccel2) {
                    moveInDirection(rotationAngle2, true);
                    p2velocity = Math.min(p2velocity + accel2, maxSpeed);
                }
                if (isDecel2) {
                    p2velocity = Math.max(p2velocity - deceleration2, 0.0);
                    moveInDirection(rotationAngle2, true);

                    if (p2velocity == 0.0) {
                        isDecel2 = false;
                    }
                }
                if (pS) {
                    p2velocity = Math.max(p2velocity - decel2, 0.0);
                    moveInDirection(rotationAngle2 + pi, true);
                }
                if (pA) {
                    accelerating = false;
                    rotationAngle2 -= rotateStep2;
                    panel.setPlayer2Rotation(rotationAngle2);
                }
                if (pD) {
                    accelerating = false;
                    rotationAngle2 += rotateStep2;
                    panel.setPlayer2Rotation(rotationAngle2);
                }

                SwingUtilities.invokeLater(() -> {
                    panel.repaint();
                });
            }
        }

        //method to send car in direction its pointing
        private void moveInDirection(double angle, boolean isPlayer2) {
            double adjustedAngle = angle - (Math.PI / 2.0);
            double velocity = isPlayer2 ? p2velocity : p1velocity;

            double newX = isPlayer2 ? panel.player2X : panel.player1X;
            double newY = isPlayer2 ? panel.player2Y : panel.player1Y;

            newX += velocity * Math.cos(adjustedAngle);
            newY += velocity * Math.sin(adjustedAngle);

            newX = Math.max(XOFFSET, Math.min(newX, XOFFSET + WINWIDTH - 70));
            newY = Math.max(YOFFSET, Math.min(newY, YOFFSET + WINHEIGHT - 100));

            int pixelX = (int) newX;
            int pixelY = (int) newY;
            BufferedImage terrain = toBufferedImage(twisty_turn_outline);

            // if (isSlowTerrain(terrain, newX, newY)) {
            //      velocity *= 0.95;
            // }

            if (isPlayer2) {
                panel.setPlayer2Position(newX, newY);
                p2velocity = velocity;
            } else {
                panel.setPlayerPosition(newX, newY); 
                p1velocity = velocity;
            }
        }

        private boolean isSlowTerrain(BufferedImage terrain, double x, double y) {
            int pixelX = (int) Math.round(x);
            int pixelY = (int) Math.round(y);

            if (pixelX < 0 || pixelY < 0 || pixelX >= twisty_turn_outline.getWidth(null) 
                || pixelY >= twisty_turn_outline.getHeight(null)) {
                // Make sure the indices are within valid bounds
                return false;
            }

            terrain = toBufferedImage(twisty_turn_outline);
            // Get the alpha value of the pixel
            int alpha = (terrain.getRGB(pixelX, pixelY) >> 24) & 0xFF;

            // Adjust the threshold based on your needs
            int alphaThreshold = 260;

            // If the alpha value is above the threshold, consider it part of the outline
            return alpha > alphaThreshold;
        }

        private static BufferedImage toBufferedImage(Image image) {
            if (image == null) {
                // Handle the case when the image is not loaded properly
                System.err.println("Error: Unable to load image");
                return null;
            }
    
            if (image instanceof BufferedImage) {
                return (BufferedImage) image;
            }
    
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
    
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
    
            return bufferedImage;
        }

        //private double velocityStep;
        private double rotateStep, rotateStep2, rotationAngle, rotationAngle2;
        private final MyPanel panel;
        private static final double maxSpeed = 3.0;
        private static final double accel = 0.05;
        private static final double accel2 = 0.05;
        private static final double decel = 0.5;
        private static final double decel2 = 0.5;
        private boolean accelerating = false;
        private static final double deceleration = 0.045;
        private static final double deceleration2 = 0.045;
        private boolean isAccel, isDecel, isAccel2, isDecel2 = false;
    }

    /*private static class ImageObject {
        public ImageObject() { }

        public ImageObject(double xinput, double yinput, double xwidthinput,
                           double yheightinput, double angleinput) {
            x = xinput;
            y = yinput;
            xwidth = xwidthinput;
            yheight = yheightinput;
            angle = angleinput;
            internalangle = 0.0;
            coords = new Vector<Double>();
        }

        public double getX() { return x; }

        public double getY() { return y; }

        public double getWidth() { return xwidth; }

        public double getHeight() { return yheight; }

        public double getAngle() { return angle; }

        public double getInternalAngle() { return internalangle; }

        public void setAngle(double angleinput) { angle = angleinput; }

        public void setInternalAngle(double input) { internalangle = input; }

        public Vector<Double> getCoords() { return coords; }

        public void setCoords(Vector<Double> input) {
            coords = input;
            generateTriangles();
        }
    }*/

    private static class CollisionChecker implements Runnable {
        public void run() {
            Random randomNumbers = new Random (LocalTime.now().getNano());
        }
    }

    public static class MusicPlayer {
        private Clip clip;

        public MusicPlayer(String filePath) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));

                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            if (clip != null) {
                clip.start();
            }
        }

        public void stop() {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public static void main(String[] args) {
        setup();
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setResizable(false);
    }

    ///////////////////////////////////////////////////////////////
    //list of variables
    ///////////////////////////////////////////////////////////////
    private static Boolean endgame;

    //private static ImageObject p1 ;
    private static double p1width, p1height, p1originalX, p1originalY, p1velocity;

    private static double p2width, p2height, p2originalX, p2originalY, p2velocity;

    private static int XOFFSET, YOFFSET, WINWIDTH, WINHEIGHT;

    private static double pi;

    private static Boolean pUp = false;
    private static Boolean pDown = false;
    private static Boolean pLeft = false;
    private static Boolean pRight = false;

    private static Boolean pW = false;
    private static Boolean pS = false;
    private static Boolean pA = false;
    private static Boolean pD = false;

    private static JFrame appFrame;
    private static MyPanel gamePanel;

    private static Color SAGE_GREEN = new Color(100,170,140);
    private static Color GREENLIGHT = new Color(46,139,87);
    private static Color SEASHELL = new Color(255,245,238);
}

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
        p1originalX = (double)XOFFSET + ((double)WINWIDTH /  2.0) - (p1width / 2.0);
        p1originalY = (double)YOFFSET + ((double)WINHEIGHT / 2.0) - (p1height / 2.0);
        //player 2 values
        p2width = 25;
        p2height = 25;
        p2originalX = (double)XOFFSET + ((double)WINWIDTH /  2.0) - (p2width / 2.0);
        p2originalY = (double)YOFFSET + ((double)WINHEIGHT / 2.0) - (p2height / 2.0);

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
        private JLabel velLabel1;
        private JLabel velLabel2;
        
        public MyPanel() {
            twisty_turn_main = new ImageIcon("twist_and_turn_maintrack.png").getImage();
            twisty_turn_outline = new ImageIcon("twist_and_turn_outline.png").getImage();

            carModel = loadAndResizeImage("car1.png", 50, 80);
            carModel2 = loadAndResizeImage("car2.png", 50, 80);

            //velLabel1 = new JLabel("P1 Speed: 0");
            //velLabel2 = new JLabel("P2 Speed: 0");
            //Font labelFont = new Font("SansSerif Plain", Font.PLAIN, 18);
            //velLabel1.setFont(labelFont);
            //velLabel2.setFont(labelFont);
            //velLabel1.setForeground(SEASHELL);
            //velLabel2.setForeground(SEASHELL);
            //add(velLabel1);
            //add(velLabel2);

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

                AffineTransform rotationTransform = AffineTransform.getRotateInstance(playerRotation,
                        player1X + carModel.getWidth(null) / 2, player1Y + carModel.getHeight(null) / 2);
                g2d.setTransform(rotationTransform);

                g2d.drawImage(carModel, (int) player1X, (int) player1Y, null);

                AffineTransform rotationTransform2 = AffineTransform.getRotateInstance(player2Rotation,
                    player2X + carModel2.getWidth(null) / 2, player2Y + carModel2.getHeight(null) / 2);
                g2d.setTransform(rotationTransform2);

                g2d.drawImage(carModel2, (int) player2X, (int) player2Y, null);
            }
                //velLabel1.setText("P1 Speed: " + Math.round(p1velocity));
                //velLabel2.setText("P2 Speed: " + Math.round(p2velocity));

                //velLabel1.setBounds(10, 10, 100, 20);
                //velLabel2.setBounds(800, 10, 100, 20);
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
            repaint();
        }

        public void setPlayer2Position(double x, double y) {
            x = Math.max(XOFFSET, Math.min(x, XOFFSET + WINWIDTH - 70));
            y = Math.max(YOFFSET, Math.min(y, YOFFSET + WINHEIGHT - 100));

            player2X = x;
            player2Y = y;
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
            panel.startRace();

            startBut.setVisible(false);
            exitBut.setVisible(false);

            panel.requestFocusInWindow();

            PlayerMover playerMover = new PlayerMover(panel);
            new Thread(playerMover).start();
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
            velocityStep = 1.0;
            rotateStep = 0.045; 
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
                    // accelerating = true;
                    // p1velocity = Math.min(p1velocity + accel, maxSpeed);
                    // moveInDirection(rotationAngle, false);
                } else if (isAccel) {
                    isAccel = false;
                    isDecel = true;
                    // accelerating = false;
                    // p1velocity = Math.max(p1velocity - decel, 0.0);
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
                    // accelerating = true;
                    // moveInDirection(rotationAngle2, true);
                    // p2velocity = Math.min(p2velocity + accel, maxSpeed);
                } else if (isAccel2) {
                    isAccel2 = false;
                    isDecel2 = true;
                    // accelerating = false;
                    // p2velocity = Math.max(p2velocity - decel, 0.0);
                }
                if (isAccel2) {
                    moveInDirection(rotationAngle2, true);
                    p2velocity = Math.min(p2velocity + accel, maxSpeed);
                }
                if (isDecel2) {
                    p2velocity = Math.max(p2velocity - deceleration, 0.0);
                    moveInDirection(rotationAngle2, true);

                    if (p2velocity == 0.0) {
                        isDecel2 = false;
                    }
                }
                if (pS) {
                    p2velocity = Math.max(p2velocity - decel, 0.0);
                    moveInDirection(rotationAngle2 + pi, true);
                }
                if (pA) {
                    accelerating = false;
                    rotationAngle2 -= rotateStep;
                    panel.setPlayer2Rotation(rotationAngle2);
                }
                if (pD) {
                    accelerating = false;
                    rotationAngle2 += rotateStep;
                    panel.setPlayer2Rotation(rotationAngle2);
                }
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
            // BufferedImage terrain = toBufferedImage(twisty_turn);

            // if (isSlowTerrain(terrain, newX, newY)) {
            //     velocity *= 0.9;
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
            
            if (pixelX < 0 || pixelY < 0 || pixelX >= terrain.getWidth() || pixelY >= terrain.getHeight()) {
                // Make sure the indices are within valid bounds
                return false;
            }
        
            int pixelColor = terrain.getRGB(pixelX, pixelY);
            Color color = new Color(pixelColor);

            int grayscaleValue = (int) (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);

            // Define a threshold range for grayscale values corresponding to the main track
            int trackMinGrayscale = 100;  // Adjust this value based on your needs
            int trackMaxGrayscale = 200;  // Adjust this value based on your needs

            // Check if the grayscale value is not within the main track range
            return (grayscaleValue < trackMinGrayscale || grayscaleValue > trackMaxGrayscale);
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

        private double velocityStep;
        private double rotateStep;
        private double rotationAngle;
        private double rotationAngle2;
        private final MyPanel panel;
        private static final double maxSpeed = 3.25;
        private static final double accel = 0.0725;
        private static final double decel = 0.5;
        private boolean accelerating = false;
        private static final double deceleration = 0.08;
        private boolean isAccel = false;
        private boolean isDecel = false;
        private boolean isAccel2 = false;
        private boolean isDecel2 = false;
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
    private static double p1width ;
    private static double p1height ;
    private static double p1originalX ;
    private static double p1originalY ;
    private static double p1velocity ;

    private static double p2width;
    private static double p2height;
    private static double p2originalX;
    private static double p2originalY;
    private static double p2velocity;

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

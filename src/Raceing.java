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
    private static MyPanel panel;
    private static MyPanel2 panel2;

    public Raceing() {
        setup();
    }

    public void setup() {
        appFrame = new JFrame("Raceing");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setResizable(false);

        XOFFSET = 0;
        YOFFSET = 40;
        WINWIDTH = 1000;
        WINHEIGHT = 800;
        pi = 3.14159265358979;
        endgame = false;
        //init player 1 dimensions and position
        p1width = 25;
        p1height = 25;
        p1originalX = (double)XOFFSET + ((double)WINWIDTH /  2.0) - (p1width / 2.0);
        p1originalY = (double)YOFFSET + ((double)WINHEIGHT / 2.0) - (p1height / 2.0);
        //init player 2 dimensions and position
        p2width = 25;
        p2height = 25;
        p2originalX = (double)XOFFSET + ((double)WINWIDTH /  2.0) - (p2width);
        p2originalY = (double)YOFFSET + ((double)WINHEIGHT / 2.0) - (p2height);

        panel = new MyPanel();
        panel2 = new MyPanel2();

        //init start screen with buttons
        startScreen = new StartScreen(new StartGame(panel, panel2), new QuitGame());
        //init music player as soon as game starts up
        MusicPlayer musicPlayer = new MusicPlayer("AI_Test_Kitchen_pitchy_traditional_japanese_koto.wav");
        musicPlayer.start();

        //add elements to app frame
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

            StartGame startGameListener = new StartGame(panel, panel2);
            QuitGame quitGameListener = new QuitGame();
            startBut.addActionListener(startGameListener);
            exitBut.addActionListener(quitGameListener);

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

        private Image twisty_turn;
        private Image carModel;
        private double player1X;
        private double player1Y;
        private double playerRotation;

        public MyPanel() {
            twisty_turn = new ImageIcon("twist_and_turn.png").getImage();
            carModel = loadAndResizeImage("car1.png", 70, 100);
            player1X = p1originalX;
            player1Y = p1originalY;
            playerRotation = 0.0;

            setPreferredSize(new Dimension(WINWIDTH, WINHEIGHT));

            setFocusable(true);
            addKeyListener(this);
            pUp = false;
        }

        public static Image loadAndResizeImage(String filePath, int width, int height) {
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (startRace) {
                Graphics2D g2d = (Graphics2D) g;
                double scaleX = (double) getWidth() / twisty_turn.getWidth(null);
                double scaleY = (double) getHeight() / twisty_turn.getHeight(null);

                AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
                g2d.drawImage(twisty_turn, scaleTransform, this);

                //save original transform
                AffineTransform ogTransform = g2d.getTransform();

                //draw first player car
                AffineTransform rotationTransform1 = AffineTransform.getRotateInstance(playerRotation,
                    player1X + carModel.getWidth(null) / 2, player1Y + carModel.getHeight(null) / 2);
                g2d.setTransform(rotationTransform1);
                g2d.drawImage(carModel, (int) player1X, (int) player1Y, null);

                //set back to original
                g2d.setTransform(ogTransform);
            }
        }

        public void startRace() {
            startRace = true;
            repaint();
        }

        public void setPlayerPosition(double x, double y) {
            player1X = x;
            player1Y = y;
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
            }
        }

        public void setPlayerRotation(double angle) {
            playerRotation = angle;
            repaint();
        }
    }

    private static class MyPanel2 extends MyPanel {
        private boolean startRace = false;
        private Image carModel2;
        private double player2X;
        private double player2Y;
        private double playerRotation2;

        public MyPanel2() {
            super();
            carModel2 = loadAndResizeImage("car2.png", 70, 100);
            player2X = p2originalX;
            player2Y = p2originalY;
            playerRotation2 = 0.0;
            pW = false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (startRace) {
                Graphics2D g2d = (Graphics2D) g;
            //draw player 2 car
                AffineTransform rotationTransform2 = AffineTransform.getRotateInstance(playerRotation2,
                    player2X + carModel2.getWidth(null) / 2, player2Y + carModel2.getHeight(null) / 2); 
                g2d.setTransform(rotationTransform2);
                g2d.drawImage(carModel2, (int) player2X, (int) player2Y + 50, null);
            }
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
                case KeyEvent.VK_A:
                    pA = pressed;
                    break;
                case KeyEvent.VK_W:
                    pW = pressed;
                    break;
                case KeyEvent.VK_D:
                    pD = pressed;
                    break;
                case KeyEvent.VK_S:
                    pS = pressed;
                    break;
            }
        }

        public void startRace() {
            startRace = true;
            repaint();
        }

        public void setPlayerPosition(double x, double y) {
            player2X = x;
            player2Y = y + 20;
        }

        public void setPlayerRotation(double angle) {
            playerRotation2 = angle;
        }
    }

    private static class StartGame implements ActionListener {
        private final MyPanel panel;
        private final MyPanel2 panel2;
        private final JButton startBut;
        private final JButton exitBut;

        public StartGame(MyPanel panel, MyPanel2 panel2) {
            this.panel = panel;
            this.panel2 = panel2;
            this.startBut = StartScreen.startBut;
            this.exitBut = StartScreen.exitBut;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (appFrame == null) {
                appFrame = new JFrame("Raceing");
                appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                appFrame.setResizable(false);
                appFrame.setSize(WINWIDTH, WINHEIGHT);
                appFrame.setLocationRelativeTo(null);
                appFrame.setVisible(true);
            }

            appFrame.remove(startScreen);

            JPanel containerPanel = new JPanel(new BorderLayout());

        // Create a race panel with GridLayout (2 rows, 1 column)
            JPanel racePanel = new JPanel(new GridLayout(2, 1));
            racePanel.add(panel);
            racePanel.add(panel2);

        // Set preferred size for racePanel
            racePanel.setPreferredSize(new Dimension(WINWIDTH, WINHEIGHT));

            containerPanel.add(racePanel, BorderLayout.CENTER);  // Add race panel to the center of containerPanel

            appFrame.add(containerPanel);  // Add containerPanel to appFrame
            appFrame.pack();
            appFrame.revalidate();
            appFrame.repaint();

            panel.setPreferredSize(new Dimension(WINWIDTH, WINHEIGHT / 2));
            panel2.setPreferredSize(new Dimension(WINWIDTH, WINHEIGHT / 2));

            panel.setPlayerPosition(p1originalX, p1originalY);
            panel2.setPlayerPosition(p2originalX, p1originalY + WINHEIGHT / 2);

            panel.startRace();
            panel2.startRace();
            startBut.setVisible(false);
            exitBut.setVisible(false);
            panel.requestFocusInWindow();
            panel2.requestFocusInWindow();

            PlayerMover playerMover = new PlayerMover(panel, panel2);
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
        public PlayerMover(MyPanel panel, MyPanel2 panel2) {
            this.panel = panel;
            this.panel2 = panel2;
            velocityStep = 1.0;
            rotateStep = 0.03;
            rotationAngle = 0.0;
            rotationAngle2 = 0.0;
            p1velocity = 0.0;
            p2velocity = 0.0;
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
                if (pUp) {
                    accelerating = true;
                    p1velocity = Math.min(p1velocity + accel, maxSpeed);
                    moveInDirection(rotationAngle, p1velocity, panel.player1X, panel.player1Y);
                }
                else {
                    accelerating = false;
                    p1velocity = Math.max(p1velocity - decel, 0.0);
                }
                if (pDown) {
                    moveInDirection(rotationAngle + pi, p1velocity, panel.player1X, panel.player1Y);
                    p1velocity = Math.max(p1velocity - decel, 0.0);
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

                //movement for 2nd player
                if (pW) {
                    p2velocity = Math.min(p2velocity + accel, maxSpeed);
                    moveInDirection(rotationAngle2, p2velocity, panel2.player2X, panel2.player2Y);
                }
                if (pS) {
                    p2velocity = Math.max(p2velocity - decel, 0.0);
                    moveInDirection(rotationAngle2 + pi, p2velocity, panel2.player2X, panel2.player2Y);
                }
                if (pA) {
                    accelerating = false;
                    rotationAngle2 -= rotateStep;
                    panel2.setPlayerRotation(rotationAngle2);
                }
                if (pD) {
                    accelerating = false;
                    rotationAngle2 += rotateStep;
                    panel2.setPlayerRotation(rotationAngle2);
                }
            }
        }

        private void moveInDirection(double angle, double velocity, double playerX, double playerY) {
            double adjustedAngle = angle - (Math.PI / 2.0);
            double newX = playerX + velocity * Math.cos(adjustedAngle);
            double newY = playerY + velocity * Math.sin(adjustedAngle);
            panel.setPlayerPosition(newX, newY);
            panel2.setPlayerPosition(newX, newY + 20);
        }

        private double velocityStep;
        private double rotateStep;
        private double rotationAngle;
        private double rotationAngle2;
        private final MyPanel panel;
        private final MyPanel2 panel2;
        //private double currentSpeed = 0.0;
        private boolean accelerating = false;
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
        Raceing raceGame = new Raceing();
        raceGame.setup();
    }

    ///////////////////////////////////////////////////////////////
    //list of variables
    ///////////////////////////////////////////////////////////////
    private static StartScreen startScreen;
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
    
    private static final double maxSpeed = 2.5;
    private static final double accel = 0.026;
    private static final double decel = 0.5;

    private static JFrame appFrame;

    private static Color SAGE_GREEN = new Color(100,170,140);
    private static Color GREENLIGHT = new Color(46,139,87);
    private static Color SEASHELL = new Color(255,245,238);
}

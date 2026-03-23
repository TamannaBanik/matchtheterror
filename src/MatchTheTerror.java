import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MatchTheTerror extends JFrame {
    private List<Card> cards;
    private Card firstSelectedCard = null;
    private Card secondSelectedCard = null;
    private int pairsFound = 0;
    private final int totalPairs = 8;

    private Timer timer;

    private long startTime = 0;
    private JLabel displayTimerLabel;
    private Timer gameTimer;

    private Font customTitleFont;
    private Font customCardFont;
    private BufferedImage backgroundImage;

    public MatchTheTerror() {
        try {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, new File("GrimeSlimeDripping-Regular.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);

            customTitleFont = baseFont.deriveFont(Font.PLAIN, 56f);
            customCardFont = baseFont.deriveFont(Font.PLAIN, 28f);
        } catch (Exception e) {
            e.printStackTrace();
            customTitleFont = new Font("Serif", Font.BOLD | Font.ITALIC, 46);
            customCardFont = new Font("Arial", Font.BOLD, 24);
        }

        setTitle("Match The Terror");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeCards();

        try {
            backgroundImage = ImageIO.read(new File("bg.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JPanel backgroundPane = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(backgroundPane);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("MATCH THE TERROR", SwingConstants.CENTER);
        titleLabel.setFont(customTitleFont);
        titleLabel.setForeground(new Color(255, 60, 60));
        titleLabel.setText("<html><div style='text-shadow: 2px 2px #000000;'>MATCH THE TERROR</div></html>");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 5, 10));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);

        displayTimerLabel = new JLabel("Time: 00:00", SwingConstants.CENTER);
        displayTimerLabel
                .setFont(customCardFont != null ? customCardFont.deriveFont(28f) : new Font("Arial", Font.BOLD, 28));
        displayTimerLabel.setForeground(new Color(255, 60, 60));
        displayTimerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        displayTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(displayTimerLabel);

        add(topPanel, BorderLayout.NORTH);

        gameTimer = new Timer(1000, e -> {
            if (startTime > 0) {
                long timeTaken = (System.currentTimeMillis() - startTime) / 1000;
                long minutes = timeTaken / 60;
                long seconds = timeTaken % 60;
                displayTimerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
            }
        });

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 4, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel.setOpaque(false);

        for (Card card : cards) {
            gridPanel.add(card);
        }

        JPanel centerPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }

            @Override
            public void doLayout() {
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                gridPanel.setBounds(x, y, size, size);
            }
        };

        centerPanel.setOpaque(false);
        centerPanel.add(gridPanel);

        add(centerPanel, BorderLayout.CENTER);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkMatch();
            }
        });
        timer.setRepeats(false);
    }

    private void initializeCards() {
        cards = new ArrayList<>();

        String[][] data = {
                { "Adolf Hitler", "Germany", "hitler.jpg", "flag_germany.png" },
                { "Joseph Stalin", "Soviet Union", "stalin.jpg", "flag_ussr.png" },
                { "Benito Mussolini", "Italy", "mussolini.jpg", "flag_italy.png" },
                { "Mao Zedong", "China", "mao.jpg", "flag_china.png" },
                { "Saddam Hussein", "Iraq", "saddam.jpg", "flag_iraq.png" },
                { "Idi Amin", "Uganda", "idi.jpg", "flag_uganda.jpg" },
                { "Donald Trump", "United States", "donald.jpg", "flag_us.jpg" },
                { "Kim Jong Un", "North Korea", "kim.jpg", "flag_nk.jpg" }
        };

        int id = 0;
        for (String[] pair : data) {
            Card dictatorCard = new Card(id, pair[0], "Dictator", pair[2]);
            Card countryCard = new Card(id, pair[1], "Country", pair[3]);

            cards.add(dictatorCard);
            cards.add(countryCard);
            id++;
        }

        Collections.shuffle(cards);
    }

    private void checkMatch() {
        if (firstSelectedCard.getPairId() == secondSelectedCard.getPairId()) {
            firstSelectedCard.setMatched(true);
            secondSelectedCard.setMatched(true);
            pairsFound++;

            if (pairsFound == totalPairs) {
                if (gameTimer != null)
                    gameTimer.stop();
                showGameOverScreen();
            }
        } else {
            firstSelectedCard.flipDown();
            secondSelectedCard.flipDown();
        }

        firstSelectedCard = null;
        secondSelectedCard = null;
    }

    private void showGameOverScreen() {
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false);

        JLabel gameOverLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        gameOverLabel
                .setFont(customTitleFont != null ? customTitleFont.deriveFont(80f) : new Font("Serif", Font.BOLD, 80));
        gameOverLabel.setForeground(new Color(255, 60, 60));
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("You found all the matches!", SwingConstants.CENTER);
        subtitleLabel
                .setFont(customCardFont != null ? customCardFont.deriveFont(32f) : new Font("Arial", Font.BOLD, 32));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        long timeTaken = startTime > 0 ? (System.currentTimeMillis() - startTime) / 1000 : 0;
        long minutes = timeTaken / 60;
        long seconds = timeTaken % 60;
        JLabel timeLabel = new JLabel(String.format("Time: %02d:%02d", minutes, seconds), SwingConstants.CENTER);
        timeLabel.setFont(customCardFont != null ? customCardFont.deriveFont(28f) : new Font("Arial", Font.BOLD, 28));
        timeLabel.setForeground(new Color(200, 200, 200));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messagePanel.add(gameOverLabel);
        messagePanel.add(Box.createVerticalStrut(20));
        messagePanel.add(subtitleLabel);
        messagePanel.add(Box.createVerticalStrut(15));
        messagePanel.add(timeLabel);

        overlay.add(messagePanel);

        overlay.addMouseListener(new java.awt.event.MouseAdapter() {
        });

        setGlassPane(overlay);
        overlay.setVisible(true);
    }

    private class Card extends JButton implements ActionListener {
        private int pairId;
        private String faceText;
        private boolean isFaceUp = false;
        private boolean isMatched = false;
        private Image faceImage;

        private Timer animTimer;
        private double scaleX = 1.0;
        private boolean isAnimating = false;
        private int flippingHalf = 0;
        private boolean targetFaceUp;

        public Card(int pairId, String faceText, String type, String imageFilename) {
            this.pairId = pairId;
            this.faceText = faceText;

            if (imageFilename != null) {
                try {
                    faceImage = ImageIO.read(new File("images/" + imageFilename));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (faceImage == null) {
                    faceImage = new ImageIcon("images/" + imageFilename).getImage();
                }
            }

            setFaceDownAppearance();
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(200, 200));

            addActionListener(this);

            animTimer = new Timer(15, e -> {
                if (flippingHalf == 1) {
                    scaleX -= 0.1;
                    if (scaleX <= 0.01) {
                        scaleX = 0.01;
                        flippingHalf = 2;
                        isFaceUp = targetFaceUp;
                        if (isFaceUp) {
                            setFaceUpAppearance();
                        } else {
                            setFaceDownAppearance();
                        }
                    }
                } else if (flippingHalf == 2) {
                    scaleX += 0.1;
                    if (scaleX >= 1.0) {
                        scaleX = 1.0;
                        isAnimating = false;
                        flippingHalf = 0;
                        animTimer.stop();
                    }
                }
                repaint();
            });
        }

        private void setFaceDownAppearance() {
            setText("?");
            setFont(customCardFont);
            setForeground(new Color(220, 220, 220));
            setIcon(null);
        }

        private void setFaceUpAppearance() {
            if (faceImage != null) {
                setText("");
                setFont(customCardFont);
            } else {
                setText("<html><center>" + faceText.replaceAll(" ", "<br>") + "</center></html>");
                if (faceText.length() <= 2) {
                    setFont(new Font("SansSerif", Font.PLAIN, 65));
                } else {
                    setFont(customCardFont);
                }
            }
            setForeground(Color.WHITE);
        }

        public int getPairId() {
            return pairId;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
            setForeground(new Color(200, 255, 200));
            setEnabled(false);
        }

        public void flipUp() {
            if (!isFaceUp && !isMatched && !isAnimating) {
                startFlip(true);
            }
        }

        public void flipDown() {
            if (isFaceUp && !isMatched) {
                startFlip(false);
            }
        }

        private void startFlip(boolean faceUp) {
            targetFaceUp = faceUp;
            isAnimating = true;
            flippingHalf = 1;
            animTimer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
                if (gameTimer != null)
                    gameTimer.start();
            }

            if (isFaceUp || isMatched || timer.isRunning() || isAnimating) {
                return;
            }

            flipUp();

            if (firstSelectedCard == null) {
                firstSelectedCard = this;
            } else if (secondSelectedCard == null) {
                secondSelectedCard = this;
                timer.start();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isAnimating) {
                double sx = Math.max(scaleX, 0.01);
                g2.translate(w / 2.0 * (1.0 - sx), 0);
                g2.scale(sx, 1.0);
            }
            GradientPaint gp;
            if (isMatched) {
                gp = new GradientPaint(0, 0, new Color(50, 120, 50), 0, h, new Color(10, 40, 10));
            } else if (isFaceUp) {
                gp = new GradientPaint(0, 0, new Color(150, 20, 20), 0, h, new Color(50, 5, 5));
            } else {
                gp = new GradientPaint(0, 0, new Color(60, 60, 60), 0, h, new Color(20, 20, 20));
            }
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 30, 30);

            if ((isFaceUp || isMatched) && faceImage != null) {
                int pad = 15;
                Shape oldClip = g2.getClip();
                RoundRectangle2D rect = new RoundRectangle2D.Float(pad, pad, w - 2 * pad, h - 2 * pad, 15, 15);
                g2.setClip(rect);
                g2.drawImage(faceImage, pad, pad, w - 2 * pad, h - 2 * pad, this);
                g2.setClip(oldClip);
            }

            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 30, 30);

            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MatchTheTerror game = new MatchTheTerror();
            game.setVisible(true);
        });
    }
}

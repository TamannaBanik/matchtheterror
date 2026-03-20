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
    
    // Timer for delaying the flip back when cards do not match
    // Timer for delaying the flip back when cards do not match
    private Timer timer;
    
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

        // Initialize pairs
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
                
                // Add a semi-transparent dark overlay to make cards and text stand out
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(backgroundPane);
        
        // Setup Title Label
        JLabel titleLabel = new JLabel("MATCH THE TERROR", SwingConstants.CENTER);
        titleLabel.setFont(customTitleFont);
        titleLabel.setForeground(new Color(255, 60, 60)); // Crimson red
        // Add a subtle drop shadow effect by using HTML
        titleLabel.setText("<html><div style='text-shadow: 2px 2px #000000;'>MATCH THE TERROR</div></html>");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        // Setup the grid UI
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 4, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel.setOpaque(false);

        for (Card card : cards) {
            gridPanel.add(card);
        }

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(gridPanel);
        
        add(wrapperPanel, BorderLayout.CENTER);

        // Setup the timer for flipping cards back if they don't match
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
        
        // Define the pairs: {Dictator, Country Display, Dictator Image, Flag Image}
        String[][] data = {
            {"Adolf Hitler", "Germany", "hitler.jpg", "flag_germany.png"},
            {"Joseph Stalin", "Soviet Union", "stalin.jpg", "flag_ussr.png"},
            {"Benito Mussolini", "Italy", "mussolini.jpg", "flag_italy.png"},
            {"Mao Zedong", "China", "mao.jpg", "flag_china.png"},
            {"Saddam Hussein", "Iraq", "saddam.jpg", "flag_iraq.png"},
            {"Idi Amin", "Uganda", "idi.jpg", "flag_uganda.jpg"},
            {"Donald Trump", "United States", "donald.jpg", "flag_us.jpg"},
            {"Kim Jong Un", "North Korea", "kim.jpg", "flag_nk.jpg"}
        };

        int id = 0;
        for (String[] pair : data) {
            // Dictator Card
            Card dictatorCard = new Card(id, pair[0], "Dictator", pair[2]);
            // Country Card
            Card countryCard = new Card(id, pair[1], "Country", pair[3]);
            
            cards.add(dictatorCard);
            cards.add(countryCard);
            id++;
        }

        // Shuffle the cards
        Collections.shuffle(cards);
    }

    private void checkMatch() {
        if (firstSelectedCard.getPairId() == secondSelectedCard.getPairId()) {
            // Match found!
            firstSelectedCard.setMatched(true);
            secondSelectedCard.setMatched(true);
            pairsFound++;

            if (pairsFound == totalPairs) {
                JOptionPane.showMessageDialog(this, "You found all the matches!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // No match, flip them back
            firstSelectedCard.flipDown();
            secondSelectedCard.flipDown();
        }

        firstSelectedCard = null;
        secondSelectedCard = null;
    }

    private class Card extends JButton implements ActionListener {
        private int pairId;
        private String faceText;
        private boolean isFaceUp = false;
        private boolean isMatched = false;
        private Image faceImage;
        
        // Animation fields
        private Timer animTimer;
        private double scaleX = 1.0;
        private boolean isAnimating = false;
        private int flippingHalf = 0; // 0=none, 1=shrinking, 2=expanding
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
            
            // Initial appearance (face down)
            setFaceDownAppearance();
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(200, 200));
            
            addActionListener(this);
            
            animTimer = new Timer(15, e -> {
                if (flippingHalf == 1) { // shrinking
                    scaleX -= 0.1;
                    if (scaleX <= 0.01) {
                        scaleX = 0.01;
                        flippingHalf = 2; // start expanding
                        isFaceUp = targetFaceUp;
                        if (isFaceUp) {
                            setFaceUpAppearance();
                        } else {
                            setFaceDownAppearance();
                        }
                    }
                } else if (flippingHalf == 2) { // expanding
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
                if (faceText.length() <= 2) { // Likely an emoji or short code
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

        public boolean isMatched() {
            return isMatched;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
            setForeground(new Color(200, 255, 200)); // Pale green text
            setEnabled(false); // Disable interaction once matched
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
            flippingHalf = 1; // start shrinking
            animTimer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing if card is already face up or game is checking a match or currently animating
            if (isFaceUp || isMatched || timer.isRunning() || isAnimating) {
                return;
            }

            flipUp();

            if (firstSelectedCard == null) {
                firstSelectedCard = this;
            } else if (secondSelectedCard == null) {
                secondSelectedCard = this;
                // Start a short delay before checking to let user see the card
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
                // Translate to center so it scales around its horizontal center
                g2.translate(w / 2.0 * (1.0 - sx), 0);
                g2.scale(sx, 1.0);
            }
            // Draw rounded background with gradient
            GradientPaint gp;
            if (isMatched) {
                gp = new GradientPaint(0, 0, new Color(50, 120, 50), 0, h, new Color(10, 40, 10)); // Toxic green gradient
            } else if (isFaceUp) {
                gp = new GradientPaint(0, 0, new Color(150, 20, 20), 0, h, new Color(50, 5, 5)); // Blood red gradient
            } else {
                gp = new GradientPaint(0, 0, new Color(60, 60, 60), 0, h, new Color(20, 20, 20)); // Dark iron gradient
            }
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, 30, 30);
            
            if ((isFaceUp || isMatched) && faceImage != null) {
                int pad = 15;
                Shape oldClip = g2.getClip();
                RoundRectangle2D rect = new RoundRectangle2D.Float(pad, pad, w - 2*pad, h - 2*pad, 15, 15);
                g2.setClip(rect);
                g2.drawImage(faceImage, pad, pad, w - 2*pad, h - 2*pad, this);
                g2.setClip(oldClip);
            }
            
            // Draw a subtle thin border
            g2.setColor(new Color(255, 255, 255, 40));
            g2.drawRoundRect(0, 0, w-1, h-1, 30, 30);
            
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

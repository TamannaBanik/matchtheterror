import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchTheTerror extends JFrame {
    private List<Card> cards;
    private Card firstSelectedCard = null;
    private Card secondSelectedCard = null;
    private int pairsFound = 0;
    private final int totalPairs = 8;
    
    // Timer for delaying the flip back when cards do not match
    private Timer timer;

    public MatchTheTerror() {
        setTitle("Match The Terror");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize pairs
        initializeCards();

        // Setup the grid UI
        getContentPane().setBackground(Color.BLACK);
        
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 4, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gridPanel.setBackground(Color.BLACK);

        for (Card card : cards) {
            gridPanel.add(card);
        }

        add(gridPanel, BorderLayout.CENTER);

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
        
        // Define the pairs: {Dictator, Country/Flag}
        String[][] data = {
            {"Adolf Hitler", "Germany"},
            {"Joseph Stalin", "Soviet Union"},
            {"Benito Mussolini", "Italy"},
            {"Mao Zedong", "China"},
            {"Saddam Hussein", "Iraq"},
            {"Idi Amin", "Uganda"},
            {"Pol Pot", "Cambodia"},
            {"Kim Jong-il", "North Korea"}
        };

        int id = 0;
        for (String[] pair : data) {
            // Dictator Card
            Card dictatorCard = new Card(id, pair[0], "Dictator");
            // Country Card
            Card countryCard = new Card(id, pair[1], "Country");
            
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
        private String type; // "Dictator" or "Country"
        private boolean isFaceUp = false;
        private boolean isMatched = false;
        
        // Animation fields
        private Timer animTimer;
        private double scaleX = 1.0;
        private boolean isAnimating = false;
        private int flippingHalf = 0; // 0=none, 1=shrinking, 2=expanding
        private boolean targetFaceUp;
        
        public Card(int pairId, String faceText, String type) {
            this.pairId = pairId;
            this.faceText = faceText;
            this.type = type;
            
            // Initial appearance (face down)
            setFaceDownAppearance();
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            
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
            setFont(new Font("Arial", Font.BOLD, 30));
            setBackground(new Color(200, 160, 255)); // Light purple
            setForeground(Color.WHITE);
            setIcon(null);
        }
        
        private void setFaceUpAppearance() {
            setText("<html><center>" + faceText.replaceAll(" ", "<br>") + "</center></html>");
            setFont(new Font("Arial", Font.BOLD, 30));
            setBackground(new Color(230, 210, 255)); // Lighter purple
            setForeground(Color.BLACK);
        }

        public int getPairId() {
            return pairId;
        }

        public boolean isMatched() {
            return isMatched;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
            setBackground(new Color(150, 255, 150)); // Light green
            setForeground(Color.BLACK);
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
            
            // Draw rounded background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, 30, 30);
            
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

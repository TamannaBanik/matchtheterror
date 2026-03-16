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
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(4, 4, 10, 10));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        
        public Card(int pairId, String faceText, String type) {
            this.pairId = pairId;
            this.faceText = faceText;
            this.type = type;
            
            // Initial appearance (face down)
            setText("?");
            setFont(new Font("Arial", Font.BOLD, 30));
            setBackground(Color.DARK_GRAY);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            
            addActionListener(this);
        }

        public int getPairId() {
            return pairId;
        }

        public boolean isMatched() {
            return isMatched;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
            setBackground(Color.GREEN);
            setForeground(Color.BLACK);
            setEnabled(false); // Disable interaction once matched
        }

        public void flipUp() {
            if (!isFaceUp && !isMatched) {
                isFaceUp = true;
                setText("<html><center>" + faceText.replaceAll(" ", "<br>") + "</center></html>");
                setBackground(Color.LIGHT_GRAY);
                setForeground(Color.BLACK);
                // In a future version with images, you'd load the image here:
                // setIcon(new ImageIcon("images/" + faceText + ".png"));
            }
        }

        public void flipDown() {
            if (isFaceUp && !isMatched) {
                isFaceUp = false;
                setText("?");
                setBackground(Color.DARK_GRAY);
                setForeground(Color.WHITE);
                setIcon(null);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing if card is already face up or game is checking a match
            if (isFaceUp || isMatched || timer.isRunning()) {
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MatchTheTerror game = new MatchTheTerror();
            game.setVisible(true);
        });
    }
}

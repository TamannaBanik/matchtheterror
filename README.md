# Match The Terror

A classic card-matching memory game built in Java Swing. 
The objective is to match a dictator with their corresponding country.

## How playing works:
- You are presented with a 4x4 board of flipped down cards (16 total).
- 8 of the cards represent dictators, and 8 of the cards represent the countries they ruled.
- Click a card to flip it over. Click another card to try and find its match.
- If they match, they will light up green and stay face up!
- If they don't, they will flip back over after 1 second.
- Memorize the positions and find all the pairs to win.

## Prerequisites
- **Java JDK (11 or higher)**: Make sure you have Java installed.

## How to Play

### Compile
Open your terminal/command prompt, navigate to the folder with the `src` directory, and run:
```bash
javac src/MatchTheTerror.java
```

### Run
To launch the game, run:
```bash
java -cp src MatchTheTerror
```

## Adding Images (Future Development)
Currently, the application displays large text for simplicity. If you want to use custom images for dictators and flags:
1. Place their image files (e.g. `hitler.png`, `germany.png`) into the `images/` folder.
2. Edit `MatchTheTerror.java` in the `flipUp()` method to display the image using `setIcon(new ImageIcon("images/" + filename));` instead of `setText`.

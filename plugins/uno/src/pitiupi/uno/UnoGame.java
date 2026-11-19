package pitiupi.uno;

import java.util.ArrayList;
import java.util.List;

public class UnoGame {
    private final List<String> hand = new ArrayList<>();
    private final List<String> deck = new ArrayList<>();
    private String cartaAntiga = "a5";
    private String opponentName;
    private int opponentCards = 7;
    private boolean connected = false;
    private boolean myTurn = false;
    private boolean gameOver = false;

    public void startNewGame(long seed, boolean isInitiator) {
        hand.clear();
        deck.clear();
        deck.addAll(UnoProtocol.createShuffledDeck(seed));

        for (int i = 0; i < 7; i++) {
            String p1 = deck.remove(0);
            String p2 = deck.remove(0);
            hand.add(isInitiator ? p1 : p2);
        }

        int top = 0;
        while (top < deck.size() && deck.get(top).charAt(0) == 'k') {
            top++;
        }
        this.cartaAntiga = deck.remove(top < deck.size() ? top : 0);

        this.connected = true;
        this.gameOver = false;
        this.myTurn = isInitiator;
        this.opponentCards = 7;
    }

    public void disconnect() {
        this.connected = false;
        this.myTurn = false;
        this.gameOver = false;
        this.opponentName = null;
        this.opponentCards = 7;
        this.hand.clear();
        this.deck.clear();
    }

    public boolean playCard(int index, String cartaFinal) {
        if (!myTurn || index < 0 || index >= hand.size()) {
            return false;
        }
        String cartaAtual = hand.get(index);
        if (!UnoProtocol.isValidPlay(cartaAtual, cartaAntiga)) {
            return false;
        }

        hand.remove(index);
        this.cartaAntiga = cartaFinal.substring(0, 2);

        char valor = cartaFinal.charAt(1);
        int penalty = valor == UnoProtocol.SYMBOL_WILD4 ? 4 : (valor == UnoProtocol.SYMBOL_PLUS2 ? 2 : 0);
        for (int i = 0; i < penalty; i++) {
            drawFromDeck();
        }
        opponentCards += penalty;

        if (hand.isEmpty()) {
            this.gameOver = true;
            this.myTurn = false;
            return true;
        }

        myTurn = penalty > 0 || valor == UnoProtocol.SYMBOL_BLOCK || valor == UnoProtocol.SYMBOL_REVERSE;
        return true;
    }

    public String drawCard() {
        if (!myTurn) {
            return null;
        }
        String card = drawFromDeck();
        hand.add(card);
        myTurn = false;
        return card;
    }

    public void receiveOpponentAction(String player, String code) {
        this.opponentName = player;

        if (code.startsWith(UnoProtocol.ACTION_CONNECT) || code.startsWith(UnoProtocol.ACTION_ACCEPT) || code.startsWith(UnoProtocol.ACTION_RESET)) {
            startNewGame(parseSeed(code), !code.startsWith(UnoProtocol.ACTION_CONNECT));
            return;
        }

        if (UnoProtocol.ACTION_DISCONNECT.equals(code) || code.startsWith(UnoProtocol.ACTION_REJECT)) {
            disconnect();
            return;
        }

        if (code.toUpperCase().startsWith("PASS") || code.toUpperCase().startsWith("DRAW")) {
            drawFromDeck();
            opponentCards++;
            myTurn = !gameOver;
            return;
        }

        if (code.length() == 2 && "abcdk".indexOf(code.charAt(0)) >= 0) {
            this.cartaAntiga = code;
            opponentCards--;
            if (opponentCards <= 0) {
                this.gameOver = true;
                this.myTurn = false;
                return;
            }

            char val = cartaAntiga.charAt(1);
            int penalty = val == UnoProtocol.SYMBOL_WILD4 ? 4 : (val == UnoProtocol.SYMBOL_PLUS2 ? 2 : 0);
            for (int i = 0; i < penalty; i++) {
                hand.add(drawFromDeck());
            }
            myTurn = penalty == 0 && val != UnoProtocol.SYMBOL_BLOCK && val != UnoProtocol.SYMBOL_REVERSE;
        }
    }

    private String drawFromDeck() {
        if (deck.isEmpty()) {
            deck.addAll(UnoProtocol.createShuffledDeck(System.currentTimeMillis()));
        }
        return deck.remove(0);
    }

    private long parseSeed(String code) {
        if (code.contains(":")) {
            try {
                return Long.parseLong(code.substring(code.indexOf(":") + 1));
            } catch (Exception ignored) {
            }
        }
        return System.currentTimeMillis();
    }

    public List<String> getHand() {
        return hand;
    }

    public String getCartaAntiga() {
        return cartaAntiga;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getOpponentCards() {
        return opponentCards;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }
}

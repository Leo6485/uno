package pitiupi.uno;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class UnoProtocol {
    public static final String ACTION_CONNECT = "CONNECT";
    public static final String ACTION_ACCEPT = "ACCEPT";
    public static final String ACTION_DISCONNECT = "DISCONNECT";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_PASS = "PASS";
    public static final String ACTION_RESET = "RESET";

    public static final char COLOR_BLUE = 'a';
    public static final char COLOR_GREEN = 'b';
    public static final char COLOR_RED = 'c';
    public static final char COLOR_YELLOW = 'd';

    public static final char SYMBOL_PLUS2 = 'a';
    public static final char SYMBOL_REVERSE = 'b';
    public static final char SYMBOL_BLOCK = 'c';
    public static final char SYMBOL_WILD = 'd';
    public static final char SYMBOL_WILD4 = 'e';

    private static final String[] CORES = {"a", "b", "c", "d"};

    private UnoProtocol() {
    }

    public static boolean isValidPlay(String cartaAtual, String cartaAntiga) {
        char val = cartaAtual.charAt(1);
        return val == SYMBOL_WILD || val == SYMBOL_WILD4 || cartaAtual.charAt(0) == cartaAntiga.charAt(0) || val == cartaAntiga.charAt(1);
    }

    public static String getCardLabel(String code) {
        return switch (code.charAt(1)) {
            case SYMBOL_PLUS2 -> "+2";
            case SYMBOL_REVERSE -> "R";
            case SYMBOL_BLOCK -> "B";
            case SYMBOL_WILD -> "E";
            case SYMBOL_WILD4 -> "+4";
            default -> String.valueOf(code.charAt(1));
        };
    }

    public static Color getCardColor(String code) {
        return switch (code.charAt(0)) {
            case COLOR_BLUE -> Color.decode("#0072BC");
            case COLOR_GREEN -> Color.decode("#00A651");
            case COLOR_RED -> Color.decode("#ED1C24");
            case COLOR_YELLOW -> Color.decode("#FFDE00");
            default -> new Color(33, 33, 33);
        };
    }

    public static List<String> createShuffledDeck(long seed) {
        List<String> deck = new ArrayList<>(108);
        for (String cor : CORES) {
            deck.add(cor + "0");
            for (String val : new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c"}) {
                deck.add(cor + val);
                deck.add(cor + val);
            }
        }
        for (int i = 0; i < 4; i++) {
            deck.add("k" + SYMBOL_WILD);
            deck.add("k" + SYMBOL_WILD4);
        }
        Collections.shuffle(deck, new Random(seed));
        return deck;
    }
}
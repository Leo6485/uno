package pitiupi.uno.GUI;

import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.control.ConnectionManager;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.PeerIpv4;
import pitiupi.uno.UnoCardMessage;
import pitiupi.uno.UnoGame;
import pitiupi.uno.UnoProtocol;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnoPanel extends JPanel {
    private final ConnectionManager connectionManager;
    private final UnoGame game = new UnoGame();
    private final Map<String, Long> blockedPeers = new HashMap<>();

    private final JPanel connectBar;
    private final JLabel ipLabel;
    private final JTextField ipInput;
    private final JButton connectButton;
    private final JButton disconnectButton;
    private final JLabel opponentLabel;
    private final JLabel turnLabel;
    private final JLabel statusLabel;
    private final JLabel topCardDisplay;
    private final JLabel opponentCardDisplay;
    private final JPanel handButtonsPanel;
    private final JLabel handCountLabel;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton drawButton;
    private final JButton resetButton;

    private final ImageIcon blockIconSmall;
    private final ImageIcon reverseIconSmall;
    private final ImageIcon changeColorIconSmall;
    private final ImageIcon blockIconLarge;
    private final ImageIcon reverseIconLarge;
    private final ImageIcon changeColorIconLarge;

    private int handStartIndex = 0;
    private InetAddress opponentAddress;

    public UnoPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;

        blockIconSmall = loadScaledIcon("block.png", 42);
        reverseIconSmall = loadScaledIcon("reverse.png", 42);
        changeColorIconSmall = loadScaledIcon("change-color.png", 42);

        blockIconLarge = loadScaledIcon("block.png", 80);
        reverseIconLarge = loadScaledIcon("reverse.png", 80);
        changeColorIconLarge = loadScaledIcon("change-color.png", 80);

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        connectBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        ipLabel = new JLabel("IP:");
        ipInput = new JTextField(11);
        connectButton = new JButton("Conectar");
        disconnectButton = new JButton("Desconectar");

        connectButton.addActionListener(e -> connectByIpAction());
        ipInput.addActionListener(e -> connectByIpAction());
        disconnectButton.addActionListener(e -> disconnectAction());

        connectBar.add(ipLabel);
        connectBar.add(ipInput);
        connectBar.add(connectButton);
        connectBar.add(disconnectButton);
        topContainer.add(connectBar);

        JPanel opponentRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        opponentCardDisplay = new JLabel();
        opponentLabel = new JLabel("Aguardando adversário...", JLabel.CENTER);
        opponentLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        opponentRow.add(opponentCardDisplay);
        opponentRow.add(opponentLabel);
        topContainer.add(opponentRow);

        turnLabel = new JLabel("Aguardando conexão...", JLabel.CENTER);
        turnLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        turnLabel.setForeground(new Color(230, 81, 0));
        turnLabel.setAlignmentX(CENTER_ALIGNMENT);

        statusLabel = new JLabel("Informe o IP para conectar.", JLabel.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(new Color(117, 117, 117));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        topContainer.add(turnLabel);
        topContainer.add(statusLabel);
        add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));

        JPanel tableCardPanel = new JPanel(new BorderLayout(0, 6));
        JLabel tableLabel = new JLabel("Mesa", JLabel.CENTER);
        tableLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tableCardPanel.add(tableLabel, BorderLayout.NORTH);

        topCardDisplay = new JLabel("?", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                paintRoundedCard(g, this, 14, 2f);
                super.paintComponent(g);
            }
        };
        topCardDisplay.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        topCardDisplay.setBackground(Color.DARK_GRAY);
        topCardDisplay.setForeground(Color.WHITE);
        topCardDisplay.setPreferredSize(new Dimension(130, 170));
        tableCardPanel.add(topCardDisplay, BorderLayout.CENTER);

        centerPanel.add(tableCardPanel);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout(6, 6));

        handCountLabel = new JLabel("Sua mão (0):");
        handCountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        bottomContainer.add(handCountLabel, BorderLayout.NORTH);

        JPanel handRow = new JPanel(new BorderLayout(6, 6));

        prevButton = new JButton("◀");
        prevButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        prevButton.setPreferredSize(new Dimension(46, 95));
        prevButton.setFocusPainted(false);
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> { handStartIndex--; refreshHandUi(); });

        nextButton = new JButton("▶");
        nextButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        nextButton.setPreferredSize(new Dimension(46, 95));
        nextButton.setFocusPainted(false);
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> { handStartIndex++; refreshHandUi(); });

        handButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));

        handRow.add(prevButton, BorderLayout.WEST);
        handRow.add(handButtonsPanel, BorderLayout.CENTER);
        handRow.add(nextButton, BorderLayout.EAST);
        bottomContainer.add(handRow, BorderLayout.CENTER);

        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        drawButton = new JButton("Comprar (+1)");
        drawButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        drawButton.setEnabled(false);
        drawButton.addActionListener(e -> drawCardAction());

        resetButton = new JButton("Nova Partida");
        resetButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        resetButton.setEnabled(true);
        resetButton.addActionListener(e -> resetGame(true));

        actionsBar.add(drawButton);
        actionsBar.add(resetButton);
        bottomContainer.add(actionsBar, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

        setConnectionUiState(false);
        updateOpponentCardDisplay("?");
        updateTurnUi();
    }

    private void setConnectionUiState(boolean inGame) {
        ipLabel.setVisible(!inGame);
        ipInput.setVisible(!inGame);
        connectButton.setVisible(!inGame);
        disconnectButton.setVisible(inGame);
        connectBar.revalidate();
        connectBar.repaint();
    }

    private static void paintRoundedCard(Graphics g, JComponent c, int arc, float stroke) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.getBackground());
        g2.fillRoundRect(1, 1, c.getWidth() - 2, c.getHeight() - 2, arc, arc);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(stroke));
        g2.drawRoundRect(1, 1, c.getWidth() - 2, c.getHeight() - 2, arc, arc);
        g2.dispose();
    }

    private ImageIcon createOpponentCardImage(String countText) {
        int w = 50;
        int h = 70;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(25, 25, 25));
        g2.fillRoundRect(1, 1, w - 2, h - 2, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, w - 2, h - 2, 8, 8);

        g2.setColor(new Color(237, 28, 36));
        AffineTransform oldTx = g2.getTransform();
        g2.translate(w / 2.0, h / 2.0);
        g2.rotate(-Math.toRadians(25));
        g2.fillOval(-18, -11, 36, 22);
        g2.setTransform(oldTx);

        int fontSize = countText.length() > 1 ? 16 : 22;
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(countText)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(countText, tx + 1, ty + 1);

        g2.setColor(Color.WHITE);
        g2.drawString(countText, tx, ty);

        g2.dispose();
        return new ImageIcon(img);
    }

    private void updateOpponentCardDisplay(String countText) {
        opponentCardDisplay.setIcon(createOpponentCardImage(countText));
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            URL url = getClass().getResource("assets/" + name);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                return new ImageIcon(img.getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private ImageIcon getCardIcon(String code, boolean large) {
        return switch (code.charAt(1)) {
            case UnoProtocol.SYMBOL_BLOCK -> large ? blockIconLarge : blockIconSmall;
            case UnoProtocol.SYMBOL_REVERSE -> large ? reverseIconLarge : reverseIconSmall;
            case UnoProtocol.SYMBOL_WILD -> large ? changeColorIconLarge : changeColorIconSmall;
            default -> null;
        };
    }

    private void connectByIpAction() {
        String ip = ipInput.getText().trim();
        if (ip.isEmpty()) {
            statusLabel.setText("Informe o IP do adversário.");
            return;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            connectToPeer(ip, addr);
        } catch (Exception ex) {
            statusLabel.setText("IP inválido.");
        }
    }

    public void connectToPeer(String name, InetAddress address) {
        this.opponentAddress = address;
        game.setOpponentName(name);

        long seed = System.currentTimeMillis();
        sendMessage(UnoProtocol.ACTION_CONNECT + ":" + seed);

        opponentLabel.setText("Conectando a " + name + "...");
        statusLabel.setText("Aguardando resposta...");
        setConnectionUiState(true);
        updateOpponentCardDisplay("?");
    }

    private void resetTableUi() {
        topCardDisplay.setIcon(null);
        topCardDisplay.setText("?");
        topCardDisplay.setForeground(Color.WHITE);
        topCardDisplay.setBackground(Color.DARK_GRAY);
        updateOpponentCardDisplay("?");
    }

    private void disconnectAction() {
        sendMessage(UnoProtocol.ACTION_DISCONNECT);
        game.disconnect();
        opponentAddress = null;

        opponentLabel.setText("Aguardando adversário...");
        turnLabel.setText("Aguardando conexão...");
        turnLabel.setForeground(new Color(230, 81, 0));
        statusLabel.setText("Desconectado.");
        resetTableUi();

        setConnectionUiState(false);
        drawButton.setEnabled(false);

        refreshHandUi();
        updateTurnUi();
    }

    private void refreshHandUi() {
        handButtonsPanel.removeAll();
        List<String> hand = game.getHand();
        int total = hand.size();

        handStartIndex = Math.max(0, Math.min(handStartIndex, total - 7));

        int end = Math.min(total, handStartIndex + 7);
        for (int i = handStartIndex; i < end; i++) {
            final int cardIndex = i;
            String card = hand.get(i);
            JButton btn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    paintRoundedCard(g, this, 10, 1.5f);
                    super.paintComponent(g);
                }
            };
            ImageIcon icon = getCardIcon(card, false);
            if (icon != null) {
                btn.setIcon(icon);
            } else {
                btn.setText(UnoProtocol.getCardLabel(card));
            }
            btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            btn.setBackground(UnoProtocol.getCardColor(card));
            btn.setForeground(card.charAt(0) == UnoProtocol.COLOR_YELLOW && icon == null ? Color.BLACK : Color.WHITE);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new Dimension(72, 95));
            btn.setEnabled(game.isConnected() && game.isMyTurn() && !game.isGameOver());
            btn.addActionListener(e -> attemptPlayCard(cardIndex, card));
            handButtonsPanel.add(btn);
        }

        prevButton.setEnabled(game.isConnected() && handStartIndex > 0);
        nextButton.setEnabled(game.isConnected() && handStartIndex + 7 < total);

        handCountLabel.setText("Sua mão (" + total + "):");

        handButtonsPanel.revalidate();
        handButtonsPanel.repaint();
    }

    private void attemptPlayCard(int cardIndex, String cartaAtual) {
        char valor = cartaAtual.charAt(1);
        char chosenColor = cartaAtual.charAt(0);
        if (valor == UnoProtocol.SYMBOL_WILD || valor == UnoProtocol.SYMBOL_WILD4) {
            String[] options = {"Azul", "Verde", "Vermelho", "Amarelo"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Escolha a cor:",
                    "Cor",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (choice < 0) {
                return;
            }
            chosenColor = (char) ('a' + choice);
        }

        String cartaFinal = "" + chosenColor + valor;
        if (!game.playCard(cardIndex, cartaFinal)) {
            statusLabel.setText("Jogada inválida.");
            return;
        }

        updateTopCardDisplay();
        refreshHandUi();

        sendMessage(cartaFinal);

        if (game.getHand().isEmpty()) {
            statusLabel.setText("Você venceu!");
            turnLabel.setText("VITÓRIA!");
            turnLabel.setForeground(new Color(46, 125, 50));
        } else if (game.isMyTurn()) {
            statusLabel.setText("Adversário pulado.");
        } else {
            statusLabel.setText("Vez do adversário.");
        }

        updateTurnUi();
    }

    private void drawCardAction() {
        String newCard = game.drawCard();
        if (newCard == null) {
            statusLabel.setText("Aguarde sua vez.");
            return;
        }

        handStartIndex = Math.max(0, game.getHand().size() - 7);
        refreshHandUi();

        sendMessage(UnoProtocol.ACTION_PASS);
        statusLabel.setText("Carta comprada.");

        updateTurnUi();
    }

    public boolean isBlocked(String player, String ip) {
        long now = System.currentTimeMillis();
        Long byName = blockedPeers.get(player);
        Long byIp = ip != null ? blockedPeers.get(ip) : null;
        return (byName != null && now < byName) || (byIp != null && now < byIp);
    }

    private void blockPeer(String player, String ip, long durationMs) {
        long until = System.currentTimeMillis() + durationMs;
        blockedPeers.put(player, until);
        if (ip != null) {
            blockedPeers.put(ip, until);
        }
    }

    public void handleOpponentMessage(UnoCardMessage msg) {
        String player = msg.getSenderName();
        String code = msg.getCardCode();

        if (msg.getSenderIp() != null && opponentAddress == null) {
            try {
                this.opponentAddress = InetAddress.getByName(msg.getSenderIp());
            } catch (Exception ignored) {
            }
        }

        if (code.startsWith(UnoProtocol.ACTION_CONNECT)) {
            if (isBlocked(player, msg.getSenderIp())) {
                sendMessage(UnoProtocol.ACTION_REJECT);
                return;
            }
            int choice = JOptionPane.showOptionDialog(
                    this,
                    player + " quer jogar Uno. Aceitar?",
                    "Partida de Uno",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Aceitar", "Recusar", "Bloquear (1 min)"},
                    "Aceitar"
            );
            if (choice == 0) {
                game.receiveOpponentAction(player, code);
                String seedStr = code.contains(":") ? code.substring(code.indexOf(":") + 1) : String.valueOf(System.currentTimeMillis());
                sendMessage(UnoProtocol.ACTION_ACCEPT + ":" + seedStr);
                setConnectionUiState(true);
                opponentLabel.setText("Adversário: " + player);
                statusLabel.setText("Conectado.");
            } else {
                long duration = choice == 2 ? 60000L : 5000L;
                blockPeer(player, msg.getSenderIp(), duration);
                sendMessage(UnoProtocol.ACTION_REJECT);
                opponentAddress = null;
                statusLabel.setText(choice == 2 ? "Jogador bloqueado por 1 min." : "Convite recusado.");
                return;
            }
        } else {
            game.receiveOpponentAction(player, code);
        }

        if (code.startsWith(UnoProtocol.ACTION_ACCEPT)) {
            setConnectionUiState(true);
            opponentLabel.setText("Adversário: " + player);
            statusLabel.setText("Conectado com " + player + ".");
        } else if (code.startsWith(UnoProtocol.ACTION_REJECT) || code.equals(UnoProtocol.ACTION_DISCONNECT)) {
            setConnectionUiState(false);
            opponentAddress = null;
            opponentLabel.setText("Aguardando adversário...");
            statusLabel.setText(code.startsWith(UnoProtocol.ACTION_REJECT) ? "Convite recusado." : player + " desconectou.");
            resetTableUi();
        } else if (code.toUpperCase().startsWith("PASS") || code.toUpperCase().startsWith("DRAW")) {
            statusLabel.setText(player + " comprou uma carta.");
        } else if (code.startsWith(UnoProtocol.ACTION_RESET)) {
            statusLabel.setText("Partida reiniciada.");
        } else if (code.length() == 2 && "abcdk".indexOf(code.charAt(0)) >= 0) {
            String card = code;

            if (game.isGameOver()) {
                turnLabel.setText("FIM DE JOGO: " + player + " venceu");
                turnLabel.setForeground(new Color(198, 40, 40));
                statusLabel.setText("Derrota.");
            } else {
                char valor = card.charAt(1);
                if (valor == UnoProtocol.SYMBOL_BLOCK || valor == UnoProtocol.SYMBOL_REVERSE) {
                    statusLabel.setText("Vez pulada!");
                } else if (valor == UnoProtocol.SYMBOL_PLUS2 || valor == UnoProtocol.SYMBOL_WILD4) {
                    statusLabel.setText((valor == UnoProtocol.SYMBOL_WILD4 ? "+4! Comprou 4" : "+2! Comprou 2") + " cartas.");
                    handStartIndex = Math.max(0, game.getHand().size() - 7);
                } else {
                    statusLabel.setText("Sua vez.");
                }
            }
        }

        if (game.isConnected()) {
            updateTopCardDisplay();
        }
        refreshHandUi();
        updateTurnUi();
    }

    private void resetGame(boolean broadcast) {
        long seed = System.currentTimeMillis();
        if (!game.isConnected()) {
            sendMessage(UnoProtocol.ACTION_CONNECT + ":" + seed);
            statusLabel.setText("Procurando adversário...");
            return;
        }

        game.startNewGame(seed, true);
        updateTopCardDisplay();
        refreshHandUi();

        statusLabel.setText("Partida reiniciada.");
        updateTurnUi();

        if (broadcast) {
            sendMessage(UnoProtocol.ACTION_RESET + ":" + seed);
        }
    }

    private void updateTopCardDisplay() {
        String cartaAntiga = game.getCartaAntiga();
        ImageIcon icon = getCardIcon(cartaAntiga, true);
        if (icon != null) {
            topCardDisplay.setIcon(icon);
            topCardDisplay.setText("");
        } else {
            topCardDisplay.setIcon(null);
            topCardDisplay.setText(UnoProtocol.getCardLabel(cartaAntiga));
            topCardDisplay.setForeground(cartaAntiga.charAt(0) == UnoProtocol.COLOR_YELLOW ? Color.BLACK : Color.WHITE);
        }
        topCardDisplay.setBackground(UnoProtocol.getCardColor(cartaAntiga));
    }

    private void updateTurnUi() {
        boolean active = game.isConnected() && !game.isGameOver();
        boolean myTurn = active && game.isMyTurn();

        if (!game.isConnected()) {
            turnLabel.setText("Aguardando conexão...");
            turnLabel.setForeground(new Color(230, 81, 0));
            updateOpponentCardDisplay("?");
        } else {
            int opp = game.getOpponentCards();
            updateOpponentCardDisplay(opp >= 8 ? "7+" : String.valueOf(opp));
            if (!game.isGameOver()) {
                turnLabel.setText(myTurn ? "SEU TURNO" : "TURNO DO ADVERSÁRIO");
                turnLabel.setForeground(myTurn ? new Color(46, 125, 50) : new Color(230, 81, 0));
            }
        }

        drawButton.setEnabled(myTurn);
        for (Component c : handButtonsPanel.getComponents()) {
            c.setEnabled(myTurn);
        }
        prevButton.setEnabled(handStartIndex > 0);
        nextButton.setEnabled(handStartIndex + 7 < game.getHand().size());
    }

    private void sendMessage(String code) {
        String localIp = PeerIpv4.normalize(LocalNetworkInfo.bestLocalIpv4());
        UnoCardMessage message = new UnoCardMessage(connectionManager.getUserName(), localIp, code);
        SendTarget target = opponentAddress != null ? SendTarget.singlePeer(opponentAddress) : SendTarget.broadcastAll();
        TransportMode mode = opponentAddress != null ? TransportMode.UNICAST_UDP : TransportMode.MULTICAST_UDP;
        MessageDispatcher.send(connectionManager, message, target, mode);
    }
}

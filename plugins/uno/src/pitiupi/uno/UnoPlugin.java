package pitiupi.uno;

import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.uno.GUI.UnoPanel;

import javax.swing.SwingUtilities;
import java.util.List;

public class UnoPlugin extends Plugin {
    private UnoPanel panel;
    private PluginWorkspace workspace;
    private ConnectionManager connectionManager;

    @Override
    public String getName() {
        return "Uno";
    }

    @Override
    public String getDisplayName() {
        return "Uno";
    }

    @Override
    public String getAuthor() {
        return "";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public String getIdentifier() {
        return "pitiupi.uno";
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
        this.connectionManager = connectionManager;
        this.workspace = workspace;
        this.panel = new UnoPanel(connectionManager);
        bindShellWorkspaceContent(panel);
        installPeerActions(workspace.peerActions());
    }

    @Override
    public void installPeerActions(PeerActionsRegistrar registrar) {
        registrar.register(PeerAction.of(
                "uno.peer",
                "Jogar Uno com este peer",
                PeerActionTarget.SINGLE_PEER,
                target -> {
                    workspace.openOrFocusTab(getIdentifier(), getDisplayName());
                    if (target instanceof PeerTarget.Single single) {
                        panel.connectToPeer(
                                single.peer().getDisplayName(),
                                single.peer().getInetAddress().orElse(null)
                        );
                    }
                }
        ));

        registrar.register(PeerAction.of(
                "uno.open",
                "Abrir Mesa de Uno",
                PeerActionTarget.ALL_PEERS,
                target -> workspace.openOrFocusTab(getIdentifier(), getDisplayName())
        ));
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(UnoCardMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof UnoCardMessage cardMessage && panel != null) {
            if (connectionManager.getUserName().equals(cardMessage.getSenderName())) {
                return;
            }
            if (cardMessage.getCardCode() != null && cardMessage.getCardCode().startsWith(UnoProtocol.ACTION_CONNECT)) {
                if (!panel.isBlocked(cardMessage.getSenderName(), cardMessage.getSenderIp())) {
                    SwingUtilities.invokeLater(() -> workspace.openOrFocusTab(getIdentifier(), getDisplayName()));
                }
            }
            SwingUtilities.invokeLater(() -> panel.handleOpponentMessage(cardMessage));
        }
    }
}

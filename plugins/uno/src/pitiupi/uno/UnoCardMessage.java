package pitiupi.uno;

import pitiupi.models.Messages.Message;

public class UnoCardMessage extends Message {
    private String senderName;
    private String senderIp;
    private String cardCode;

    public UnoCardMessage() {
    }

    public UnoCardMessage(String senderName, String senderIp, String cardCode) {
        this.senderName = senderName;
        this.senderIp = senderIp;
        this.cardCode = cardCode;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public String getCardCode() {
        return cardCode;
    }
}

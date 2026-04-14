package com.dalegames.highlowjack.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single chat message sent by a player during a multiplayer game.
 *
 * <p>Types:
 * <ul>
 *   <li>TEXT    — free-text (up to MAX_CHAT_LENGTH characters)</li>
 *   <li>REACTION — single emoji reaction (sent instantly)</li>
 *   <li>PRESET  — a pre-defined canned phrase chosen from a list</li>
 * </ul>
 *
 * @author Dale &amp; Primus
 * @version 1.0
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { TEXT, REACTION, PRESET }

    private final String playerName;
    private final String text;
    private final Type   type;
    private final long   epochSecond;   // seconds since epoch, for lightweight timestamp display
    private final String recipient;     // null = broadcast to all; player name = DM

    /** Broadcast constructor (recipient = all). */
    public ChatMessage(String playerName, String text, Type type) {
        this(playerName, text, type, null);
    }

    /** Targeted constructor. */
    public ChatMessage(String playerName, String text, Type type, String recipient) {
        this.playerName  = playerName;
        this.text        = text;
        this.type        = type;
        this.recipient   = recipient;
        this.epochSecond = Instant.now().getEpochSecond();
    }

    public String getPlayerName() { return playerName; }
    public String getText()       { return text; }
    public Type   getType()       { return type; }
    public long   getEpochSecond(){ return epochSecond; }
    public String getRecipient()  { return recipient; }

    /**
     * Returns a plain Map representation suitable for JSON serialisation in the poll response.
     * Keeps the response format independent of Jackson configuration.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("player",    playerName);
        m.put("text",      text);
        m.put("type",      type.name());
        m.put("ts",        epochSecond);
        m.put("recipient", recipient);   // null serialises as JSON null
        return m;
    }
}

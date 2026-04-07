package com.dalegames.highlowjack.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Personality quip entity - contextual commentary for players.
 * 
 * @author Dale & Primus
 * @version 1.0
 */
@Entity
@Table(name = "personality_quips")
public class PersonalityQuip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "player_name", length = 50)
    private String playerName;  // NULL = generic quip
    
    @Column(name = "trigger_context", nullable = false, length = 100)
    private String triggerContext;
    
    @Column(name = "quip_text", nullable = false, columnDefinition = "TEXT")
    private String quipText;
    
    @Column(name = "category", length = 50)
    private String category;  // CELEBRATION, TAUNT, SIGNATURE, etc.
    
    @Column(name = "tone", length = 20)
    private String tone = "NEUTRAL";  // POSITIVE, NEGATIVE, NEUTRAL
    
    @Column(name = "times_used")
    private int timesUsed = 0;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    // Constructors
    public PersonalityQuip() {}
    
    public PersonalityQuip(String playerName, String triggerContext, String quipText, String category) {
        this.playerName = playerName;
        this.triggerContext = triggerContext;
        this.quipText = quipText;
        this.category = category;
    }
    
    // Business method
    public void recordUsage() {
        this.timesUsed++;
        this.lastUsed = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getTriggerContext() {
        return triggerContext;
    }
    
    public void setTriggerContext(String triggerContext) {
        this.triggerContext = triggerContext;
    }
    
    public String getQuipText() {
        return quipText;
    }
    
    public void setQuipText(String quipText) {
        this.quipText = quipText;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTone() {
        return tone;
    }
    
    public void setTone(String tone) {
        this.tone = tone;
    }
    
    public int getTimesUsed() {
        return timesUsed;
    }
    
    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }
    
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "PersonalityQuip{" +
                "playerName='" + playerName + '\'' +
                ", trigger='" + triggerContext + '\'' +
                ", quip='" + quipText + '\'' +
                ", used=" + timesUsed +
                '}';
    }
}

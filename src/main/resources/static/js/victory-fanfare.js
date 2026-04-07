/**
 * Victory Fanfare Synthesizer
 * Creates an epic trumpet-style fanfare using Web Audio API
 * No external audio files needed!
 * 
 * @author Dale & Primus
 * @version 1.0 - Epic victory celebration
 */

class VictoryFanfare {
    constructor() {
        this.audioContext = null;
        this.isPlaying = false;
    }
    
    /**
     * Play the epic victory fanfare!
     */
    play() {
        if (this.isPlaying) return;
        
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
        this.isPlaying = true;
        
        // The fanfare sequence - a classic victory pattern!
        // Pattern: Ta-da-da-DAAA! (Like Olympic medals)
        
        const now = this.audioContext.currentTime;
        
        // First note: C5 (short, bright)
        this.playNote(523.25, now, 0.15, 0.6);
        
        // Second note: E5 (short, rising)
        this.playNote(659.25, now + 0.2, 0.15, 0.6);
        
        // Third note: G5 (short, higher)
        this.playNote(783.99, now + 0.4, 0.15, 0.6);
        
        // FINAL NOTE: C6 (long, triumphant!)
        this.playNote(1046.50, now + 0.6, 1.2, 0.8);
        
        // Add harmonics for richness
        this.playNote(1046.50 * 2, now + 0.6, 1.2, 0.3); // Octave above
        this.playNote(1046.50 * 3, now + 0.6, 1.2, 0.15); // Harmonic
        
        // Timpani hit for impact!
        this.playTimpani(now + 0.6);
        
        // Cymbal crash for celebration!
        this.playCymbalCrash(now + 0.6);
        
        // Reset after fanfare completes
        setTimeout(() => {
            this.isPlaying = false;
            if (this.audioContext) {
                this.audioContext.close();
            }
        }, 2000);
    }
    
    /**
     * Play a single trumpet-like note
     */
    playNote(frequency, startTime, duration, volume) {
        const ctx = this.audioContext;
        
        // Create oscillators for trumpet-like sound (bright, brassy)
        const osc1 = ctx.createOscillator();
        const osc2 = ctx.createOscillator();
        const osc3 = ctx.createOscillator();
        
        // Main frequency
        osc1.type = 'sawtooth';
        osc1.frequency.setValueAtTime(frequency, startTime);
        
        // Slight detune for richness
        osc2.type = 'sawtooth';
        osc2.frequency.setValueAtTime(frequency * 1.01, startTime);
        
        // Harmonic for brightness
        osc3.type = 'triangle';
        osc3.frequency.setValueAtTime(frequency * 2, startTime);
        
        // Create gain nodes for volume control
        const gain1 = ctx.createGain();
        const gain2 = ctx.createGain();
        const gain3 = ctx.createGain();
        
        // Master gain
        const masterGain = ctx.createGain();
        
        // Envelope (trumpet attack/decay)
        const attackTime = 0.05;
        const releaseTime = 0.1;
        
        masterGain.gain.setValueAtTime(0, startTime);
        masterGain.gain.linearRampToValueAtTime(volume, startTime + attackTime);
        masterGain.gain.setValueAtTime(volume, startTime + duration - releaseTime);
        masterGain.gain.linearRampToValueAtTime(0, startTime + duration);
        
        // Balance oscillators
        gain1.gain.value = 0.5;
        gain2.gain.value = 0.3;
        gain3.gain.value = 0.2;
        
        // Filter for warmth (remove harsh highs)
        const filter = ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.setValueAtTime(3000, startTime);
        filter.Q.value = 1;
        
        // Connect everything
        osc1.connect(gain1);
        osc2.connect(gain2);
        osc3.connect(gain3);
        
        gain1.connect(masterGain);
        gain2.connect(masterGain);
        gain3.connect(masterGain);
        
        masterGain.connect(filter);
        filter.connect(ctx.destination);
        
        // Start and stop
        osc1.start(startTime);
        osc2.start(startTime);
        osc3.start(startTime);
        
        osc1.stop(startTime + duration);
        osc2.stop(startTime + duration);
        osc3.stop(startTime + duration);
    }
    
    /**
     * Play timpani drum hit for impact
     */
    playTimpani(startTime) {
        const ctx = this.audioContext;
        
        // Low frequency oscillator for drum
        const osc = ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(80, startTime);
        osc.frequency.exponentialRampToValueAtTime(40, startTime + 0.5);
        
        const gain = ctx.createGain();
        gain.gain.setValueAtTime(0.6, startTime);
        gain.gain.exponentialRampToValueAtTime(0.01, startTime + 0.5);
        
        osc.connect(gain);
        gain.connect(ctx.destination);
        
        osc.start(startTime);
        osc.stop(startTime + 0.5);
    }
    
    /**
     * Play cymbal crash for celebration
     */
    playCymbalCrash(startTime) {
        const ctx = this.audioContext;
        const duration = 1.5;
        
        // Multiple high-frequency oscillators for metallic sound
        for (let i = 0; i < 10; i++) {
            const osc = ctx.createOscillator();
            osc.type = 'square';
            
            // Random high frequencies for cymbal shimmer
            const freq = 2000 + Math.random() * 3000;
            osc.frequency.setValueAtTime(freq, startTime);
            
            const gain = ctx.createGain();
            gain.gain.setValueAtTime(0.1, startTime);
            gain.gain.exponentialRampToValueAtTime(0.01, startTime + duration);
            
            // High-pass filter for brightness
            const filter = ctx.createBiquadFilter();
            filter.type = 'highpass';
            filter.frequency.value = 3000;
            
            osc.connect(filter);
            filter.connect(gain);
            gain.connect(ctx.destination);
            
            osc.start(startTime);
            osc.stop(startTime + duration);
        }
    }
}

// Global fanfare instance
const victoryFanfare = new VictoryFanfare();

/**
 * Play the victory fanfare (call this from HTML)
 */
function playVictoryFanfare() {
    victoryFanfare.play();
}

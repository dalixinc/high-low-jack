package com.dalegames.highlowjack.web;

import com.dalegames.highlowjack.model.Game;
import com.dalegames.highlowjack.model.GameSetup;
import com.dalegames.highlowjack.model.PlayerInfo;
import com.dalegames.highlowjack.multiplayer.GameRegistry;
import com.dalegames.highlowjack.multiplayer.MultiplayerGame;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for multiplayer game functionality.
 * 
 * @author Dale & Primus
 * @version 1.2 - Fixed /host to accept form data directly
 */
@Controller
@RequestMapping("/highlowjack/multiplayer")
public class MultiplayerController {
    
    @Autowired
    private GameRegistry gameRegistry;
    
    /**
     * Host creates a new multiplayer game.
     */
    @PostMapping("/host")
    public String hostGame(
            @RequestParam String gameMode,
            @RequestParam String matchType,
            @RequestParam(required = false) String player1Name,
            @RequestParam(required = false) String player1Type,
            @RequestParam(required = false) String player2Name,
            @RequestParam(required = false) String player2Type,
            @RequestParam(required = false) String player3Name,
            @RequestParam(required = false) String player3Type,
            @RequestParam(required = false) String player4Name,
            @RequestParam(required = false) String player4Type,
            @RequestParam(required = false) String team1Name,
            @RequestParam(required = false) String team2Name,
            HttpSession session,
            Model model) {
        
        System.out.println("🎮 HOST ENDPOINT HIT!");
        System.out.println("🎮 Game Mode: " + gameMode);
        System.out.println("🎮 Match Type: " + matchType);
        
        try {
            // Build setup from form data
            GameSetup setup = buildSetupFromForm(
                gameMode, matchType,
                player1Name, player1Type,
                player2Name, player2Type,
                player3Name, player3Type,
                player4Name, player4Type,
                team1Name, team2Name
            );
            
            System.out.println("✅ Setup created from form data");
            
            // Save setup to session for later use
            session.setAttribute("hlj_setup", setup);
            
            // Create new game
            Game game = new Game(setup);
            game.dealCards();
            
            // Create and register multiplayer game
            MultiplayerGame mpGame = gameRegistry.createGame(game, setup);
            String joinCode = mpGame.getJoinCode();
            
            // Host automatically joins as position 0 (North)
            String hostName = getHostPlayerName(setup);
            String token = mpGame.joinPlayer(0, hostName);
            
            // Store multiplayer session info
            session.setAttribute("mp_token", token);
            session.setAttribute("mp_code", joinCode);
            session.setAttribute("mp_position", 0);
            session.setAttribute("mp_playerName", hostName);
            
            System.out.println("🎮 Hosting new game: " + joinCode + " (host: " + hostName + ")");
            
            return "redirect:/highlowjack/multiplayer/lobby";
            
        } catch (Exception e) {
            System.err.println("❌ Error creating multiplayer game: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to create game: " + e.getMessage());
            return "highlowjack/setup";
        }
    }
    
    /**
     * Builds a GameSetup from form parameters.
     */
    private GameSetup buildSetupFromForm(
            String gameMode, String matchType,
            String p1Name, String p1Type,
            String p2Name, String p2Type,
            String p3Name, String p3Type,
            String p4Name, String p4Type,
            String team1Name, String team2Name) {
        
        // Parse match type
        GameSetup.MatchType match = GameSetup.MatchType.valueOf(matchType);
        
        // Build player list (Player 1 is always controller)
        List<PlayerInfo> players = new ArrayList<>();
        players.add(new PlayerInfo(p1Name, 
            "HUMAN".equals(p1Type) ? PlayerInfo.PlayerType.HUMAN : PlayerInfo.PlayerType.COMPUTER,
            true));  // Player 1 is controller
        players.add(new PlayerInfo(p2Name,
            "HUMAN".equals(p2Type) ? PlayerInfo.PlayerType.HUMAN : PlayerInfo.PlayerType.COMPUTER,
            false));
        players.add(new PlayerInfo(p3Name,
            "HUMAN".equals(p3Type) ? PlayerInfo.PlayerType.HUMAN : PlayerInfo.PlayerType.COMPUTER,
            false));
        players.add(new PlayerInfo(p4Name,
            "HUMAN".equals(p4Type) ? PlayerInfo.PlayerType.HUMAN : PlayerInfo.PlayerType.COMPUTER,
            false));
        
        // Create setup based on mode
        if ("TEAM".equals(gameMode)) {
            if (team1Name != null && team2Name != null && !team1Name.isEmpty() && !team2Name.isEmpty()) {
                return GameSetup.createTeam(players, match, team1Name, team2Name);
            } else {
                return GameSetup.createTeam(players, match);
            }
        } else {
            return GameSetup.createIndividual(players, match);
        }
    }
    
    /**
     * Helper to get the host's player name from setup.
     */
    private String getHostPlayerName(GameSetup setup) {
        // Get first human player (not AI)
        for (PlayerInfo player : setup.getPlayers()) {
            if (player.isHuman()) {
                return player.getName();
            }
        }
        
        // Fallback: return first player
        return setup.getPlayers().get(0).getName();
    }
    
    /**
     * Shows the join page where players enter a game code.
     */
    @GetMapping("/join")
    public String showJoinPage(@RequestParam(required = false) String code, Model model) {
        if (code != null) {
            MultiplayerGame mpGame = gameRegistry.getGame(code);
            if (mpGame != null) {
                model.addAttribute("mpGame", mpGame);
                model.addAttribute("joinCode", code);
            } else {
                model.addAttribute("error", "Invalid game code: " + code);
            }
        }
        return "highlowjack/multiplayer-join";
    }
    
    /**
     * Process join request.
     */
    @PostMapping("/join")
    public String joinGame(
            @RequestParam String joinCode,
            @RequestParam String playerName,
            @RequestParam int position,
            HttpSession session,
            Model model) {
        
        MultiplayerGame mpGame = gameRegistry.getGame(joinCode);
        
        if (mpGame == null) {
            model.addAttribute("error", "Invalid game code");
            return "highlowjack/multiplayer-join";
        }
        
        if (mpGame.isPositionTaken(position)) {
            model.addAttribute("error", "Position already taken");
            model.addAttribute("mpGame", mpGame);
            return "highlowjack/multiplayer-join";
        }
        
        try {
            String token = mpGame.joinPlayer(position, playerName);
            
            // Store multiplayer session info
            session.setAttribute("mp_token", token);
            session.setAttribute("mp_code", joinCode);
            session.setAttribute("mp_position", position);
            session.setAttribute("mp_playerName", playerName);
            
            return "redirect:/highlowjack/multiplayer/lobby";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to join: " + e.getMessage());
            model.addAttribute("mpGame", mpGame);
            return "highlowjack/multiplayer-join";
        }
    }
    
    /**
     * Shows the lobby where players wait for game to start.
     */
    @GetMapping("/lobby")
    public String showLobby(HttpSession session, Model model) {
        String code = (String) session.getAttribute("mp_code");
        Integer position = (Integer) session.getAttribute("mp_position");
        
        if (code == null || position == null) {
            return "redirect:/highlowjack/multiplayer/join";
        }
        
        MultiplayerGame mpGame = gameRegistry.getGame(code);
        
        if (mpGame == null) {
            return "redirect:/highlowjack/multiplayer/join";
        }
        
        model.addAttribute("mpGame", mpGame);
        model.addAttribute("myPosition", position);
        
        return "highlowjack/multiplayer-lobby";
    }
    
    /**
     * Polling endpoint for lobby updates.
     * Returns JSON with state version.
     */
    @GetMapping("/poll")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> pollGameState(
            @RequestParam String code,
            @RequestParam int lastVersion) {
        
        MultiplayerGame mpGame = gameRegistry.getGame(code);
        
        Map<String, Object> response = new HashMap<>();
        
        if (mpGame == null) {
            response.put("error", "Game not found");
            return ResponseEntity.ok(response);
        }
        
        int currentVersion = mpGame.getStateVersion();
        response.put("version", currentVersion);
        response.put("updated", currentVersion > lastVersion);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Host starts the game (position 0 only).
     */
    @PostMapping("/start")
    public String startGame(HttpSession session, Model model) {
        String code = (String) session.getAttribute("mp_code");
        Integer position = (Integer) session.getAttribute("mp_position");
        String token = (String) session.getAttribute("mp_token");
        
        if (code == null || position == null || position != 0) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        MultiplayerGame mpGame = gameRegistry.getGame(code);
        
        if (mpGame == null || !mpGame.isFullyPopulated()) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        // Verify this is the host
        if (!mpGame.isValidPlayer(0, token)) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        // Transfer multiplayer game to regular session
        session.setAttribute("hlj_game", mpGame.getGame());
        session.setAttribute("hlj_setup", mpGame.getSetup());
        
        // Clean up multiplayer session
        session.removeAttribute("mp_token");
        session.removeAttribute("mp_code");
        session.removeAttribute("mp_position");
        session.removeAttribute("mp_playerName");
        
        System.out.println("🚀 Starting multiplayer game: " + code);
        
        return "redirect:/highlowjack";
    }
}

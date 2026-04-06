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
 * @version 1.3 - Added auto-AI player addition
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
            // Individual mode params
            @RequestParam(required = false) String player1Name,
            @RequestParam(required = false) String player1Type,
            @RequestParam(required = false) String player2Name,
            @RequestParam(required = false) String player2Type,
            @RequestParam(required = false) String player3Name,
            @RequestParam(required = false) String player3Type,
            @RequestParam(required = false) String player4Name,
            @RequestParam(required = false) String player4Type,
            // Team mode params
            @RequestParam(required = false) String player1NameTeam,
            @RequestParam(required = false) String player1TypeTeam,
            @RequestParam(required = false) String player2NameTeam,
            @RequestParam(required = false) String player2TypeTeam,
            @RequestParam(required = false) String player3NameTeam,
            @RequestParam(required = false) String player3TypeTeam,
            @RequestParam(required = false) String player4NameTeam,
            @RequestParam(required = false) String player4TypeTeam,
            @RequestParam(required = false) String team1Name,
            @RequestParam(required = false) String team2Name,
            HttpSession session,
            Model model) {

        System.out.println("🎮 HOST ENDPOINT HIT!");
        System.out.println("🎮 Game Mode: " + gameMode);
        System.out.println("🎮 Match Type: " + matchType);

        boolean isTeamMode = "TEAM".equals(gameMode);

        try {
            // Build setup from form data — use team params when in team mode
            GameSetup setup = buildSetupFromForm(
                gameMode, matchType,
                isTeamMode ? player1NameTeam : player1Name, isTeamMode ? player1TypeTeam : player1Type,
                isTeamMode ? player2NameTeam : player2Name, isTeamMode ? player2TypeTeam : player2Type,
                isTeamMode ? player3NameTeam : player3Name, isTeamMode ? player3TypeTeam : player3Type,
                isTeamMode ? player4NameTeam : player4Name, isTeamMode ? player4TypeTeam : player4Type,
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
            int hostPosition = getHostPosition(setup);
            String token = mpGame.joinPlayer(hostPosition, hostName);
            
            System.out.println("🎮 Host joined: " + hostName + " at position " + hostPosition);
            
            // AUTO-ADD AI PLAYERS to their positions
            autoAddComputerPlayers(mpGame, setup);
            
            // Store multiplayer session info
            session.setAttribute("mp_token", token);
            session.setAttribute("mp_code", joinCode);
            session.setAttribute("mp_position", hostPosition);
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
     * Automatically adds computer players to the game.
     */
    private void autoAddComputerPlayers(MultiplayerGame mpGame, GameSetup setup) {
        List<PlayerInfo> players = setup.getPlayers();
        
        for (int i = 0; i < players.size(); i++) {
            PlayerInfo player = players.get(i);
            
            // Skip if position already taken
            if (mpGame.isPositionTaken(i)) {
                continue;
            }
            
            // If this is a computer player, auto-add them
            if (player.isComputer()) {
                String aiToken = mpGame.joinPlayer(i, player.getName());
                System.out.println("🤖 Auto-added AI player: " + player.getName() + " at position " + i);
            }
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
     * Helper to get the host's position (0-3).
     */
    private int getHostPosition(GameSetup setup) {
        // Find position of first human player
        List<PlayerInfo> players = setup.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isHuman()) {
                return i;
            }
        }
        
        // Fallback: position 0
        return 0;
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
                model.addAttribute("setup", mpGame.getSetup());  // For AI badge display
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

            // If a human player joined with a different name than the slot's setup name,
            // rename them in the Game object so hand/score lookups use the new name.
            PlayerInfo playerInfo = mpGame.getSetup().getPlayers().get(position);
            if (playerInfo.isHuman() && !playerInfo.getName().equals(playerName)) {
                mpGame.getGame().renamePlayer(position, playerName);
            }

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
        model.addAttribute("setup", mpGame.getSetup());  // Add setup for AI detection
        
        return "highlowjack/multiplayer-lobby";
    }
    
    /**
     * Polling endpoint for lobby updates.
     * Returns JSON with state version and game started status.
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
        response.put("gameStarted", mpGame.isGameStarted());  // For detecting game start
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Host starts the game (any valid player).
     */
    @PostMapping("/start")
    public String startGame(HttpSession session, Model model) {
        String code = (String) session.getAttribute("mp_code");
        Integer position = (Integer) session.getAttribute("mp_position");
        String token = (String) session.getAttribute("mp_token");
        
        if (code == null || position == null) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        MultiplayerGame mpGame = gameRegistry.getGame(code);
        
        if (mpGame == null || !mpGame.isFullyPopulated()) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        // Verify this is a valid player (not just position 0)
        if (!mpGame.isValidPlayer(position, token)) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        // MARK GAME AS STARTED (triggers polling for other players)
        mpGame.startGame();
        
        // Transfer multiplayer game to regular session
        session.setAttribute("hlj_game", mpGame.getGame());
        session.setAttribute("hlj_setup", mpGame.getSetup());
        
     // CRITICAL: Store which player THIS session is!
        String playerName = mpGame.getPlayers().get(position).getPlayerName();
        session.setAttribute("hlj_playerName", playerName);  // ADD THIS LINE
        
        // Clean up multiplayer session
        session.removeAttribute("mp_token");
        session.removeAttribute("mp_code");
        session.removeAttribute("mp_position");
        session.removeAttribute("mp_playerName");
        
        System.out.println("🚀 Host starting multiplayer game: " + code);
        
        return "redirect:/highlowjack";
    }
    
    /**
     * Non-host players call this when game starts (detected by polling).
     * Transfers game to their session and redirects to game.
     */
    @PostMapping("/start-player")
    public String startPlayer(HttpSession session) {
        String code = (String) session.getAttribute("mp_code");
        Integer position = (Integer) session.getAttribute("mp_position");
        String token = (String) session.getAttribute("mp_token");
        
        if (code == null || position == null || token == null) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        MultiplayerGame mpGame = gameRegistry.getGame(code);
        
        if (mpGame == null || !mpGame.isGameStarted()) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        // Verify this is a valid player
        if (!mpGame.isValidPlayer(position, token)) {
            return "redirect:/highlowjack/multiplayer/lobby";
        }
        
        // Transfer game to this player's session
        session.setAttribute("hlj_game", mpGame.getGame());
        session.setAttribute("hlj_setup", mpGame.getSetup());

        // Use the name from the GameSetup (which matches the Game object's player names),
        // not the name the joining player typed in the join form.
        String playerName = mpGame.getSetup().getPlayers().get(position).getName();
        session.setAttribute("hlj_playerName", playerName);
        
        // Clean up multiplayer session
        session.removeAttribute("mp_token");
        session.removeAttribute("mp_code");
        session.removeAttribute("mp_position");
        session.removeAttribute("mp_playerName");
        
        System.out.println("🚀 Player joined game: position " + position);
        
        return "redirect:/highlowjack";
    }
}  
    /**
     * Non-host players enter the game after host starts.
     * Called by lobby polling when gameStarted is detected.
     */
//    @PostMapping("/start-player")
//    public String startPlayer(HttpSession session) {
//        String code = (String) session.getAttribute("mp_code");
//        
//        if (code == null) {
//            return "redirect:/highlowjack/setup";
//        }
//        
//        MultiplayerGame mpGame = gameRegistry.getGame(code);
//        
//        if (mpGame != null && mpGame.isGameStarted()) {
//            // Transfer game to this player's session
//            session.setAttribute("hlj_game", mpGame.getGame());
//            session.setAttribute("hlj_setup", mpGame.getSetup());
//            
//            // Clean up multiplayer session
//            session.removeAttribute("mp_token");
//            session.removeAttribute("mp_code");
//            session.removeAttribute("mp_position");
//            session.removeAttribute("mp_playerName");
//            
//            System.out.println("🎮 Player entering started game: " + code);
//        }
//        
//        return "redirect:/highlowjack";
//    }
///}

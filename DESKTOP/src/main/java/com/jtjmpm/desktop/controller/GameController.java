package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.desktop.utils.AnimationEngine;
import com.jtjmpm.desktop.utils.GameStateUtils;
import com.jtjmpm.desktop.utils.ShapeDrawer;
import com.jtjmpm.desktop.utils.ViewLoader;
import com.jtjmpm.*;
import com.jtjmpm.messages.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import com.jtjmpm.desktop.model.GameStateManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class GameController {
    public static final String LOBBY_VIEW = "/com/jtjmpm/desktop/lobby-view.fxml";

    @FXML private AnchorPane mainPane;
    @FXML private PlayerPanelController hostPanelController;
    @FXML private PlayerPanelController enemyPanelController;

    @FXML private ImageView hostWizardImage;
    @FXML private ImageView hostEffectImage;
    @FXML private ImageView enemyWizardImage;
    @FXML private ImageView enemyEffectImage;

    @FXML private Label lobbyInfoLabel;
    @FXML private Label spell1Label;
    @FXML private Label spell2Label;
    @FXML private Label spell3Label;
    @FXML private Label spell4Label;

    @FXML private Canvas patternCanvas;
    @FXML private Canvas gestureCanvas;

    private final Gson gson = new Gson();

    private GameStateDTO gameState;
    private List<String> myLoadout;

    private AnimationEngine animationEngine;

    private double hostHp = -1.0;
    private double enemyHp = -1.0;

    private boolean isGameOver = false;

    @FXML
    public void initialize() {
        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);

        enemyWizardImage.setScaleX(-1.0);

        animationEngine = new AnimationEngine(mainPane, hostWizardImage, hostEffectImage,
                enemyWizardImage, enemyEffectImage);
        animationEngine.startIdleTimelines();

        myLoadout = GameStateManager.getInstance().getCurrentLoadout();
        if (myLoadout != null && myLoadout.size() == 4) {
            spell1Label.setText("1  " + myLoadout.get(0));
            spell2Label.setText("2  " + myLoadout.get(1));
            spell3Label.setText("3  " + myLoadout.get(2));
            spell4Label.setText("4  " + myLoadout.get(3));
        }

        ShapeDrawer.clearCanvas(patternCanvas);
        ShapeDrawer.clearCanvas(gestureCanvas);

        List<Point2D.Double> pending = GameStateManager.getInstance().getPendingPatternPoints();
        if (pending != null) {
            ShapeDrawer.drawMove(patternCanvas, pending, Color.GOLD);
            GameStateManager.getInstance().clearPendingPattern();
        }
    }

    // -------------------------------------------------------------------------
    // Message routing
    // -------------------------------------------------------------------------

    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case MessageType.MOVE_RESULT:
                Platform.runLater(() -> handleMoveResult(gson.fromJson(message, MoveResultMessage.class)));
                break;
            case MessageType.GAME_STATE_UPDATE:
                Platform.runLater(() -> handleGameStateUpdate(gson.fromJson(message, GameStateUpdateMessage.class)));
                break;
            case MessageType.GAME_OVER:
                Platform.runLater(() -> handleGameOver(gson.fromJson(message, GameOverMessage.class)));
                break;
            case MessageType.SHAPE_DRAWN:
                Platform.runLater(() -> handleShapeDrawn(gson.fromJson(message, ShapeMessage.class)));
                break;
            case MessageType.ROUND_OVER:
                Platform.runLater(() -> handleRoundOver(gson.fromJson(message, RoundOverMessage.class)));
                break;
            case MessageType.COUNTDOWN:
                Platform.runLater(() -> handleCountdown(gson.fromJson(message, CountdownMessage.class)));
                break;
            case MessageType.ROUND_START:
                Platform.runLater(() -> handleRoundStart(gson.fromJson(message, RoundStartMessage.class)));
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    // -------------------------------------------------------------------------
    // Spell cast result
    // -------------------------------------------------------------------------

    private void handleMoveResult(MoveResultMessage message) {
        String hostId = GameStateManager.getInstance().getHostId();
        if (hostId == null) {
            System.err.println("Local player id is not set");
            return;
        }

        boolean isHost = message.playerId.equals(hostId);

        // Draw the gesture for the local player's cast.
        if (isHost && message.result != null
                && message.result.points != null
                && !message.result.points.isEmpty()) {
            ShapeDrawer.drawMove(gestureCanvas, message.result.points, Color.AQUA);
        }

        if (message.status != CastStatus.SUCCESS) {
            if (isHost) {
                String failText = message.status == CastStatus.FAILED_MANA ? "No mana" : "Missed";
                animationEngine.showCenteredText(failText, Color.web("#FF6B6B"));
            } else {
                String statusText = switch (message.status) {
                    case FAILED_MANA     -> "No mana";
                    case FAILED_ACCURACY -> "Missed";
                    case FAILED_DEATH    -> "Dead";
                    default              -> "Failed";
                };
                animationEngine.showFloatingText(statusText, Color.YELLOW, false);
            }
            return;
        }

        // Accuracy rating for successful host cast.
        if (isHost) {
            String rating = message.accuracyRating != null
                    ? message.accuracyRating
                    : (message.result != null ? message.result.getAccuracyRating() : null);
            if (rating != null) {
                Color ratingColor = switch (rating) {
                    case "AMAZING" -> Color.DEEPSKYBLUE;
                    case "GOOD"    -> Color.LIMEGREEN;
                    case "DECENT"  -> Color.YELLOW;
                    default        -> Color.RED;
                };
                animationEngine.showCenteredText(rating, ratingColor);
            }
        }

        int fixedAttackTime = 1000;
        int projTime = Math.max(50, message.castDurationMs - fixedAttackTime);
        animationEngine.playAttack(isHost, fixedAttackTime);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(fixedAttackTime),
                        ae -> animationEngine.playProjectile(isHost, projTime, message.spellId)),
                new KeyFrame(Duration.millis(message.castDurationMs),
                        ae -> animationEngine.playEffect(!isHost, message.spellId)));
        timeline.play();
    }

    // -------------------------------------------------------------------------
    // Pattern updates
    // -------------------------------------------------------------------------

    private void handleShapeDrawn(ShapeMessage message) {
        ShapeDrawer.drawMove(patternCanvas, message.points, Color.GOLD);
    }

    // -------------------------------------------------------------------------
    // Game state / HP updates
    // -------------------------------------------------------------------------

    private void handleGameStateUpdate(GameStateUpdateMessage message) {
        gameState = message.gameState;

        String hostId = GameStateManager.getInstance().getHostId();

        PlayerDTO hostPlayer = gameState.players().get(hostId);
        PlayerDTO enemyPlayer = Objects.requireNonNull(GameStateUtils.getEnemy(gameState.players().values(), hostId));

        if (hostHp < 0) hostHp = hostPlayer.maxHp();
        if (enemyHp < 0) enemyHp = enemyPlayer.maxHp();

        if (hostPlayer.hp() < hostHp && hostPlayer.hp() > 0) {
            animationEngine.playHit(true);
        } else if (hostPlayer.hp() <= 0 && hostHp > 0) {
            animationEngine.playDeath(true);
        }
        hostHp = hostPlayer.hp();

        if (enemyPlayer.hp() < enemyHp && enemyPlayer.hp() > 0) {
            animationEngine.playHit(false);
        } else if (enemyPlayer.hp() <= 0 && enemyHp > 0) {
            animationEngine.playDeath(false);
        }
        enemyHp = enemyPlayer.hp();

        hostPanelController.playerStateUpdate(hostPlayer);
        enemyPanelController.playerStateUpdate(enemyPlayer);

        if (message.events != null) {
            for (CombatEventMessage event : message.events) {
                boolean onHost = event.targetId.equals(hostId);
                Color color = Color.YELLOW;
                String text;

                if (event.combatType == CombatEventType.HIT) {
                    color = Color.RED;
                    text = "-" + (int) event.value;
                } else if (event.combatType == CombatEventType.HEAL) {
                    color = Color.LIMEGREEN;
                    text = "+" + (int) event.value;
                } else {
                    text = switch (event.combatType) {
                        case BLOCKED        -> "Blocked";
                        case STATUS_APPLIED -> "Status applied";
                        default             -> event.combatType.name();
                    };
                }

                animationEngine.showFloatingText(text, color, onHost);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Round system
    // -------------------------------------------------------------------------

    private void handleRoundOver(RoundOverMessage message) {
        String hostId = GameStateManager.getInstance().getHostId();
        if (message.roundWinnerId != null) {
            boolean hostWon = message.roundWinnerId.equals(hostId);
            animationEngine.showCenteredText(
                    hostWon ? "Round Won!" : "Round Lost!",
                    hostWon ? Color.GOLD : Color.web("#FF6B6B"));
        }
        // Death animation was already triggered by the GAME_STATE_UPDATE that preceded this.
        // The wins panel update is also reflected in that GAME_STATE_UPDATE.
    }

    private void handleCountdown(CountdownMessage message) {
        if (message.count == 0) {
            animationEngine.showCenteredText("Battle!", Color.GOLD);
        } else {
            animationEngine.showCountdownNumber(String.valueOf(message.count));
        }
    }

    private void handleRoundStart(RoundStartMessage message) {
        // Restore wizards to idle (they were "dead" last round).
        animationEngine.resetForNewRound();

        // Clear gesture canvas; draw the new round's pattern.
        ShapeDrawer.clearCanvas(gestureCanvas);
        ShapeDrawer.clearCanvas(patternCanvas);
        if (message.initialPattern != null) {
            ShapeDrawer.drawMove(patternCanvas, message.initialPattern, Color.GOLD);
        }
    }

    // -------------------------------------------------------------------------
    // Game over (match end)
    // -------------------------------------------------------------------------

    private void handleGameOver(GameOverMessage message) {
        isGameOver = true;

        String hostId = GameStateManager.getInstance().getHostId();
        String text;
        if (message.reason == GameOverReason.DRAW) {
            text = "GAME OVER\nDRAW!";
        } else if (message.winnerId != null && message.winnerId.equals(hostId)) {
            text = "GAME OVER\nYOU WON!";
        } else {
            text = "GAME OVER\nYOU LOST!";
        }

        Label gameOverLabel = new Label(text);
        gameOverLabel.getStyleClass().add("game-over-label");

        AnchorPane.setTopAnchor(gameOverLabel, 0.0);
        AnchorPane.setBottomAnchor(gameOverLabel, 0.0);
        AnchorPane.setLeftAnchor(gameOverLabel, 0.0);
        AnchorPane.setRightAnchor(gameOverLabel, 0.0);

        mainPane.getChildren().add(gameOverLabel);
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    @FXML
    private void onBackToMenu() {
        try {
            ApiSocketClient.getInstance().setOnMessageCallback(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOBBY_VIEW));
            Scene scene = ViewLoader.loadScaledScene(loader);
            Stage stage = (Stage) lobbyInfoLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

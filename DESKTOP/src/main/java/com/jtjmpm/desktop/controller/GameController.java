package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.desktop.utils.AnimationEngine;
import com.jtjmpm.desktop.utils.GameStateUtils;
import com.jtjmpm.desktop.utils.ViewLoader;
import com.jtjmpm.messages.*;
import com.jtjmpm.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import com.jtjmpm.desktop.model.GameStateManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
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
    @FXML private ImageView projectileImage;

    @FXML private Label lobbyInfoLabel;
    @FXML private Button spell1Button;
    @FXML private Button spell2Button;
    @FXML private Button spell3Button;
    @FXML private Button spell4Button;

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
                enemyWizardImage, enemyEffectImage, projectileImage);
        animationEngine.startIdleTimelines();

        myLoadout = GameStateManager.getInstance().getCurrentLoadout();
        if (myLoadout != null && myLoadout.size() == 4) {
            spell1Button.setText(myLoadout.get(0));
            spell2Button.setText(myLoadout.get(1));
            spell3Button.setText(myLoadout.get(2));
            spell4Button.setText(myLoadout.get(3));
        }
    }


    private void handleApiMessage(String message) {
        WsMessage base = gson.fromJson(message, WsMessage.class);

        switch (base.type) {
            case MessageType.MOVE_RESULT:
                Platform.runLater(() -> {
                    handleMoveResult(gson.fromJson(message, MoveResultMessage.class));
                });
                break;
            case MessageType.GAME_STATE_UPDATE:
                Platform.runLater(() -> {
                    handleGameStateUpdate(gson.fromJson(message, GameStateUpdateMessage.class));
                });
                break;
            case MessageType.GAME_OVER:
                Platform.runLater(() -> {
                    handleGameOver(gson.fromJson(message, GameOverMessage.class));
                });
                break;
            case MessageType.SHAPE_DRAWN:
                Platform.runLater(() -> {
                    handleShapeDrawn(gson.fromJson(message, ShapeMessage.class));
                });
                break;
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    private void handleMoveResult(MoveResultMessage message){
        String hostId = GameStateManager.getInstance().getHostId();
        if (hostId == null) {
            System.err.println("Local player id is not set");
            return;
        }

        boolean isHost = message.playerId.equals(hostId);
        
        if (isHost) {
            hostPanelController.moveUpdate(message.result, message.accuracyRating);
        } else {
            enemyPanelController.moveUpdate(message.result, null);
        }

        if (message.status != CastStatus.SUCCESS) {
            String statusText = switch (message.status) {
                case FAILED_MANA -> "Not enough mana";
                case FAILED_ACCURACY -> "Missed";
                case FAILED_DEATH -> "Dead";
                default -> "Failed";
            };
            animationEngine.showFloatingText(statusText, Color.YELLOW, isHost);
            return;
        }

        if (isHost) {
            int fixedAttackTime = 800;
            int projTime = Math.max(50, message.castDurationMs - fixedAttackTime);
            animationEngine.playAttack(true, fixedAttackTime);
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(fixedAttackTime), ae -> animationEngine.playProjectile(true, projTime, message.spellId)),
                    new KeyFrame(Duration.millis(message.castDurationMs), ae -> animationEngine.playEffect(false, message.spellId)));
            timeline.play();
        } else {
            int fixedAttackTime = 800;
            int projTime = Math.max(50, message.castDurationMs - fixedAttackTime);
            animationEngine.playAttack(false, fixedAttackTime);
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(fixedAttackTime), ae -> animationEngine.playProjectile(false, projTime, message.spellId)),
                    new KeyFrame(Duration.millis(message.castDurationMs), ae -> animationEngine.playEffect(true, message.spellId)));
            timeline.play();
        }
    }

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
                String text = "";
                
                if (event.combatType == CombatEventType.HIT) {
                    color = Color.RED;
                    text = "-" + (int)event.value;
                } else if (event.combatType == CombatEventType.HEAL) {
                    color = Color.LIMEGREEN;
                    text = "+" + (int)event.value;
                } else {
                    text = switch (event.combatType) {
                        case BLOCKED -> "Blocked";
                        case STATUS_APPLIED -> "Status applied";
                        default -> event.combatType.name();
                    };
                }
                
                animationEngine.showFloatingText(text, color, onHost);
            }
        }
    }

    private void handleShapeDrawn(ShapeMessage message) {
        hostPanelController.patternUpdate(message.points, message.shapeName);
    }

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

    private void castSpell(int index) {
        if (isGameOver) return;
        
        if (myLoadout != null && myLoadout.size() > index) {
            String spellId = myLoadout.get(index);
            PlayerMoveMessage moveMessage = new PlayerMoveMessage(new ArrayList<>(), index);
            ApiSocketClient.getInstance().send(moveMessage);
            System.out.println("DEV: Sent fake spell cast for " + spellId);
        }
    }

    @FXML
    private void onSpell1() { castSpell(0); }

    @FXML
    private void onSpell2() { castSpell(1); }

    @FXML
    private void onSpell3() { castSpell(2); }

    @FXML
    private void onSpell4() { castSpell(3); }
}
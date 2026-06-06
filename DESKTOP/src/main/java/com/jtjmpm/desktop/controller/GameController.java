package com.jtjmpm.desktop.controller;

import com.google.gson.Gson;
import com.jtjmpm.desktop.utils.GameStateUtils;
import com.jtjmpm.messages.*;
import com.jtjmpm.*;
import com.jtjmpm.desktop.service.ApiSocketClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GameController {
    public static final String LOBBY_VIEW = "/com/jtjmpm/desktop/lobby-view.fxml";

    @FXML private PlayerPanelController hostPanelController;
    @FXML private PlayerPanelController enemyPanelController;

    @FXML private javafx.scene.image.ImageView hostWizardImage;
    @FXML private javafx.scene.image.ImageView hostEffectImage;
    @FXML private javafx.scene.image.ImageView enemyWizardImage;
    @FXML private javafx.scene.image.ImageView enemyEffectImage;
    @FXML private javafx.scene.image.ImageView projectileImage;

    @FXML private Label lobbyInfoLabel;
    @FXML private javafx.scene.control.Button spell1Button;
    @FXML private javafx.scene.control.Button spell2Button;
    @FXML private javafx.scene.control.Button spell3Button;
    @FXML private javafx.scene.control.Button spell4Button;

    private final Gson gson = new Gson();

    private GameStateDTO gameState;
    private java.util.List<String> myLoadout;

    private final java.util.List<javafx.scene.image.Image> idleFrames = new java.util.ArrayList<>();
    private final java.util.List<javafx.scene.image.Image> attackFrames = new java.util.ArrayList<>();
    private final java.util.List<javafx.scene.image.Image> attack2Frames = new java.util.ArrayList<>();
    private final java.util.List<javafx.scene.image.Image> hitFrames = new java.util.ArrayList<>();
    private final java.util.List<javafx.scene.image.Image> deathFrames = new java.util.ArrayList<>();
    private final java.util.Map<String, java.util.List<javafx.scene.image.Image>> spellEffects = new java.util.HashMap<>();
    private final java.util.List<java.util.List<javafx.scene.image.Image>> fireballFramesList = new java.util.ArrayList<>();
    
    private javafx.animation.Timeline hostIdleTimeline;
    private javafx.animation.Timeline enemyIdleTimeline;

    private double hostHp = 100.0;
    private double enemyHp = 100.0;
    private boolean hostDead = false;
    private boolean enemyDead = false;
    private java.util.Random random = new java.util.Random();

    @FXML
    public void initialize() {
        ApiSocketClient.getInstance().setOnMessageCallback(this::handleApiMessage);
        
        enemyWizardImage.setScaleX(-1.0);

        loadAnimations();
        startIdleTimelines();

        myLoadout = ApiSocketClient.getInstance().getCurrentLoadout();
        if (myLoadout != null && myLoadout.size() == 4) {
            spell1Button.setText(myLoadout.get(0));
            spell2Button.setText(myLoadout.get(1));
            spell3Button.setText(myLoadout.get(2));
            spell4Button.setText(myLoadout.get(3));
        }
    }

    private void loadAnimations() {
        try {
            for (int i = 1; i <= 6; i++) {
                idleFrames.add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Idle_animation/Idle" + i + ".png")));
            }
            for (int i = 1; i <= 8; i++) {
                attackFrames.add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Attack1_animation/Attack1_" + i + ".png")));
            }
            for (int i = 0; i < 8; i++) {
                attack2Frames.add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Attack2_animation/Attack2_" + i + ".png")));
            }
            for (int i = 0; i < 4; i++) {
                hitFrames.add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Hit_animation/Hit_" + i + ".png")));
            }
            for (int i = 0; i < 7; i++) {
                deathFrames.add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/WizardPack/Wizard Pack/Death_animation/Death_" + i + ".png")));
            }
            spellEffects.put("Fireball", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Explosions/epic_explosion_001/epic_explosion_001_small_orange/frame%04d.png", 13));
            spellEffects.put("Ice Shard", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Impacts/directional_impact_001/directional_impact_001_small_blue/frame%04d.png", 7));
            spellEffects.put("Tornado", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Smoke Bursts/symmetrical_smoke_burst_001/symmetrical_smoke_burst_001_small_brown/frame%04d.png", 10));
            spellEffects.put("Poison", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Fantasy Spells/spell_poison_001/spell_poison_001_small_green/frame%04d.png", 17));
            spellEffects.put("Water Beam", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Splatters/burst_splatter_001/burst_splatter_001_small_red/frame%04d.png", 10));
            spellEffects.put("Air Slash", loadFrames("/com/jtjmpm/desktop/EffectsAssetsPack/PNG/Impacts/directional_impact_001/directional_impact_001_small_blue/frame%04d.png", 7));

            String[] colors = {"Blue", "Green", "Orange", "Purple", "Red"};
            for (String color : colors) {
                java.util.List<javafx.scene.image.Image> fFrames = new java.util.ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    fFrames.add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/jtjmpm/desktop/Fireballs/Fireball" + color + "0" + i + ".png")));
                }
                fireballFramesList.add(fFrames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private java.util.List<javafx.scene.image.Image> loadFrames(String pathFormat, int count) {
        java.util.List<javafx.scene.image.Image> list = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new javafx.scene.image.Image(getClass().getResourceAsStream(String.format(pathFormat, i))));
        }
        return list;
    }

    private void startIdleTimelines() {
        if (!idleFrames.isEmpty()) {
            hostIdleTimeline = createIdleTimeline(hostWizardImage);
            enemyIdleTimeline = createIdleTimeline(enemyWizardImage);
            hostIdleTimeline.play();
            enemyIdleTimeline.play();
        }
    }

    private javafx.animation.Timeline createIdleTimeline(javafx.scene.image.ImageView target) {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        for (int i = 0; i < idleFrames.size(); i++) {
            final int index = i;
            timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * 150),
                    ae -> target.setImage(idleFrames.get(index))
            ));
        }
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(idleFrames.size() * 150), ae -> {}
        ));
        return timeline;
    }

    private void playAttack(boolean isHost, int durationMs) {
        if (isHost ? hostDead : enemyDead) return;

        javafx.scene.image.ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        javafx.animation.Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;

        if (idle != null) idle.stop();
        
        java.util.List<javafx.scene.image.Image> selectedAttack = (random.nextBoolean()) ? attackFrames : attack2Frames;

        javafx.animation.Timeline attack = new javafx.animation.Timeline();
        double frameDuration = (double) durationMs / selectedAttack.size();
        for (int i = 0; i < selectedAttack.size(); i++) {
            final int index = i;
            attack.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * frameDuration),
                    ae -> wizard.setImage(selectedAttack.get(index))
            ));
        }
        attack.setOnFinished(e -> {
            if (!(isHost ? hostDead : enemyDead) && idle != null) idle.play();
        });
        attack.play();
    }

    private void playHit(boolean isHost) {
        if (isHost ? hostDead : enemyDead) return;

        javafx.scene.image.ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        javafx.animation.Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;

        if (idle != null) idle.stop();
        
        javafx.animation.Timeline hit = new javafx.animation.Timeline();
        for (int i = 0; i < hitFrames.size(); i++) {
            final int index = i;
            hit.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * 100),
                    ae -> wizard.setImage(hitFrames.get(index))
            ));
        }
        hit.setOnFinished(e -> {
            if (!(isHost ? hostDead : enemyDead) && idle != null) idle.play();
        });
        hit.play();
    }

    private void playDeath(boolean isHost) {
        if (isHost) hostDead = true;
        else enemyDead = true;

        javafx.scene.image.ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        javafx.animation.Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;

        if (idle != null) idle.stop();
        
        javafx.animation.Timeline death = new javafx.animation.Timeline();
        for (int i = 0; i < deathFrames.size(); i++) {
            final int index = i;
            death.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * 150),
                    ae -> wizard.setImage(deathFrames.get(index))
            ));
        }
        death.play(); // Stops at the last frame
    }

    private void playEffect(boolean onHost, String spellId) {
        javafx.scene.image.ImageView effectView = onHost ? hostEffectImage : enemyEffectImage;
        effectView.setScaleX(onHost ? 1.0 : -1.0);
        java.util.List<javafx.scene.image.Image> frames = spellEffects.get(spellId);
        
        if (frames == null || frames.isEmpty()) {
            frames = spellEffects.get("Fireball");
            if (frames == null || frames.isEmpty()) return;
        }

        final java.util.List<javafx.scene.image.Image> finalFrames = frames;

        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        for (int i = 0; i < finalFrames.size(); i++) {
            final int index = i;
            timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * 50),
                    ae -> effectView.setImage(finalFrames.get(index))
            ));
        }
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(finalFrames.size() * 50),
                ae -> effectView.setImage(null)
        ));
        timeline.play();
    }

    private void playProjectile(boolean fromHost, int durationMs) {
        projectileImage.setTranslateX(0); // Zapobiega miganiu w starym miejscu
        projectileImage.setVisible(true);
        projectileImage.setScaleX(0.01);
        projectileImage.setScaleY(0.01);
        
        double targetScaleX = fromHost ? 1.0 : -1.0;
        
        java.util.List<javafx.scene.image.Image> frames = fireballFramesList.get(random.nextInt(fireballFramesList.size()));
        
        double hostX = 100 + 550 / 2.0 - 50; 
        double enemyX = 1366 - 100 - 550 / 2.0 - 50;
        double y = 768 - 0 - 550 / 2.0 - 50;

        hostX = hostX + 100;
        enemyX = enemyX + 100;
        y = y + 100;

        double startX = fromHost ? hostX : enemyX;
        double endX = fromHost ? enemyX : hostX;

        projectileImage.setLayoutX(startX);
        projectileImage.setLayoutY(y);

        double scaleDuration = durationMs * 0.2;
        double moveDuration = durationMs - scaleDuration;

        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(scaleDuration), projectileImage);
        st.setToX(targetScaleX);
        st.setToY(1.0);

        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(moveDuration), projectileImage);
        tt.setFromX(0);
        tt.setToX(endX - startX);
        tt.setDelay(javafx.util.Duration.millis(scaleDuration));
        tt.setOnFinished(e -> projectileImage.setVisible(false));
        
        st.play();
        tt.play();

        javafx.animation.Timeline animation = new javafx.animation.Timeline();
        animation.setCycleCount(javafx.animation.Animation.INDEFINITE);
        for (int i = 0; i < frames.size(); i++) {
            final int index = i;
            animation.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(i * 100),
                    ae -> projectileImage.setImage(frames.get(index))
            ));
        }
        animation.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.millis(frames.size() * 100)));
        animation.play();
        
        tt.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == javafx.animation.Animation.Status.STOPPED) {
                animation.stop();
            }
        });
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
            default:
                System.out.println("Unknown message type: " + base.type);
        }
    }

    private void handleMoveResult(MoveResultMessage message){
        String hostId = ApiSocketClient.getInstance().getHostId();
        if (hostId == null) {
            System.err.println("Local player id is not set");
            return;
        }

        if (message.playerId.equals(hostId)) {
            hostPanelController.moveUpdate(message.result);
            if (message.status == com.jtjmpm.messages.CastStatus.SUCCESS) {
                int halfDuration = message.castDurationMs / 2;
                playAttack(true, halfDuration);
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(halfDuration), ae -> playProjectile(true, halfDuration)),
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(message.castDurationMs), ae -> playEffect(false, message.spellId)));
                timeline.play();
            }
        } else {
            enemyPanelController.moveUpdate(message.result);
            if (message.status == com.jtjmpm.messages.CastStatus.SUCCESS) {
                int halfDuration = message.castDurationMs / 2;
                playAttack(false, halfDuration);
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(halfDuration), ae -> playProjectile(false, halfDuration)),
                        new javafx.animation.KeyFrame(javafx.util.Duration.millis(message.castDurationMs), ae -> playEffect(true, message.spellId)));
                timeline.play();
            }
        }
    }

    private void handleGameStateUpdate(GameStateUpdateMessage message) {
        gameState = message.gameState;

        String hostId = ApiSocketClient.getInstance().getHostId();

        PlayerDTO hostPlayer = gameState.players().get(hostId);
        PlayerDTO enemyPlayer = Objects.requireNonNull(GameStateUtils.getEnemy(gameState.players().values(), hostId));
        
        if (hostPlayer.hp() < hostHp && hostPlayer.hp() > 0) {
            playHit(true);
        } else if (hostPlayer.hp() <= 0 && hostHp > 0) {
            playDeath(true);
        }
        hostHp = hostPlayer.hp();

        if (enemyPlayer.hp() < enemyHp && enemyPlayer.hp() > 0) {
            playHit(false);
        } else if (enemyPlayer.hp() <= 0 && enemyHp > 0) {
            playDeath(false);
        }
        enemyHp = enemyPlayer.hp();

        hostPanelController.playerStateUpdate(hostPlayer);
        enemyPanelController.playerStateUpdate(enemyPlayer);
    }

    @FXML
    private void onBackToMenu() {
        try {
            ApiSocketClient.getInstance().setOnMessageCallback(null);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOBBY_VIEW));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lobbyInfoLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void castSpell(int index) {
        if (myLoadout != null && myLoadout.size() > index) {
            String spellId = myLoadout.get(index);
            PlayerMoveMessage moveMessage = new PlayerMoveMessage(new java.util.ArrayList<>(), index);
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
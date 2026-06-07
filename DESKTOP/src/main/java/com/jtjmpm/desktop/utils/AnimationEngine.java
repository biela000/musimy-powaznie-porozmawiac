package com.jtjmpm.desktop.utils;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class AnimationEngine {

    // default screen resolution
    public static final double SCREEN_WIDTH = 1366.0;
    public static final double SCREEN_HEIGHT = 768.0;

    // wizard image size & offset from the screen edges
    public static final double WIZARD_SIZE = 550.0;
    public static final double WIZARD_OFFSET_X = 100.0;
    public static final double WIZARD_OFFSET_Y = 0.0;

    // projectile image size and offset from the wizard
    public static final double PROJECTILE_IMAGE_SIZE = 100.0;
    public static final double PROJECTILE_SPAWN_OFFSET_X = 150.0;
    public static final double PROJECTILE_SPAWN_OFFSET_Y = 50.0;

    // floating text offset from the wizard and its flight distance
    public static final double TEXT_SPAWN_OFFSET_X = 50.0;
    public static final double TEXT_SPAWN_OFFSET_Y = 0.0;
    public static final double TEXT_FLOAT_DISTANCE = -150.0;

    private final AnchorPane mainPane;
    private final ImageView hostWizardImage;
    private final ImageView hostEffectImage;
    private final ImageView enemyWizardImage;
    private final ImageView enemyEffectImage;
    private final ImageView projectileImage;

    private Timeline hostIdleTimeline;
    private Timeline enemyIdleTimeline;

    private boolean hostDead = false;
    private boolean enemyDead = false;

    private final Random random = new Random();
    private final AssetManager assets = AssetManager.getInstance();

    public AnimationEngine(AnchorPane mainPane, ImageView hostWizardImage, ImageView hostEffectImage,
                           ImageView enemyWizardImage, ImageView enemyEffectImage, ImageView projectileImage) {
        this.mainPane = mainPane;
        this.hostWizardImage = hostWizardImage;
        this.hostEffectImage = hostEffectImage;
        this.enemyWizardImage = enemyWizardImage;
        this.enemyEffectImage = enemyEffectImage;
        this.projectileImage = projectileImage;
    }

    public void startIdleTimelines() {
        if (!assets.getIdleFrames().isEmpty()) {
            hostIdleTimeline = createIdleTimeline(hostWizardImage);
            enemyIdleTimeline = createIdleTimeline(enemyWizardImage);
            hostIdleTimeline.play();
            enemyIdleTimeline.play();
        }
    }

    private Timeline createIdleTimeline(ImageView target) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        List<Image> frames = assets.getIdleFrames();
        for (int i = 0; i < frames.size(); i++) {
            final int index = i;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * 150),
                    ae -> target.setImage(frames.get(index))
            ));
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(frames.size() * 150), ae -> {}));
        return timeline;
    }

    public void playAttack(boolean isHost, int durationMs) {
        if (isHost ? hostDead : enemyDead) return;

        ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;

        if (idle != null) idle.stop();
        
        List<Image> selectedAttack = random.nextBoolean() ? assets.getAttackFrames() : assets.getAttack2Frames();

        Timeline attack = new Timeline();
        double frameDuration = (double) durationMs / selectedAttack.size();
        for (int i = 0; i < selectedAttack.size(); i++) {
            final int index = i;
            attack.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * frameDuration),
                    ae -> wizard.setImage(selectedAttack.get(index))
            ));
        }
        attack.setOnFinished(e -> {
            if (!(isHost ? hostDead : enemyDead) && idle != null) idle.play();
        });
        attack.play();
    }

    public void playHit(boolean isHost) {
        if (isHost ? hostDead : enemyDead) return;

        ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;

        if (idle != null) idle.stop();
        
        Timeline hit = new Timeline();
        List<Image> hitFrames = assets.getHitFrames();
        for (int i = 0; i < hitFrames.size(); i++) {
            final int index = i;
            hit.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * 100),
                    ae -> wizard.setImage(hitFrames.get(index))
            ));
        }
        hit.setOnFinished(e -> {
            if (!(isHost ? hostDead : enemyDead) && idle != null) idle.play();
        });
        hit.play();
    }

    public void playDeath(boolean isHost) {
        if (isHost) hostDead = true;
        else enemyDead = true;

        ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;

        if (idle != null) idle.stop();
        
        Timeline death = new Timeline();
        List<Image> deathFrames = assets.getDeathFrames();
        for (int i = 0; i < deathFrames.size(); i++) {
            final int index = i;
            death.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * 150),
                    ae -> wizard.setImage(deathFrames.get(index))
            ));
        }
        death.play(); // Stops at the last frame
    }

    public void playEffect(boolean onHost, String spellId) {
        ImageView effectView = onHost ? hostEffectImage : enemyEffectImage;
        effectView.setScaleX(onHost ? 1.0 : -1.0);
        List<Image> finalFrames = assets.getEffectFrames(spellId);
        if (finalFrames == null || finalFrames.isEmpty()) return;

        Timeline timeline = new Timeline();
        for (int i = 0; i < finalFrames.size(); i++) {
            final int index = i;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * 50),
                    ae -> effectView.setImage(finalFrames.get(index))
            ));
        }
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(finalFrames.size() * 50),
                ae -> effectView.setImage(null)
        ));
        timeline.play();
    }

    public void playProjectile(boolean fromHost, int durationMs, String spellId) {
        projectileImage.setTranslateX(0); 
        projectileImage.setVisible(true);
        projectileImage.setScaleX(0.01);
        projectileImage.setScaleY(0.01);
        
        double targetScaleX = fromHost ? 1.0 : -1.0;
        
        List<Image> frames = assets.getProjectileFrames(spellId);

        double hostCenterX = WIZARD_OFFSET_X + (WIZARD_SIZE / 2.0);
        double enemyCenterX = SCREEN_WIDTH - WIZARD_OFFSET_X - (WIZARD_SIZE / 2.0);
        double wizardCenterY = SCREEN_HEIGHT - WIZARD_OFFSET_Y - (WIZARD_SIZE / 2.0);

        double hostSpawnX = hostCenterX + PROJECTILE_SPAWN_OFFSET_X - (PROJECTILE_IMAGE_SIZE / 2.0);
        double enemySpawnX = enemyCenterX - PROJECTILE_SPAWN_OFFSET_X - (PROJECTILE_IMAGE_SIZE / 2.0);
        double spawnY = wizardCenterY + PROJECTILE_SPAWN_OFFSET_Y - (PROJECTILE_IMAGE_SIZE / 2.0);

        double startX = fromHost ? hostSpawnX : enemySpawnX;
        double endX = fromHost ? enemySpawnX : hostSpawnX;

        projectileImage.setLayoutX(startX);
        projectileImage.setLayoutY(spawnY);

        double scaleDuration = durationMs * 0.2;
        double moveDuration = durationMs - scaleDuration;

        ScaleTransition st = new ScaleTransition(Duration.millis(scaleDuration), projectileImage);
        st.setToX(targetScaleX);
        st.setToY(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(moveDuration), projectileImage);
        tt.setFromX(0);
        tt.setToX(endX - startX);
        tt.setDelay(Duration.millis(scaleDuration));
        tt.setOnFinished(e -> projectileImage.setVisible(false));
        
        st.play();
        tt.play();

        Timeline animation = new Timeline();
        animation.setCycleCount(Timeline.INDEFINITE);
        for (int i = 0; i < frames.size(); i++) {
            final int index = i;
            animation.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * 100),
                    ae -> projectileImage.setImage(frames.get(index))
            ));
        }
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(frames.size() * 100)));
        animation.play();
        
        tt.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == javafx.animation.Animation.Status.STOPPED) {
                animation.stop();
            }
        });
    }

    public void showFloatingText(String text, Color color, boolean onHost) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 30));
        label.setTextFill(color);
        label.setEffect(new DropShadow(4, Color.BLACK));
        
        double hostCenterX = WIZARD_OFFSET_X + (WIZARD_SIZE / 2.0);
        double enemyCenterX = SCREEN_WIDTH - WIZARD_OFFSET_X - (WIZARD_SIZE / 2.0);
        double wizardCenterY = SCREEN_HEIGHT - WIZARD_OFFSET_Y - (WIZARD_SIZE / 2.0);
        
        double x = (onHost ? hostCenterX : enemyCenterX) + TEXT_SPAWN_OFFSET_X;
        double y = wizardCenterY + TEXT_SPAWN_OFFSET_Y;

        label.setLayoutX(x);
        label.setLayoutY(y);
        
        mainPane.getChildren().add(label);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(2000), label);
        tt.setByY(TEXT_FLOAT_DISTANCE);
        
        FadeTransition ft = new FadeTransition(Duration.millis(2000), label);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> mainPane.getChildren().remove(label));
        pt.play();
    }
}

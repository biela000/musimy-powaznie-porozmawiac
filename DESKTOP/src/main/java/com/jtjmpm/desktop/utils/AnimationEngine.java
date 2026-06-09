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
    public static final double PROJECTILE_SPAWN_OFFSET_X = 100.0;
    public static final double PROJECTILE_SPAWN_OFFSET_Y = 25.0;

    // floating text offset from the wizard and its flight distance
    public static final double TEXT_SPAWN_OFFSET_X = 50.0;
    public static final double TEXT_SPAWN_OFFSET_Y = 0.0;
    public static final double TEXT_FLOAT_DISTANCE = -150.0;

    private final AnchorPane mainPane;
    private final ImageView hostWizardImage;
    private final ImageView hostEffectImage;
    private final ImageView enemyWizardImage;
    private final ImageView enemyEffectImage;

    private Timeline hostIdleTimeline;
    private Timeline enemyIdleTimeline;

    // tracks the currently-running sprite timeline for each wizard so we can
    // stop it cleanly before starting a new one
    private Timeline hostActiveTimeline;
    private Timeline enemyActiveTimeline;

    private boolean hostDead = false;
    private boolean enemyDead = false;

    private final Random random = new Random();
    private final AssetManager assets = AssetManager.getInstance();

    public AnimationEngine(AnchorPane mainPane,
                           ImageView hostWizardImage, ImageView hostEffectImage,
                           ImageView enemyWizardImage, ImageView enemyEffectImage) {
        this.mainPane = mainPane;
        this.hostWizardImage = hostWizardImage;
        this.hostEffectImage = hostEffectImage;
        this.enemyWizardImage = enemyWizardImage;
        this.enemyEffectImage = enemyEffectImage;
    }

    // -------------------------------------------------------------------------
    // idle
    // -------------------------------------------------------------------------

    public void startIdleTimelines() {
        if (!assets.getIdleFrames().isEmpty()) {
            // Always stop whatever is currently running before creating new timelines.
            // Without this, every call leaks the old Timeline which keeps firing forever,
            // causing multiple animations to compete on the same ImageView simultaneously.
            if (hostIdleTimeline != null) hostIdleTimeline.stop();
            if (enemyIdleTimeline != null) enemyIdleTimeline.stop();

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

    // -------------------------------------------------------------------------
    // wizard sprite helpers
    // -------------------------------------------------------------------------

    /** Stops whatever sprite animation is running on this wizard and records the new one. */
    private void setActiveTimeline(boolean isHost, Timeline newTimeline) {
        if (isHost) {
            if (hostActiveTimeline != null) hostActiveTimeline.stop();
            hostActiveTimeline = newTimeline;
        } else {
            if (enemyActiveTimeline != null) enemyActiveTimeline.stop();
            enemyActiveTimeline = newTimeline;
        }
    }

    private void resumeIdle(boolean isHost) {
        if (isHost ? hostDead : enemyDead) return;
        Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;
        if (idle != null) idle.play();
    }

    // -------------------------------------------------------------------------
    // wizard animations
    // -------------------------------------------------------------------------

    public void playAttack(boolean isHost, int durationMs) {
        if (isHost ? hostDead : enemyDead) return;

        ImageView wizard = isHost ? hostWizardImage : enemyWizardImage;
        Timeline idle = isHost ? hostIdleTimeline : enemyIdleTimeline;
        if (idle != null) idle.stop();

        List<Image> selectedAttack = random.nextBoolean()
                ? assets.getAttackFrames() : assets.getAttack2Frames();

        Timeline attack = new Timeline();
        double frameDuration = (double) durationMs / selectedAttack.size();
        for (int i = 0; i < selectedAttack.size(); i++) {
            final int index = i;
            attack.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * frameDuration),
                    ae -> wizard.setImage(selectedAttack.get(index))
            ));
        }
        attack.setOnFinished(e -> resumeIdle(isHost));
        setActiveTimeline(isHost, attack);
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
        hit.setOnFinished(e -> resumeIdle(isHost));
        setActiveTimeline(isHost, hit);
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
        setActiveTimeline(isHost, death);
        death.play(); // stops at the last frame
    }

    // -------------------------------------------------------------------------
    // spell effect (impact on target)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // projectile  –  each call creates its own ImageView so multiple
    // simultaneous projectiles are fully independent and pass through each other
    // -------------------------------------------------------------------------

    public void playProjectile(boolean fromHost, int durationMs, String spellId) {
        List<Image> frames = assets.getProjectileFrames(spellId);
        if (frames == null || frames.isEmpty()) return;

        // create a fresh node for this projectile
        ImageView proj = new ImageView();
        proj.setFitWidth(PROJECTILE_IMAGE_SIZE);
        proj.setFitHeight(PROJECTILE_IMAGE_SIZE);
        proj.setPreserveRatio(true);
        proj.setScaleX(0.01);
        proj.setScaleY(0.01);

        double targetScaleX = fromHost ? 1.0 : -1.0;

        double hostCenterX  = WIZARD_OFFSET_X + (WIZARD_SIZE / 2.0);
        double enemyCenterX = SCREEN_WIDTH - WIZARD_OFFSET_X - (WIZARD_SIZE / 2.0);
        double wizardCenterY = SCREEN_HEIGHT - WIZARD_OFFSET_Y - (WIZARD_SIZE / 2.0);

        double hostSpawnX  = hostCenterX  + PROJECTILE_SPAWN_OFFSET_X - (PROJECTILE_IMAGE_SIZE / 2.0);
        double enemySpawnX = enemyCenterX - PROJECTILE_SPAWN_OFFSET_X - (PROJECTILE_IMAGE_SIZE / 2.0);
        double spawnY      = wizardCenterY + PROJECTILE_SPAWN_OFFSET_Y - (PROJECTILE_IMAGE_SIZE / 2.0);

        double startX = fromHost ? hostSpawnX : enemySpawnX;
        double endX   = fromHost ? enemySpawnX : hostSpawnX;

        proj.setLayoutX(startX);
        proj.setLayoutY(spawnY);

        mainPane.getChildren().add(proj);

        double scaleDuration = durationMs * 0.2;
        double moveDuration  = durationMs - scaleDuration;

        ScaleTransition st = new ScaleTransition(Duration.millis(scaleDuration), proj);
        st.setToX(targetScaleX);
        st.setToY(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(moveDuration), proj);
        tt.setFromX(0);
        tt.setToX(endX - startX);
        tt.setDelay(Duration.millis(scaleDuration));
        tt.setOnFinished(e -> mainPane.getChildren().remove(proj));

        // sprite animation (loops until movement ends)
        Timeline spriteAnim = new Timeline();
        spriteAnim.setCycleCount(Timeline.INDEFINITE);
        for (int i = 0; i < frames.size(); i++) {
            final int index = i;
            spriteAnim.getKeyFrames().add(new KeyFrame(
                    Duration.millis(i * 100),
                    ae -> proj.setImage(frames.get(index))
            ));
        }
        spriteAnim.getKeyFrames().add(new KeyFrame(Duration.millis(frames.size() * 100)));

        st.play();
        tt.play();
        spriteAnim.play();

        tt.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == javafx.animation.Animation.Status.STOPPED) {
                spriteAnim.stop();
            }
        });
    }

    // -------------------------------------------------------------------------
    // round reset
    // -------------------------------------------------------------------------

    /**
     * Resets wizard state for a new round: clears the "dead" flags, stops any
     * lingering animations, and restarts the idle timelines.
     */
    public void resetForNewRound() {
        hostDead = false;
        enemyDead = false;

        if (hostActiveTimeline != null) { hostActiveTimeline.stop(); hostActiveTimeline = null; }
        if (enemyActiveTimeline != null) { enemyActiveTimeline.stop(); enemyActiveTimeline = null; }

        // Restart idle animations for both wizards.
        startIdleTimelines();
    }

    // -------------------------------------------------------------------------
    // countdown number (3 / 2 / 1)  –  snappy pop-in that fades before the next
    // -------------------------------------------------------------------------

    public void showCountdownNumber(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 160));
        label.setTextFill(Color.WHITE);
        label.setEffect(new DropShadow(18, Color.BLACK));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(javafx.geometry.Pos.CENTER);

        AnchorPane.setTopAnchor(label, SCREEN_HEIGHT / 2.0 - 110.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);

        label.setScaleX(0.1);
        label.setScaleY(0.1);

        mainPane.getChildren().add(label);

        ScaleTransition popIn = new ScaleTransition(Duration.millis(250), label);
        popIn.setToX(1.0);
        popIn.setToY(1.0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(550));
        fadeOut.setOnFinished(e -> mainPane.getChildren().remove(label));

        popIn.play();
        fadeOut.play();
    }

    // -------------------------------------------------------------------------
    // centered rating / miss text  –  big, pops in then floats up and fades
    // -------------------------------------------------------------------------

    public void showCenteredText(String text, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 80));
        label.setTextFill(color);
        label.setEffect(new DropShadow(12, Color.BLACK));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(javafx.geometry.Pos.CENTER);

        // stretch full width and sit at vertical center so the text is centered
        AnchorPane.setTopAnchor(label, SCREEN_HEIGHT / 2.0 - 60.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);

        label.setScaleX(0.3);
        label.setScaleY(0.3);

        mainPane.getChildren().add(label);

        // pop-in scale
        ScaleTransition popIn = new ScaleTransition(Duration.millis(250), label);
        popIn.setToX(1.0);
        popIn.setToY(1.0);

        // float up and fade out after a short hold
        TranslateTransition floatUp = new TranslateTransition(Duration.millis(1400), label);
        floatUp.setByY(-120.0);
        floatUp.setDelay(Duration.millis(600));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(1400), label);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(600));

        popIn.setOnFinished(e -> {
            ParallelTransition exit = new ParallelTransition(floatUp, fadeOut);
            exit.setOnFinished(ev -> mainPane.getChildren().remove(label));
            exit.play();
        });

        popIn.play();
    }

    // -------------------------------------------------------------------------
    // floating damage / status text
    // -------------------------------------------------------------------------

    public void showFloatingText(String text, Color color, boolean onHost) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 30));
        label.setTextFill(color);
        label.setEffect(new DropShadow(4, Color.BLACK));

        double hostCenterX  = WIZARD_OFFSET_X + (WIZARD_SIZE / 2.0);
        double enemyCenterX = SCREEN_WIDTH - WIZARD_OFFSET_X - (WIZARD_SIZE / 2.0);
        double wizardCenterY = SCREEN_HEIGHT - WIZARD_OFFSET_Y - (WIZARD_SIZE / 2.0);

        double x = (onHost ? hostCenterX : enemyCenterX) + (onHost ? TEXT_SPAWN_OFFSET_X : -TEXT_SPAWN_OFFSET_X);
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

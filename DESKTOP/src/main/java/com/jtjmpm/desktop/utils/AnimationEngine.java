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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.Animation;
import javafx.geometry.Pos;

public class AnimationEngine {

    public static final double SCREEN_WIDTH = DesktopConstants.SCREEN_WIDTH;
    public static final double SCREEN_HEIGHT = DesktopConstants.SCREEN_HEIGHT;

    public static final double WIZARD_SIZE = DesktopConstants.WIZARD_SIZE;
    public static final double WIZARD_OFFSET_X = DesktopConstants.WIZARD_OFFSET_X;
    public static final double WIZARD_OFFSET_Y = DesktopConstants.WIZARD_OFFSET_Y;

    public static final double PROJECTILE_IMAGE_SIZE = DesktopConstants.PROJECTILE_IMAGE_SIZE;
    public static final double PROJECTILE_SPAWN_OFFSET_X = DesktopConstants.PROJECTILE_SPAWN_OFFSET_X;
    public static final double PROJECTILE_SPAWN_OFFSET_Y = DesktopConstants.PROJECTILE_SPAWN_OFFSET_Y;

    public static final double TEXT_SPAWN_OFFSET_X = DesktopConstants.TEXT_SPAWN_OFFSET_X;
    public static final double TEXT_SPAWN_OFFSET_Y = DesktopConstants.TEXT_SPAWN_OFFSET_Y;
    public static final double TEXT_FLOAT_DISTANCE = DesktopConstants.TEXT_FLOAT_DISTANCE;

    private final AnchorPane mainPane;
    private final ImageView hostWizardImage;
    private final ImageView hostEffectImage;
    private final ImageView enemyWizardImage;
    private final ImageView enemyEffectImage;

    private Timeline hostIdleTimeline;
    private Timeline enemyIdleTimeline;

    private Timeline hostActiveTimeline;
    private Timeline enemyActiveTimeline;

    private boolean hostDead = false;
    private boolean enemyDead = false;

    private final Random random = new Random();
    private final AssetManager assets = AssetManager.getInstance();

    private final List<Timeline> effectTimelines = new ArrayList<>();

    public AnimationEngine(AnchorPane mainPane,
                           ImageView hostWizardImage, ImageView hostEffectImage,
                           ImageView enemyWizardImage, ImageView enemyEffectImage) {
        this.mainPane = mainPane;
        this.hostWizardImage = hostWizardImage;
        this.hostEffectImage = hostEffectImage;
        this.enemyWizardImage = enemyWizardImage;
        this.enemyEffectImage = enemyEffectImage;
    }

    public void startIdleTimelines() {
        if (!assets.getIdleFrames().isEmpty()) {

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
        death.play();
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
        timeline.setOnFinished(e -> effectTimelines.remove(timeline));
        effectTimelines.add(timeline);
        timeline.play();
    }

    public void playProjectile(boolean fromHost, int durationMs, String spellId) {
        List<Image> frames = assets.getProjectileFrames(spellId);
        if (frames == null || frames.isEmpty()) return;

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
            if (newStatus == Animation.Status.STOPPED) {
                spriteAnim.stop();
            }
        });
    }

    public void resetForNewRound() {
        hostDead = false;
        enemyDead = false;

        if (hostActiveTimeline != null) { hostActiveTimeline.stop(); hostActiveTimeline = null; }
        if (enemyActiveTimeline != null) { enemyActiveTimeline.stop(); enemyActiveTimeline = null; }

        startIdleTimelines();
    }

    public void showCountdownNumber(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 160));
        label.setTextFill(Color.WHITE);
        label.setEffect(new DropShadow(18, Color.BLACK));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);

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

    public void showCenteredText(String text, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 80));
        label.setTextFill(color);
        label.setEffect(new DropShadow(12, Color.BLACK));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(label, SCREEN_HEIGHT / 2.0 - 60.0);
        AnchorPane.setLeftAnchor(label, 0.0);
        AnchorPane.setRightAnchor(label, 0.0);

        label.setScaleX(0.3);
        label.setScaleY(0.3);

        mainPane.getChildren().add(label);

        ScaleTransition popIn = new ScaleTransition(Duration.millis(250), label);
        popIn.setToX(1.0);
        popIn.setToY(1.0);

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

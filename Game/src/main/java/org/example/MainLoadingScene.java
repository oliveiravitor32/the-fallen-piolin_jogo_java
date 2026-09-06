package org.example;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getUIFactoryService;

import static com.almasb.fxgl.dsl.FXGL.*;

/*
     ESTA CLASSE É RESPONSÁVEL POR CRIAR A CENA DE CARREGAMENTO (LOADING) DO JOGO!
 */

public class MainLoadingScene extends LoadingScene {

    public MainLoadingScene() {
        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.DARKGREEN);

        Text text = getUIFactoryService().newText("Loading level", Color.BLACK, 46.0);
        centerText(text, getAppWidth() / 2, getAppHeight() / 3  + 25);

        var hbox = new HBox(5);

        // Create "." ".." "..."
        for (int i = 0; i < 3; i++) {
            var textDot = getUIFactoryService().newText(".", Color.BLACK, 46.0);

            hbox.getChildren().add(textDot);

            animationBuilder(this)
                    .autoReverse(true)
                    .delay(Duration.seconds(i * 0.5))
                    .repeatInfinitely()
                    .fadeIn(textDot)
                    .buildAndPlay();
        }

        hbox.setTranslateX(getAppWidth() / 2 - 20);
        hbox.setTranslateY(getAppHeight() / 2);

        // Primeiro quadro (64x64) da folha de sprites do Piolin.
        // Antes esta linha pedia "player.png", arquivo que não existe no projeto: o FXGL
        // caía num placeholder e registrava apenas um aviso no console.
        var playerTexture = texture("walk_piolin1-Sheet.png").subTexture(new Rectangle2D(0, 0, 64, 64));
        playerTexture.setTranslateX(getAppWidth() / 2.0 - 32);
        playerTexture.setTranslateY(getAppHeight() / 2.0 - 32);

        animationBuilder(this)
                .duration(Duration.seconds(1.25))
                .repeatInfinitely()
                .autoReverse(true)
                .interpolator(Interpolators.EXPONENTIAL.EASE_IN_OUT())
                .rotate(playerTexture)
                .from(0)
                .to(360)
                .buildAndPlay();

        getContentRoot().getChildren().setAll(bg, text, hbox, playerTexture);

    }
}
package org.example.utilitarios;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.example.Main;

import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameController;

public class FimDeJogo {

    // Perdemos
    public static void terminarLoser() {
        // Configurações:
        getGameController().pauseEngine();
        Rectangle barra_de_vida = new Rectangle(260, 50, Color.DARKORANGE);
        barra_de_vida.setX(getAppWidth()/2 - 130);
        barra_de_vida.setY(300);

        Button btn = new Button("REINICIAR");
        btn.setTranslateX(getAppWidth()/2 - 125);
        btn.setTranslateY(300);
        btn.setBackground(Background.fill(Color.ORANGE));
        btn.setPrefWidth(250); // Define a largura do botão para 200 pixels
        btn.setPrefHeight(40); // Define a altura do botão para 100 pixels

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                reiniciar();
            }
        };

        btn.setOnAction(event);

        Text textOptions = FXGL.getUIFactoryService().newText("DERROTA!", Color.WHITE, FontType.GAME, 54);
        textOptions.setTranslateX(getAppWidth()/2 - 100);

        textOptions.setTranslateY(290);
        textOptions.setMouseTransparent(true);

        getGameScene().addUINode(barra_de_vida);
        getGameScene().addUINode(btn);
        getGameScene().addUINode(textOptions);
    }


    // Ganhamos
    public static void terminarWinner() {
        // Configurações:
        getGameController().pauseEngine();
        Rectangle barra_de_vida = new Rectangle(260, 50, Color.GRAY);
        barra_de_vida.setX(getAppWidth()/2 - 130);
        barra_de_vida.setY(300);

        Button btn = new Button("REINICIAR");
        btn.setTranslateX(getAppWidth()/2 - 125);
        btn.setTranslateY(300);
        btn.setBackground(Background.fill(Color.WHITE));
        btn.setPrefWidth(250); // Define a largura do botão para 200 pixels
        btn.setPrefHeight(40); // Define a altura do botão para 100 pixels

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                reiniciar();
            }
        };

        btn.setOnAction(event);

        Text textOptions = FXGL.getUIFactoryService().newText("VITÓRIA!", Color.BLACK, FontType.GAME, 54);
        textOptions.setTranslateX(getAppWidth()/2 - 75);

        textOptions.setTranslateY(290);
        textOptions.setMouseTransparent(true);

        getGameScene().addUINode(barra_de_vida);
        getGameScene().addUINode(btn);
        getGameScene().addUINode(textOptions);
    }


    public static void reiniciar() {
        Main app = FXGL.getAppCast();
        app.setAceitaEntradaDeDados(false);
        FXGL.getGameController().startNewGame();
        FXGL.getGameController().resumeEngine();
    }
}
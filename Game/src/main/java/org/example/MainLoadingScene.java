package org.example;

import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.ui.menu.EstiloDaInterface;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.texture;

/*
    Tela de carregamento, no mesmo estilo dos menus.

    Antes era um fundo verde chapado com o texto "Loading level" e o Piolin girando.
    A rotacao era o principal problema para o visual em pixel art: girar um sprite
    obriga o JavaFX a interpolar as cores e desmancha a grade de pixels. Aqui ele
    apenas sobe e desce, movimento que preserva os pixels intactos.
*/
public class MainLoadingScene extends LoadingScene {

    private static final double LADO_DO_SPRITE = 64;
    private static final double ESCALA_DO_SPRITE = 2;

    private static final double LARGURA_DA_BARRA = 360;
    private static final double ALTURA_DA_BARRA = 24;
    private static final double LARGURA_DO_BLOCO = 96;
    private static final double MARGEM_DO_BLOCO = 5;

    public MainLoadingScene() {
        Rectangle fundo = new Rectangle(getAppWidth(), getAppHeight(), Color.web("#12101C"));

        VBox coluna = new VBox(26, montarSprite(), montarTexto(), montarBarra());
        coluna.setAlignment(Pos.CENTER);
        coluna.setPrefSize(getAppWidth(), getAppHeight());

        getContentRoot().getChildren().setAll(new StackPane(fundo, coluna));
    }

    /*
        Primeiro quadro da folha de sprites do Piolin, ampliado por um fator inteiro
        para que cada pixel da arte vire um quadrado exato, sem meias medidas.
    */
    private StackPane montarSprite() {
        var piolin = texture("walk_piolin1-Sheet.png")
                .subTexture(new Rectangle2D(0, 0, LADO_DO_SPRITE, LADO_DO_SPRITE));

        piolin.setFitWidth(LADO_DO_SPRITE * ESCALA_DO_SPRITE);
        piolin.setFitHeight(LADO_DO_SPRITE * ESCALA_DO_SPRITE);
        piolin.setSmooth(false);

        TranslateTransition pulo = new TranslateTransition(Duration.seconds(0.55), piolin);
        pulo.setByY(-16);
        pulo.setAutoReverse(true);
        pulo.setCycleCount(TranslateTransition.INDEFINITE);
        pulo.setInterpolator(Interpolator.EASE_BOTH);
        pulo.play();

        StackPane caixa = new StackPane(piolin);
        caixa.setAlignment(Pos.CENTER);

        return caixa;
    }

    private HBox montarTexto() {
        Text rotulo = EstiloDaInterface.texto("Carregando", 30, true, EstiloDaInterface.TEXTO);
        rotulo.setEffect(new DropShadow(0, 4, 4, Color.web("#000000", 0.8)));

        HBox linha = new HBox(4, rotulo);
        linha.setAlignment(Pos.CENTER);

        // Os tres pontos acendem em sequencia, dando a sensacao de progresso
        for (int i = 0; i < 3; i++) {
            Text ponto = EstiloDaInterface.texto(".", 30, true, EstiloDaInterface.TEXTO);

            FadeTransition piscada = new FadeTransition(Duration.seconds(0.5), ponto);
            piscada.setFromValue(0.15);
            piscada.setToValue(1);
            piscada.setAutoReverse(true);
            piscada.setCycleCount(FadeTransition.INDEFINITE);
            piscada.setDelay(Duration.seconds(i * 0.25));
            piscada.play();

            linha.getChildren().add(ponto);
        }

        return linha;
    }

    /*
        Barra indeterminada: um bloco solido vai e volta dentro da trilha. Nao mede
        progresso real, so indica que o jogo esta trabalhando.
    */
    private StackPane montarBarra() {
        Rectangle trilha = new Rectangle(LARGURA_DA_BARRA, ALTURA_DA_BARRA);
        trilha.setFill(Color.web("#FFFFFF", 0.10));
        trilha.setStroke(Color.web("#FFFFFF", 0.45));
        trilha.setStrokeWidth(EstiloDaInterface.BORDA);
        trilha.setArcWidth(EstiloDaInterface.RAIO);
        trilha.setArcHeight(EstiloDaInterface.RAIO);

        Rectangle bloco = new Rectangle(LARGURA_DO_BLOCO, ALTURA_DA_BARRA - MARGEM_DO_BLOCO * 2);
        bloco.setFill(Color.web(EstiloDaInterface.VERDE));
        bloco.setTranslateX(MARGEM_DO_BLOCO);
        bloco.setTranslateY(MARGEM_DO_BLOCO);

        double percurso = LARGURA_DA_BARRA - LARGURA_DO_BLOCO - MARGEM_DO_BLOCO * 2;

        TranslateTransition vaievem = new TranslateTransition(Duration.seconds(1.1), bloco);
        vaievem.setFromX(MARGEM_DO_BLOCO);
        vaievem.setToX(MARGEM_DO_BLOCO + percurso);
        vaievem.setAutoReverse(true);
        vaievem.setCycleCount(TranslateTransition.INDEFINITE);
        vaievem.setInterpolator(Interpolator.EASE_BOTH);
        vaievem.play();

        Pane barra = new Pane(trilha, bloco);
        barra.setPrefSize(LARGURA_DA_BARRA, ALTURA_DA_BARRA);
        barra.setMaxSize(LARGURA_DA_BARRA, ALTURA_DA_BARRA);

        StackPane caixa = new StackPane(barra);
        caixa.setAlignment(Pos.CENTER);

        return caixa;
    }
}

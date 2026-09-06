package org.example.utilitarios;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontType;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameController;

/*
    Telas de fim de jogo (vitória e derrota).

    As duas eram blocos praticamente idênticos, duplicados; agora compartilham
    montarTela(), que muda apenas o texto e as cores.
*/
public class FimDeJogo {

    private static final double LARGURA_DO_PAINEL = 260;
    private static final double ALTURA_DO_PAINEL = 50;
    private static final double LARGURA_DO_BOTAO = 250;
    private static final double ALTURA_DO_BOTAO = 40;
    private static final double TOPO_DO_PAINEL = 300;

    private FimDeJogo() {
        // Classe utilitária: não deve ser instanciada.
    }

    /** Fim de jogo por derrota: a floresta ou o Piolin não resistiram. */
    public static void terminarLoser() {
        montarTela("DERROTA!", Color.DARKORANGE, Color.ORANGE, Color.WHITE, 100);
    }

    /** Fim de jogo por vitória: o Espalha Lixo foi derrotado. */
    public static void terminarWinner() {
        montarTela("VITÓRIA!", Color.GRAY, Color.WHITE, Color.BLACK, 75);
    }

    private static void montarTela(String mensagem, Color corDoPainel, Color corDoBotao,
                                   Color corDoTexto, double deslocamentoDoTexto) {

        getGameController().pauseEngine();

        Rectangle painel = new Rectangle(LARGURA_DO_PAINEL, ALTURA_DO_PAINEL, corDoPainel);
        painel.setX(getAppWidth() / 2.0 - LARGURA_DO_PAINEL / 2);
        painel.setY(TOPO_DO_PAINEL);

        Button botaoReiniciar = new Button("REINICIAR");
        botaoReiniciar.setTranslateX(getAppWidth() / 2.0 - LARGURA_DO_BOTAO / 2);
        botaoReiniciar.setTranslateY(TOPO_DO_PAINEL);
        botaoReiniciar.setBackground(Background.fill(corDoBotao));
        botaoReiniciar.setPrefWidth(LARGURA_DO_BOTAO);
        botaoReiniciar.setPrefHeight(ALTURA_DO_BOTAO);
        botaoReiniciar.setOnAction(evento -> reiniciar());

        Text texto = FXGL.getUIFactoryService().newText(mensagem, corDoTexto, FontType.GAME, 54);
        texto.setTranslateX(getAppWidth() / 2.0 - deslocamentoDoTexto);
        texto.setTranslateY(TOPO_DO_PAINEL - 10);
        texto.setMouseTransparent(true);

        getGameScene().addUINodes(painel, botaoReiniciar, texto);
    }

    /*
        Reinicia a partida. Não é preciso mexer em nenhum estado aqui: initGame()
        limpa a interface e recria HUD, Floresta e fábrica do zero.
    */
    public static void reiniciar() {
        FXGL.getGameController().startNewGame();
        FXGL.getGameController().resumeEngine();
    }
}

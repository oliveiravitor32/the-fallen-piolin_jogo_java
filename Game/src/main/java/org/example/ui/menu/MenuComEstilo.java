package org.example.ui.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/*
    Base comum ao menu principal e ao menu de pausa: os dois compartilham o mesmo
    tamanho de botao, a mesma animacao de entrada e a mesma forma de abrir folhas.
*/
public abstract class MenuComEstilo extends FXGLMenu {

    protected static final double LARGURA_DO_BOTAO = 300;
    protected static final double ALTURA_DO_BOTAO = 52;
    protected static final double ESPACO_ENTRE_BOTOES = 14;

    private static final Duration DURACAO_DA_ENTRADA = Duration.millis(420);
    private static final double SUBIDA_DA_ENTRADA = 22;

    protected MenuComEstilo(MenuType tipo) {
        super(tipo);
    }

    protected Button botao(String rotulo, EstiloDoMenu.Tipo tipo, Runnable acao) {
        return EstiloDoMenu.botao(rotulo, tipo, LARGURA_DO_BOTAO, ALTURA_DO_BOTAO, acao);
    }

    /** Coloca uma folha por cima do menu e a faz subir. */
    protected void abrirPainel(PainelDeslizante painel) {
        getContentRoot().getChildren().add(painel);
        painel.abrir();
    }

    /*
        Confirmacao de saida, usada pelos dois menus. A mensagem muda conforme o
        contexto: so faz sentido avisar sobre perda de progresso durante uma partida.
    */
    protected void confirmarSaida(String mensagem) {
        abrirPainel(PaineisDoMenu.confirmarSaida(getAppWidth(), getAppHeight(),
                mensagem, () -> getController().exit()));
    }

    protected void abrirControles() {
        abrirPainel(PaineisDoMenu.controles(getAppWidth(), getAppHeight()));
    }

    /** O elemento surge subindo alguns pixels, dando peso a abertura da tela. */
    protected void animarSubida(Region alvo, Duration atraso) {
        alvo.setOpacity(0);
        alvo.setTranslateY(SUBIDA_DA_ENTRADA);

        FadeTransition surgimento = new FadeTransition(DURACAO_DA_ENTRADA, alvo);
        surgimento.setToValue(1);
        surgimento.setDelay(atraso);

        TranslateTransition subida = new TranslateTransition(DURACAO_DA_ENTRADA, alvo);
        subida.setToY(0);
        subida.setInterpolator(Interpolator.SPLINE(0.32, 0.72, 0, 1));
        subida.setDelay(atraso);

        surgimento.play();
        subida.play();
    }
}

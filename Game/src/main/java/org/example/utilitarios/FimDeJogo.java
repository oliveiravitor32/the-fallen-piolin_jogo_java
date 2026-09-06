package org.example.utilitarios;

import com.almasb.fxgl.dsl.FXGL;
import org.example.ui.menu.EstiloDaInterface;
import org.example.ui.menu.PainelCentral;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameController;

/*
    Telas de fim de partida (vitoria e derrota).

    Antes eram um retangulo colorido com um Button padrao do JavaFX, montado a mao em
    dois blocos quase identicos. Agora usam o PainelCentral, com a mesma arrumacao
    das demais telas cheias do jogo.
*/
public class FimDeJogo {

    private FimDeJogo() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    /** Derrota: o Piolin caiu. */
    public static void terminarLoserPorPiolin() {
        mostrar("Derrota!", "O Piolin não resistiu aos disparos do Espalha Lixo.",
                "Tentar de novo");
    }

    /** Derrota: a floresta queimou por inteiro. */
    public static void terminarLoserPorFloresta() {
        mostrar("Derrota!", "A floresta foi consumida pelo fogo.", "Tentar de novo");
    }

    /** Vitoria: o Espalha Lixo foi derrotado. */
    public static void terminarWinner() {
        mostrar("Vitória!", "O Espalha Lixo foi derrotado e a floresta está a salvo.",
                "Jogar de novo");
    }

    /*
        O desfecho ocupa o centro da tela, com a mesma arrumacao dos menus, e nao ha
        como dispensa-lo: o motor fica pausado atras, entao sair dele sem escolher uma
        das acoes deixaria o jogador preso numa partida congelada.
    */
    private static void mostrar(String titulo, String mensagem, String rotuloDeReinicio) {
        getGameController().pauseEngine();

        PainelCentral painel = new PainelCentral(getAppWidth(), getAppHeight(), titulo)
                .comMensagem(mensagem)
                .comAcao(rotuloDeReinicio, EstiloDaInterface.Tipo.PRIMARIO, FimDeJogo::reiniciar)
                .comAcao("Menu principal", EstiloDaInterface.Tipo.VIDRO, FimDeJogo::voltarAoMenu);

        getGameScene().addUINode(painel);
        painel.abrir();
    }

    /*
        Reinicia a partida. Nao e preciso mexer em nenhum estado aqui: initGame()
        limpa a interface e recria HUD, Floresta e fabrica do zero.
    */
    public static void reiniciar() {
        FXGL.getGameController().startNewGame();
        FXGL.getGameController().resumeEngine();
    }

    private static void voltarAoMenu() {
        FXGL.getGameController().gotoMainMenu();
        FXGL.getGameController().resumeEngine();
    }
}

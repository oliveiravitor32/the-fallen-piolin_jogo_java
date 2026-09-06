package org.example.ui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.example.ui.menu.EstiloDaInterface;

import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;

/*
    Dono único de toda a interface de jogo (as três barras de vida).

    Antes, cada componente criava seus próprios nós de UI dentro do construtor e
    nunca os removia. A cada reinício as barras eram empilhadas umas sobre as outras.
    Agora o HUD é criado uma vez por partida em Main.initGame() e removido por
    inteiro em remover(), de modo que nenhum nó sobrevive a um reinício.
*/
public class Hud {

    private final BarraDeVida barraDoPiolin;
    private final BarraDeVida barraDoEspalhaLixo;
    private final BarraDeVida barraDaFloresta;

    /*
        Deslocamentos e tamanhos do interior de cada moldura, medidos direto nos PNGs.
        Piolin e Espalha Lixo usam molduras do mesmo formato.
    */
    private static final double INTERIOR_X = 36;
    private static final double INTERIOR_Y = 4;
    private static final double INTERIOR_LARGURA = 100;
    private static final double INTERIOR_ALTURA = 30;

    private static final double FLORESTA_LARGURA_DA_MOLDURA = 324;
    private static final double FLORESTA_INTERIOR_X = 12;
    private static final double FLORESTA_INTERIOR_Y = 35;
    private static final double FLORESTA_INTERIOR_LARGURA = 300;
    private static final double FLORESTA_INTERIOR_ALTURA = 60;

    public Hud() {
        barraDoPiolin = new BarraDeVida(
                "assets/textures/barra_de_vida_piolin.png",
                50, 52,
                INTERIOR_X, INTERIOR_Y, INTERIOR_LARGURA, INTERIOR_ALTURA,
                Color.web(EstiloDaInterface.VERDE));

        barraDoEspalhaLixo = new BarraDeVida(
                "assets/textures/barra_de_vida_espalha_lixo.png",
                getAppWidth() - 200, 50,
                INTERIOR_X, INTERIOR_Y, INTERIOR_LARGURA, INTERIOR_ALTURA,
                Color.web(EstiloDaInterface.VERMELHO));

        barraDaFloresta = new BarraDeVida(
                "assets/textures/barra_de_vida_floresta.png",
                Math.round(getAppWidth() / 2.0 - FLORESTA_LARGURA_DA_MOLDURA / 2), 20,
                FLORESTA_INTERIOR_X, FLORESTA_INTERIOR_Y,
                FLORESTA_INTERIOR_LARGURA, FLORESTA_INTERIOR_ALTURA,
                Color.web(EstiloDaInterface.AZUL_AGUA));

        for (Node no : todosOsNos()) {
            getGameScene().addUINode(no);
        }
    }

    public void atualizarPiolin(double fracao) {
        barraDoPiolin.atualizar(fracao);
    }

    public void atualizarEspalhaLixo(double fracao) {
        barraDoEspalhaLixo.atualizar(fracao);
    }

    public void atualizarFloresta(double fracao) {
        barraDaFloresta.atualizar(fracao);
    }

    /** Retira todas as barras da cena. Chamado ao encerrar a partida. */
    public void remover() {
        for (Node no : todosOsNos()) {
            getGameScene().removeUINode(no);
        }
    }

    private Node[] todosOsNos() {
        Node[] piolin = barraDoPiolin.getNos();
        Node[] espalhaLixo = barraDoEspalhaLixo.getNos();
        Node[] floresta = barraDaFloresta.getNos();

        Node[] todos = new Node[piolin.length + espalhaLixo.length + floresta.length];
        System.arraycopy(piolin, 0, todos, 0, piolin.length);
        System.arraycopy(espalhaLixo, 0, todos, piolin.length, espalhaLixo.length);
        System.arraycopy(floresta, 0, todos, piolin.length + espalhaLixo.length, floresta.length);

        return todos;
    }
}

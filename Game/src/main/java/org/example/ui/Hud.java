package org.example.ui;

import javafx.scene.Node;
import javafx.scene.paint.Color;

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

    public Hud() {
        barraDoPiolin = new BarraDeVida(
                "assets/textures/barra_de_vida_piolin.png", 38,
                getAppWidth() - 1000, 52,
                getAppWidth() - 964, 56,
                100, 30, Color.GREEN);

        barraDoEspalhaLixo = new BarraDeVida(
                "assets/textures/barra_de_vida_espalha_lixo.png", 38,
                getAppWidth() - 200, 50,
                getAppWidth() - 164, 54,
                100, 30, Color.DARKRED);

        barraDaFloresta = new BarraDeVida(
                "assets/textures/barra_de_vida_floresta.png", 85,
                getAppWidth() / 2.0 - 125, 20,
                getAppWidth() / 2.0 - 115, 48,
                247, 50, Color.LIGHTBLUE);

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

package org.example;

import org.example.ui.Hud;
import org.example.utilitarios.FimDeJogo;
import org.example.utilitarios.Vida;

/*
    Vida coletiva da floresta, compartilhada por todos os objetos combustíveis do mapa.

    Antes esta classe era mantida num campo "static" dentro de ObjetoCombustivelComponent.
    Como "static" sobrevive ao reinício do jogo, a floresta começava a nova partida com a
    vida da partida anterior e uma barra de UI extra era empilhada a cada reinício.
    Agora uma instância nova é criada por partida em Main.initGame() e injetada nos
    componentes pela MainFactory.
*/
public class Floresta {

    private static final int VIDA_MAXIMA = 30;
    private static final int DANO_POR_ACERTO = 3;
    private static final int CURA_POR_ACERTO = 5;

    private final Vida vida = new Vida(VIDA_MAXIMA);
    private final Hud hud;

    public Floresta(Hud hud) {
        this.hud = hud;
        hud.atualizarFloresta(vida.getFracao());
    }

    public void tomarDano() {
        vida.tomarDano(DANO_POR_ACERTO);
        hud.atualizarFloresta(vida.getFracao());

        if (vida.estaZerada()) {
            FimDeJogo.terminarLoserPorFloresta();
        }
    }

    public void recuperarVida() {
        vida.curar(CURA_POR_ACERTO);
        hud.atualizarFloresta(vida.getFracao());
    }
}

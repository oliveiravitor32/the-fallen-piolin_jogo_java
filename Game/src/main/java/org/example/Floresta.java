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

    /*
        Vida, dano e cura vem do menu Ajustes (ver Ajustes). Sao lidos quando a
        floresta nasce, entao valem para a partida inteira.
    */
    private final int danoPorAcerto = Ajustes.DANO_DO_FOGO.getInt();

    /*
        Cura ao apagar um objeto, POR disparo de fogo que ele levou. No padrao e igual
        ao dano: apagar a tempo devolve exatamente o que o fogo tirou. Antes apagar
        devolvia 5 de uma vez, qualquer que fosse o estrago, e o jogador podia sair
        no lucro so por apagar.
    */
    private final int curaPorAcertoApagado = Ajustes.CURA_DA_AGUA.getInt();

    private final Vida vida = new Vida(Ajustes.VIDA_DA_FLORESTA.getInt());
    private final Hud hud;

    public Floresta(Hud hud) {
        this.hud = hud;
        hud.atualizarFloresta(vida.getFracao());
    }

    public void tomarDano() {
        vida.tomarDano(danoPorAcerto);
        hud.atualizarFloresta(vida.getFracao());

        if (vida.estaZerada()) {
            FimDeJogo.terminarLoserPorFloresta();
        }
    }

    /** Cura proporcional a quantos disparos de fogo o objeto apagado tinha levado. */
    public void recuperarVida(int acertosApagados) {
        vida.curar(curaPorAcertoApagado * acertosApagados);
        hud.atualizarFloresta(vida.getFracao());
    }
}

package org.example;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

import java.util.Comparator;
import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

/*
    CEREBRO do Espalha Lixo. O corpo que executa as ordens e o EnemyComponent.

    O que ele fazia antes: sorteava uma direcao a cada segundo e, se o Piolin
    estivesse a menos de 400 unidades, jogava cara-ou-coroa para atirar nele. A
    floresta, que e o objetivo declarado do vilao, so pegava fogo por acidente --
    quando um tiro perdido acertava um objeto combustivel. A arma de agua do jogador
    quase nunca era necessaria.

    Agora ele tem uma intencao: ATEAR FOGO. Caminha ate o objeto combustivel mais
    proximo que ainda nao esta queimando e o incendeia. So larga esse plano quando o
    Piolin chega perto demais, e volta a ele assim que a ameaca se afasta.

    Sao tres estados:

      INCENDIANDO  procura combustivel, anda ate ele e atira (padrao)
      DEFENDENDO   o jogador esta perto: vira contra ele e atira
      CACANDO      nao ha combustivel alcancavel: vai atras do jogador
*/
public class IaDoEspalhaLixo extends Component {

    private enum Estado { INCENDIANDO, DEFENDENDO, CACANDO }

    /*
        A partir desta distancia o jogador vira a prioridade. Com zoom 2.5 a area
        visivel tem cerca de 420 unidades, entao ele reage quando voces se enxergam.
    */
    private static final double DISTANCIA_DE_AMEACA = 260;

    /*
        De quao perto ele precisa estar para acertar. O projetil anda 300 por segundo
        e vive 1,2s, entao alem de ~360 unidades o tiro morre antes de chegar.
    */
    private static final double ALCANCE_DE_TIRO = 250;

    /*
        O disparo e horizontal, entao um objeto muito acima ou abaixo e inalcancavel.
        Sem este filtro a IA caminharia eternamente ate um alvo que nunca conseguiria
        acertar, e o inimigo travaria contra uma parede.
    */
    private static final double DESNIVEL_MAXIMO = 90;

    // Folga para ele nao ficar tremendo entre andar e parar em cima do alvo
    private static final double MARGEM_DE_PARADA = 40;

    /*
        De quao perto ele chega do combustivel antes de atear fogo.

        Precisa ser curto: o disparo e horizontal e para no primeiro objeto que
        encontra, entao mirar de longe fazia o tiro morrer numa arvore ja em chamas no
        meio do caminho. O alvo escolhido nunca acendia, e ele repetia o disparo sem
        sair do lugar -- de fora, parecia que estava queimando a mesma arvore para sempre.
    */
    private static final double DISTANCIA_PARA_INCENDIAR = 80;

    /*
        Faixa de distancia que ele tenta manter do Piolin enquanto o enfrenta. Perto
        demais, recua; longe demais, avanca. Antes ele simplesmente parava e virava
        uma torre fixa.
    */
    private static final double DISTANCIA_MINIMA_DE_COMBATE = 130;
    private static final double DISTANCIA_MAXIMA_DE_COMBATE = 230;

    /*
        Quanto tempo ele no maximo larga o incendio para enfrentar o Piolin, e quanto
        espera antes de poder largar de novo.

        Sem esse limite ele ficava presos em DEFENDENDO: bastava o jogador parar por
        perto para o Espalha Lixo estacionar e atirar no mesmo ponto ate o fim da
        partida. Com o limite ele sempre volta a circular pelo mapa.
    */
    private static final double SEGUNDOS_MAXIMOS_DEFENDENDO = 2.5;
    private static final double SEGUNDOS_DE_DESCANSO = 3.0;

    private EnemyComponent corpo;

    private Estado estado = Estado.INCENDIANDO;
    private double tempoDefendendo = 0;
    private double tempoDesdeAUltimaDefesa = SEGUNDOS_DE_DESCANSO;

    @Override
    public void onUpdate(double tpf) {
        // Durante o preparo do disparo ele fica travado no lugar, de proposito
        if (corpo.estaOcupado()) {
            return;
        }

        Optional<Entity> jogador = getGameWorld().getSingletonOptional(EntityType.JOGADOR);

        // Sem jogador em cena (contagem regressiva, fim de partida) nao ha o que decidir
        if (jogador.isEmpty()) {
            corpo.parar();
            return;
        }

        decidirEstado(jogador.get(), tpf);

        switch (estado) {
            case DEFENDENDO -> atacar(jogador.get());
            case INCENDIANDO -> incendiar(jogador.get());
            case CACANDO -> perseguir(jogador.get());
        }
    }

    private void decidirEstado(Entity jogador, double tpf) {
        if (estado == Estado.DEFENDENDO) {
            tempoDefendendo += tpf;

            // Encerra a defesa por tempo, mesmo com o jogador ainda por perto
            if (tempoDefendendo < SEGUNDOS_MAXIMOS_DEFENDENDO) {
                return;
            }

            tempoDesdeAUltimaDefesa = 0;
        }
        else {
            tempoDesdeAUltimaDefesa += tpf;
        }

        boolean ameacado = getEntity().distance(jogador) < DISTANCIA_DE_AMEACA;

        if (ameacado && tempoDesdeAUltimaDefesa >= SEGUNDOS_DE_DESCANSO) {
            estado = Estado.DEFENDENDO;
            tempoDefendendo = 0;
            return;
        }

        estado = procurarCombustivel().isPresent() ? Estado.INCENDIANDO : Estado.CACANDO;
    }

    /*
        O jogador esta perto: mantem uma faixa de distancia dele e atira. Recua se
        estiver colado, avanca se estiver longe demais -- so fica parado dentro da
        faixa, e mesmo assim por pouco tempo.
    */
    private void atacar(Entity jogador) {
        double distancia = distanciaHorizontal(jogador);
        double jogadorX = jogador.getCenter().getX();
        double meuX = getEntity().getCenter().getX();

        if (distancia < DISTANCIA_MINIMA_DE_COMBATE) {
            // Recua para o lado oposto ao do jogador
            corpo.moverEmDirecaoA(meuX + (jogadorX < meuX ? 1 : -1) * DISTANCIA_MAXIMA_DE_COMBATE);
        }
        else if (distancia > DISTANCIA_MAXIMA_DE_COMBATE) {
            corpo.moverEmDirecaoA(jogadorX);
        }
        else {
            corpo.parar();
        }

        corpo.encarar(jogador);

        if (corpo.podeAtirar()) {
            corpo.atirar(jogador);
        }
    }

    /** Plano principal: chegar ao combustivel escolhido e atear fogo nele. */
    private void incendiar(Entity jogador) {
        Optional<Entity> alvo = procurarCombustivel();

        if (alvo.isEmpty()) {
            perseguir(jogador);
            return;
        }

        avancarEAtirar(alvo.get(), DISTANCIA_PARA_INCENDIAR);
    }

    /** Sem combustivel ao alcance, o alvo passa a ser o proprio Piolin. */
    private void perseguir(Entity jogador) {
        avancarEAtirar(jogador, ALCANCE_DE_TIRO - MARGEM_DE_PARADA);
    }

    private void avancarEAtirar(Entity alvo, double distanciaDeParada) {
        double distanciaHorizontal =
                Math.abs(alvo.getCenter().getX() - getEntity().getCenter().getX());

        if (distanciaHorizontal > distanciaDeParada) {
            corpo.moverEmDirecaoA(alvo.getCenter().getX());
            return;
        }

        corpo.parar();
        corpo.encarar(alvo);

        if (corpo.podeAtirar()) {
            corpo.atirar(alvo);
        }
    }

    /*
        Escolhe o combustivel a incendiar: o mais proximo na horizontal, entre os que
        ainda nao pegaram fogo e estao num desnivel que o disparo alcanca.

        Objetos ja em chamas ficam de fora de proposito. Numa versao anterior a IA
        caia de volta no mais proximo quando nao havia nenhum apagado, e o efeito era
        o Espalha Lixo estacionar diante da mesma arvore atirando nela sem parar. Sem
        alvo novo ele parte para cima do Piolin, e volta a incendiar assim que o
        jogador apagar alguma chama.
    */
    private Optional<Entity> procurarCombustivel() {
        return getGameWorld()
                .getEntitiesByType(EntityType.OBJETO_COMBUSTIVEL)
                .stream()
                .filter(this::estaNoMesmoNivel)
                .filter(objeto -> !objeto.getComponent(ObjetoCombustivelComponent.class).estaEmChamas())
                .min(Comparator.comparingDouble(this::distanciaHorizontal));
    }

    private boolean estaNoMesmoNivel(Entity objeto) {
        return Math.abs(objeto.getCenter().getY() - getEntity().getCenter().getY()) < DESNIVEL_MAXIMO;
    }

    private double distanciaHorizontal(Entity alvo) {
        return Math.abs(alvo.getCenter().getX() - getEntity().getCenter().getX());
    }
}

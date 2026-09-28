package org.example;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

/*
    CEREBRO do Espalha Lixo. O corpo que executa as ordens e o EnemyComponent.

    O jogo e uma luta de chefe com um segundo objetivo: o Piolin precisa derrotar o
    Espalha Lixo E manter a floresta viva. A IA existe para criar esse dilema -- se
    eu vou atras dele, a floresta queima; se vou apagar o fogo, ele me ataca.

    Por isso ela e uma IA de UTILIDADE (utility AI), a tecnica usada quando um
    personagem precisa pesar objetivos que competem entre si. A cada meio segundo
    cada acao recebe uma nota de 0 a 1 a partir do que esta acontecendo, e a de
    maior nota vence:

      INCENDIAR  vale mais quando ha combustivel perto e o Piolin esta longe
      ENFRENTAR  vale mais quando o Piolin esta perto, e mais ainda com furia
      PERSEGUIR  so vale quando nao sobrou nada apagado para queimar

    A versao anterior era uma maquina de estados com regras fixas, e o problema dela
    era ficar PARADA: parava ao chegar no alvo, parava na faixa de combate, e nao
    alcancava nada fora do proprio andar. Agora a execucao de cada acao mantem o
    corpo sempre em movimento, como os chefes de jogos de plataforma:

      - em combate ele circula (strafe) de um lado para o outro e pula para desviar;
      - alvos em outro andar sao alcancados pulando;
      - se empaca numa parede, pula; se mesmo assim nao chega, desiste daquele alvo
        por um tempo em vez de ficar tentando para sempre.

    Uma acao escolhida fica "comprometida" por um tempo minimo, para ele nao trocar
    de ideia a cada quadro e parecer indeciso.
*/
public class IaDoEspalhaLixo extends Component {

    private enum Acao { INCENDIAR, ENFRENTAR, PERSEGUIR }

    // ------------------------------------------------------------ balanceamento

    /** De quanto em quanto tempo as notas sao recalculadas. */
    private static final double INTERVALO_DE_DECISAO = 0.5;

    /** Tempo minimo numa acao antes de poder trocar por outra. */
    private static final double COMPROMISSO_MINIMO = 1.5;

    /*
        Depois deste tempo seguido enfrentando o Piolin, a nota de ENFRENTAR vai
        caindo ate zero. E o que o faz largar a briga e voltar a incendiar, mesmo com
        o jogador colado nele -- o dilema do jogo depende disso.
    */
    private final double folegoDeCombate = Ajustes.TEMPO_DE_BRIGA.get();

    /** Distancias usadas para dar nota: acima delas, "longe"; abaixo, "perto". */
    private static final double DISTANCIA_DE_AMEACA = 320;
    private static final double DISTANCIA_DE_INTERESSE = 900;

    /*
        O disparo e horizontal e sai na altura do peito, entao so acerta o que esta
        mais ou menos na mesma altura. Com o pulo ele sobe e desce de andar para
        chegar nessa condicao.
    */
    private static final double DESNIVEL_DE_TIRO = 60;

    /*
        De quao perto ele chega do combustivel antes de atear fogo. Curto de
        proposito: o tiro para no primeiro objeto que encontra, e de longe ele
        batia numa arvore ja em chamas no meio do caminho.
    */
    private static final double DISTANCIA_PARA_INCENDIAR = 80;

    /** Faixa de distancia que ele circula em volta do Piolin durante o combate. */
    private static final double DISTANCIA_MINIMA_DE_COMBATE = 120;
    private static final double DISTANCIA_MAXIMA_DE_COMBATE = 240;

    /** A cada quanto tempo ele escolhe um novo ponto para onde circular. */
    private static final double TROCA_DE_POSICAO_MIN = 0.7;
    private static final double TROCA_DE_POSICAO_MAX = 1.4;

    /** Chance por segundo de dar um pulo de esquiva em combate. */
    private static final double CHANCE_DE_ESQUIVA = 0.45;

    /** Quanto tempo querendo andar sem sair do lugar conta como "empacado". */
    private static final double TEMPO_PARA_EMPACAR = 0.35;

    /** Tempo tentando chegar num combustivel antes de desistir dele. */
    private static final double PACIENCIA_COM_ALVO = 6.0;
    private static final double TEMPO_DE_DESISTENCIA = 8.0;

    // ------------------------------------------------------------------- estado

    private EnemyComponent corpo;

    private Acao acao = Acao.INCENDIAR;
    private double tempoNaAcao = 0;
    private double tempoAteDecidir = 0;
    private double tempoEnfrentando = 0;

    private Entity alvoDeFogo;
    private double tempoNoAlvo = 0;
    private final Map<Entity, Double> alvosIgnorados = new HashMap<>();

    private double pontoDeCombateX = Double.NaN;
    private double tempoAteTrocarPosicao = 0;

    private double ultimoX = Double.NaN;
    private double tempoEmpacado = 0;

    @Override
    public void onUpdate(double tpf) {
        esquecerAlvosIgnorados(tpf);

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

        decidir(jogador.get(), tpf);

        switch (acao) {
            case INCENDIAR -> incendiar(jogador.get(), tpf);
            case ENFRENTAR -> enfrentar(jogador.get(), tpf);
            case PERSEGUIR -> perseguir(jogador.get());
        }
    }

    // ---------------------------------------------------------------- decisao

    private void decidir(Entity jogador, double tpf) {
        tempoNaAcao += tpf;
        tempoAteDecidir -= tpf;
        tempoEnfrentando = acao == Acao.ENFRENTAR ? tempoEnfrentando + tpf : 0;

        if (tempoAteDecidir > 0) {
            return;
        }

        tempoAteDecidir = INTERVALO_DE_DECISAO;

        Acao melhor = Acao.PERSEGUIR;
        double melhorNota = notaPerseguir();

        double incendiar = notaIncendiar(jogador);
        if (incendiar > melhorNota) {
            melhor = Acao.INCENDIAR;
            melhorNota = incendiar;
        }

        double enfrentar = notaEnfrentar(jogador);
        if (enfrentar > melhorNota) {
            melhor = Acao.ENFRENTAR;
        }

        // Troca so depois do compromisso minimo, exceto para fugir de uma acao impossivel
        boolean acaoAtualImpossivel = acao == Acao.INCENDIAR && incendiar == 0;

        if (melhor != acao && (tempoNaAcao >= COMPROMISSO_MINIMO || acaoAtualImpossivel)) {
            acao = melhor;
            tempoNaAcao = 0;
            pontoDeCombateX = Double.NaN;
        }
    }

    /*
        Quanto mais perto o combustivel e mais longe o Piolin, maior a vontade de
        queimar: e quando o jogador nao consegue impedir.
    */
    private double notaIncendiar(Entity jogador) {
        Optional<Entity> alvo = escolherCombustivel();

        if (alvo.isEmpty()) {
            return 0;
        }

        double pertoDoAlvo = 1 - limitar(getEntity().distance(alvo.get()) / DISTANCIA_DE_INTERESSE);
        double longeDoJogador = limitar(getEntity().distance(jogador) / DISTANCIA_DE_AMEACA);

        return 0.35 + 0.35 * pertoDoAlvo + 0.3 * longeDoJogador;
    }

    private double notaEnfrentar(Entity jogador) {
        double perto = 1 - limitar(getEntity().distance(jogador) / DISTANCIA_DE_AMEACA);

        // Com furia ele briga mais, e o folego dura o mesmo
        double agressividade = corpo.estaFurioso() ? 1.0 : 0.9;

        double folego = 1 - limitar((tempoEnfrentando - folegoDeCombate) / folegoDeCombate);

        return perto * agressividade * folego;
    }

    /** Nota fixa e baixa: so ganha quando nao ha mais nada para fazer. */
    private double notaPerseguir() {
        return 0.2;
    }

    // --------------------------------------------------------------- execucao

    /** Vai ate o combustivel escolhido (pulando se preciso) e ateia fogo. */
    private void incendiar(Entity jogador, double tpf) {
        Optional<Entity> alvo = escolherCombustivel();

        if (alvo.isEmpty()) {
            perseguir(jogador);
            return;
        }

        if (alvo.get() != alvoDeFogo) {
            alvoDeFogo = alvo.get();
            tempoNoAlvo = 0;
        }

        tempoNoAlvo += tpf;

        if (tempoNoAlvo > PACIENCIA_COM_ALVO) {
            // Nao consegue chegar: deixa esse de lado e escolhe outro no proximo quadro
            alvosIgnorados.put(alvoDeFogo, TEMPO_DE_DESISTENCIA);
            alvoDeFogo = null;
            return;
        }

        irAteEAtirar(alvoDeFogo, DISTANCIA_PARA_INCENDIAR, tpf);
    }

    /*
        Combate: circula pela faixa de distancia em volta do Piolin, trocando de
        ponto a cada pouco tempo, pula de vez em quando para desviar das penas e
        atira sempre que puder.
    */
    private void enfrentar(Entity jogador, double tpf) {
        tempoAteTrocarPosicao -= tpf;

        if (Double.isNaN(pontoDeCombateX) || tempoAteTrocarPosicao <= 0) {
            escolherPontoDeCombate(jogador);
        }

        double meuX = getEntity().getCenter().getX();

        if (Math.abs(pontoDeCombateX - meuX) > 15) {
            corpo.moverEmDirecaoA(pontoDeCombateX);
            detectarEmpaque(tpf);
        }
        else {
            corpo.parar();
        }

        // Pulo de esquiva, e tambem para subir ate o Piolin se ele estiver acima
        boolean jogadorAcima = jogador.getCenter().getY() < getEntity().getCenter().getY() - DESNIVEL_DE_TIRO;

        if (corpo.estaNoChao() && (jogadorAcima || FXGLMath.randomBoolean(CHANCE_DE_ESQUIVA * tpf))) {
            corpo.pular();
        }

        if (corpo.podeAtirar() && naMesmaAltura(jogador)) {
            corpo.atirar(jogador);
        }
        else {
            corpo.encarar(jogador);
        }
    }

    /*
        Sorteia um ponto na faixa de combate, de um lado ou do outro do Piolin. Trocar
        de lado de vez em quando o faz cruzar por cima/por baixo do jogador, em vez
        de so recuar e avancar numa linha.
    */
    private void escolherPontoDeCombate(Entity jogador) {
        double jogadorX = jogador.getCenter().getX();
        double meuX = getEntity().getCenter().getX();

        int lado = meuX < jogadorX ? -1 : 1;

        if (FXGLMath.randomBoolean(0.25)) {
            lado = -lado;
        }

        double distancia = FXGLMath.random(DISTANCIA_MINIMA_DE_COMBATE, DISTANCIA_MAXIMA_DE_COMBATE);

        pontoDeCombateX = jogadorX + lado * distancia;
        tempoAteTrocarPosicao = FXGLMath.random(TROCA_DE_POSICAO_MIN, TROCA_DE_POSICAO_MAX);
    }

    /** Sem combustivel, o alvo passa a ser o proprio Piolin. */
    private void perseguir(Entity jogador) {
        irAteEAtirar(jogador, DISTANCIA_MAXIMA_DE_COMBATE, 0);
    }

    private void irAteEAtirar(Entity alvo, double distanciaDeParada, double tpf) {
        double distancia = distanciaHorizontal(alvo);
        boolean alvoAcima = alvo.getCenter().getY() < getEntity().getCenter().getY() - DESNIVEL_DE_TIRO;

        if (distancia > distanciaDeParada) {
            corpo.moverEmDirecaoA(alvo.getCenter().getX());
            detectarEmpaque(tpf);
        }
        else {
            corpo.parar();
        }

        // Chegando perto de algo num andar acima, sobe pulando
        if (alvoAcima && distancia < distanciaDeParada + 120 && corpo.estaNoChao()) {
            corpo.pular();
        }

        if (distancia <= distanciaDeParada && naMesmaAltura(alvo) && corpo.podeAtirar()) {
            corpo.atirar(alvo);
        }
    }

    /*
        Se ele esta tentando andar mas o X nao muda, bateu numa parede ou num degrau:
        pula. Sem isso ele ficava empurrando o obstaculo com a animacao de caminhada.
    */
    private void detectarEmpaque(double tpf) {
        double x = getEntity().getX();

        if (!Double.isNaN(ultimoX) && Math.abs(x - ultimoX) < 0.5) {
            tempoEmpacado += tpf;
        }
        else {
            tempoEmpacado = 0;
        }

        ultimoX = x;

        if (tempoEmpacado > TEMPO_PARA_EMPACAR && corpo.pular()) {
            tempoEmpacado = 0;
        }
    }

    // ----------------------------------------------------------------- auxiliares

    /*
        O combustivel mais proximo que ainda nao pegou fogo e do qual ele nao
        desistiu. Objetos ja em chamas ficam de fora: numa versao anterior ele
        estacionava diante da mesma arvore atirando nela sem parar.
    */
    private Optional<Entity> escolherCombustivel() {
        return getGameWorld()
                .getEntitiesByType(EntityType.OBJETO_COMBUSTIVEL)
                .stream()
                .filter(objeto -> !alvosIgnorados.containsKey(objeto))
                .filter(objeto -> !objeto.getComponent(ObjetoCombustivelComponent.class).estaEmChamas())
                .min(Comparator.comparingDouble(objeto -> getEntity().distance(objeto)));
    }

    private void esquecerAlvosIgnorados(double tpf) {
        alvosIgnorados.replaceAll((objeto, restante) -> restante - tpf);
        alvosIgnorados.values().removeIf(restante -> restante <= 0);
    }

    private boolean naMesmaAltura(Entity alvo) {
        return Math.abs(alvo.getCenter().getY() - getEntity().getCenter().getY()) < DESNIVEL_DE_TIRO;
    }

    private double distanciaHorizontal(Entity alvo) {
        return Math.abs(alvo.getCenter().getX() - getEntity().getCenter().getX());
    }

    private static double limitar(double valor) {
        return Math.max(0, Math.min(1, valor));
    }
}

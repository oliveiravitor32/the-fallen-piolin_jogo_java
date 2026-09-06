package org.example;

import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.example.ui.Hud;
import org.example.utilitarios.FimDeJogo;
import org.example.utilitarios.Vida;

import static com.almasb.fxgl.dsl.FXGLForKtKt.entityBuilder;
import static com.almasb.fxgl.dsl.FXGLForKtKt.image;
import static org.example.EntityType.DISPARO_INIMIGO;

/*
    CORPO do inimigo (Espalha Lixo): visual, movimento, disparo e vida.

    As decisoes ficam separadas, em IaDoEspalhaLixo: esta classe apenas executa o que
    lhe pedem. Antes ela tambem decidia, sorteando uma direcao a cada segundo, e o
    resultado era um vaivem sem intencao nenhuma.
*/
public class EnemyComponent extends Component {

    private static final double ESCALA = 0.8;
    private static final int VIDA_MAXIMA = 30;

    private static final double VELOCIDADE = 180;
    private static final double VELOCIDADE_DO_PROJETIL = 300;

    /*
        Tempo de vida do disparo. Junto com a velocidade, define o alcance real:
        300 x 1,2s = 360 unidades.
    */
    private static final Duration DURACAO_DO_PROJETIL = Duration.seconds(1.2);

    /*
        Espera entre disparos. Atirar no jogador e mais rapido do que atear fogo:
        o incendio precisa dar tempo de o Piolin cruzar o mapa e apagar as chamas.
    */
    private static final Duration ESPERA_CONTRA_JOGADOR = Duration.millis(700);
    private static final Duration ESPERA_CONTRA_OBJETO = Duration.millis(2200);

    /*
        Preparo antes de o projetil sair: o inimigo trava no lugar e toca a animacao
        de tiro. Antes o fogo aparecia no mesmo quadro em que ele decidia atirar, sem
        aviso nenhum; com o preparo da para ver o disparo vindo e sair da frente.
    */
    private static final Duration TEMPO_DE_PREPARO = Duration.millis(260);

    /*
        Abaixo desta fracao de vida o Espalha Lixo se enfurece: anda mais rapido,
        atira com menos espera e a animacao de caminhada acelera, para a mudanca
        ficar visivel.
    */
    private static final double LIMIAR_DE_FURIA = 1.0 / 3.0;
    private static final double MULTIPLICADOR_DE_VELOCIDADE_FURIOSO = 1.45;
    private static final double MULTIPLICADOR_DE_ESPERA_FURIOSO = 0.6;

    private PhysicsComponent physics;

    // Injetando componentes para gerar animação ao visual (sprite) do inimigo
    private final AnimatedTexture texture;
    private final AnimationChannel animIdle, animWalk, animWalkFurioso, animTiro;

    private final Vida vida = new Vida(VIDA_MAXIMA);
    private final Hud hud;

    private boolean tiroEmEspera = false;
    private boolean preparandoTiro = false;
    private boolean furioso = false;

    public EnemyComponent(Hud hud) {
        this.hud = hud;

        // Definindo o PNG com os quadros (frames) de animação
        Image image = image("whole-espalha-lixot.png");

        animIdle = new AnimationChannel(image, 6, 64, 64, Duration.seconds(1), 0, 0);
        animWalk = new AnimationChannel(image, 6, 64, 64, Duration.seconds(1), 4, 5);
        animWalkFurioso = new AnimationChannel(image, 6, 64, 64, Duration.seconds(0.55), 4, 5);
        animTiro = new AnimationChannel(image, 6, 64, 64, Duration.seconds(0.16), 2, 3);

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(25, 21));
        entity.getViewComponent().addChild(texture);

        entity.setScaleX(ESCALA);
        entity.setScaleY(ESCALA);

        hud.atualizarEspalhaLixo(vida.getFracao());
    }

    // ------------------------------------------------------------------ movimento

    /** Anda na horizontal em direcao a uma coordenada do mundo. */
    public void moverEmDirecaoA(double destinoX) {
        if (destinoX < getEntity().getCenter().getX()) {
            moveParaEsquerda();
        }
        else {
            moveParaDireita();
        }
    }

    public void moveParaEsquerda() {
        andar(-1);
    }

    public void moveParaDireita() {
        andar(1);
    }

    private void andar(int sentido) {
        if (preparandoTiro) {
            return;
        }

        texture.loopAnimationChannel(furioso ? animWalkFurioso : animWalk);
        getEntity().setScaleX(sentido * ESCALA);
        physics.setVelocityX(sentido * velocidade());
    }

    /** Para no lugar e volta a animacao de parado. */
    public void parar() {
        physics.setVelocityX(0);

        if (!preparandoTiro && texture.getAnimationChannel() != animIdle) {
            texture.loopAnimationChannel(animIdle);
        }
    }

    /** Vira para o lado do alvo sem sair do lugar. */
    public void encarar(Entity alvo) {
        boolean paraEsquerda = alvo.getCenter().getX() < getEntity().getCenter().getX();

        getEntity().setScaleX(paraEsquerda ? -ESCALA : ESCALA);
    }

    // -------------------------------------------------------------------- disparo

    /** Verdadeiro enquanto o disparo esta em preparo: a IA nao deve mover o inimigo. */
    public boolean estaOcupado() {
        return preparandoTiro;
    }

    public boolean podeAtirar() {
        return !tiroEmEspera && !preparandoTiro;
    }

    /*
        Dispara contra um alvo. Serve tanto para o jogador quanto para os objetos
        combustiveis: o que muda e so a espera ate o proximo tiro.
    */
    public void atirar(Entity alvo) {
        if (!podeAtirar() || alvo == null || !alvo.isActive()) {
            return;
        }

        boolean paraEsquerda = alvo.getCenter().getX() < getEntity().getCenter().getX();
        boolean alvoEhJogador = alvo.isType(EntityType.JOGADOR);

        preparandoTiro = true;
        tiroEmEspera = true;

        parar();
        encarar(alvo);
        texture.playAnimationChannel(animTiro);
        tocar("fire_launcher.wav", 0.5);

        int restante = vida.getAtual();

        if (restante == 28 || restante == 15 || restante == 12
                || restante == 10 || restante == 5 || restante == 1) {
            tocar("fogo.wav", 0.5);
        }

        // O projetil so nasce ao fim do preparo, dando ao jogador tempo de reagir
        FXGL.getGameTimer().runOnceAfter(() -> {
            preparandoTiro = false;

            if (entity != null && entity.isActive()) {
                lancarProjetil(paraEsquerda);
                texture.loopAnimationChannel(animIdle);
            }
        }, TEMPO_DE_PREPARO);

        Duration espera = alvoEhJogador ? ESPERA_CONTRA_JOGADOR : ESPERA_CONTRA_OBJETO;

        FXGL.getGameTimer().runOnceAfter(() -> tiroEmEspera = false, aplicarFuria(espera));
    }

    /*
        O disparo do inimigo NAO usa OffscreenCleanComponent.

        A camera segue o Piolin, entao o Espalha Lixo passa boa parte da partida fora
        de quadro. Com a limpeza por tela, o projetil nascia fora da vista e era
        destruido no mesmo instante: dos tiros dele, quase nenhum chegava ao alvo, e
        o fogo so contava quando os dois estavam na mesma tela. O que limita o alcance
        aqui e o tempo de vida, como ja acontece com os disparos do jogador.
    */
    private void lancarProjetil(boolean paraEsquerda) {
        double origemY = getEntity().getCenter().getY() - 28;
        double origemX = getEntity().getCenter().getX() - (paraEsquerda ? 40 : 0);

        // Vetor unitário: a direção do projétil não depende de onde o inimigo está no mapa
        Point2D direcao = new Point2D(paraEsquerda ? -1 : 1, 0);

        AnimationChannel animacaoDoTiro =
                new AnimationChannel(image("tiro_de_fogo.png"), 4, 32, 32, Duration.seconds(0.1), 0, 3);

        Entity projetil = entityBuilder()
                .at(origemX, origemY)
                .type(DISPARO_INIMIGO)
                .viewWithBBox(new AnimatedTexture(animacaoDoTiro))
                .collidable()
                .with(new ProjectileComponent(direcao, VELOCIDADE_DO_PROJETIL))
                .scale(ESCALA, paraEsquerda ? -ESCALA : ESCALA)
                .buildAndAttach();

        FXGL.getGameTimer().runOnceAfter(() -> {
            if (projetil.isActive()) {
                projetil.removeFromWorld();
            }
        }, DURACAO_DO_PROJETIL);
    }

    // ------------------------------------------------------------------ vida/furia

    public void tomaDano() {
        vida.tomarDano(1);
        hud.atualizarEspalhaLixo(vida.getFracao());

        if (vida.estaZerada()) {
            entity.removeFromWorld();
            FimDeJogo.terminarWinner();
            return;
        }

        avaliarFuria();

        int restante = vida.getAtual();

        if (restante == 4 || restante == 15 || restante == 22) {
            tocar("haha.wav", 0.5);
        }
        else if (restante == 8 || restante == 18 || restante == 28) {
            tocar("vaiqueimar.wav", 0.8);
        }
    }

    private void avaliarFuria() {
        if (furioso || vida.getFracao() > LIMIAR_DE_FURIA) {
            return;
        }

        furioso = true;
        tocar("haha.wav", 0.8);
    }

    public boolean estaFurioso() {
        return furioso;
    }

    private double velocidade() {
        return furioso ? VELOCIDADE * MULTIPLICADOR_DE_VELOCIDADE_FURIOSO : VELOCIDADE;
    }

    private Duration aplicarFuria(Duration espera) {
        return furioso ? espera.multiply(MULTIPLICADOR_DE_ESPERA_FURIOSO) : espera;
    }

    private void tocar(String arquivo, double volume) {
        Sound som = FXGL.getAssetLoader().loadSound(arquivo);
        som.getAudio().setVolume(volume);
        som.getAudio().play();
    }
}

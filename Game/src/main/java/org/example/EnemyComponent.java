package org.example;

import com.almasb.fxgl.audio.Sound;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.time.TimerAction;
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
    Componente do inimigo (Espalha Lixo): movimentação, disparo de fogo e vida.
*/
public class EnemyComponent extends Component {

    private static final double ESCALA = 0.8;
    private static final int VIDA_MAXIMA = 30;
    private static final double VELOCIDADE = 180;
    private static final Duration ESPERA_ENTRE_DISPAROS = Duration.millis(600);
    private static final Duration INTERVALO_DE_MOVIMENTACAO = Duration.seconds(1);
    private static final Duration DURACAO_DA_PARADA = Duration.millis(400);
    private static final double VELOCIDADE_DO_PROJETIL = 300;

    private PhysicsComponent physics;

    // Injetando componentes para gerar animação ao visual (sprite) do inimigo
    private final AnimatedTexture texture;
    private final AnimationChannel animIdle, animWalk, animTiro;

    private final Vida vida = new Vida(VIDA_MAXIMA);
    private final Hud hud;

    private boolean estaParado = false;
    private double ultimaDirecao = VELOCIDADE;
    private boolean tiroEmEspera = false;
    private TimerAction tarefaDeMovimentacaoAleatoria;

    public EnemyComponent(Hud hud) {
        this.hud = hud;

        // Definindo o PNG com os quadros (frames) de animação
        Image image = image("whole-espalha-lixot.png");

        animIdle = new AnimationChannel(image, 6, 64, 64, Duration.seconds(1), 0, 0);
        animWalk = new AnimationChannel(image, 6, 64, 64, Duration.seconds(1), 4, 5);
        animTiro = new AnimationChannel(image, 6, 64, 64, Duration.seconds(0.16), 2, 3);

        texture = new AnimatedTexture(animWalk);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(25, 21));
        entity.getViewComponent().addChild(texture);

        entity.setScaleX(ESCALA);
        entity.setScaleY(ESCALA);

        hud.atualizarEspalhaLixo(vida.getFracao());

        /*
            A movimentação aleatória só começa depois que a entidade entra no mundo.
            Se fosse iniciada no construtor (como era antes), o temporizador poderia
            disparar antes de o corpo físico existir.
        */
        tarefaDeMovimentacaoAleatoria = FXGL.run(this::movimentacaoAleatoria, INTERVALO_DE_MOVIMENTACAO);
    }

    @Override
    public void onRemoved() {
        // Encerra o temporizador para não continuar rodando após a morte do inimigo
        if (tarefaDeMovimentacaoAleatoria != null) {
            tarefaDeMovimentacaoAleatoria.expire();
            tarefaDeMovimentacaoAleatoria = null;
        }
    }

    @Override
    public void onUpdate(double tpf) {
        if (!estaParado) {
            physics.setVelocityX(ultimaDirecao > 0 ? VELOCIDADE : -VELOCIDADE);
        }
    }

    public void tomaDano() {
        vida.tomarDano(1);
        hud.atualizarEspalhaLixo(vida.getFracao());

        if (vida.estaZerada()) {
            entity.removeFromWorld();
            FimDeJogo.terminarWinner();
            return;
        }

        int restante = vida.getAtual();

        if (restante == 4 || restante == 15 || restante == 22) {
            tocar("haha.wav", 0.5);
        }
        else if (restante == 8 || restante == 18 || restante == 28) {
            tocar("vaiqueimar.wav", 0.8);
        }
    }

    public void atirar(Entity alvo) {
        if (tiroEmEspera) {
            return;
        }

        boolean paraEsquerda = getEntity().getPosition().getX() > alvo.getPosition().getX();

        double origemY = getEntity().getCenter().getY() - 28;
        double origemX = getEntity().getCenter().getX() - (paraEsquerda ? 40 : 0);

        // Vetor unitário: a direção do projétil não depende de onde o inimigo está no mapa
        Point2D direcao = new Point2D(paraEsquerda ? -1 : 1, 0);
        double escalaVertical = paraEsquerda ? -ESCALA : ESCALA;

        if (paraEsquerda) {
            moveParaEsquerda();
        }
        else {
            moveParaDireita();
        }

        AnimationChannel animacaoDoTiro =
                new AnimationChannel(image("tiro_de_fogo.png"), 4, 32, 32, Duration.seconds(0.1), 0, 3);

        tocar("fire_launcher.wav", 0.5);

        int restante = vida.getAtual();

        if (restante == 28 || restante == 15 || restante == 12
                || restante == 10 || restante == 5 || restante == 1) {
            tocar("fogo.wav", 0.5);
        }

        entityBuilder()
                .at(origemX, origemY)
                .type(DISPARO_INIMIGO)
                .viewWithBBox(new AnimatedTexture(animacaoDoTiro))
                .collidable()
                .with(new ProjectileComponent(direcao, VELOCIDADE_DO_PROJETIL))
                .with(new OffscreenCleanComponent())
                .scale(ESCALA, escalaVertical)
                .buildAndAttach();

        texture.playAnimationChannel(animTiro);

        // Volta ao loop de animação normal após a animação de tiro
        texture.setOnCycleFinished(() -> texture.loopAnimationChannel(animIdle));

        tiroEmEspera = true;
        FXGL.getGameTimer().runOnceAfter(() -> tiroEmEspera = false, ESPERA_ENTRE_DISPAROS);
    }

    public void moveParaEsquerda() {
        texture.loopAnimationChannel(animWalk);
        getEntity().setScaleX(-ESCALA);
        physics.setVelocityX(-VELOCIDADE);
        ultimaDirecao = -VELOCIDADE;
    }

    public void moveParaDireita() {
        texture.loopAnimationChannel(animWalk);
        getEntity().setScaleX(ESCALA);
        physics.setVelocityX(VELOCIDADE);
        ultimaDirecao = VELOCIDADE;
    }

    public void pararPersonagem() {
        estaParado = true;
        physics.setVelocityX(0);

        FXGL.getGameTimer().runOnceAfter(() -> estaParado = false, DURACAO_DA_PARADA);
    }

    private void movimentacaoAleatoria() {
        if (FXGLMath.randomBoolean()) {
            moveParaDireita();
        }
        else {
            moveParaEsquerda();
        }
    }

    private void tocar(String arquivo, double volume) {
        Sound som = FXGL.getAssetLoader().loadSound(arquivo);
        som.getAudio().setVolume(volume);
        som.getAudio().play();
    }
}

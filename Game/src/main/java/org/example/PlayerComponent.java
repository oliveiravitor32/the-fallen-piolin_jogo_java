package org.example;

import com.almasb.fxgl.dsl.FXGL;
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

import static com.almasb.fxgl.dsl.FXGLForKtKt.image;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;

/*
    Componente da entidade jogador (Piolin): define o visual animado e as ações.
*/
public class PlayerComponent extends Component {

    // Escala do personagem em tela. O sinal define para que lado ele está virado.
    public static final double ESCALA = 0.4;

    private static final int VIDA_MAXIMA = 10;
    private static final int PULOS_DISPONIVEIS = 2;
    private static final double VELOCIDADE = 220;
    private static final double IMPULSO_DO_PULO = -400;

    /*
        Tempo de espera entre disparos.

        É PROPOSITALMENTE COMPARTILHADO entre a pena e a água: trata-se de um limite
        global de cadência de tiro, para que alternar entre as duas armas não permita
        disparar duas vezes mais rápido.
    */
    private static final Duration ESPERA_ENTRE_DISPAROS = Duration.millis(600);

    // Injetando o componente de física para ser utilizado dentro da classe
    private PhysicsComponent physics;

    // Injetando componentes para gerar animação ao visual (sprite) do jogador
    private final AnimatedTexture texture;
    private final AnimationChannel animIdle, animWalk;

    private final Vida vida = new Vida(VIDA_MAXIMA);
    private final Hud hud;

    private boolean disparoEmEspera = false;
    private int pulosRestantes = PULOS_DISPONIVEIS;

    public PlayerComponent(Hud hud) {
        this.hud = hud;

        // Definindo o PNG com os quadros (frames) de animação
        Image image = image("walk_piolin1-Sheet.png");

        // Definindo animação para jogador parado
        animIdle = new AnimationChannel(image, 4, 64, 64, Duration.seconds(1), 0, 0);

        // Definindo animação para jogador andando
        animWalk = new AnimationChannel(image, 4, 64, 64, Duration.seconds(1), 1, 3);

        // Colocando a primeira textura do jogador ao ser invocado (animIdle = parado)
        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(25, 21));
        entity.getViewComponent().addChild(texture);

        entity.setScaleX(ESCALA);
        entity.setScaleY(ESCALA);

        hud.atualizarPiolin(vida.getFracao());

        // Recarrega os pulos quando o jogador encosta no chão
        physics.onGroundProperty().addListener((obs, antes, estaNoChao) -> {
            if (estaNoChao) {
                pulosRestantes = PULOS_DISPONIVEIS;
            }
        });
    }

    // Alterna as animações entre parado e andando
    @Override
    public void onUpdate(double tpf) {
        AnimationChannel desejado = physics.isMovingX() ? animWalk : animIdle;

        if (texture.getAnimationChannel() != desejado) {
            texture.loopAnimationChannel(desejado);
        }
    }

    public void left() {
        entity.setScaleX(-ESCALA);
        physics.setVelocityX(-VELOCIDADE);
    }

    public void right() {
        entity.setScaleX(ESCALA);
        physics.setVelocityX(VELOCIDADE);
    }

    // Chamado ao final de cada ação de movimento para parar o personagem
    public void stop() {
        physics.setVelocityX(0);
    }

    public void jump() {
        if (pulosRestantes == 0) {
            return;
        }

        physics.setVelocityY(IMPULSO_DO_PULO);
        pulosRestantes--;
    }

    public void shoot() {
        dispararComEspera("feather", "paper.wav");
    }

    public void dispararAgua() {
        dispararComEspera("disparo_de_agua", "water.wav");
    }

    private void dispararComEspera(String entidade, String som) {
        if (disparoEmEspera) {
            return;
        }

        spawn(entidade);
        FXGL.play(som);

        disparoEmEspera = true;
        FXGL.getGameTimer().runOnceAfter(() -> disparoEmEspera = false, ESPERA_ENTRE_DISPAROS);
    }

    public void tomaDano() {
        vida.tomarDano(1);
        hud.atualizarPiolin(vida.getFracao());

        if (vida.estaZerada()) {
            FimDeJogo.terminarLoserPorPiolin();
        }
    }
}

package org.example;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;

import com.almasb.fxgl.ui.FontType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.utilitarios.FimDeJogo;


import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.*;


/*
 Componente da entidade jogador para definir o visual e as ações
 */

public class PlayerComponent extends Component {

    // Injetando o componente de física para ser utilizado dentro da classe
    private PhysicsComponent physics;

    //Injetando componentes para gerar animação ao visual (sprite) do jogador
    private AnimatedTexture texture;
    private AnimationChannel animIdle, animWalk, test;

    private int vida = 10;

    //Propriedades de configurações
    //Colocar o jogador em tempo de espera para evitar tiros simultaneos
    private boolean shootInCooldown = false;

    //Definindo limite de pulos do jogador
    private int jumps = 2;

    //Barra visual de vida
    private Rectangle barra_de_vida = new Rectangle(100, 30, Color.GREEN);

    // Construtor da classe
    public PlayerComponent() {

        // Definindo o PNG com os quadros (frames) de animação
        Image image = image("walk_piolin1-Sheet.png");

        // Definindo animação para jogador parado
        animIdle = new AnimationChannel(image, 1, 64, 64, Duration.seconds(1), 0, 0);

        // Definindo animação para jogador andando
        animWalk = new AnimationChannel(image, 4, 64, 64, Duration.seconds(1), 1, 3);

        // Colocando a primeira textura do jogodor ao ser invocado
        // animIdle = parado
        texture = new AnimatedTexture(animIdle);

        // Loop para gerar a animação
        texture.loop();

        //Imagem Barra de Vida do Piolin
        Image imagemBarraDeVidaDiretorio = new Image("assets/textures/barra_de_vida_piolin.png");
        ImageView imagemBarraDeVida = new ImageView(imagemBarraDeVidaDiretorio);
        imagemBarraDeVida.setFitHeight(38);

        imagemBarraDeVida.setX(getAppWidth() - 1000);
        imagemBarraDeVida.setY(52);
        imagemBarraDeVida.setPreserveRatio(true);

        getGameScene().addUINode(imagemBarraDeVida);

        //BARRA DE VIDA
        barra_de_vida.setX(getAppWidth() - 964);
        barra_de_vida.setY(56);
        getGameScene().addUINode(barra_de_vida);
    }


    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(25, 21));
        entity.getViewComponent().addChild(texture);


        // Escala do personagem
        entity.setScaleX(0.4);
        entity.setScaleY(0.4);

        // Adicionando ouvinte (listener) para capturar o momento do jogador encostando no chão
        // e recarregar os pulos
        physics.onGroundProperty().addListener((obs, old, isOnGround) -> {
            if (isOnGround) {
                jumps = 2;
            }
        });
    }


    // Método chamado quando ouver atualização a ser feita no personagem
    @Override
    public void onUpdate(double tpf) {

        // NECESSÁRIA ATUALIZAÇÃO NESTE BLOCO DE CÓDIGO FUTURAMENTE
        // alternar as animações entre parado e andando
        if (physics.isMovingX()) {

            if (texture.getAnimationChannel() != animWalk) {
                texture.loopAnimationChannel(animWalk);
            }
        }
        else {

            if (texture.getAnimationChannel() != animIdle) {
                texture.loopAnimationChannel(animIdle);
            }
        }
    }

    // Os métodos abaixo seram chamados quando o jogador utilizar cada atalho correspondente
    // eles são os responsaveis por configurar as regras de cada ação

    //Mover para a esquerda
    public void left() {
        getEntity().setScaleX(-0.4);
        physics.setVelocityX(-220);

    }

    //Mover para a direita
    public void right() {
        getEntity().setScaleX(0.4);
        physics.setVelocityX(220);
    }

    // Este método em especifico é chamado no final de cada ação de movimento para a esquerda e direita
    // para parar o personagem depois de andar
    public void stop() {
        physics.setVelocityX(0);
    }


    // Pular com o personagem
    public void jump() {
        if (jumps == 0)
            return;

        physics.setVelocityY(-400);

        jumps--;
    }

    // Atirar penas com o personagem
    public void shoot() {

        if(!shootInCooldown) {
            spawn("feather");
            FXGL.play("paper.wav");

            shootInCooldown = true;

            FXGL.getGameTimer().runOnceAfter(() -> shootInCooldown = false, Duration.millis(600));
        }
    }

    public void dispararAgua() {

        if(!shootInCooldown) {
            spawn("disparo_de_agua");
            FXGL.play("water.wav");

            shootInCooldown = true;

            FXGL.getGameTimer().runOnceAfter(() -> shootInCooldown = false, Duration.millis(600));
        }
    }

    public void tomaDano() {
        vida--;
        barra_de_vida.setWidth(barra_de_vida.getWidth()-10);

        if (vida <= 0 ) {

            // GAME OVER!
            FimDeJogo.terminarLoser();
        }
    }
}
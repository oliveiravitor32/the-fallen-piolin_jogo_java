package org.example;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.dsl.views.ScrollingBackgroundView;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.almasb.fxgl.particle.ParticleEmitters;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import org.example.ui.Hud;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;
import static org.example.EntityType.*;

/*
   Fábrica responsável por configurar cada entidade quando invocada (spawn).

   COMENTÁRIOS IMPORTANTES!

   entityBuilder() = método responsável por criar a entidade

   bbox() = método para definir o tamanho da caixa de colisão (hit box)

   .with(new PhysicsComponent()) = utilizado junto ao bbox() torna a entidade um corpo
                        físico, ou seja, que sofre colisão e cuja hit box passa a funcionar

   build() = método para finalizar a criação da entidade no entityBuilder()

   A fábrica recebe o HUD e a Floresta da partida atual e os repassa aos componentes.
   Como uma fábrica nova é criada a cada initGame(), nenhum estado atravessa reinícios.
*/
public class MainFactory implements EntityFactory {

    // Tempo de vida dos projéteis, caso não colidam nem saiam da tela antes
    private static final Duration DURACAO_DOS_PROJETEIS = Duration.seconds(1.2);

    private final Hud hud;
    private final Floresta floresta;

    public MainFactory(Hud hud, Floresta floresta) {
        this.hud = hud;
        this.floresta = floresta;
    }

    // Invocar plano de fundo
    @Spawns("background")
    public Entity newBackground(SpawnData data) {
        return entityBuilder()
                .at(-10, 60)
                .view(new ScrollingBackgroundView(texture("background/forest.png").getImage(),
                        getAppWidth() - 500, getAppHeight()))
                .zIndex(-5)
                .with(new IrremovableComponent())
                .build();
    }

    /*
        Invocar plataforma.

        OBS: este método serve apenas para dar física ao personagem colidir com as
        plataformas, não estando ligado literalmente às plataformas visuais do mapa!
    */
    @Spawns("platform")
    public Entity newPlataforma(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();

        // Estes são objetos jbox2d diretos, então na verdade não introduzimos uma nova API
        FixtureDef fd = new FixtureDef();
        fd.setRestitution(0);
        fd.setFriction(0);
        physics.setFixtureDef(fd);

        return entityBuilder()
                .type(PLATFORM)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(physics)
                .build();
    }

    @Spawns("platform-diagonal")
    public Entity newPlataformaDiagonal(SpawnData data) {
        return entityBuilder()
                .type(PLATFORM)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .build();
    }

    @Spawns("objetoCombustivel")
    public Entity newObjetoCombustivel(SpawnData data) {
        // Configurando partícula de fogo
        ParticleEmitter emissorDeParticula = ParticleEmitters.newFireEmitter();

        emissorDeParticula.setMaxEmissions(Integer.MAX_VALUE);
        emissorDeParticula.setNumParticles(0);
        emissorDeParticula.setEmissionRate(1);
        emissorDeParticula.setSize(1, 1);
        emissorDeParticula.setScaleFunction(i -> FXGLMath.randomPoint2D().multiply(0.001));
        emissorDeParticula.setExpireFunction(i -> Duration.seconds(0.5));
        emissorDeParticula.setSpawnPointFunction(i -> new Point2D(5, 5));

        return entityBuilder()
                .type(OBJETO_COMBUSTIVEL)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new CollidableComponent(true))
                .with(new ObjetoCombustivelComponent(floresta))
                .with(new ParticleComponent(emissorDeParticula))
                .build();
    }

    @Spawns("parede_limite_do_mapa")
    public Entity newParedeLimiteDoMapa(SpawnData data) {
        return entityBuilder()
                .type(PAREDE_INVISIVEL_LIMITE_DO_MAPA)
                .bbox(new HitBox(BoundingShape.box(data.<Integer>get("width"), data.<Integer>get("height"))))
                .with(new PhysicsComponent())
                .with(new CollidableComponent())
                .build();
    }

    @Spawns("poligono")
    public Entity newPoligono(SpawnData data) {
        Polygon poligono = data.<Polygon>get("polygon");
        List<Double> pontos = poligono.getPoints();

        return entityBuilder()
                .type(PLATFORM)
                .bbox(new HitBox(BoundingShape.polygon(
                        pontos.get(0), pontos.get(1),
                        pontos.get(2), pontos.get(3),
                        pontos.get(4), pontos.get(5))))
                .with(new PhysicsComponent())
                .build();
    }

    /*
        Invoca a pena quando o jogador atira: define a direção, a velocidade,
        onde ela aparece, e a remove ao sair do campo de visão ou após um tempo.
    */
    @Spawns("feather")
    public Entity newFeather(SpawnData data) {
        return newProjetilDoJogador(DISPARO_DE_PENA_JOGADOR, "normal_feather.png",
                39, 40, 45, 0.4, 0.4, 200);
    }

    @Spawns("disparo_de_agua")
    public Entity newDisparoDeAgua(SpawnData data) {
        return newProjetilDoJogador(DISPARO_DE_AGUA_JOGADOR, "waterD.png",
                90, 140, 188, 0.1, 0.05, 200);
    }

    /*
        Lógica comum aos projéteis do jogador.

        A direção é um vetor UNITÁRIO derivado do lado para onde o Piolin está virado.
        Antes usava-se a coordenada X absoluta do jogador como direção: funcionava por
        acidente (o ProjectileComponent normaliza o vetor e só o sinal importava), mas
        produzia um vetor nulo (projétil parado) quando o jogador estava em x = 0,
        que é justamente a posição inicial dele no mapa.
    */
    private Entity newProjetilDoJogador(EntityType tipo, String textura,
                                        double deslocamentoY,
                                        double deslocamentoXDireita, double deslocamentoXEsquerda,
                                        double escalaHorizontal, double escalaVertical,
                                        double velocidade) {

        Entity player = getGameWorld().getSingleton(JOGADOR);
        boolean paraEsquerda = player.getScaleX() < 0;

        double origemX = player.getCenter().getX()
                - (paraEsquerda ? deslocamentoXEsquerda : deslocamentoXDireita);
        double origemY = player.getCenter().getY() - deslocamentoY;

        Point2D direcao = new Point2D(paraEsquerda ? -1 : 1, 0);

        Entity projetil = entityBuilder()
                .at(origemX, origemY)
                .type(tipo)
                .viewWithBBox(textura)
                .collidable()
                .with(new ProjectileComponent(direcao, velocidade))
                .with(new OffscreenCleanComponent())
                .scale(escalaHorizontal, paraEsquerda ? -escalaVertical : escalaVertical)
                .build();

        FXGL.getGameTimer().runOnceAfter(() -> {
            if (projetil.isActive()) {
                projetil.removeFromWorld();
            }
        }, DURACAO_DOS_PROJETEIS);

        return projetil;
    }

    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(JOGADOR)

                // PODE SER UTILIZADO DEPOIS PARA UMA SEGUNDA HIT BOX REDONDA
                // SIGNIFICANDO A CABEÇA DO PERSONAGEM
                //.bbox(new HitBox(new Point2D(5, 15), BoundingShape.circle(6)))

                .bbox(new HitBox(new Point2D(25, 10), BoundingShape.box(15, 50)))
                .with(newCorpoDePersonagem())
                .with(new CollidableComponent(true))
                .with(new IrremovableComponent())

                /*
                 INJETA AQUI A CLASSE PLAYER (COMPONENTE) QUE É CONFIGURADA EM UMA CLASSE
                 SEPARADA POR TER DIVERSOS RECURSOS QUE AS DEMAIS ENTIDADES NÃO PRECISAM!
                 */
                .with(new PlayerComponent(hud))
                .build();
    }

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(ENEMY)
                .bbox(new HitBox(new Point2D(25, 10), BoundingShape.box(15, 50)))
                .with(newCorpoDePersonagem())
                .with(new CollidableComponent(true))

                // Sensor para verificar proximidade de alvos
                .with(new SensorComponent())
                .with(new EnemyComponent(hud))
                .build();
    }

    /*
        Corpo físico comum ao Piolin e ao Espalha Lixo: dinâmico, com sensor de chão
        e sem fricção (para o personagem não grudar nas paredes).
    */
    private PhysicsComponent newCorpoDePersonagem() {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        physics.addGroundSensor(new HitBox("GROUND_SENSOR", new Point2D(25, 10), BoundingShape.box(15, 50)));

        // Estes são objetos jbox2d diretos, então na verdade não introduzimos uma nova API
        FixtureDef fd = new FixtureDef();
        fd.setRestitution(0);
        fd.setFriction(0);
        physics.setFixtureDef(fd);

        return physics;
    }
}

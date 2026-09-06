package org.example;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.StartupScene;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.Body;
import com.almasb.fxgl.ui.FontType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.ui.Hud;
import org.example.ui.menu.MenuDePausa;
import org.example.ui.menu.MenuPrincipal;

import java.util.function.Consumer;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameController;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGLForKtKt.spawn;

/*
 Classe principal para rodar o programa!
 Estende a classe GameApplication da biblioteca FXGL, responsável por
 disponibilizar os métodos de configuração do jogo.
*/
public class Main extends GameApplication {

    private static final int LARGURA_DA_TELA = 15 * 70;  // 1050
    private static final int ALTURA_DA_TELA = 10 * 70;   // 700

    private static final int SEGUNDOS_DE_CONTAGEM_REGRESSIVA = 3;

    // Modo da aplicação. Troque para ApplicationMode.DEVELOPER para habilitar os atalhos de teste.
    private static final ApplicationMode MODO_DA_APLICACAO = ApplicationMode.RELEASE;

    private Entity player;
    private Entity enemy;

    // Interface e vida da floresta da partida em andamento
    private Hud hud;
    private Floresta floresta;

    // Libera a entrada de dados apenas após a contagem regressiva
    private boolean aceitaEntradaDeDados = false;

    /*
        Método de configurações usado para definir o tamanho da tela, entre outros.
    */
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setVersion("1.0.0");
        settings.setTitle("The Fallen Piolin");

        // Icone da barra de titulo e da barra de tarefas
        settings.setAppIcon("A1piolinPNG1.png");

        settings.setWidth(LARGURA_DA_TELA);
        settings.setHeight(ALTURA_DA_TELA);

        // Habilita os menus de configurações dentro do jogo
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setDeveloperMenuEnabled(true);

        // Cena de carregamento e menus (principal e de pausa)
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public StartupScene newStartup(int largura, int altura) {
                return new MainStartupScene(largura, altura);
            }

            @Override
            public LoadingScene newLoadingScene() {
                return new MainLoadingScene();
            }

            @Override
            public FXGLMenu newMainMenu() {
                return new MenuPrincipal();
            }

            @Override
            public FXGLMenu newGameMenu() {
                return new MenuDePausa();
            }
        });

        /*
            Fontes da propria engine. O FXGL usa estes arquivos em tudo o que desenha
            por conta propria (notificacoes, menu de desenvolvimento, dialogos), entao
            defini-las mantem a fonte pixelada mesmo nas telas que nao sao nossas.
        */
        settings.setFontUI("PixelifySans-Regular.ttf");
        settings.setFontGame("PixelifySans-Bold.ttf");
        settings.setFontText("PixelifySans-Regular.ttf");
        settings.setFontMono("PixelifySans-Regular.ttf");

        settings.setApplicationMode(MODO_DA_APLICACAO);
    }

    /*
        Método responsável por criar os atalhos das teclas e definir as ações.
    */
    @Override
    protected void initInput() {

        // Mover personagem para a esquerda
        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                comJogador(PlayerComponent::left);
            }

            @Override
            protected void onActionEnd() {
                comJogador(PlayerComponent::stop);
            }
        }, KeyCode.A, VirtualButton.LEFT);

        // Mover personagem para a direita
        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                comJogador(PlayerComponent::right);
            }

            @Override
            protected void onActionEnd() {
                comJogador(PlayerComponent::stop);
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        // Pular com o personagem
        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                comJogador(PlayerComponent::jump);
            }
        }, KeyCode.W, VirtualButton.A);

        // Atirar penas com o personagem
        getInput().addAction(new UserAction("Disparar pena") {
            @Override
            protected void onActionBegin() {
                comJogador(PlayerComponent::shoot);
            }
        }, MouseButton.PRIMARY);

        // Disparar água com o personagem
        getInput().addAction(new UserAction("Disparar agua") {
            @Override
            protected void onActionBegin() {
                comJogador(PlayerComponent::dispararAgua);
            }
        }, MouseButton.SECONDARY);

        /*
            Pausa. O FXGL já abre este mesmo menu com ESC; P existe apenas como
            alternativa. Antes havia F para pausar e G para retomar o motor sem
            passar pelo menu, o que permitia despausar a tela de fim de jogo.
        */
        getInput().addAction(new UserAction("Pausar") {
            @Override
            protected void onActionBegin() {
                getGameController().gotoGameMenu();
            }
        }, KeyCode.P);

        initInputDeDesenvolvimento();
    }

    /*
        Atalhos de teste do inimigo. Só são registrados fora do modo RELEASE,
        para que o jogador final não consiga controlar o Espalha Lixo.
    */
    private void initInputDeDesenvolvimento() {
        if (getSettings().getApplicationMode() == ApplicationMode.RELEASE) {
            return;
        }

        getInput().addAction(new UserAction("DEV: mover Espalha Lixo para a esquerda") {
            @Override
            protected void onActionBegin() {
                comInimigo(EnemyComponent::moveParaEsquerda);
            }
        }, KeyCode.J);

        getInput().addAction(new UserAction("DEV: mover Espalha Lixo para a direita") {
            @Override
            protected void onActionBegin() {
                comInimigo(EnemyComponent::moveParaDireita);
            }
        }, KeyCode.L);

        getInput().addAction(new UserAction("DEV: disparar com Espalha Lixo") {
            @Override
            protected void onActionBegin() {
                getGameWorld().getSingletonOptional(EntityType.JOGADOR)
                        .ifPresent(jogador -> comInimigo(inimigo -> inimigo.atirar(jogador)));
            }
        }, KeyCode.I);
    }

    /*
        Executa uma ação sobre o jogador apenas se ele já existir e a entrada
        estiver liberada. Centraliza a checagem que antes era repetida em cada atalho
        e evita o NullPointerException durante a contagem regressiva.
    */
    private void comJogador(Consumer<PlayerComponent> acao) {
        if (!aceitaEntradaDeDados || player == null || !player.isActive()) {
            return;
        }

        acao.accept(player.getComponent(PlayerComponent.class));
    }

    private void comInimigo(Consumer<EnemyComponent> acao) {
        if (!aceitaEntradaDeDados || enemy == null || !enemy.isActive()) {
            return;
        }

        acao.accept(enemy.getComponent(EnemyComponent.class));
    }

    /*
        Método acionado antes do init para definir o som de fundo.
    */
    @Override
    protected void onPreInit() {
        getSettings().setGlobalMusicVolume(0.20);
        loopBGM("backsound.wav");
    }

    @Override
    protected void onUpdate(double tpf) {
        tpf = Math.min(tpf, 0.06); // limita o tpf a no máximo 60ms
        super.onUpdate(tpf);
    }

    /*
        Método para iniciar o jogo: monta a interface, adiciona a classe de fábrica,
        invoca o plano de fundo e o mapa, e configura a câmera.
    */
    @Override
    protected void initGame() {
        /*
            initGame() roda de novo a cada reinício. Limpar a interface e recriar
            HUD e Floresta garante que nenhuma barra de vida seja empilhada e que a
            partida comece sempre com a vida cheia.
        */
        getGameScene().clearUINodes();
        hud = new Hud();
        floresta = new Floresta(hud);

        player = null;
        enemy = null;
        aceitaEntradaDeDados = false;

        FXGL.play("detenha.wav");

        /*
            Vinculando a classe de fábrica, responsável por saber as configurações de
            criação de cada entidade e suas características quando invocada no jogo.
        */
        getGameWorld().addEntityFactory(new MainFactory(hud, floresta));

        // Invocando plano de fundo
        spawn("background");

        // Definindo o mapa
        setLevelFromMap("tmx/map-remastered8.tmx");

        // Define posição de início da câmera
        Viewport viewport = getGameScene().getViewport();
        viewport.setX(0);
        viewport.setY(490);
        viewport.setBounds(-1500, -200, 2560, 1200);
        viewport.setZoom(2.5);

        // Contagem regressiva para dar início ao jogo
        contagemRegressiva(SEGUNDOS_DE_CONTAGEM_REGRESSIVA);
    }

    private void contagemRegressiva(int segundos) {
        Text textoDeContagemRegressiva = getUIFactoryService()
                .newText("", Color.BLACK, FontType.GAME, 100);

        textoDeContagemRegressiva.setTranslateX(getAppWidth() / 2.0 - 50);
        textoDeContagemRegressiva.setTranslateY(getAppHeight() / 2.0 - 50);
        textoDeContagemRegressiva.setMouseTransparent(true);
        getGameScene().addUINode(textoDeContagemRegressiva);

        for (int i = 0; i <= segundos; i++) {
            final int restante = segundos - i;

            runOnce(() -> {
                if (restante > 0) {
                    textoDeContagemRegressiva.setText(String.valueOf(restante));
                }
                else {
                    textoDeContagemRegressiva.setText("Vai!");
                    textoDeContagemRegressiva.setFill(Color.GREEN);
                }
            }, Duration.seconds(i));
        }

        // Remove o texto e invoca os personagens após a contagem terminar
        runOnce(() -> {
            getGameScene().removeUINode(textoDeContagemRegressiva);
            invocaPersonagens();
            aceitaEntradaDeDados = true;
        }, Duration.seconds(segundos + 1.0));
    }

    private void invocaPersonagens() {
        // PIOLIN
        player = spawn("player", 0, 620);

        /*
            Amortecimento linear alto evita que o personagem quique ao bater no chão
            e o deixa planando quando pula.
        */
        aplicarAmortecimento(player);

        // ESPALHA LIXO
        enemy = spawn("enemy", 1200, 600);
        aplicarAmortecimento(enemy);

        // Configurações de tela (viewport) para se vincular ao jogador
        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(-1500, -200, 2560, 1200);
        viewport.bindToEntity(player, getAppWidth() / 2.0, 450);
        viewport.setZoom(2.5);
        viewport.setLazy(true);
    }

    private void aplicarAmortecimento(Entity entidade) {
        Body corpo = entidade.getComponent(PhysicsComponent.class).getBody();
        corpo.setLinearDamping(10.0f);
    }

    /*
        Método responsável por definir as configurações de física do jogo, como a
        gravidade das entidades e as ações de colisão entre elas.
    */
    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 760);

        /*
            Os handlers usam as entidades recebidas por parâmetro, e não os campos da
            classe. Antes o dano do inimigo era aplicado ao campo "enemy", que podia
            apontar para o inimigo de uma partida anterior.
        */
        onCollisionBegin(EntityType.DISPARO_DE_PENA_JOGADOR, EntityType.ENEMY, (pena, inimigo) -> {
            pena.removeFromWorld();
            inimigo.getComponent(EnemyComponent.class).tomaDano();
        });

        onCollisionBegin(EntityType.DISPARO_INIMIGO, EntityType.JOGADOR, (tiro, jogador) -> {
            tiro.removeFromWorld();
            jogador.getComponent(PlayerComponent.class).tomaDano();
        });

        onCollisionBegin(EntityType.DISPARO_INIMIGO, EntityType.OBJETO_COMBUSTIVEL, (tiro, objetoCombustivel) -> {
            tiro.removeFromWorld();
            objetoCombustivel.getComponent(ObjetoCombustivelComponent.class).tomaDano();
        });

        onCollisionBegin(EntityType.DISPARO_DE_AGUA_JOGADOR, EntityType.OBJETO_COMBUSTIVEL, (tiro, objetoCombustivel) -> {
            tiro.removeFromWorld();
            objetoCombustivel.getComponent(ObjetoCombustivelComponent.class).recuperarVida();
        });
    }

    // Método main padrão chamando as configurações da biblioteca FXGL
    public static void main(String[] args) {
        launch(args);
    }
}

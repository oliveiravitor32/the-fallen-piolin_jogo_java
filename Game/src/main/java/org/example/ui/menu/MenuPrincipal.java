package org.example.ui.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.texture;

/*
    Tela de inicio do jogo, com a linguagem visual do iOS.

    A arte da floresta entra desfocada como papel de parede, o titulo fica sobre ela
    e as acoes sao botoes arredondados de material translucido. Controles e Creditos
    abrem folhas que sobem pela base da tela (ver PainelDeslizante).
*/
public class MenuPrincipal extends FXGLMenu {

    private static final double LARGURA_DO_BOTAO = 300;
    private static final double ALTURA_DO_BOTAO = 52;
    private static final double ESPACO_ENTRE_BOTOES = 12;

    public MenuPrincipal() {
        super(MenuType.MAIN_MENU);

        StackPane raiz = new StackPane();
        raiz.setPrefSize(getAppWidth(), getAppHeight());

        raiz.getChildren().addAll(montarPapelDeParede(), montarConteudo());

        getContentRoot().getChildren().add(raiz);
    }

    /*
        Papel de parede: a propria arte do jogo, desfocada e escurecida, do jeito que o
        iOS trata o fundo atras de um material translucido.
    */
    private StackPane montarPapelDeParede() {
        ImageView arte = new ImageView(texture("background/forest.png").getImage());

        /*
            preserveRatio fica desligado de proposito: a distorcao e imperceptivel sob
            um desfoque desta intensidade e garante que a arte cubra a tela inteira.
        */
        arte.setFitWidth(getAppWidth());
        arte.setFitHeight(getAppHeight());
        arte.setPreserveRatio(false);
        arte.setEffect(new GaussianBlur(26));

        Rectangle escurecimento = new Rectangle(getAppWidth(), getAppHeight(), Color.web("#000000", 0.5));

        return new StackPane(arte, escurecimento);
    }

    private StackPane montarConteudo() {
        Text titulo = EstiloIOS.texto("The Fallen Piolin", 52, true, EstiloIOS.TEXTO);
        titulo.setEffect(new DropShadow(18, Color.web("#000000", 0.6)));

        Text subtitulo = EstiloIOS.texto("Defenda a floresta do Espalha Lixo",
                16, false, EstiloIOS.TEXTO_SECUNDARIO);

        VBox cabecalho = new VBox(6, titulo, subtitulo);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(0, 0, 38, 0));

        VBox botoes = new VBox(ESPACO_ENTRE_BOTOES,
                botao("Jogar", EstiloIOS.Tipo.PRIMARIO, this::fireNewGame),
                botao("Controles", EstiloIOS.Tipo.VIDRO, this::abrirControles),
                botao("Créditos", EstiloIOS.Tipo.VIDRO, this::abrirCreditos),
                botao("Sair", EstiloIOS.Tipo.DESTRUTIVO, this::fireExit));
        botoes.setAlignment(Pos.CENTER);

        VBox coluna = new VBox(cabecalho, botoes);
        coluna.setAlignment(Pos.CENTER);

        StackPane conteudo = new StackPane(coluna);
        conteudo.setPrefSize(getAppWidth(), getAppHeight());

        animarEntrada(cabecalho, botoes);

        return conteudo;
    }

    private javafx.scene.control.Button botao(String rotulo, EstiloIOS.Tipo tipo, Runnable acao) {
        return EstiloIOS.botao(rotulo, tipo, LARGURA_DO_BOTAO, ALTURA_DO_BOTAO, acao);
    }

    /* O titulo e os botoes sobem levemente ao aparecer, como as telas do iOS. */
    private void animarEntrada(VBox cabecalho, VBox botoes) {
        animarSubida(cabecalho, Duration.ZERO);
        animarSubida(botoes, Duration.millis(90));
    }

    private void animarSubida(VBox alvo, Duration atraso) {
        alvo.setOpacity(0);
        alvo.setTranslateY(22);

        FadeTransition surgimento = new FadeTransition(Duration.millis(420), alvo);
        surgimento.setToValue(1);
        surgimento.setDelay(atraso);

        TranslateTransition subida = new TranslateTransition(Duration.millis(420), alvo);
        subida.setToY(0);
        subida.setInterpolator(Interpolator.SPLINE(0.32, 0.72, 0, 1));
        subida.setDelay(atraso);

        surgimento.play();
        subida.play();
    }

    private void abrirControles() {
        abrirPainel(new PainelDeslizante(getAppWidth(), getAppHeight(), "Controles",
                PainelDeslizante.itens(
                        "A  /  D", "Andar para a esquerda / direita",
                        "W", "Pular (pulo duplo)",
                        "Botão esquerdo", "Disparar pena",
                        "Botão direito", "Disparar água",
                        "ESC  ou  P", "Pausar"),
                "A pena fere o Espalha Lixo. A água apaga o fogo e devolve vida à floresta."));
    }

    private void abrirCreditos() {
        abrirPainel(new PainelDeslizante(getAppWidth(), getAppHeight(), "Créditos",
                PainelDeslizante.itens(
                        "Desenvolvimento", "Guilherme Leandro, Helen Silva e Vitor Oliveira",
                        "Engine", "FXGL 21, de Almas Baimagambetov",
                        "Editor de mapas", "Tiled"),
                "Licenciado sob a MIT License · 2024"));
    }

    private void abrirPainel(PainelDeslizante painel) {
        getContentRoot().getChildren().add(painel);
        painel.abrir();
    }
}

package org.example.ui.menu;

import com.almasb.fxgl.app.scene.MenuType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.texture;

/*
    Tela de inicio do jogo: arrumacao no estilo iOS, aparencia em pixel art.

    A arte da floresta entra pixelizada como papel de parede, o titulo aparece sobre
    ela com sombra dura e as acoes ficam numa coluna central. Controles e Creditos
    abrem folhas que sobem pela base da tela (ver PainelDeslizante).
*/
public class MenuPrincipal extends MenuComEstilo {

    /*
        Lado do bloco, em pixels de tela. Valores maiores deixam o fundo mais
        grosseiro; 6 mantem a silhueta da floresta reconhecivel.
    */
    private static final int TAMANHO_DO_BLOCO = 6;

    public MenuPrincipal() {
        super(MenuType.MAIN_MENU);

        StackPane raiz = new StackPane();
        raiz.setPrefSize(getAppWidth(), getAppHeight());

        raiz.getChildren().addAll(montarPapelDeParede(), montarConteudo());

        getContentRoot().getChildren().add(raiz);
    }

    /*
        Papel de parede: a propria arte da floresta, reduzida a blocos grandes e
        escurecida.

        Antes havia um desfoque gaussiano aqui, que e o oposto de pixel art: ele
        dissolve exatamente a grade que queremos enxergar. A pixelizacao mantem a
        silhueta e as cores da floresta, mas em blocos que combinam com os sprites.
    */
    private StackPane montarPapelDeParede() {
        Image arteEmBlocos = pixelar(texture("background/forest.png").getImage(),
                getAppWidth(), getAppHeight(), TAMANHO_DO_BLOCO);

        ImageView arte = new ImageView(arteEmBlocos);
        arte.setSmooth(false);

        Rectangle escurecimento = new Rectangle(getAppWidth(), getAppHeight(), Color.web("#0B0A12", 0.62));

        return new StackPane(arte, escurecimento);
    }

    /*
        Desenha a arte ja no tamanho final da tela, preenchendo blocos solidos de
        "tamanhoDoBloco" pixels com a cor amostrada da imagem original.

        A imagem sai pronta no tamanho exato de exibicao de proposito: qualquer
        redimensionamento feito depois pelo JavaFX interpola as cores e devolve as
        bordas suaves que estamos justamente tentando eliminar.
    */
    private static Image pixelar(Image original, int larguraFinal, int alturaFinal, int tamanhoDoBloco) {
        WritableImage destino = new WritableImage(larguraFinal, alturaFinal);
        PixelReader leitor = original.getPixelReader();
        PixelWriter escritor = destino.getPixelWriter();

        double escalaX = original.getWidth() / larguraFinal;
        double escalaY = original.getHeight() / alturaFinal;

        for (int y = 0; y < alturaFinal; y += tamanhoDoBloco) {
            for (int x = 0; x < larguraFinal; x += tamanhoDoBloco) {

                // Cor do bloco inteiro: a do canto superior esquerdo dele na arte original
                int origemX = (int) Math.min(original.getWidth() - 1, x * escalaX);
                int origemY = (int) Math.min(original.getHeight() - 1, y * escalaY);
                int cor = leitor.getArgb(origemX, origemY);

                int limiteX = Math.min(x + tamanhoDoBloco, larguraFinal);
                int limiteY = Math.min(y + tamanhoDoBloco, alturaFinal);

                for (int blocoY = y; blocoY < limiteY; blocoY++) {
                    for (int blocoX = x; blocoX < limiteX; blocoX++) {
                        escritor.setArgb(blocoX, blocoY, cor);
                    }
                }
            }
        }

        return destino;
    }

    private StackPane montarConteudo() {
        Text titulo = EstiloDoMenu.texto("The Fallen Piolin", 52, true, EstiloDoMenu.TEXTO);
        // Sombra dura, deslocada em pixels inteiros: nada de esfumado
        titulo.setEffect(new DropShadow(0, 5, 5, Color.web("#0B0A12", 0.85)));

        Text subtitulo = EstiloDoMenu.texto("Defenda a floresta do Espalha Lixo",
                16, false, EstiloDoMenu.TEXTO_SECUNDARIO);

        VBox cabecalho = new VBox(6, titulo, subtitulo);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(0, 0, 38, 0));

        VBox botoes = new VBox(ESPACO_ENTRE_BOTOES,
                botao("Jogar", EstiloDoMenu.Tipo.PRIMARIO, this::fireNewGame),
                botao("Controles", EstiloDoMenu.Tipo.VIDRO, this::abrirControles),
                botao("Créditos", EstiloDoMenu.Tipo.VIDRO, this::abrirCreditos),
                botao("Sair", EstiloDoMenu.Tipo.DESTRUTIVO, () -> confirmarSaida(null)));
        botoes.setAlignment(Pos.CENTER);

        VBox coluna = new VBox(cabecalho, botoes);
        coluna.setAlignment(Pos.CENTER);

        StackPane conteudo = new StackPane(coluna);
        conteudo.setPrefSize(getAppWidth(), getAppHeight());

        animarSubida(cabecalho, Duration.ZERO);
        animarSubida(botoes, Duration.millis(90));

        return conteudo;
    }

    private void abrirCreditos() {
        abrirPainel(PaineisDoMenu.creditos(getAppWidth(), getAppHeight()));
    }
}

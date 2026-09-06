package org.example.ui.menu;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FontFactory;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/*
    Estilo do menu: a ARRUMACAO vem do iOS, a APARENCIA vem do pixel art.

    O nome nao cita o iOS de proposito: dele vem so o arranjo dos elementos, nunca
    as cores, a fonte ou o acabamento, que seguem a arte do jogo.

    Do iOS ficam a organizacao limpa e o formato dos itens na tela: uma coluna
    central de acoes, cartoes agrupados com os rotulos a esquerda e os valores a
    direita, folha que sobe pela base, muito respiro entre os elementos.

    Do pixel art vem tudo o que e visual: a fonte Pixelify Sans, cantos quase retos,
    bordas grossas e sombras duras (sem esfumado), para combinar com os sprites do jogo.

    Todos os valores estao aqui em um lugar so: mexer nesta classe muda o menu inteiro.
*/
public final class EstiloDoMenu {

    // Paleta: cores saturadas, no espirito de uma paleta de pixel art
    public static final String VERDE = "#3FD44B";
    public static final String VERDE_REALCE = "#5CEE68";
    public static final String VERMELHO = "#FF4B4B";

    // Materiais
    public static final String VIDRO = "rgba(255,255,255,0.14)";
    public static final String VIDRO_REALCE = "rgba(255,255,255,0.30)";
    public static final String BORDA_VIDRO = "rgba(255,255,255,0.55)";
    public static final String MATERIAL_ESCURO = "rgba(24,20,37,0.98)";

    // Rotulos
    public static final Color TEXTO = Color.WHITE;
    public static final Color TEXTO_SECUNDARIO = Color.rgb(198, 205, 220);

    /*
        Cantos quase retos e bordas grossas: um raio grande produz uma curva suave,
        que destoa de tudo o que e desenhado em grade de pixels.
    */
    public static final double RAIO = 4;
    public static final double RAIO_PAINEL = 6;
    public static final double BORDA = 3;

    private static final String FONTE_REGULAR = "PixelifySans-Regular.ttf";
    private static final String FONTE_NEGRITO = "PixelifySans-Bold.ttf";

    // Carregadas sob demanda: o servico de assets do FXGL so existe apos a inicializacao
    private static FontFactory fabricaRegular;
    private static FontFactory fabricaNegrito;

    private EstiloDoMenu() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    private static FontFactory fabrica(boolean negrito) {
        if (negrito) {
            if (fabricaNegrito == null) {
                fabricaNegrito = FXGL.getAssetLoader().loadFont(FONTE_NEGRITO);
            }

            return fabricaNegrito;
        }

        if (fabricaRegular == null) {
            fabricaRegular = FXGL.getAssetLoader().loadFont(FONTE_REGULAR);
        }

        return fabricaRegular;
    }

    public static Font fonte(double tamanho, boolean negrito) {
        return fabrica(negrito).newFont(tamanho);
    }

    public static Text texto(String conteudo, double tamanho, boolean negrito, Color cor) {
        Text texto = new Text(conteudo);
        texto.setFont(fonte(tamanho, negrito));
        texto.setFill(cor);

        return texto;
    }

    /*
        Rotulo de lista. Diferente de Text, um Label sabe quebrar linha quando o
        espaco disponivel acaba, o que impede que rotulo e valor se sobreponham
        quando o conteudo e longo.
    */
    public static Label rotulo(String conteudo, double tamanho, boolean negrito, Color cor) {
        Label rotulo = new Label(conteudo);
        rotulo.setFont(fonte(tamanho, negrito));
        rotulo.setTextFill(cor);

        return rotulo;
    }

    /** Estilos de botao: preenchido, de vidro e destrutivo. */
    public enum Tipo {
        /** Acao principal: preenchido com a cor de destaque. */
        PRIMARIO(VERDE, VERDE_REALCE, "#101018", "#101018"),
        /** Acao secundaria: material translucido sobre o fundo. */
        VIDRO(EstiloDoMenu.VIDRO, VIDRO_REALCE, "white", BORDA_VIDRO),
        /** Acao destrutiva: texto vermelho sobre vidro. */
        DESTRUTIVO(EstiloDoMenu.VIDRO, VIDRO_REALCE, VERMELHO, BORDA_VIDRO);

        private final String fundo;
        private final String fundoRealce;
        private final String corDoTexto;
        private final String borda;

        Tipo(String fundo, String fundoRealce, String corDoTexto, String borda) {
            this.fundo = fundo;
            this.fundoRealce = fundoRealce;
            this.corDoTexto = corDoTexto;
            this.borda = borda;
        }
    }

    /*
        Botao com retorno de toque: clareia sob o cursor e afunda alguns pixels ao
        ser pressionado. O afundamento substitui a reducao de escala que era usada
        antes: escalar um botao produz meias medidas e borra a grade de pixels.
    */
    public static Button botao(String rotulo, Tipo tipo, double largura, double altura, Runnable acao) {
        Button botao = new Button(rotulo);
        botao.setPrefSize(largura, altura);
        botao.setFont(fonte(altura * 0.40, true));
        botao.setCursor(Cursor.HAND);
        botao.setOnAction(evento -> acao.run());

        aplicarEstilo(botao, tipo, tipo.fundo);

        botao.setOnMouseEntered(evento -> aplicarEstilo(botao, tipo, tipo.fundoRealce));
        botao.setOnMouseExited(evento -> {
            aplicarEstilo(botao, tipo, tipo.fundo);
            botao.setTranslateY(0);
        });

        botao.setOnMousePressed(evento -> botao.setTranslateY(3));
        botao.setOnMouseReleased(evento -> botao.setTranslateY(0));

        return botao;
    }

    private static void aplicarEstilo(Button botao, Tipo tipo, String fundo) {
        botao.setStyle(
                "-fx-background-color: " + fundo + ";"
                        + "-fx-text-fill: " + tipo.corDoTexto + ";"
                        + "-fx-background-radius: " + RAIO + ";"
                        + "-fx-border-radius: " + RAIO + ";"
                        + "-fx-border-color: " + tipo.borda + ";"
                        + "-fx-border-width: " + BORDA + ";"
                        // Remove o anel de foco padrao do JavaFX, que destoa do visual
                        + "-fx-focus-color: transparent;"
                        + "-fx-faint-focus-color: transparent;");
    }

    /** Alca retangular no topo da folha, sem cantos arredondados. */
    public static Rectangle alca() {
        return new Rectangle(48, 6, Color.web("#FFFFFF", 0.45));
    }

    /** Linha de separacao entre itens de uma lista, com espessura de alguns pixels. */
    public static Rectangle separador(double largura) {
        return new Rectangle(largura, 2, Color.web("#FFFFFF", 0.16));
    }
}

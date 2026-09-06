package org.example.ui.menu;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

/*
    Tokens visuais e componentes no estilo do iOS (tema escuro).

    As cores seguem a paleta de sistema da Apple no modo escuro; os valores estao
    aqui em um lugar so para que o menu inteiro mude de aparencia editando esta classe.
*/
public final class EstiloIOS {

    // Cores de sistema (iOS dark)
    public static final String AZUL = "#0A84FF";
    public static final String VERDE = "#30D158";
    public static final String VERDE_REALCE = "#4AE070";
    public static final String VERMELHO = "#FF453A";

    // Materiais translucidos
    public static final String VIDRO = "rgba(255,255,255,0.16)";
    public static final String VIDRO_REALCE = "rgba(255,255,255,0.28)";
    public static final String BORDA_VIDRO = "rgba(255,255,255,0.22)";
    public static final String MATERIAL_ESCURO = "rgba(28,28,30,0.98)";
    public static final String SEPARADOR = "rgba(255,255,255,0.12)";

    // Rotulos
    public static final Color TEXTO = Color.WHITE;
    public static final Color TEXTO_SECUNDARIO = Color.rgb(235, 235, 245, 0.6);

    // Cantos arredondados
    public static final double RAIO = 14;
    public static final double RAIO_PAINEL = 22;

    /*
        A Apple usa a familia San Francisco, que nao existe fora dos aparelhos dela.
        Escolhemos a primeira fonte disponivel no sistema que tenha o mesmo espirito
        (humanista, sem serifa), com Arial como ultimo recurso.
    */
    public static final String FAMILIA = escolherFamilia(
            "SF Pro Display", "SF Pro Text", "Helvetica Neue", "Segoe UI Variable Display",
            "Segoe UI", "Inter", "Roboto", "Arial");

    private EstiloIOS() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    private static String escolherFamilia(String... preferidas) {
        List<String> instaladas = Font.getFamilies();

        for (String familia : preferidas) {
            if (instaladas.contains(familia)) {
                return familia;
            }
        }

        return Font.getDefault().getFamily();
    }

    public static Font fonte(double tamanho, boolean negrito) {
        return Font.font(FAMILIA, negrito ? javafx.scene.text.FontWeight.SEMI_BOLD
                : javafx.scene.text.FontWeight.NORMAL, tamanho);
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
    public static javafx.scene.control.Label rotulo(String conteudo, double tamanho,
                                                    boolean negrito, Color cor) {
        javafx.scene.control.Label rotulo = new javafx.scene.control.Label(conteudo);
        rotulo.setFont(fonte(tamanho, negrito));
        rotulo.setTextFill(cor);

        return rotulo;
    }

    /** Estilos de botao equivalentes aos do iOS: preenchido, de vidro e destrutivo. */
    public enum Tipo {
        /** Acao principal: preenchido com a cor de destaque. */
        PRIMARIO(VERDE, VERDE_REALCE, "white", "transparent"),
        /** Acao secundaria: material translucido sobre o fundo. */
        VIDRO(EstiloIOS.VIDRO, VIDRO_REALCE, "white", BORDA_VIDRO),
        /** Acao destrutiva: texto vermelho sobre vidro. */
        DESTRUTIVO(EstiloIOS.VIDRO, VIDRO_REALCE, VERMELHO, BORDA_VIDRO);

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
        Botao arredondado com o retorno tatil do iOS: clareia ao passar o mouse e
        encolhe levemente enquanto pressionado.
    */
    public static Button botao(String rotulo, Tipo tipo, double largura, double altura, Runnable acao) {
        Button botao = new Button(rotulo);
        botao.setPrefSize(largura, altura);
        botao.setFont(fonte(altura * 0.36, true));
        botao.setCursor(javafx.scene.Cursor.HAND);
        botao.setOnAction(evento -> acao.run());

        aplicarEstilo(botao, tipo, tipo.fundo);

        botao.setOnMouseEntered(evento -> aplicarEstilo(botao, tipo, tipo.fundoRealce));
        botao.setOnMouseExited(evento -> {
            aplicarEstilo(botao, tipo, tipo.fundo);
            escalar(botao, 1.0);
        });

        botao.setOnMousePressed(evento -> escalar(botao, 0.97));
        botao.setOnMouseReleased(evento -> escalar(botao, 1.0));

        return botao;
    }

    private static void aplicarEstilo(Button botao, Tipo tipo, String fundo) {
        botao.setStyle(
                "-fx-background-color: " + fundo + ";"
                        + "-fx-text-fill: " + tipo.corDoTexto + ";"
                        + "-fx-background-radius: " + RAIO + ";"
                        + "-fx-border-radius: " + RAIO + ";"
                        + "-fx-border-color: " + tipo.borda + ";"
                        + "-fx-border-width: 1;"
                        // Remove o anel de foco padrao do JavaFX, que destoa do visual
                        + "-fx-focus-color: transparent;"
                        + "-fx-faint-focus-color: transparent;");
    }

    private static void escalar(Region no, double escala) {
        no.setScaleX(escala);
        no.setScaleY(escala);
    }

    /** Painel de material escuro com cantos arredondados, base das folhas do iOS. */
    public static Rectangle material(double largura, double altura, double raio) {
        Rectangle painel = new Rectangle(largura, altura);
        painel.setArcWidth(raio * 2);
        painel.setArcHeight(raio * 2);
        painel.setFill(Color.web("#1C1C1E", 0.94));
        painel.setStroke(Color.web("#FFFFFF", 0.12));
        painel.setStrokeWidth(1);

        return painel;
    }

    /** Alca cinza que o iOS desenha no topo das folhas arrastaveis. */
    public static Rectangle alca() {
        Rectangle alca = new Rectangle(36, 5);
        alca.setArcWidth(5);
        alca.setArcHeight(5);
        alca.setFill(Color.web("#FFFFFF", 0.3));

        return alca;
    }

    /** Linha fina de separacao entre itens de uma lista. */
    public static Rectangle separador(double largura) {
        Rectangle linha = new Rectangle(largura, 1);
        linha.setFill(Color.web("#FFFFFF", 0.12));

        return linha;
    }
}

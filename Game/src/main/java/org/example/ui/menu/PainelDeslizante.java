package org.example.ui.menu;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

/*
    Folha que sobe pela base da tela, equivalente ao "sheet" do iOS.

    Usada pelos itens Controles e Creditos: uma camada escura cobre o menu, o painel
    entra deslizando de baixo e sai da mesma forma. Clicar fora dele o fecha.
*/
public class PainelDeslizante extends StackPane {

    private static final double LARGURA = 520;
    private static final double MARGEM_INTERNA = 24;
    private static final Duration DURACAO = Duration.millis(320);

    private final StackPane fundoEscuro;
    private final VBox folha;

    public PainelDeslizante(double larguraDaTela, double alturaDaTela,
                            String titulo, Map<String, String> itens, String rodape) {

        setPrefSize(larguraDaTela, alturaDaTela);
        setAlignment(Pos.BOTTOM_CENTER);

        // Camada que escurece o menu atras da folha
        fundoEscuro = new StackPane(new Rectangle(larguraDaTela, alturaDaTela, Color.web("#000000", 0.55)));
        fundoEscuro.setPrefSize(larguraDaTela, alturaDaTela);
        fundoEscuro.setOnMouseClicked(evento -> fechar());

        folha = montarFolha(titulo, itens, rodape);

        getChildren().addAll(fundoEscuro, folha);
    }

    private VBox montarFolha(String titulo, Map<String, String> itens, String rodape) {
        VBox conteudo = new VBox(0);
        conteudo.setAlignment(Pos.TOP_CENTER);
        conteudo.setPadding(new Insets(12, MARGEM_INTERNA, MARGEM_INTERNA, MARGEM_INTERNA));
        conteudo.setMaxWidth(LARGURA);
        conteudo.setPrefWidth(LARGURA);

        /*
            Sem esta linha o StackPane estica a folha ate a altura inteira da tela
            (o maxHeight padrao de um VBox e infinito) e o alinhamento na base nao
            tem efeito algum. Fixando no tamanho preferido, ela vira uma folha compacta
            encostada embaixo, como no iOS.
        */
        conteudo.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        /*
            O material de fundo e um retangulo arredondado atras do conteudo. Como a
            folha encosta na base da tela, so os cantos de cima ficam visiveis.
        */
        conteudo.setStyle(
                "-fx-background-color: " + EstiloIOS.MATERIAL_ESCURO + ";"
                        + "-fx-background-radius: " + EstiloIOS.RAIO_PAINEL + " "
                        + EstiloIOS.RAIO_PAINEL + " 0 0;");

        conteudo.getChildren().add(new Group(EstiloIOS.alca()));

        VBox cabecalho = new VBox(EstiloIOS.texto(titulo, 22, true, EstiloIOS.TEXTO));
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(16, 0, 14, 0));
        conteudo.getChildren().add(cabecalho);

        conteudo.getChildren().add(montarLista(itens));

        if (rodape != null && !rodape.isBlank()) {
            VBox caixaDoRodape = new VBox(EstiloIOS.texto(rodape, 13, false, EstiloIOS.TEXTO_SECUNDARIO));
            caixaDoRodape.setAlignment(Pos.CENTER);
            caixaDoRodape.setPadding(new Insets(14, 0, 0, 0));
            conteudo.getChildren().add(caixaDoRodape);
        }

        VBox caixaDoBotao = new VBox(EstiloIOS.botao("Concluído", EstiloIOS.Tipo.VIDRO,
                LARGURA - MARGEM_INTERNA * 2, 46, this::fechar));
        caixaDoBotao.setAlignment(Pos.CENTER);
        caixaDoBotao.setPadding(new Insets(18, 0, 0, 0));
        conteudo.getChildren().add(caixaDoBotao);

        return conteudo;
    }

    /*
        Lista agrupada do iOS: cada linha tem o rotulo a esquerda, o valor a direita
        e uma linha fina de separacao entre elas (menos depois da ultima).
    */
    private VBox montarLista(Map<String, String> itens) {
        double larguraInterna = LARGURA - MARGEM_INTERNA * 2;

        VBox lista = new VBox(0);
        lista.setStyle("-fx-background-color: rgba(255,255,255,0.08);"
                + "-fx-background-radius: " + EstiloIOS.RAIO + ";");
        lista.setPadding(new Insets(2, 14, 2, 14));
        lista.setMaxWidth(larguraInterna);

        int restantes = itens.size();

        for (Map.Entry<String, String> item : itens.entrySet()) {
            // O espacamento garante uma folga minima entre rotulo e valor
            HBox linha = new HBox(16);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(12, 0, 12, 0));

            Label rotulo = EstiloIOS.rotulo(item.getKey(), 15, true, EstiloIOS.TEXTO);
            rotulo.setMinWidth(Region.USE_PREF_SIZE);

            /*
                O valor ocupa o espaco restante e quebra a linha quando nao cabe.
                Sem isso, um texto longo (a lista de creditos, por exemplo) invadia
                o rotulo e os dois apareciam colados.
            */
            Label valor = EstiloIOS.rotulo(item.getValue(), 15, false, EstiloIOS.TEXTO_SECUNDARIO);
            valor.setWrapText(true);
            valor.setMaxWidth(Double.MAX_VALUE);
            valor.setAlignment(Pos.CENTER_RIGHT);
            valor.setTextAlignment(TextAlignment.RIGHT);
            HBox.setHgrow(valor, Priority.ALWAYS);

            linha.getChildren().addAll(rotulo, valor);
            lista.getChildren().add(linha);

            if (--restantes > 0) {
                lista.getChildren().add(EstiloIOS.separador(larguraInterna - 28));
            }
        }

        return lista;
    }

    /** Faz a folha entrar deslizando de baixo, com a camada escura surgindo junto. */
    public void abrir() {
        setVisible(true);

        folha.setTranslateY(folha.prefHeight(-1) + 60);
        fundoEscuro.setOpacity(0);

        animar(0, 1, Interpolator.SPLINE(0.32, 0.72, 0, 1)).play();
    }

    /*
        Recolhe a folha e a descarta ao terminar.

        Remover o painel da arvore (em vez de apenas escondê-lo) evita acumular uma
        folha invisivel a cada vez que Controles ou Creditos e aberto.
    */
    public void fechar() {
        ParallelTransition saida = animar(folha.prefHeight(-1) + 60, 0, Interpolator.EASE_IN);

        saida.setOnFinished(evento -> {
            if (getParent() instanceof javafx.scene.layout.Pane pai) {
                pai.getChildren().remove(this);
            }
        });

        saida.play();
    }

    private ParallelTransition animar(double destinoY, double opacidadeDoFundo, Interpolator suavizacao) {
        TranslateTransition deslize = new TranslateTransition(DURACAO, folha);
        deslize.setToY(destinoY);
        deslize.setInterpolator(suavizacao);

        FadeTransition escurecimento = new FadeTransition(DURACAO, fundoEscuro);
        escurecimento.setToValue(opacidadeDoFundo);

        return new ParallelTransition(deslize, escurecimento);
    }

    /** Atalho para montar os itens preservando a ordem de insercao. */
    public static Map<String, String> itens(String... paresRotuloValor) {
        Map<String, String> mapa = new LinkedHashMap<>();

        for (int i = 0; i + 1 < paresRotuloValor.length; i += 2) {
            mapa.put(paresRotuloValor[i], paresRotuloValor[i + 1]);
        }

        return mapa;
    }
}

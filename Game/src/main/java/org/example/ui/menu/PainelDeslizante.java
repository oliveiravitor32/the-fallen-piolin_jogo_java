package org.example.ui.menu;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    Folha que sobe pela base da tela, no formato do "sheet" do iOS.

    O conteudo e montado em partes, na ordem em que sao pedidas:

        new PainelDeslizante(largura, altura, "Controles")
                .comLista(itens(...))
                .comRodape("...")
                .comAcao("Concluido", Tipo.VIDRO, painel::fechar)

    Serve tanto para telas de informacao (Controles, Creditos) quanto para
    confirmacoes de duas opcoes, bastando trocar a lista por uma mensagem.

    Uma camada escura cobre o que esta atras e fecha a folha ao ser clicada.
*/
public class PainelDeslizante extends StackPane {

    private static final double LARGURA = 520;
    private static final double MARGEM_INTERNA = 24;
    private static final double LARGURA_INTERNA = LARGURA - MARGEM_INTERNA * 2;
    private static final double ALTURA_DO_BOTAO = 46;
    private static final Duration DURACAO = Duration.millis(320);

    private final StackPane fundoEscuro;
    private final VBox folha;
    private final VBox acoes;

    public PainelDeslizante(double larguraDaTela, double alturaDaTela, String titulo) {
        setPrefSize(larguraDaTela, alturaDaTela);
        setAlignment(Pos.BOTTOM_CENTER);

        // Camada que escurece o que esta atras da folha
        fundoEscuro = new StackPane(new Rectangle(larguraDaTela, alturaDaTela, Color.web("#000000", 0.55)));
        fundoEscuro.setPrefSize(larguraDaTela, alturaDaTela);
        fundoEscuro.setOnMouseClicked(evento -> fechar());

        folha = new VBox(0);
        folha.setAlignment(Pos.TOP_CENTER);
        folha.setPadding(new Insets(12, MARGEM_INTERNA, MARGEM_INTERNA, MARGEM_INTERNA));
        folha.setMaxWidth(LARGURA);
        folha.setPrefWidth(LARGURA);

        /*
            Sem esta linha o StackPane estica a folha ate a altura inteira da tela
            (o maxHeight padrao de um VBox e infinito) e o alinhamento na base nao
            tem efeito algum. Fixando no tamanho preferido, ela vira uma folha
            compacta encostada embaixo.
        */
        folha.setMaxHeight(Region.USE_PREF_SIZE);

        // Bordas so nos tres lados visiveis: a base fica fora da tela
        folha.setStyle(
                "-fx-background-color: " + EstiloDaInterface.MATERIAL_ESCURO + ";"
                        + "-fx-background-radius: " + EstiloDaInterface.RAIO_PAINEL + " "
                        + EstiloDaInterface.RAIO_PAINEL + " 0 0;"
                        + "-fx-border-color: rgba(255,255,255,0.20);"
                        + "-fx-border-width: " + EstiloDaInterface.BORDA + " " + EstiloDaInterface.BORDA
                        + " 0 " + EstiloDaInterface.BORDA + ";"
                        + "-fx-border-radius: " + EstiloDaInterface.RAIO_PAINEL + " "
                        + EstiloDaInterface.RAIO_PAINEL + " 0 0;");

        folha.getChildren().add(new Group(EstiloDaInterface.alca()));

        VBox cabecalho = new VBox(EstiloDaInterface.texto(titulo, 22, true, EstiloDaInterface.TEXTO));
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(16, 0, 14, 0));
        folha.getChildren().add(cabecalho);

        // As acoes ficam sempre no rodape: o conteudo e inserido acima delas
        acoes = new VBox(10);
        acoes.setAlignment(Pos.CENTER);
        acoes.setPadding(new Insets(18, 0, 0, 0));
        folha.getChildren().add(acoes);

        getChildren().addAll(fundoEscuro, folha);
    }

    /** Cartao agrupado com rotulo a esquerda e valor a direita, um item por linha. */
    public PainelDeslizante comLista(Map<String, String> itens) {
        VBox lista = new VBox(0);
        lista.setStyle("-fx-background-color: rgba(255,255,255,0.08);"
                + "-fx-background-radius: " + EstiloDaInterface.RAIO + ";"
                + "-fx-border-radius: " + EstiloDaInterface.RAIO + ";"
                + "-fx-border-color: rgba(255,255,255,0.20);"
                + "-fx-border-width: 2;");
        lista.setPadding(new Insets(2, 14, 2, 14));
        lista.setMaxWidth(LARGURA_INTERNA);

        int restantes = itens.size();

        for (Map.Entry<String, String> item : itens.entrySet()) {
            // O espacamento garante uma folga minima entre rotulo e valor
            HBox linha = new HBox(16);
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(12, 0, 12, 0));

            Label rotulo = EstiloDaInterface.rotulo(item.getKey(), 15, true, EstiloDaInterface.TEXTO);
            rotulo.setMinWidth(Region.USE_PREF_SIZE);

            /*
                O valor ocupa o espaco restante e quebra a linha quando nao cabe.
                Sem isso, um texto longo (a lista de creditos, por exemplo) invadia
                o rotulo e os dois apareciam colados.
            */
            Label valor = EstiloDaInterface.rotulo(item.getValue(), 15, false, EstiloDaInterface.TEXTO_SECUNDARIO);
            valor.setWrapText(true);
            valor.setMaxWidth(Double.MAX_VALUE);
            valor.setAlignment(Pos.CENTER_RIGHT);
            valor.setTextAlignment(TextAlignment.RIGHT);
            HBox.setHgrow(valor, Priority.ALWAYS);

            linha.getChildren().addAll(rotulo, valor);
            lista.getChildren().add(linha);

            if (--restantes > 0) {
                lista.getChildren().add(EstiloDaInterface.separador(LARGURA_INTERNA - 28));
            }
        }

        return inserirAntesDasAcoes(lista);
    }

    /** Texto centralizado, usado nas confirmacoes no lugar da lista. */
    public PainelDeslizante comMensagem(String mensagem) {
        return inserirAntesDasAcoes(
                caixaDeTexto(mensagem, 16, EstiloDaInterface.TEXTO, new Insets(4, 0, 10, 0)));
    }

    /** Observacao em texto menor, abaixo do conteudo principal. */
    public PainelDeslizante comRodape(String rodape) {
        return inserirAntesDasAcoes(
                caixaDeTexto(rodape, 13, EstiloDaInterface.TEXTO_SECUNDARIO, new Insets(14, 0, 0, 0)));
    }

    private VBox caixaDeTexto(String conteudo, double tamanho, Color cor, Insets espacamento) {
        Label texto = EstiloDaInterface.rotulo(conteudo, tamanho, false, cor);
        texto.setWrapText(true);
        texto.setMaxWidth(LARGURA_INTERNA);
        texto.setAlignment(Pos.CENTER);
        texto.setTextAlignment(TextAlignment.CENTER);

        VBox caixa = new VBox(texto);
        caixa.setAlignment(Pos.CENTER);
        caixa.setPadding(espacamento);

        return caixa;
    }

    /** Botao de rodape. Podem ser varios: aparecem empilhados na ordem pedida. */
    public PainelDeslizante comAcao(String rotulo, EstiloDaInterface.Tipo tipo, Runnable acao) {
        acoes.getChildren().add(
                EstiloDaInterface.botao(rotulo, tipo, LARGURA_INTERNA, ALTURA_DO_BOTAO, acao));

        return this;
    }

    /*
        Impede que um clique fora da folha a feche.

        Usado pelas telas de fim de jogo: ali a folha nao e opcional, e o motor fica
        pausado atras dela. Se pudesse ser dispensada, o jogador ficaria preso numa
        partida congelada sem nenhum botao na tela.
    */
    public PainelDeslizante semFecharAoClicarFora() {
        fundoEscuro.setOnMouseClicked(null);

        return this;
    }

    /** Botao que apenas recolhe a folha, como o "Concluido" e o "Cancelar". */
    public PainelDeslizante comAcaoDeFechar(String rotulo) {
        return comAcao(rotulo, EstiloDaInterface.Tipo.VIDRO, this::fechar);
    }

    private PainelDeslizante inserirAntesDasAcoes(Node conteudo) {
        folha.getChildren().add(folha.getChildren().indexOf(acoes), conteudo);

        return this;
    }

    /** Faz a folha entrar deslizando de baixo, com a camada escura surgindo junto. */
    public void abrir() {
        folha.setTranslateY(alturaOculta());
        fundoEscuro.setOpacity(0);

        animar(0, 1, Interpolator.SPLINE(0.32, 0.72, 0, 1)).play();
    }

    /*
        Recolhe a folha e a descarta ao terminar.

        Remover o painel da arvore (em vez de apenas escondê-lo) evita acumular uma
        folha invisivel a cada vez que um painel e aberto.
    */
    public void fechar() {
        ParallelTransition saida = animar(alturaOculta(), 0, Interpolator.EASE_IN);

        saida.setOnFinished(evento -> {
            if (getParent() instanceof Pane pai) {
                pai.getChildren().remove(this);
            }
        });

        saida.play();
    }

    /** Deslocamento que esconde a folha inteira abaixo da borda da tela. */
    private double alturaOculta() {
        return folha.prefHeight(-1) + 60;
    }

    private ParallelTransition animar(double destinoY, double opacidadeDoFundo, Interpolator suavizacao) {
        TranslateTransition deslize = new TranslateTransition(DURACAO, folha);
        deslize.setToY(destinoY);
        deslize.setInterpolator(suavizacao);

        FadeTransition escurecimento = new FadeTransition(DURACAO, fundoEscuro);
        escurecimento.setToValue(opacidadeDoFundo);

        return new ParallelTransition(deslize, escurecimento);
    }

    /** Atalho para montar os itens de uma lista preservando a ordem de insercao. */
    public static Map<String, String> itens(String... paresRotuloValor) {
        Map<String, String> mapa = new LinkedHashMap<>();

        for (int i = 0; i + 1 < paresRotuloValor.length; i += 2) {
            mapa.put(paresRotuloValor[i], paresRotuloValor[i + 1]);
        }

        return mapa;
    }
}

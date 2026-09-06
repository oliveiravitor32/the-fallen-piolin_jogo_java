package org.example.ui.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/*
    Tela centralizada, com a mesma arrumacao do menu principal e do menu de pausa:
    titulo grande no meio, mensagem logo abaixo e as acoes empilhadas.

    Usada no fim de jogo. Antes a vitoria e a derrota apareciam na folha inferior
    (PainelDeslizante), que e o formato certo para conteudo secundario aberto a
    pedido do jogador, mas fica deslocado para o desfecho da partida -- este merece
    o centro da tela, como as outras telas cheias.

    O fundo e apenas uma camada escura translucida: a partida continua visivel atras.
*/
public class PainelCentral extends StackPane {

    private static final double LARGURA_DO_BOTAO = 300;
    private static final double ALTURA_DO_BOTAO = 52;
    private static final double ESPACO_ENTRE_BOTOES = 14;
    private static final double LARGURA_DA_MENSAGEM = 420;

    private final VBox cabecalho;
    private final VBox acoes;

    public PainelCentral(double larguraDaTela, double alturaDaTela, String titulo) {
        setPrefSize(larguraDaTela, alturaDaTela);
        setAlignment(Pos.CENTER);

        Rectangle escurecimento =
                new Rectangle(larguraDaTela, alturaDaTela, Color.web("#0B0A12", 0.78));

        Text textoDoTitulo = EstiloDaInterface.texto(titulo, 44, true, EstiloDaInterface.TEXTO);
        textoDoTitulo.setEffect(new DropShadow(0, 5, 5, Color.web("#0B0A12", 0.85)));

        cabecalho = new VBox(10, textoDoTitulo);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(0, 0, 34, 0));

        acoes = new VBox(ESPACO_ENTRE_BOTOES);
        acoes.setAlignment(Pos.CENTER);

        VBox coluna = new VBox(cabecalho, acoes);
        coluna.setAlignment(Pos.CENTER);

        getChildren().addAll(escurecimento, coluna);
    }

    /** Linha de texto abaixo do titulo, explicando o desfecho. */
    public PainelCentral comMensagem(String mensagem) {
        Label texto = EstiloDaInterface.rotulo(mensagem, 16, false, EstiloDaInterface.TEXTO_SECUNDARIO);
        texto.setWrapText(true);
        texto.setMaxWidth(LARGURA_DA_MENSAGEM);
        texto.setAlignment(Pos.CENTER);
        texto.setTextAlignment(TextAlignment.CENTER);

        cabecalho.getChildren().add(texto);

        return this;
    }

    /** Botao de acao. Podem ser varios: aparecem empilhados na ordem pedida. */
    public PainelCentral comAcao(String rotulo, EstiloDaInterface.Tipo tipo, Runnable acao) {
        acoes.getChildren().add(
                EstiloDaInterface.botao(rotulo, tipo, LARGURA_DO_BOTAO, ALTURA_DO_BOTAO, acao));

        return this;
    }

    /** Faz o conteudo surgir subindo, igual as demais telas. */
    public void abrir() {
        EstiloDaInterface.animarEntrada(cabecalho, Duration.ZERO);
        EstiloDaInterface.animarEntrada(acoes, Duration.millis(90));
    }
}

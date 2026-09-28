package org.example.ui.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.Ajustes;

import java.util.ArrayList;
import java.util.List;

/*
    Folha "Ajustes" do menu principal: muda o balanceamento da partida sem editar o
    codigo. Cada linha tem o nome, um botao de menos, o valor e um de mais. No topo,
    tres predefinicoes (Facil, Normal, Dificil) mudam tudo de uma vez.

    Os valores moram em org.example.Ajustes e valem a partir da proxima partida.
*/
public final class PainelDeAjustes {

    private static final double ALTURA_DA_LINHA = 38;
    private static final double TAMANHO_DO_PASSO = 30;
    private static final double LARGURA_DO_VALOR = 64;

    private PainelDeAjustes() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    public static PainelDeslizante criar(double largura, double altura) {
        double larguraInterna = PainelDeslizante.larguraInterna();

        // Cada linha sabe se redesenhar; as predefinicoes chamam todas de uma vez
        List<Runnable> atualizacoes = new ArrayList<>();
        Runnable atualizarTudo = () -> atualizacoes.forEach(Runnable::run);

        double larguraDaPredefinicao = (larguraInterna - 16) / 3;

        HBox predefinicoes = new HBox(8,
                EstiloDaInterface.botao("Fácil", EstiloDaInterface.Tipo.VIDRO, larguraDaPredefinicao, 40,
                        () -> { Ajustes.facil(); atualizarTudo.run(); }),
                EstiloDaInterface.botao("Normal", EstiloDaInterface.Tipo.VIDRO, larguraDaPredefinicao, 40,
                        () -> { Ajustes.restaurarPadrao(); atualizarTudo.run(); }),
                EstiloDaInterface.botao("Difícil", EstiloDaInterface.Tipo.VIDRO, larguraDaPredefinicao, 40,
                        () -> { Ajustes.dificil(); atualizarTudo.run(); }));
        predefinicoes.setAlignment(Pos.CENTER);
        predefinicoes.setPadding(new Insets(0, 0, 12, 0));

        VBox lista = new VBox(0);
        lista.setStyle("-fx-background-color: rgba(255,255,255,0.08);"
                + "-fx-background-radius: " + EstiloDaInterface.RAIO + ";"
                + "-fx-border-radius: " + EstiloDaInterface.RAIO + ";"
                + "-fx-border-color: rgba(255,255,255,0.20);"
                + "-fx-border-width: 2;");
        lista.setPadding(new Insets(2, 10, 2, 14));
        lista.setMaxWidth(larguraInterna);

        List<Ajustes.Ajuste> ajustes = Ajustes.TODOS;

        for (int i = 0; i < ajustes.size(); i++) {
            lista.getChildren().add(linha(ajustes.get(i), atualizacoes));

            if (i < ajustes.size() - 1) {
                lista.getChildren().add(EstiloDaInterface.separador(larguraInterna - 24));
            }
        }

        return new PainelDeslizante(largura, altura, "Ajustes")
                .comConteudo(new VBox(predefinicoes, lista))
                .comRodape("As mudanças valem a partir da próxima partida e ficam salvas.")
                .comAcaoDeFechar("Concluído");
    }

    private static HBox linha(Ajustes.Ajuste ajuste, List<Runnable> atualizacoes) {
        Label nome = EstiloDaInterface.rotulo(ajuste.getNome(), 14, true, EstiloDaInterface.TEXTO);
        nome.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Label valor = EstiloDaInterface.rotulo(ajuste.getTexto(), 14, false, EstiloDaInterface.TEXTO_SECUNDARIO);
        valor.setMinWidth(LARGURA_DO_VALOR);
        valor.setPrefWidth(LARGURA_DO_VALOR);
        valor.setAlignment(Pos.CENTER);

        Runnable atualizar = () -> valor.setText(ajuste.getTexto());
        atualizacoes.add(atualizar);

        Button menos = passo("−", () -> { ajuste.diminuir(); atualizar.run(); });
        Button mais = passo("+", () -> { ajuste.aumentar(); atualizar.run(); });

        HBox linha = new HBox(6, nome, menos, valor, mais);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setMinHeight(ALTURA_DA_LINHA);
        linha.setPrefHeight(ALTURA_DA_LINHA);

        return linha;
    }

    private static Button passo(String simbolo, Runnable acao) {
        Button botao = EstiloDaInterface.botao(simbolo, EstiloDaInterface.Tipo.VIDRO,
                TAMANHO_DO_PASSO, TAMANHO_DO_PASSO, acao);
        botao.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        botao.setPadding(Insets.EMPTY);

        return botao;
    }
}

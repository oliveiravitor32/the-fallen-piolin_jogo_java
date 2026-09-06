package org.example.ui.menu;

import com.almasb.fxgl.app.scene.MenuType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/*
    Menu de pausa (tecla ESC ou P durante a partida).

    Antes esta tela era a padrao do FXGL, com fonte e cores da engine, o que destoava
    do resto do jogo. Agora segue o mesmo estilo do menu principal.

    A diferenca em relacao ao menu principal e o fundo: aqui nao entra papel de parede,
    so uma camada escura translucida, para o jogador continuar vendo a partida pausada
    atras do menu.
*/
public class MenuDePausa extends MenuComEstilo {

    public MenuDePausa() {
        super(MenuType.GAME_MENU);

        StackPane raiz = new StackPane();
        raiz.setPrefSize(getAppWidth(), getAppHeight());

        Rectangle escurecimento =
                new Rectangle(getAppWidth(), getAppHeight(), Color.web("#0B0A12", 0.72));

        raiz.getChildren().addAll(escurecimento, montarConteudo());

        getContentRoot().getChildren().add(raiz);
    }

    private StackPane montarConteudo() {
        Text titulo = EstiloDaInterface.texto("Pausa", 44, true, EstiloDaInterface.TEXTO);
        titulo.setEffect(new DropShadow(0, 5, 5, Color.web("#0B0A12", 0.85)));

        VBox cabecalho = new VBox(titulo);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(0, 0, 34, 0));

        VBox botoes = new VBox(ESPACO_ENTRE_BOTOES,
                botao("Continuar", EstiloDaInterface.Tipo.PRIMARIO, this::fireResume),
                botao("Controles", EstiloDaInterface.Tipo.VIDRO, this::abrirControles),
                botao("Reiniciar", EstiloDaInterface.Tipo.VIDRO, this::fireNewGame),
                botao("Menu principal", EstiloDaInterface.Tipo.VIDRO, this::confirmarVoltarAoMenu),
                botao("Sair", EstiloDaInterface.Tipo.DESTRUTIVO,
                        () -> confirmarSaida("O progresso da partida atual será perdido.")));
        botoes.setAlignment(Pos.CENTER);

        VBox coluna = new VBox(cabecalho, botoes);
        coluna.setAlignment(Pos.CENTER);

        StackPane conteudo = new StackPane(coluna);
        conteudo.setPrefSize(getAppWidth(), getAppHeight());

        animarSubida(cabecalho, Duration.ZERO);
        animarSubida(botoes, Duration.millis(90));

        return conteudo;
    }

    /*
        Nao usa fireExitToMainMenu() porque aquele metodo abre o dialogo padrao do
        FXGL. A confirmacao aqui e a nossa, no mesmo estilo do restante do menu.
    */
    private void confirmarVoltarAoMenu() {
        abrirPainel(PaineisDoMenu.confirmarVoltarAoMenu(getAppWidth(), getAppHeight(),
                () -> getController().gotoMainMenu()));
    }
}

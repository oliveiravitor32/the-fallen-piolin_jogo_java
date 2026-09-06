package org.example.ui.menu;

/*
    Conteudo das folhas que os dois menus abrem.

    Fica separado das telas porque Controles e a confirmacao de saida aparecem tanto
    no menu principal quanto no de pausa: assim o texto existe em um lugar so.
*/
public final class PaineisDoMenu {

    private PaineisDoMenu() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    public static PainelDeslizante controles(double largura, double altura) {
        return new PainelDeslizante(largura, altura, "Controles")
                .comLista(PainelDeslizante.itens(
                        "A  /  D", "Andar para a esquerda / direita",
                        "W", "Pular (pulo duplo)",
                        "Botão esquerdo", "Disparar pena",
                        "Botão direito", "Disparar água",
                        "ESC  ou  P", "Pausar"))
                .comRodape("A pena fere o Espalha Lixo. A água apaga o fogo e devolve vida à floresta.")
                .comAcaoDeFechar("Concluído");
    }

    public static PainelDeslizante creditos(double largura, double altura) {
        return new PainelDeslizante(largura, altura, "Créditos")
                .comLista(PainelDeslizante.itens(
                        "Desenvolvimento", "Guilherme Leandro, Helen Silva e Vitor Oliveira",
                        "Engine", "FXGL 21, de Almas Baimagambetov",
                        "Editor de mapas", "Tiled",
                        "Fonte", "Pixelify Sans (SIL OFL 1.1)"))
                .comRodape("Licenciado sob a MIT License · 2024")
                .comAcaoDeFechar("Concluído");
    }

    /*
        Substitui o dialogo padrao do FXGL, que usa a fonte e as cores da engine.
        Por isso as telas chamam este painel em vez de fireExit().
    */
    public static PainelDeslizante confirmarSaida(double largura, double altura,
                                                  String mensagem, Runnable aoConfirmar) {

        PainelDeslizante painel = new PainelDeslizante(largura, altura, "Sair do jogo?");

        // A partir do menu principal nao ha partida em andamento, entao nao ha aviso
        if (mensagem != null && !mensagem.isBlank()) {
            painel.comMensagem(mensagem);
        }

        return painel
                .comAcao("Sair", EstiloDaInterface.Tipo.DESTRUTIVO, aoConfirmar)
                .comAcaoDeFechar("Cancelar");
    }

    /*
        Confirmacao para abandonar a partida em andamento e voltar ao inicio.
    */
    public static PainelDeslizante confirmarVoltarAoMenu(double largura, double altura, Runnable aoConfirmar) {
        PainelDeslizante painel = new PainelDeslizante(largura, altura, "Voltar ao menu?");

        return painel
                .comMensagem("O progresso da partida atual será perdido.")
                .comAcao("Voltar ao menu", EstiloDaInterface.Tipo.DESTRUTIVO, aoConfirmar)
                .comAcaoDeFechar("Cancelar");
    }
}

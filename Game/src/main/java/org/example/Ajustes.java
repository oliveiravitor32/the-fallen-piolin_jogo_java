package org.example;

import java.util.List;
import java.util.prefs.Preferences;

/*
    Numeros de balanceamento que podem ser mudados pelo menu "Ajustes", sem mexer no
    codigo. Pensado para apresentacoes: da para deixar a partida mais facil para
    quem nunca jogou e mais dificil para quem ja conhece, entre uma partida e outra.

    E o unico estado "static" do jogo que atravessa partidas, e isso e proposital: os
    ajustes sao do jogador, nao da partida. Cada componente le o valor no momento em
    que nasce (initGame), entao uma mudanca vale a partir da proxima partida.

    Os valores ficam salvos com java.util.prefs, entao sobrevivem a fechar o jogo.
*/
public final class Ajustes {

    /** Um numero ajustavel: nome no menu, limites, passo e valor padrao. */
    public static final class Ajuste {
        private final String chave;
        private final String nome;
        private final String unidade;
        private final double minimo, maximo, passo, padrao;
        private double valor;

        private Ajuste(String chave, String nome, String unidade,
                       double minimo, double maximo, double passo, double padrao) {
            this.chave = chave;
            this.nome = nome;
            this.unidade = unidade;
            this.minimo = minimo;
            this.maximo = maximo;
            this.passo = passo;
            this.padrao = padrao;
            this.valor = PREFERENCIAS.getDouble(chave, padrao);
            this.valor = limitar(valor);
        }

        public String getNome() {
            return nome;
        }

        public double get() {
            return valor;
        }

        public int getInt() {
            return (int) Math.round(valor);
        }

        public void set(double novo) {
            valor = limitar(novo);
            PREFERENCIAS.putDouble(chave, valor);
        }

        public void aumentar() {
            set(valor + passo);
        }

        public void diminuir() {
            set(valor - passo);
        }

        /** Valor formatado para o menu, sem casas decimais quando o passo e inteiro. */
        public String getTexto() {
            boolean inteiro = passo == Math.floor(passo);
            String numero = inteiro
                    ? String.valueOf(getInt())
                    : String.format("%.2f", valor).replace('.', ',');

            return numero + unidade;
        }

        private double limitar(double v) {
            // Arredonda ao passo para nao acumular erro de ponto flutuante (0,1 + 0,2...)
            double arredondado = Math.round(v / passo) * passo;
            return Math.max(minimo, Math.min(maximo, arredondado));
        }
    }

    private static final Preferences PREFERENCIAS = Preferences.userNodeForPackage(Ajustes.class);

    // ------------------------------------------------------------------ ajustes

    public static final Ajuste VIDA_DO_PIOLIN =
            new Ajuste("vidaPiolin", "Vida do Piolin", "", 1, 50, 1, 10);

    public static final Ajuste VIDA_DO_ESPALHA_LIXO =
            new Ajuste("vidaInimigo", "Vida do Espalha Lixo", "", 5, 100, 5, 30);

    public static final Ajuste VIDA_DA_FLORESTA =
            new Ajuste("vidaFloresta", "Vida da floresta", "", 5, 100, 5, 30);

    public static final Ajuste DANO_DO_FOGO =
            new Ajuste("danoFogo", "Dano do fogo na floresta", "", 1, 10, 1, 3);

    public static final Ajuste CURA_DA_AGUA =
            new Ajuste("curaAgua", "Cura da água por fogo apagado", "", 0, 10, 1, 3);

    public static final Ajuste ESPERA_CONTRA_PIOLIN =
            new Ajuste("esperaPiolin", "Espera entre tiros no Piolin", " s", 0.3, 4, 0.05, 1.05);

    public static final Ajuste ESPERA_CONTRA_FLORESTA =
            new Ajuste("esperaFloresta", "Espera entre incêndios", " s", 0.5, 8, 0.25, 2.25);

    public static final Ajuste VELOCIDADE_DO_ESPALHA_LIXO =
            new Ajuste("velocidadeInimigo", "Velocidade do Espalha Lixo", "", 60, 360, 20, 180);

    public static final Ajuste TEMPO_DE_BRIGA =
            new Ajuste("tempoBriga", "Tempo brigando antes de queimar", " s", 1, 15, 0.5, 5.5);

    public static final List<Ajuste> TODOS = List.of(
            VIDA_DO_PIOLIN, VIDA_DO_ESPALHA_LIXO, VIDA_DA_FLORESTA,
            DANO_DO_FOGO, CURA_DA_AGUA,
            ESPERA_CONTRA_PIOLIN, ESPERA_CONTRA_FLORESTA,
            VELOCIDADE_DO_ESPALHA_LIXO, TEMPO_DE_BRIGA);

    // ------------------------------------------------------------------ predefinicoes

    public static void facil() {
        restaurarPadrao();
        VIDA_DO_PIOLIN.set(20);
        VIDA_DO_ESPALHA_LIXO.set(20);
        VIDA_DA_FLORESTA.set(45);
        DANO_DO_FOGO.set(2);
        CURA_DA_AGUA.set(2);
        ESPERA_CONTRA_PIOLIN.set(1.8);
        ESPERA_CONTRA_FLORESTA.set(3.5);
        VELOCIDADE_DO_ESPALHA_LIXO.set(140);
        TEMPO_DE_BRIGA.set(3.5);
    }

    public static void restaurarPadrao() {
        TODOS.forEach(ajuste -> ajuste.set(ajuste.padrao));
    }

    public static void dificil() {
        restaurarPadrao();
        VIDA_DO_PIOLIN.set(6);
        VIDA_DO_ESPALHA_LIXO.set(40);
        VIDA_DA_FLORESTA.set(25);
        DANO_DO_FOGO.set(4);
        CURA_DA_AGUA.set(4);
        ESPERA_CONTRA_PIOLIN.set(0.7);
        ESPERA_CONTRA_FLORESTA.set(1.5);
        VELOCIDADE_DO_ESPALHA_LIXO.set(220);
        TEMPO_DE_BRIGA.set(7);
    }

    private Ajustes() {
        // Classe utilitaria: nao deve ser instanciada.
    }
}

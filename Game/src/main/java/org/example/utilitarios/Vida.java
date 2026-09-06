package org.example.utilitarios;

/*
    Estado de vida de uma entidade.

    Esta classe é propositalmente "pura": não depende do FXGL nem do JavaFX.
    Isso resolve dois problemas de uma vez:

    1. A barra visual passa a ser DERIVADA da vida (getFracao()) em vez de ser
       decrementada em paralelo. Antes, vida e largura da barra eram atualizadas
       separadamente e saíam de sincronia (a barra do Espalha Lixo, por exemplo,
       perdia 3.33px por acerto e acumulava erro de ponto flutuante).
    2. A regra pode ser testada sem subir o motor do jogo (ver VidaTest).
*/
public class Vida {

    private final int maximo;
    private int atual;

    public Vida(int maximo) {
        if (maximo <= 0) {
            throw new IllegalArgumentException("A vida máxima deve ser maior que zero, recebido: " + maximo);
        }

        this.maximo = maximo;
        this.atual = maximo;
    }

    /** Reduz a vida, nunca abaixo de zero. */
    public void tomarDano(int quantidade) {
        exigirQuantidadeNaoNegativa(quantidade);
        atual = Math.max(0, atual - quantidade);
    }

    /** Recupera vida, nunca acima do máximo. */
    public void curar(int quantidade) {
        exigirQuantidadeNaoNegativa(quantidade);
        atual = Math.min(maximo, atual + quantidade);
    }

    private void exigirQuantidadeNaoNegativa(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa, recebido: " + quantidade);
        }
    }

    public int getAtual() {
        return atual;
    }

    public int getMaximo() {
        return maximo;
    }

    /** Fração restante entre 0.0 e 1.0, usada para dimensionar a barra visual. */
    public double getFracao() {
        return (double) atual / maximo;
    }

    public boolean estaZerada() {
        return atual == 0;
    }
}

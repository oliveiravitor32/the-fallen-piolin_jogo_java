package org.example.ui;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
    Barra de vida da interface: uma moldura (imagem) e um preenchimento (retângulo)
    cuja largura é sempre calculada a partir da fração de vida recebida.

    Nenhum código de fora mexe na largura diretamente, o que impede que a barra
    fique dessincronizada da vida real ou chegue a uma largura negativa.
*/
public class BarraDeVida {

    private final ImageView moldura;
    private final Rectangle preenchimento;
    private final double larguraMaxima;

    public BarraDeVida(String caminhoDaMoldura, double alturaDaMoldura, double molduraX, double molduraY,
                       double preenchimentoX, double preenchimentoY,
                       double larguraMaxima, double alturaDoPreenchimento, Color cor) {

        this.larguraMaxima = larguraMaxima;

        moldura = new ImageView(new Image(caminhoDaMoldura));
        moldura.setFitHeight(alturaDaMoldura);
        moldura.setPreserveRatio(true);
        moldura.setX(molduraX);
        moldura.setY(molduraY);
        moldura.setMouseTransparent(true);

        preenchimento = new Rectangle(larguraMaxima, alturaDoPreenchimento, cor);
        preenchimento.setX(preenchimentoX);
        preenchimento.setY(preenchimentoY);
        preenchimento.setMouseTransparent(true);
    }

    /** Ajusta a barra para uma fração entre 0.0 e 1.0. Valores fora da faixa são limitados. */
    public void atualizar(double fracao) {
        double fracaoLimitada = Math.max(0.0, Math.min(1.0, fracao));
        preenchimento.setWidth(larguraMaxima * fracaoLimitada);
    }

    /*
        O preenchimento vem depois da moldura para ficar desenhado por cima dela.
    */
    public Node[] getNos() {
        return new Node[] { moldura, preenchimento };
    }
}

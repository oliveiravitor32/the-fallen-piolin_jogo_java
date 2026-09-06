package org.example.ui;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/*
    Barra de vida da interface: uma moldura (imagem) e um preenchimento (retangulo)
    cuja largura e sempre calculada a partir da fracao de vida recebida.

    Nenhum codigo de fora mexe na largura diretamente, o que impede que a barra
    fique dessincronizada da vida real ou chegue a uma largura negativa.

    A moldura e desenhada no tamanho nativo do PNG, sem redimensionar. Antes a barra
    da floresta pedia 85 pixels de altura para uma imagem de 103: o JavaFX reamostrava
    a arte e borrava os pixels da moldura. Por isso o interior e informado como um
    deslocamento dentro da imagem, e nao como uma coordenada solta de tela.
*/
public class BarraDeVida {

    private final ImageView moldura;
    private final Rectangle preenchimento;
    private final double larguraMaxima;

    public BarraDeVida(String caminhoDaMoldura, double molduraX, double molduraY,
                       double interiorX, double interiorY,
                       double larguraDoInterior, double alturaDoInterior, Color cor) {

        this.larguraMaxima = larguraDoInterior;

        moldura = new ImageView(new Image(caminhoDaMoldura));
        moldura.setX(molduraX);
        moldura.setY(molduraY);
        moldura.setSmooth(false);
        moldura.setMouseTransparent(true);

        preenchimento = new Rectangle(larguraDoInterior, alturaDoInterior, cor);
        preenchimento.setX(molduraX + interiorX);
        preenchimento.setY(molduraY + interiorY);
        preenchimento.setMouseTransparent(true);
    }

    /*
        Ajusta a barra para uma fracao entre 0.0 e 1.0. Valores fora da faixa sao
        limitados, e a largura e arredondada para um numero inteiro de pixels: uma
        largura fracionaria faria o JavaFX desenhar uma coluna esmaecida na ponta.
    */
    public void atualizar(double fracao) {
        double fracaoLimitada = Math.max(0.0, Math.min(1.0, fracao));

        preenchimento.setWidth(Math.round(larguraMaxima * fracaoLimitada));
    }

    /*
        O preenchimento vem depois da moldura para ficar desenhado por cima dela.
    */
    public Node[] getNos() {
        return new Node[] { moldura, preenchimento };
    }
}

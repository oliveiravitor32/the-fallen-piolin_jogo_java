package org.example;

import com.almasb.fxgl.app.scene.StartupScene;
import org.example.ui.menu.VisualDeCarregamento;

/*
    Primeira tela que aparece ao abrir o jogo, antes de o motor terminar de subir.

    Sem esta classe o FXGL mostra a propria tela de inicializacao: um circulo azul
    girando sobre fundo preto, que nada tem a ver com o resto do jogo. Aqui ela usa
    o mesmo visual da tela de carregamento de nivel.

    A largura e a altura chegam por parametro porque nesta altura o FXGL ainda nao
    respondeu a getAppWidth()/getAppHeight().
*/
public class MainStartupScene extends StartupScene {

    public MainStartupScene(int largura, int altura) {
        super(largura, altura);

        getContentRoot().getChildren().setAll(VisualDeCarregamento.novo(largura, altura));
    }
}

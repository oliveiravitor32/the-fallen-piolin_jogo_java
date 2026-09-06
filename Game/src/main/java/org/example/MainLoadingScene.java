package org.example;

import com.almasb.fxgl.app.scene.LoadingScene;
import org.example.ui.menu.VisualDeCarregamento;

import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;

/*
    Espera exibida enquanto o nivel e carregado, entre o menu e a partida.

    O visual vem de VisualDeCarregamento, o mesmo usado na tela de inicializacao
    (MainStartupScene), para as duas esperas do FXGL ficarem identicas.
*/
public class MainLoadingScene extends LoadingScene {

    public MainLoadingScene() {
        getContentRoot().getChildren().setAll(
                VisualDeCarregamento.novo(getAppWidth(), getAppHeight()));
    }
}

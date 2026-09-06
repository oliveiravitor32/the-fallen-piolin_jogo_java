package org.example;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;

/*
    Sensor do inimigo: a cada intervalo verifica se o JOGADOR está ao alcance
    e, em caso positivo, decide aleatoriamente se dispara.

    Antes este componente procurava a "entidade mais próxima" com o predicado (e) -> true,
    ou seja, qualquer coisa: uma plataforma logo abaixo do inimigo contava como alvo.
    O resultado era o Espalha Lixo mirando no cenário. Agora o alvo é sempre o jogador.
*/
public class SensorComponent extends Component {

    /*
        Alcance de ataque em unidades do mundo.

        Com zoom 2.5 e janela de 1050px, a área visível tem cerca de 420 unidades de
        largura, então este valor faz o inimigo reagir quando o jogador está em tela.
        É o número a ajustar caso o combate fique fácil ou difícil demais.
    */
    private static final double DISTANCIA_DE_ATAQUE = 400;

    private static final Duration INTERVALO_DE_VERIFICACAO = Duration.millis(600);

    private boolean emEspera = false;

    @Override
    public void onUpdate(double tpf) {
        if (emEspera) {
            return;
        }

        emEspera = true;
        FXGL.getGameTimer().runOnceAfter(() -> emEspera = false, INTERVALO_DE_VERIFICACAO);

        // getSingletonOptional evita a exceção que .get() causaria quando o jogador
        // ainda não foi invocado (contagem regressiva) ou já saiu do mundo.
        getGameWorld().getSingletonOptional(EntityType.JOGADOR).ifPresent(jogador -> {
            if (getEntity().distance(jogador) < DISTANCIA_DE_ATAQUE && FXGLMath.randomBoolean()) {
                getEntity().getComponent(EnemyComponent.class).atirar(jogador);
            }
        });
    }
}

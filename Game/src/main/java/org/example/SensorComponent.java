package org.example;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Random;
import java.util.function.Predicate;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;


public class SensorComponent extends Component {

    private boolean emEspera = false;

     @Override
    public void onUpdate(double tpf) {

        if (!emEspera) {
            emEspera = true;

            FXGL.getGameTimer().runOnceAfter(() -> emEspera = false, Duration.millis(600));

            Entity enemy = getGameWorld().getSingleton(EntityType.ENEMY);

            Optional<Entity> entidadeMaisProxima = getGameWorld().getClosestEntity(enemy, (e) -> {
                return true;
            });

            if (getEntity().distance(entidadeMaisProxima.get()) < 100){

                Random random = new Random();
                boolean decisaoAleatoriaDeTiro = random.nextBoolean();

                if (decisaoAleatoriaDeTiro) {
                    getEntity().getComponent(EnemyComponent.class).atirar(entidadeMaisProxima.get());
                }
            }
        }
    }
}
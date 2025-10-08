package org.example;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.particle.ParticleComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.utilitarios.FimDeJogo;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;


public class ObjetoCombustivelComponent extends Component {

    private static FlorestaComponent florestaComponente = new FlorestaComponent();
    private int contadorDeParticulas = 0;


    public void tomaDano() {
        florestaComponente.tomar_dano();
        getEntity().getComponent(ParticleComponent.class).getEmitter().setNumParticles(++contadorDeParticulas);
    }

    public void recuperarVida() {
        // Recupera vida somente se o objeto combustível acertado estiver danificado.
        if (getEntity().getComponent(ParticleComponent.class).getEmitter().getNumParticles() > 0) {
            florestaComponente.recuperar_vida_da_floresta();
            getEntity().getComponent(ParticleComponent.class).getEmitter().setNumParticles(0);
        }
    }
}
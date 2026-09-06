package org.example;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;

/*
    Objeto combustível espalhado pelo mapa: quando atingido pelo fogo do Espalha Lixo
    ele pega fogo (mais partículas) e desconta vida da floresta; quando atingido pela
    água do Piolin, o fogo se apaga e a floresta recupera vida.
*/
public class ObjetoCombustivelComponent extends Component {

    private final Floresta floresta;
    private int contadorDeParticulas = 0;

    public ObjetoCombustivelComponent(Floresta floresta) {
        this.floresta = floresta;
    }

    public void tomaDano() {
        floresta.tomarDano();
        getEmissorDeParticulas().setNumParticles(++contadorDeParticulas);
    }

    /** Verdadeiro quando o objeto ja esta pegando fogo. Usado pela IA do inimigo. */
    public boolean estaEmChamas() {
        return getEmissorDeParticulas().getNumParticles() > 0;
    }

    public void recuperarVida() {
        // Recupera vida somente se o objeto combustível acertado estiver em chamas.
        if (estaEmChamas()) {
            floresta.recuperarVida();
            getEmissorDeParticulas().setNumParticles(0);
            contadorDeParticulas = 0;
        }
    }

    private ParticleEmitter getEmissorDeParticulas() {
        return getEntity().getComponent(ParticleComponent.class).getEmitter();
    }
}

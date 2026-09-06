package org.example.utilitarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
    Testes da regra de vida. Por ser uma classe pura, roda sem subir o motor do jogo.
*/
class VidaTest {

    @Test
    @DisplayName("comeca com a vida cheia")
    void comecaCheia() {
        Vida vida = new Vida(30);

        assertEquals(30, vida.getAtual());
        assertEquals(30, vida.getMaximo());
        assertEquals(1.0, vida.getFracao());
        assertFalse(vida.estaZerada());
    }

    @Test
    @DisplayName("a fracao acompanha o dano recebido")
    void fracaoAcompanhaODano() {
        Vida vida = new Vida(10);

        vida.tomarDano(1);
        assertEquals(9, vida.getAtual());
        assertEquals(0.9, vida.getFracao(), 1e-9);

        vida.tomarDano(4);
        assertEquals(0.5, vida.getFracao(), 1e-9);
    }

    @Test
    @DisplayName("o dano nunca leva a vida abaixo de zero")
    void danoNaoPassaDeZero() {
        Vida vida = new Vida(10);

        vida.tomarDano(50);

        assertEquals(0, vida.getAtual());
        assertEquals(0.0, vida.getFracao());
        assertTrue(vida.estaZerada());
    }

    @Test
    @DisplayName("a cura nunca ultrapassa a vida maxima")
    void curaNaoPassaDoMaximo() {
        Vida vida = new Vida(30);

        vida.tomarDano(6);
        vida.curar(5);
        assertEquals(29, vida.getAtual());

        vida.curar(100);
        assertEquals(30, vida.getAtual());
        assertEquals(1.0, vida.getFracao());
    }

    @Test
    @DisplayName("30 acertos de 1 zeram exatamente a vida do Espalha Lixo")
    void trintaAcertosZeramOInimigo() {
        Vida vida = new Vida(30);

        for (int i = 0; i < 30; i++) {
            assertFalse(vida.estaZerada(), "vida zerou cedo demais no acerto " + i);
            vida.tomarDano(1);
        }

        assertTrue(vida.estaZerada());
        assertEquals(0.0, vida.getFracao());
    }

    @Test
    @DisplayName("10 acertos de 3 zeram exatamente a vida da floresta")
    void dezAcertosZeramAFloresta() {
        Vida vida = new Vida(30);

        for (int i = 0; i < 10; i++) {
            vida.tomarDano(3);
        }

        assertTrue(vida.estaZerada());
    }

    @Test
    @DisplayName("rejeita vida maxima invalida e quantidades negativas")
    void rejeitaValoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Vida(0));
        assertThrows(IllegalArgumentException.class, () -> new Vida(-5));

        Vida vida = new Vida(10);
        assertThrows(IllegalArgumentException.class, () -> vida.tomarDano(-1));
        assertThrows(IllegalArgumentException.class, () -> vida.curar(-1));
    }
}

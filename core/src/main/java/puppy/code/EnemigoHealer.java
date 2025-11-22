package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import com.badlogic.gdx.audio.Sound;

public class EnemigoHealer extends EntidadJuego {

    private float healTimer = 0;
    private float tiempoEntreCuras = 2.0f;
    private int cantidadCura = 5;
    private Sound soundHeal;
    private Sound soundDepleted;

    // Variables para el movimiento
    private float tiempoVida = 0;
    private float velocidadX = 150f; // Velocidad de patrulla horizontal
    private int reservaCuraTotal = 20;

    public EnemigoHealer(float x, float y, Texture tx , Sound soundHeal , Sound soundDepleted) {
        super(tx, x, y, 120f, 8); // Vida baja (8)

        spr.setSize(60, 60);
        spr.setOriginCenter();
        this.hitbox.setSize(60, 60);
        this.soundHeal = soundHeal;
        this.soundDepleted = soundDepleted;

        // Color Verde inicial
        spr.setColor(0.5f, 1f, 0.5f, 1f);

        // Randomizamos la dirección inicial (izquierda o derecha)
        if (MathUtils.randomBoolean()) velocidadX *= -1;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        tiempoVida += delta;

        // --- 1. MOVIMIENTO MEJORADO (Patrulla + Onda) ---

        // A. Movimiento Horizontal (Rebotar en bordes)
        position.x += velocidadX * delta;

        // Rebote Izquierda
        if (position.x < 0) {
            position.x = 0;
            velocidadX *= -1; // Invertir dirección
        }
        // Rebote Derecha (1200 - ancho)
        if (position.x > 1200 - spr.getWidth()) {
            position.x = 1200 - spr.getWidth();
            velocidadX *= -1; // Invertir dirección
        }

        // B. Movimiento Vertical (Bajar y luego Ondular)
        float alturaBase = 550f; // Altura donde se estaciona

        if (position.y > alturaBase + 50) {
            // Si está muy arriba, baja rápido para entrar en combate
            position.y -= velocidadPEI * delta * 0.8f;
        } else {
            // Si ya está en altura, oscila suavemente (Onda Senoidal)
            // Amplitud: 40px (sube y baja 40 pixeles)
            // Velocidad: 2f
            position.y = alturaBase + MathUtils.sin(tiempoVida * 2f) * 40f;
        }

        spr.setPosition(position.x, position.y);


        // --- 2. LOGICA DE CURACIÓN (NERFEADA) ---
        healTimer += delta;
        if (healTimer > tiempoEntreCuras) {
            healTimer = 0;
            curarAliadoMasCercano(juego); // <--- CAMBIO IMPORTANTE
        }

        // --- 3. FEEDBACK VISUAL ---
        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(0.5f, 1f, 0.5f, 1f); // Volver a Verde
            }
        }
    }

    // AHORA SOLO CURA A UNO (El más cercano)
    private void curarAliadoMasCercano(PantallaJuego juego) {
        // Si ya no me queda energía, exploto inmediatamente antes de intentar curar
        if (reservaCuraTotal <= 0) {
            autodestruirse();
            return;
        }

        ArrayList<EntidadJuego> enemigos = juego.getEnemigos();
        EntidadJuego candidato = null;
        float menorDistanciaCuadrada = Float.MAX_VALUE;
        float rangoMaxCuadrado = 1000 * 1000;

        for (EntidadJuego e : enemigos) {
            if (e != this && !e.estaDestruido() && !(e instanceof EnemigoHealer)) {
                float diffX = e.getX() - this.position.x;
                float diffY = e.getY() - this.position.y;
                float distSq = diffX*diffX + diffY*diffY;

                if (distSq < rangoMaxCuadrado) {
                    if (distSq < menorDistanciaCuadrada) {
                        menorDistanciaCuadrada = distSq;
                        candidato = e;
                    }
                }
            }
        }

        if (candidato != null) {
            // --- LOGICA DE GASTO DE ENERGÍA ---

            // Solo curamos lo que nos quede en la reserva
            // Si cura 5 pero le quedan 2, solo cura 2.
            int curaReal = Math.min(cantidadCura, reservaCuraTotal);

            candidato.curar(curaReal);
            reservaCuraTotal -= curaReal;

            if (soundHeal != null) {
                long id = soundHeal.play(0.6f);
                soundHeal.setPitch(id, 1.5f);
            }

            // Si se acabó la batería, muere
            if (reservaCuraTotal <= 0) {
                autodestruirse();
            }
        }
    }
    private void autodestruirse() {
        if (soundDepleted != null) {
            // Sonido un poco más grave (Pitch 0.8) para que suene a "apagado"
            long id = soundDepleted.play(1.0f);
            soundDepleted.setPitch(id, 0.8f);
        }
        this.destruir();
    }


    @Override
    public Rectangle getHitbox() {
        hitbox.setPosition(position.x, position.y);
        return hitbox;
    }

    // --- DIFICULTAD ---
    public void aumentarDificultad(float factorRonda) {

        float factorUpgradeSalud = BuffManager.getInstance().getEnemyHealthMultiplier();
        float factorUpgradeVelocidad = BuffManager.getInstance().getEnemySpeedMultiplier();

        // 1. Healing Rate (Tiempo entre curas)
        this.tiempoEntreCuras = 2.0f / factorRonda;
        if (this.tiempoEntreCuras < 0.5f) this.tiempoEntreCuras = 0.5f;

        // 2. Healing Amount (Cantidad de cura)
        this.cantidadCura = (int) (5 * factorRonda);

        // 3. RESERVA DE CURA
        this.reservaCuraTotal = (int) (20 * factorRonda * factorUpgradeSalud);

        // 4. Velocidad Horizontal (Aplicamos el factor de Ronda y el de Velocidad)
        // Multiplicamos la velocidad ya existente por ambos factores
        this.velocidadX *= factorRonda * factorUpgradeVelocidad;
        if (this.velocidadX > 400) this.velocidadX = 400;

        // 5. VIDA ACTUAL (Fórmula corregida: Escalado Unificado)
        // Multiplicamos la vida inicial por los dos factores de salud (Ronda y Upgrade)
        this.vidaActual = (int)(this.vidaActual * factorRonda * factorUpgradeSalud);
    }
}

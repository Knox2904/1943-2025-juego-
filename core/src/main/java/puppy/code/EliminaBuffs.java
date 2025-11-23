package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class EliminaBuffs extends EntidadJuego {

    private PowerUp objetivo;

    public EliminaBuffs(float x, float y, Texture tx) {

        super(tx, x, y, 400f, 1);

        // Ajuste de tamaño visual
        spr.setSize(100, 100); // (Bajé de 500 a 300 para que no sea monstruoso)
        spr.setOriginCenter();

        // Ajuste de tamaño físico (Hitbox)
        this.hitbox.setSize(100, 100);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {

        // 1. BUSCAR OBJETIVO
        if (objetivo == null || objetivo.estaDestruido()) {
            objetivo = null;
            float distanciaMinima = Float.MAX_VALUE;

            for (PowerUp p : juego.getPowerUps()) {
                if (!p.estaDestruido()) {
                    float d = Vector2.dst(this.position.x, this.position.y, p.getHitbox().x, p.getHitbox().y);
                    // Aumenté el rango de visión a todo el mapa prácticamente
                    if (d < distanciaMinima) {
                        distanciaMinima = d;
                        objetivo = p;
                    }
                }
            }
        }

        // 2. MOVIMIENTO
        if (objetivo != null) {
            // --- MODO ATAQUE (Perseguir Buff) ---
            float targetX = objetivo.getHitbox().x;
            float targetY = objetivo.getHitbox().y;

            float deltaX = targetX - this.position.x;
            float deltaY = targetY - this.position.y;
            float distancia = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distancia > 0) {
                float moveX = (deltaX / distancia) * this.velocidadPEI * delta;
                float moveY = (deltaY / distancia) * this.velocidadPEI * delta;
                position.x += moveX;
                position.y += moveY;
            }

            // Rotación mirando al buff
            float angulo = MathUtils.atan2(deltaY, deltaX) * MathUtils.radiansToDegrees;
            spr.setRotation(angulo - 90);

            // COLISIÓN CON BUFF
            if (this.getHitbox().overlaps(objetivo.getHitbox())) {
                objetivo.destruir();
                this.destruir(); // Misión cumplida
            }

        } else {
            // --- MODO PATRULLA (Lurking arriba) ---
            // Queremos que se quede en Y = 700 aprox
            float alturaIdeal = Gdx.graphics.getHeight() - 100; // ~700

            // Usamos lerp para volver suavemente a la altura ideal
            position.y = MathUtils.lerp(position.y, alturaIdeal, 0.05f);

            // Movimiento lateral sinoidal para que no sea estático
            position.x += MathUtils.sin(Gdx.graphics.getFrameId() * 0.05f) * 3;

            // Enderezar la nave
            spr.setRotation(MathUtils.lerpAngleDeg(spr.getRotation(), 0, 0.1f));
        }

        spr.setPosition(position.x, position.y);

        // --- 3. LÍMITES DE SEGURIDAD (CORREGIDO) ---

        // MUERTE ABSOLUTA: Si baja de Y = -100, muere SIEMPRE.
        // Esto previene que se quede vivo fuera de la pantalla persiguiendo nada.
        if (position.y < -100) {
            this.destruir();
        }

        // Límite lateral: Si se va muy a los lados, muere.
        if (position.x < -200 || position.x > Gdx.graphics.getWidth() + 200) {
            this.destruir();
        }
        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco
            }
        }
    }

    public void aumentarDificultad(float factorRonda) {
        float factorVelocidad = BuffManager.getInstance().getEnemySpeedMultiplier();
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();

        // 1. VELOCIDAD (Lo más importante)
        // Este debe escalar agresivamente para competir con el jugador
        this.velocidadPEI *= factorRonda * factorVelocidad;

        // Tope de velocidad para que no atraviese paredes (Tunneling)
        if (this.velocidadPEI > 1400f) {
            this.velocidadPEI = 1400f;
        }

        // 2. VIDA (Escala normal o bajo)
        // Es una nave pequeña, no debería ser tanque.
        // Quizás solo escala con la ronda, no con los buffs de salud del jugador.
        this.vidaActual = (int) (this.vidaActual * factorRonda);
    }


}

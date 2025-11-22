package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

// Kamikaze hereda de EntidadJuego
public class Kamikaze extends EntidadJuego {

    // Componentes de velocidad
    private float xSpeed;
    private float ySpeed;
    //la nave del jugador.
    private Nave4 objetivo;

    /**
     * Constructor del Kamikaze.
     * @param spawnX Posición X donde aparece.
     * @param spawnY Posición Y donde aparece (arriba de la pantalla).
     * @param naveObjetivo La instancia de la Nave4 del jugador.
     * @param tx La textura (imagen) del kamikaze.
     * @param velocidadE La velocidad base (ej: 375.0f) que usará.
     */

    float multiplicador = BuffManager.getInstance().getEnemySpeedMultiplier();

    public Kamikaze(float spawnX, float spawnY, Nave4 naveObjetivo, Texture tx, float velocidadE) {
        super(tx, spawnX, spawnY,
            velocidadE * BuffManager.getInstance().getEnemySpeedMultiplier(),
            1);
        this.objetivo = naveObjetivo;

        spr.setSize(50, 50);
        spr.setOriginCenter();

        // Actualiza la hitbox
        this.hitbox.setSize(50, 50);

    }

    /**
     * Método update (obligatorio por ser clase abstracta)
     * Mueve el kamikaze en su dirección fija.
     * @param delta El tiempo desde el último frame.
     */
    @Override
    public void update(float delta, PantallaJuego juego) {

        //posicion actual del objetivo.
        float targetX = objetivo.getX();
        float targetY = objetivo.getY();

        // 2. Calcula el vector director (la dirección hacia el objetivo)
        //    (Usa 'this.x' y 'this.y' para la posición actual del enemigo)
        float deltaX = targetX - this.position.x;
        float deltaY = targetY - this.position.y;

        // 3. Calcula la distancia
        float distancia = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // 4. Calcula las velocidades X e Y para ESTE FRAME, el actual.
        float xSpeed, ySpeed;
        if (distancia > 0) {
            xSpeed = (deltaX / distancia) * this.velocidadPEI;
            ySpeed = (deltaY / distancia) * this.velocidadPEI;
        } else {
            xSpeed = 0;
            ySpeed = 0; // Se quedó quieto (ya llego)
        }

        // 5. Mueve la entidad usando las velocidades RECIEN CALCULADAS
        position.x += xSpeed * delta;
        position.y += ySpeed * delta;

        spr.setPosition(position.x, position.y);

        // 6.  Rota el sprite para que "mire" al jugador
        float angulo = MathUtils.atan2(deltaY, deltaX) * MathUtils.radiansToDegrees;
        spr.setRotation(angulo - 90);

        // 7. Comprueba si se salio de la pantalla para destruirse
        float margen = 50f; // Un margen para que desaparezca fuera de vista
        if (position.x + spr.getWidth() < -margen || position.x > Gdx.graphics.getWidth() + margen ||
            position.y + spr.getHeight() < -margen || position.y > Gdx.graphics.getHeight() + margen) {

            // Usa 'destroyed' (heredado de EntidadJuego)
            this.destroyed = true;
        }

        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco (Normal)
            }
        }


    }


    // En Kamikaze.java

    @Override
    public Rectangle getHitbox() {
        // 1. Calculamos cuánto "sobra" de imagen a los lados
        float diferenciaAncho = spr.getWidth() - hitbox.getWidth();
        float diferenciaAlto = spr.getHeight() - hitbox.getHeight();

        // 2. Movemos la hitbox:
        // Posición base + la mitad de lo que sobra
        hitbox.setPosition(
            position.x + (diferenciaAncho / 2),
            position.y + (diferenciaAlto / 2)
        );

        return hitbox;
    }

    public void aumentarDificultad(float factor) {
        // Aumentamos su velocidad de movimiento
        this.velocidadPEI *= factor;

        //límite para que no se teletransporte si la ronda es muy alta
        if (this.velocidadPEI > 800) {
            this.velocidadPEI = 800;
        }
    }


}

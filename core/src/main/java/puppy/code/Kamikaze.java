package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils; // Importa MathUtils para los cálculos

// Kamikaze hereda de EntidadJuego
public class Kamikaze extends EntidadJuego {

    // Componentes de velocidad (float primitivo es mejor que Float objeto)
    private float xSpeed;
    private float ySpeed;

    /**
     * Constructor del Kamikaze.
     * @param spawnX Posición X donde aparece.
     * @param spawnY Posición Y donde aparece (arriba de la pantalla).
     * @param jugadorX Posición X del jugador (el objetivo inicial).
     * @param jugadorY Posición Y del jugador (el objetivo inicial).
     * @param tx La textura (imagen) del kamikaze.
     * @param velocidadE La velocidad base (ej: 375.0f) que usará.
     */
    public Kamikaze(float spawnX, float spawnY, float jugadorX , float jugadorY, Texture tx, float velocidadE ) {

        // 1. Llama al constructor padre (EntidadJuego)
        //    Esto inicializa 'x', 'y', 'spr' y guarda 'velocidadE' en el campo 'velocidadPEI' heredado.
        super(tx, spawnX, spawnY, velocidadE);

        // --- LÓGICA DE DIRECCIÓN FIJA ---
        // 2. Calcula el vector director (la dirección hacia el objetivo)
        float deltaX = jugadorX - spawnX;
        float deltaY = jugadorY - spawnY;

        // 3. Calcula la distancia al objetivo
        float distancia = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY); // len() es como sqrt(x*x + y*y)

        // 4. Calcula y guarda las velocidades X e Y (¡solo se hace UNA VEZ!)
        if (distancia > 0) {
            // Usa 'this.velocidadPEI' (el campo heredado que 'super' acaba de establecer)
            this.xSpeed = (deltaX / distancia) * this.velocidadPEI;
            this.ySpeed = (deltaY / distancia) * this.velocidadPEI;
        } else {
            // Si aparece justo sobre el jugador, que baje recto
            this.xSpeed = 0;
            this.ySpeed = -this.velocidadPEI; // Hacia abajo
        }

        // 5. (Opcional) Rota el sprite para que "mire" hacia donde va
        float angulo = MathUtils.atan2(deltaY, deltaX) * MathUtils.radiansToDegrees;
        // Usa 'spr' (heredado de EntidadJuego)
        spr.setRotation(angulo - 90); // '- 90' es porque tu sprite apunta hacia arriba
    }

    /**
     * Método update (obligatorio por ser clase abstracta)
     * Mueve el kamikaze en su dirección fija.
     * @param delta El tiempo desde el último frame.
     */
    @Override
    public void update(float delta) {

        // 6. Mueve la entidad usando las velocidades FIJAS (calculadas en el constructor)
        //    Usa 'x' e 'y' (heredados de EntidadJuego)
        x += xSpeed * delta;
        y += ySpeed * delta;

        // Actualiza la posición del sprite
        spr.setPosition(x, y);

        // 7. Comprueba si se salió de la pantalla para destruirse
        float margen = 50f; // Un margen para que desaparezca fuera de vista
        if (x + spr.getWidth() < -margen || x > Gdx.graphics.getWidth() + margen ||
            y + spr.getHeight() < -margen || y > Gdx.graphics.getHeight() + margen) {

            // Usa 'destroyed' (heredado de EntidadJuego)
            this.destroyed = true;
        }
    }


}

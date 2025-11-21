package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class KamikazeS extends EntidadJuego {

    // Variables para el movimiento (Exclusivas de este enemigo)
    private float time = 0;
    private float centerX; // El eje central de la onda

    // Variables para el disparo (Lógica similar al Kamikaze)
    private Nave4 objetivo;
    private float fireTimer = 0;
    private Texture txBala;

    public KamikazeS(float x, float y, Nave4 naveObjetivo, Texture tx, Texture txBala, float velocidadE) {
        // 1. Llama al padre
        super(tx, x, y, velocidadE, 1); // Vida 1

        this.centerX = x; // Guardamos la X inicial
        this.objetivo = naveObjetivo;
        this.txBala = txBala;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        time += delta;

        // --- 1. MOVIMIENTO (Único de este enemigo: Olas) ---
        // Baja constante en Y
        position.y -= this.velocidadPEI * delta;

        // Oscila en X usando Seno (Matemática de ondas)
        float amplitud = 150f;
        float frecuencia = 3f;
        position.x = centerX + (amplitud * MathUtils.sin(time * frecuencia));

        spr.setPosition(position.x, position.y);

        // --- 2. DISPARO (Aquí reutilizas la lógica del Kamikaze) ---
        fireTimer += delta;
        if (fireTimer > 2.0f) { // Dispara cada 2 segundos
            fireTimer = 0;
            dispararAlJugador(juego);
        }

        // --- 3. LÍMITES ---
        if (position.y < -50) {
            destruir();
        }
    }

    private void dispararAlJugador(PantallaJuego juego) {
        // AQUI ESTÁ LA REUTILIZACIÓN:
        // Usamos la misma matemática del Kamikaze, pero para la bala.

        // 1. Calcular vector hacia el jugador
        float targetX = objetivo.getX();
        float targetY = objetivo.getY();
        float deltaX = targetX - this.position.x;
        float deltaY = targetY - this.position.y;

        // 2. Calcular distancia
        float distancia = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia > 0) {
            // 3. Calcular velocidad dirigida (igual que el Kamikaze)
            float velocidadBala = 400f; // Rápida
            float balaVX = (deltaX / distancia) * velocidadBala;
            float balaVY = (deltaY / distancia) * velocidadBala;

            // 4. Crear la bala con esa velocidad calculada
            float spawnX = position.x + spr.getWidth()/2 - 5; // Centrada

            // (Asegúrate de que Bullet acepte floats en el constructor)
            Bullet b = new Bullet(spawnX, position.y, balaVX, balaVY, txBala);
            juego.agregarBalaEnemiga(b);
        }
    }
}

package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class KamikazeS extends EntidadJuego {

    // Estados del patrón Zero
    private int estado = 0; // 0: Bajar, 1: Loop (Rizo), 2: Subir/Retirada

    // Variables para el movimiento circular
    private float anguloActual;
    private float centroGiroX;
    private float centroGiroY;
    private float radioLoop = 100f; // Radio del rizo

    private float fireTimer = 0;
    private Texture txBala;

    public KamikazeS(float x, float y, Nave4 naveObjetivo, Texture tx, Texture txBala, float velocidad) {
        super(tx, x, y, velocidad, 2); // Vida 2
        this.txBala = txBala;
        spr.setRotation(180); // Empieza mirando hacia abajo
    }

    @Override
    public void update(float delta, PantallaJuego juego) {

        switch (estado) {
            case 0: // --- BAJANDO ---
                position.y -= velocidadPEI * delta;
                spr.setRotation(180);

                // Al llegar al 60% de la altura, inicia el rizo
                if (position.y < Gdx.graphics.getHeight() * 0.6f) {
                    estado = 1;
                    // Configura el centro del giro a su derecha
                    centroGiroX = position.x + radioLoop;
                    centroGiroY = position.y;
                    anguloActual = MathUtils.PI; // 180 grados (lado izquierdo del círculo)
                }
                break;

            case 1:
                // Velocidad angular = Velocidad lineal / Radio
                float velocidadAngular = velocidadPEI / radioLoop;

                // Restamos ángulo para girar en sentido horario
                anguloActual -= velocidadAngular * delta;

                // Mover en círculo
                position.x = centroGiroX + radioLoop * MathUtils.cos(anguloActual);
                position.y = centroGiroY + radioLoop * MathUtils.sin(anguloActual);

                // Rotar sprite
                float grados = anguloActual * MathUtils.radiansToDegrees;
                spr.setRotation(grados - 90);

                // Si completó el giro y mira hacia arriba, salir
                if (anguloActual <= -1.5f * MathUtils.PI) {
                    estado = 2;
                    spr.setRotation(0);
                }
                break;

            case 2: // --- SUBIENDO (RETIRADA) ---
                position.y += velocidadPEI * delta;
                spr.setRotation(0);

                if (position.y > Gdx.graphics.getHeight() + 50) {
                    destruir();
                }
                break;
        }

        spr.setPosition(position.x, position.y);

        // --- DISPARO ---
        fireTimer += delta;
        if (fireTimer > 0.8f) { // Dispara rápido
            fireTimer = 0;
            dispararAlJugador(juego);
        }
    }

    private void dispararAlJugador(PantallaJuego juego) {
        Nave4 nave = juego.getNave();
        float deltaX = nave.getX() - this.position.x;
        float deltaY = nave.getY() - this.position.y;
        float distancia = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia > 0) {
            //  Velocidad baja (5f o 6f)
            float velocidadBala = 6f;
            float balaVX = (deltaX / distancia) * velocidadBala;
            float balaVY = (deltaY / distancia) * velocidadBala;

            // Spawn centrado
            float spawnX = position.x + spr.getWidth()/2 - 5;

            // Crear bala
            Bullet b = new Bullet(spawnX, position.y, balaVX, balaVY, txBala);


            juego.agregarBalaEnemiga(b);
        }
    }
}

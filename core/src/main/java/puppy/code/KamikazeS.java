package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

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

    private float multiplicadorDificultad = 1.0f;

    public KamikazeS(float x, float y, Nave4 naveObjetivo, Texture tx, Texture txBala, float velocidad) {
        super(tx, x, y, velocidad, 2); // Vida 2
        this.txBala = txBala;
        spr.setRotation(180); // Empieza mirando hacia abajo


        spr.setSize(50, 50);
        spr.setOriginCenter();

        // Actualiza la hitbox
        this.hitbox.setSize(50, 50);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {

        switch (estado) {
            case 0: // --- BAJANDO ---
                position.y -= velocidadPEI * delta;


                // Al llegar al 60% de la altura, inicia el rizo
                if (position.y < 800 * 0.6f) {
                    estado = 1;
                    centroGiroX = position.x + radioLoop;
                    centroGiroY = position.y;
                    anguloActual = MathUtils.PI;
                }
                break;

            case 1:
                // Velocidad angular = Velocidad lineal / Radio
                float velocidadAngular = velocidadPEI / radioLoop;
                anguloActual -= velocidadAngular * delta;

                // Mover en círculo
                position.x = centroGiroX + radioLoop * MathUtils.cos(anguloActual);
                position.y = centroGiroY + radioLoop * MathUtils.sin(anguloActual);




                // Si completó el giro y mira hacia arriba, salir
                if (anguloActual <= -1.5f * MathUtils.PI) {
                    estado = 2;

                }
                break;

            case 2: // --- SUBIENDO (RETIRADA) ---
                position.y += velocidadPEI * delta;


                if (position.y > 800 + 50) {
                    destruir();
                }
                break;
        }

        spr.setPosition(position.x, position.y);

        // --- 2. NUEVA LÓGICA: SIEMPRE MIRAR AL JUGADOR ---
        // Esto hace que el enemigo rote su sprite hacia la nave, sin importar cómo se mueva

        Nave4 nave = juego.getNave();

        // Calculamos los centros de ambos
        float myCenterX = position.x + spr.getWidth() / 2;
        float myCenterY = position.y + spr.getHeight() / 2;

        float playerCenterX = nave.getX() + nave.getHitbox().width / 2;
        float playerCenterY = nave.getY() + nave.getHitbox().height / 2;


        float diffX = playerCenterX - myCenterX;
        float diffY = playerCenterY - myCenterY;

        float angleRad = MathUtils.atan2(diffY, diffX);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        // Restamos 90 porque tu sprite original "mira" hacia arriba
        spr.setRotation(angleDeg - 90);



        // --- DISPARO ---
        fireTimer += delta;

        // FORMULA: El tiempo entre disparos se divide por la dificultad.
        // Si dificultad es 1.0 -> dispara cada 0.8 seg
        // Si dificultad es 2.0 -> dispara cada 0.4 seg (el doble de rápido)
        float tiempoEntreDisparos = 0.8f / multiplicadorDificultad;

        //Que nunca dispare más rápido que 0.2s (para no colapsar el juego)
        if (tiempoEntreDisparos < 0.2f) tiempoEntreDisparos = 0.2f;

        if (fireTimer > tiempoEntreDisparos) {
            fireTimer = 0;
            dispararAlJugador(juego);
        }

        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco (Normal)
            }
        }

    }

    private void dispararAlJugador(PantallaJuego juego) {
        Nave4 nave = juego.getNave();

        float originX = this.position.x + spr.getWidth() / 2;
        float originY = this.position.y + spr.getHeight() / 2;


        float deltaX = nave.getX() + nave.getWidth()/2 - originX; // Apuntar al centro de la nave
        float deltaY = nave.getY() + nave.getHeight()/2 - originY;

        float distancia = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distancia > 0) {
            //  Velocidad baja (5f o 6f)
            float velocidadBala = 6f;
            float balaVX = (deltaX / distancia) * velocidadBala;
            float balaVY = (deltaY / distancia) * velocidadBala;

            // Spawn centrado
            //float spawnX = position.x + spr.getWidth()/2 - 5;

            // Crear bala
            //Bullet b = new Bullet(spawnX, position.y, balaVX, balaVY, txBala);
            Bullet b = new Bullet(originX, originY, balaVX, balaVY, txBala);


            juego.agregarBalaEnemiga(b);
        }
    }

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
        this.multiplicadorDificultad = factor; // Guardamos el factor para el disparo
        this.velocidadPEI *= factor;           // Aumentamos la velocidad de vuelo

        // Tope de velocidad
        if (this.velocidadPEI > 900) this.velocidadPEI = 900;
    }


}

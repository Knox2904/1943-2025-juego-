package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class NaveTanque extends EntidadJuego {

    private float fireTimer = 0;
    private Texture txBala;
    private boolean enPosicion = false;

    private float tiempoEntreDisparos = 2.0f;

    public NaveTanque(float x, float y, Texture tx, Texture txBala) {
        // Velocidad lenta: 80, Vida ALTA: 10
        super(tx, x, y, 80f, 10);
        this.txBala = txBala;

        // Ajuste de tamaño visual
        spr.setSize(180, 180); // (Bajé de 500 a 300 para que no sea monstruoso)
        spr.setOriginCenter();

        // Ajuste de tamaño físico (Hitbox)
        this.hitbox.setSize(180, 180); // (Más pequeño que el sprite para ser justo)
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        // 1. Movimiento: Bajar hasta Y = AltoPantalla - 150
        if (position.y > 400) {
            position.y -= velocidadPEI * delta;
        } else {
            enPosicion = true; // Ya llegó, se detiene y empieza a disparar
        }
        spr.setPosition(position.x, position.y);

        // 2. Disparo
        if (enPosicion) {
            fireTimer += delta;


            if (fireTimer > tiempoEntreDisparos) {
                fireTimer = 0;
                disparar(juego);
            }
        }
    }

    @Override
    public Rectangle getHitbox() {
        // Centra la hitbox matemáticamente
        float diferenciaAncho = spr.getWidth() - hitbox.getWidth();
        float diferenciaAlto = spr.getHeight() - hitbox.getHeight();

        hitbox.setPosition(
            position.x + (diferenciaAncho / 2),
            position.y + (diferenciaAlto / 2)
        );

        return hitbox;
    }


    public void aumentarDificultad(float factor) {
        // 1. Velocidad de Movimiento (Sube poco, el tanque es pesado)
        this.velocidadPEI *= (1 + (factor - 1) * 0.3f);

        // 2. Vida
        this.vidaActual = (int) (this.vidaActual * factor);

        // 3. Cadencia de Disparo (Reducimos el tiempo entre disparos)
        // Si factor es 2.0 (doble dificultad), disparará cada 1.0 segundo.
        this.tiempoEntreDisparos = 2.0f / factor;

        // Tope de seguridad: Que no dispare mas rápido que una ametralladora (0.5s)
        if (this.tiempoEntreDisparos < 0.5f) {
            this.tiempoEntreDisparos = 0.5f;
        }
    }

    private void disparar(PantallaJuego juego) {
        float balaX = position.x + spr.getWidth()/2 - 5;
        float balaY = position.y + 50;

        juego.agregarBalaEnemiga(new Bullet(balaX, balaY, 0, -5f, txBala));   // Centro
        juego.agregarBalaEnemiga(new Bullet(balaX, balaY, 2f, -4f, txBala));  // Derecha
        juego.agregarBalaEnemiga(new Bullet(balaX, balaY, -2f, -4f, txBala)); // Izquierda
    }

}

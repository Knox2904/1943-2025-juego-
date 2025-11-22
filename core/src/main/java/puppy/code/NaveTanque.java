package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class NaveTanque extends EntidadJuego {

    private float fireTimer = 0;
    private Texture txBala;
    private boolean enPosicion = false;

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
            if (fireTimer > 2.0f) {
                fireTimer = 0;

                // Calculamos el centro para disparar
                float balaX = position.x + spr.getWidth()/2 - 5;
                float balaY = position.y  + 50 ; // Ajuste para que salga del cañón

                juego.agregarBalaEnemiga(new Bullet(balaX, balaY, 0, -5f, txBala));
                juego.agregarBalaEnemiga(new Bullet(balaX, balaY, 2f, -4f, txBala));
                juego.agregarBalaEnemiga(new Bullet(balaX, balaY, -2f, -4f, txBala));
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




}

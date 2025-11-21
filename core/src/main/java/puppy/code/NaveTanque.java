package puppy.code;

import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.math.MathUtils;

public class NaveTanque extends EntidadJuego {

    private float fireTimer = 0;
    private Texture txBala;
    private boolean enPosicion = false;

    public NaveTanque(float x, float y, Texture tx, Texture txBala) {
        // Velocidad lenta: 80, Vida ALTA: 10
        super(tx, x, y, 80f, 10);
        this.txBala = txBala;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {

        // 1. Movimiento: Bajar hasta llegar a Y=400 (parte superior de la pantalla)
        if (position.y > 400) {
            position.y -= velocidadPEI * delta;
        } else {
            enPosicion = true; // Ya llegó, se detiene y empieza a disparar
        }
        spr.setPosition(position.x, position.y);

        // 2. Disparo (Solo si ya llegó a su posición)
        if (enPosicion) {
            fireTimer += delta;
            if (fireTimer > 2.0f) { // Dispara cada 2 segundos
                fireTimer = 0;

                float balaX = position.x + spr.getWidth()/2 - 5;

                // Disparo triple en abanico
                // Bala Central
                juego.agregarBalaEnemiga(new Bullet(balaX, position.y, 0, -300f, txBala));
                // Bala Diagonal Derecha (velocidad X positiva)
                juego.agregarBalaEnemiga(new Bullet(balaX, position.y, 100, -280f, txBala));
                // Bala Diagonal Izquierda (velocidad X negativa)
                juego.agregarBalaEnemiga(new Bullet(balaX, position.y, -100, -280f, txBala));
            }
        }
    }
}

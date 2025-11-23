package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class FanFireStrategy implements IFireStrategy {

    // Array de ángulos en GRADOS.
    // 90 es hacia arriba. 100 es un poco a la izquierda, 80 a la derecha.
    private float[] angles;

    public FanFireStrategy(float[] angles) {
        this.angles = angles;
    }

    @Override
    public void fire(PantallaJuego juego, Texture txBala, float x, float y , int damage , int piercing) {
        float bulletSpeed = 10f; // Velocidad de la bala

        for (float angle : angles) {
            // Calculamos la velocidad en X e Y usando trigonometría básica
            float velX = bulletSpeed * MathUtils.cosDeg(angle);
            float velY = bulletSpeed * MathUtils.sinDeg(angle);

            Bullet b = new Bullet(x, y, velX, velY, txBala);


            b.setRotation(angle - 90);
            b.setPiercing(piercing);
            b.setDamage(damage);
            juego.agregarBala(b);
        }
    }
}

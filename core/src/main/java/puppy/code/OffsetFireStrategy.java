package puppy.code;

import com.badlogic.gdx.graphics.Texture;

public class OffsetFireStrategy implements IFireStrategy {

    private float[] offsets;


    public OffsetFireStrategy(float[] offsets) {
        this.offsets = offsets;
    }

    @Override
    public void fire(PantallaJuego juego, Texture txBala, float spawnX, float spawnY , int damage) {

        for (float offset : offsets) {
            Bullet bala = new Bullet(spawnX + offset, spawnY, 0, 3, txBala);
            bala.setDamage(damage);
            juego.agregarBala(bala);
        }
    }
}

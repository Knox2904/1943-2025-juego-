package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class SideShip extends GameObject {

    private float targetX ;
    private float targetY ;
    private float offsetX ;
    private Texture txBala;
    private float fireTimer = 0f;

    public SideShip(float x, float y, Texture texture, Texture txBala, float offsetX) {
        super(x, y, texture);

        this.txBala = txBala;
        this.offsetX = offsetX;

        this.targetX = x;
        this.targetY = y;
    }



    @Override
    public void update(float delta, PantallaJuego juego) {


        position.x = MathUtils.lerp(position.x, targetX + offsetX, 0.1f);
        position.y = MathUtils.lerp(position.y, targetY, 0.1f);
        spr.setPosition(position.x, position.y);


        fireTimer -= delta;
        if (fireTimer <= 0) {
            fireTimer = 2.0f; // Resetea el timer a 2 segundos


            float spawnX = position.x + (spr.getWidth() / 2) - 5; // -5 para centrar la bala
            float spawnY = position.y + spr.getHeight(); // Punta de la nave

            juego.agregarBala(new Bullet(spawnX, spawnY, 0, 5, txBala));
        }
    }


    public void setTargetPosition(float playerX, float playerY) {
        this.targetX = playerX;
        this.targetY = playerY;
    }

}

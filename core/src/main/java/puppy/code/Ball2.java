package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


public class Ball2 extends EntidadJuego {

    private float xSpeed; // Usamos float para 'delta time'
    private float ySpeed;

    // Dentro de Ball2.java

    public Ball2(float x, float y, int size, float xSpeed, float ySpeed, Texture tx) {
        //    Esto asigna this.x = x, this.y = y, y crea el sprite.
        super(tx, x, y);

        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;

        // Validar que borde de esfera no quede fuera

        if (this.x - size < 0) {
            this.x = x + size;
        }
        if (this.x + size > Gdx.graphics.getWidth()) {
            this.x = x - size;
        }

        if (this.y - size < 0) {
            this.y = y + size;
        }
        if (this.y + size > Gdx.graphics.getHeight()) {
            this.y = y - size;
        }

        //se valida la posicion, esto lo agregue solo por si acaso y por algo raro que sucedia.
        this.spr.setPosition(this.x, this.y);
    }

    // 4. Implementamos el método abstracto 'update'
    @Override
    public void update(float delta) {

        x += xSpeed * delta;
        y += ySpeed * delta;

        // Lógica de rebote (simplificada)
        if (x < 0 || x + spr.getWidth() > Gdx.graphics.getWidth()) {
            xSpeed *= -1;
        }
        if (y < 0 || y + spr.getHeight() > Gdx.graphics.getHeight()) {
            ySpeed *= -1;
        }

        spr.setPosition(x, y);
    }

    public void checkCollision(Ball2 b2) {
        if(this.getArea().overlaps(b2.getArea())){

            if (this.getXSpeed() == 0) {
                this.setXSpeed(this.getXSpeed() + b2.getXSpeed() / 2f);
            }
            if (b2.getXSpeed() == 0) {
                b2.setXSpeed(b2.getXSpeed() + this.getXSpeed() / 2f);
            }
            this.setXSpeed(-this.getXSpeed());
            b2.setXSpeed(-b2.getXSpeed());

            if (this.getySpeed() == 0) {
                this.setySpeed(this.getySpeed() + b2.getySpeed() / 2f);
            }
            if (b2.getySpeed() == 0) {
                b2.setySpeed(b2.getySpeed() + this.getySpeed() / 2f);
            }
            this.setySpeed(-this.getySpeed());
            b2.setySpeed(-b2.getySpeed());

        }
    }

    public void setXSpeed(float xSpeed) { this.xSpeed = xSpeed; }
    public void setySpeed(float ySpeed) { this.ySpeed = ySpeed; }
    public float getXSpeed() { return xSpeed; }
    public float getySpeed() { return ySpeed; }
}

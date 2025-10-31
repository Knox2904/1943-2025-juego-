package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;


public class Ball2 extends EntidadJuego {

    private float xSpeed; // Usamos float para 'delta time'
    private float ySpeed;

    // Dentro de Ball2.java

    public Ball2(float x, float y, int size,  float velocidadAsteroide, Texture tx) {
        //Esto asigna this.x = x, this.y = y, y crea el sprite.
        super(tx, x, y, velocidadAsteroide);

        float angulo = MathUtils.random(0f, 360f);
        this.xSpeed = MathUtils.cosDeg(angulo) * this.velocidadPEI;
        this.ySpeed = MathUtils.sinDeg(angulo) * this.velocidadPEI;
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
            x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - spr.getWidth()); //un seguro para ver que se quede dentro de los parametros
        }
        if (y < 0 || y + spr.getHeight() > Gdx.graphics.getHeight()) {
            ySpeed *= -1;
            y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - spr.getHeight());//Se aplica el seguro.
        }

        spr.setPosition(x, y);
    }

    public void checkCollision(Ball2 b2) {
        if(this.getArea().overlaps(b2.getArea())){

            // ahora la logica es mas simple, ahora se va a confirmar si chocan, por que se quedaban pegados antes, ahora se intercambian velocidades.
            float tempXSpeed = this.xSpeed;
            float tempYSpeed = this.ySpeed;
            this.setXSpeed(b2.getXSpeed());
            this.setySpeed(b2.getySpeed());
            b2.setXSpeed(tempXSpeed);
            b2.setySpeed(tempYSpeed);


            //logica de empuje para separar los asteroides.
            float pushAmount = 0.5f;
            // Mueve 'this' y 'b2' ligeramente en direcciones opuestas

            if (this.x < b2.x) { // Si 'this' está a la izquierda de 'b2'
                this.x -= pushAmount;
                b2.x += pushAmount;

            }
            else { // Si 'this' está a la derecha
                this.x += pushAmount;
                b2.x -= pushAmount;
            }
            if (this.y < b2.y) { // Si 'this' está debajo de 'b2'
                this.y -= pushAmount;
                b2.y += pushAmount;
            }
            else { // Si 'this' está encima
                this.y += pushAmount;
                b2.y -= pushAmount;
            }
            // Actualiza la posición visual inmediatamente después del empujón
            this.spr.setPosition(this.x, this.y);
            b2.spr.setPosition(b2.x, b2.y);
        }


    }

    public void setXSpeed(float xSpeed) { this.xSpeed = xSpeed; }
    public void setySpeed(float ySpeed) { this.ySpeed = ySpeed; }
    public float getXSpeed() { return xSpeed; }
    public float getySpeed() { return ySpeed; }
}

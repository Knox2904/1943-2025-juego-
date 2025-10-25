package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

// Esta es nuestra Súperclase (o Clase Padre)
// Es 'abstract' porque no queremos crear una 'EntidadJuego' genérica,
// solo subclases específicas (Asteroide, Kamikaze, etc).
public abstract class EntidadJuego {

    // Usamos 'protected' para que las subclases (como EnemigoKamikaze)
    // puedan acceder a ellas directamente
    // Esto es parte de la Encapsulación
    protected float x, y;
    protected Sprite spr;
    protected boolean destroyed = false;

    // Constructor base
    public EntidadJuego(Texture tx, float x, float y) {
        this.x = x;
        this.y = y;
        this.spr = new Sprite(tx);
        this.spr.setPosition(x, y);
    }

    // Método abstracto. Obliga a todas las subclases a implementar
    // su propia lógica de 'update'
    public abstract void update(float delta);

    // Métodos comunes que todas las entidades tendrán
    public void draw(SpriteBatch batch) {
        spr.draw(batch);
    }

    public Rectangle getArea() {
        return spr.getBoundingRectangle();

    }
}

    /* logica para un enemigo siguiente, todavia no se prueba
    public boolean isDestroyed() {
        return destroyed;
    }

    public void destruir() {
        this.destroyed = true;
    }

    // Getters para la posición
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
*/

package puppy.code;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;


public abstract class GameObject {

    protected Vector2 position;
    protected Sprite spr;
    protected Rectangle hitbox;

    // Constructor
    public GameObject(float x, float y, Texture texture) {
        this.position = new Vector2(x, y);
        this.spr = new Sprite(texture);
        this.spr.setPosition(x, y);


        this.hitbox = new Rectangle(x, y, spr.getWidth(), spr.getHeight());
    }

    // ---metodos abstract---

    public abstract void update(float delta, PantallaJuego juego);

    // ---metodos comunes---
    public void draw(SpriteBatch batch) {
        spr.draw(batch);
    }

    // ---getter---

    /**Devuelve el hitbox actualizado con la posición del sprite.
     *
     * @return hitbox
     */
    public Rectangle getHitbox() {
        hitbox.setPosition(position.x, position.y);
        return hitbox;
    }

    public float getWidth() {
        return spr.getWidth();
    }

    public float getHeight() {
        return spr.getHeight();
    }

    // --- NUEVO MÉTODO ---
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.spr.setPosition(x, y);
    }

}

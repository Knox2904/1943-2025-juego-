package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

enum TipoPowerUp  {

	MEJORA_ARMA,
    RECUPERAR_COMBUSTIBLE

}


public class PowerUp extends GameObject {

    private TipoPowerUp tipo;
    private float scrollSpeed = 100f ;

    public PowerUp(float x, float y, Texture texture, TipoPowerUp tipo) {
        super(x, y, texture);
        this.tipo = tipo;

        this.hitbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());

    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        position.y -= scrollSpeed * delta;
        spr.setPosition(position.x, position.y);

    }


    public TipoPowerUp getTipo() {
        return tipo;
    }

}

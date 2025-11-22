package puppy.code;

import com.badlogic.gdx.graphics.Texture;

public interface IFireStrategy {
    void fire(PantallaJuego juego, Texture txBala, float spawnX, float spawnY , int damage);
}

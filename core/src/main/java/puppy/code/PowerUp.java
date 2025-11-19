package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;


public abstract class PowerUp extends GameObject {

    private float scrollSpeed = 100f;
    private boolean destroyed = false;

    public PowerUp(float x, float y, Texture texture) {
        super(x, y, texture);
        this.hitbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }


    public final void recoger(Nave4 nave) {
        reproducirSonido();      //(Común)
        aplicarEfecto(nave);     //(Abstracto - Diferente en cada hijo)
        destruir();              //(Común)
    }


    protected abstract void aplicarEfecto(Nave4 nave);


    private void reproducirSonido() {
        // sonidoPowerUp.play();
        System.out.println("Sonido de PowerUp recogido");
    }

    private void destruir() {
        this.destroyed = true;
    }

    public boolean estaDestruido() {
        return destroyed;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        position.y -= scrollSpeed * delta;
        spr.setPosition(position.x, position.y);
    }

}

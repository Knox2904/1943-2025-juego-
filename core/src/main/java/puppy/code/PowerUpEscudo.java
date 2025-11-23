package puppy.code;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class PowerUpEscudo extends PowerUp {
    public PowerUpEscudo(float x, float y, Texture tx , Sound sonido) {
        super(x, y, tx , sonido);


        spr.setSize(40, 40);
        hitbox.setSize(40, 40);
        spr.setOriginCenter();
        this.tipo = TipoPowerUp.ESCUDO;
    }
    @Override
    public void aplicarEfecto(Nave4 nave) {
        nave.activarEscudo(); // <--- Llama al método que creamos en Nave4
    }
}

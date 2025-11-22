package puppy.code;

import com.badlogic.gdx.graphics.Texture;

public class PowerUpEscudo extends PowerUp {
    public PowerUpEscudo(float x, float y, Texture tx) {
        super(x, y, tx);
    }
    @Override
    public void aplicarEfecto(Nave4 nave) {
        nave.activarEscudo(); // <--- Llama al método que creamos en Nave4
    }
}

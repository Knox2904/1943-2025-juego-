package puppy.code;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class PowerUpCombustible extends PowerUp {

    public PowerUpCombustible(float x, float y, Texture texture , Sound sonido) {

        super(x, y, texture , sonido);

        spr.setSize(40, 40);
        hitbox.setSize(40, 40);
        spr.setOriginCenter();
        this.tipo = TipoPowerUp.COMBUSTIBLE;
    }

    @Override
    protected void aplicarEfecto(Nave4 nave) {
        // Lógica específica de combustible
        nave.agregarCombustible(40f);
        System.out.println("Combustible rellenado!");
    }
}

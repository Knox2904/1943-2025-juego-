package puppy.code;
import com.badlogic.gdx.graphics.Texture;

public class PowerUpAliado extends PowerUp {

    public PowerUpAliado(float x, float y, Texture texture) {

        super(x, y, texture);
        spr.setSize(40, 40);
        hitbox.setSize(40, 40);
        spr.setOriginCenter();
        this.tipo = TipoPowerUp.MEJORA_ARMA;
    }

    @Override
    protected void aplicarEfecto(Nave4 nave) {
        // Lógica específica de aliado
        nave.agregarAliado();
        System.out.println("Aliado unido!");
    }
}

package puppy.code;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class PowerUpArma extends PowerUp {

    private PantallaJuego juego; // Necesitamos 'juego' para mejorarArma

    public PowerUpArma(float x, float y, Texture texture, PantallaJuego juego , Sound sonido) {
        super(x, y, texture , sonido);
        this.juego = juego;
    }

    @Override
    protected void aplicarEfecto(Nave4 nave) {
        // Lógica específica de arma
        nave.mejorarArma(juego);
        System.out.println("Arma mejorada!");
    }
}

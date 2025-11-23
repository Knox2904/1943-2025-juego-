package puppy.code;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
public class EnemigoT2 implements OleadaFactory {
    private Texture txKamikazeS; // Textura para KamikazeS
    private Texture txTank;      // Textura para NaveTanque
    private Texture txBala;// Textura para las balas enemigas
    private Texture txHealer;
    private Sound sndHeal;
    private Sound sndHealDown;

    public EnemigoT2(Texture txKamikazeS, Texture txTank, Texture txBala, Texture txHealer, Sound sndHeal, Sound sndHealDown){
        this.txKamikazeS = txKamikazeS;
        this.txTank = txTank;
        this.txBala = txBala;
        this.txHealer = txHealer;
        this.sndHeal = sndHeal;
        this.sndHealDown = sndHealDown;
    }

    @Override
    public EntidadJuego createEnemigoT1(float x, float y, PantallaJuego juego) {
        // Nivel 2 Débil: KamikazeS (Sinusoidal)
        // Constructor: (x, y, objetivo, texturaNave, texturaBala, velocidad)
        return new KamikazeS(x, y, juego.getNave(), txKamikazeS, txBala, 200f);
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        // Nivel 2 Fuerte: NaveTanque
        return new NaveTanque(x, y, txTank, txBala);
    }

    @Override
    public EntidadJuego createEnemigoT3(float x, float y, PantallaJuego juego) {
        // Tier 3 (Especial): Healer
        return new EnemigoHealer(x, y, txHealer, sndHeal, sndHealDown);
    }

}

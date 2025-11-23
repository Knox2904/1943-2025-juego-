package puppy.code;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class EnemigoT4 implements OleadaFactory {

    // Boss 1
    private Texture txBoss1;
    private Texture txBalaBoss;

    // Boss 2
    private Texture txBossThomas;

    // Boss 3 (NUEVO)
    private Texture txBlackShip;
    private Texture txMinion; // Textura para el Kamikaze hijo

    public EnemigoT4(Texture txBoss1, Texture txBalaBoss,
                     Texture txBossThomas,
                     Texture txBlackShip, Texture txMinion) {

        this.txBoss1 = txBoss1;
        this.txBalaBoss = txBalaBoss;
        this.txBossThomas = txBossThomas;

        this.txBlackShip = txBlackShip;
        this.txMinion = txMinion;
    }

    @Override
    public EntidadJuego createEnemigoT1(float x, float y, PantallaJuego juego) {
        return new Boss(x, y, txBoss1, txBalaBoss, 500);
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        return new BossThomas(x, y, txBossThomas, txBalaBoss, 800, juego.getNave());
    }

    @Override
    public EntidadJuego createEnemigoT3(float x, float y, PantallaJuego juego) {
        // JEFE NIVEL 3: BLACK SHIP
        // Vida: 1200 (Muy alta)
        return new BossBlackShip(x, y, txBlackShip, txBalaBoss, txMinion);
    }
}


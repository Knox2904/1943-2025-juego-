package puppy.code;

import com.badlogic.gdx.graphics.Texture;

public class EnemigoT4 implements OleadaFactory {

    // Boss 1
    private Texture txBoss1;
    private Texture txBalaBoss;

    // Boss 2
    private Texture txBossThomas;

    // Boss 3
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
        // Boss 1: Mothership
        // Constructor: (x, y, textura, bala, vida, nombre)
        return new Boss(x, y, txBoss1, txBalaBoss, 500, "MOTHERSHIP OMEGA");
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        // Boss 2: Thomas
        // Constructor: (x, y, textura, bala, vida, naveObjetivo, nombre)
        return new BossThomas(x, y, txBossThomas, txBalaBoss, 800, juego.getNave(), "THOMAS EL ARRASADOR");
    }

    @Override
    public EntidadJuego createEnemigoT3(float x, float y, PantallaJuego juego) {
        // Boss 3: Black Ship
        // Constructor: (x, y, textura, bala, minion, vida, nombre)
        // Nota: Agregamos 1200 de vida y el nombre que faltaba
        return new BossBlackShip(x, y, txBlackShip, txBalaBoss, txMinion, 1200, "TITÁN DE MATERIA OSCURA");
    }
}

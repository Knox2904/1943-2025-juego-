package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class CargueroPesado extends EntidadJuego {

    private float spawnTimer = 0;
    private float xInicial; // Para recordar donde nació y oscilar desde ahí
    private float tiempoVida = 0; // Para calcular el movimiento Sinusoidal

    // Texturas
    private Texture txTank;
    private Texture txBala;
    private Texture txSmallCarrier;
    private Texture txKamikaze;
    private Texture txKamikazeS;
    private Texture txEliminaBuffs;
    private float tiempoEntreSpawns = 2.0f;

    public CargueroPesado(float x, float y, Texture txSelf, Texture txTank, Texture txBala,
                          Texture txSmallCarrier, Texture txKamikaze, Texture txKamikazeS , Texture txEliminaBuffs) {
        super(txSelf, x, y, 50f, 50); // Velocidad lenta, mucha vida
        this.xInicial = x; // Guardamos la X original

        this.txTank = txTank;
        this.txBala = txBala;
        this.txSmallCarrier = txSmallCarrier;
        this.txKamikaze = txKamikaze;
        this.txKamikazeS = txKamikazeS;
        this.txEliminaBuffs = txEliminaBuffs;

        // Ajuste de tamaño visual
        spr.setSize(200, 200); // (Bajé de 500 a 300 para que no sea monstruoso)
        spr.setOriginCenter();

        // Ajuste de tamaño físico (Hitbox)
        this.hitbox.setSize(200, 200);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        tiempoVida += delta;

        // 1. LÓGICA DE MOVIMIENTO
        // Si está muy arriba (> 600), baja.
        if (position.y > 600) {
            position.y -= velocidadPEI * delta;
            // Mientras baja, actualizamos xInicial para que empiece a oscilar desde donde quede
            xInicial = position.x;
        } else {
            // UNA VEZ LLEGA A 600, SE MUEVE DE LADO A LADO
            // MathUtils.sin devuelve valor entre -1 y 1. Multiplicamos por 150 para que se mueva 150px a cada lado.
            position.x = xInicial + MathUtils.sin(tiempoVida * 2.0f) * 150f;
        }

        if (position.x < 0) position.x = 0;
        if (position.x > 1200 - spr.getWidth()) position.x = 1200 - spr.getWidth();

        spr.setPosition(position.x, position.y);


        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco
            }
        }
        // 2. SPAWNER (Cada 3 segundos)
        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;
            spawnearHijo(juego);
        }
    }

    private void spawnearHijo(PantallaJuego juego) {
        // Spawnea justo en el centro del carguero
        float x = this.position.x + spr.getWidth()/2 - 20;
        float y = this.position.y;

        if (MathUtils.randomBoolean()) {
            juego.agregarEnemigo(new NaveTanque(x, y, txTank, txBala));
        } else {
            juego.agregarEnemigo(new Carguero(x, y, txSmallCarrier, txKamikaze, txKamikazeS, txEliminaBuffs , txBala));
        }
    }

    public void aumentarDificultad(float factorRonda) {
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();
        float factorVelocidad = BuffManager.getInstance().getEnemySpeedMultiplier();

        // 1. VELOCIDAD (Aumenta poco, son naves pesadas)
        // Usamos 0.2f para que no se vuelvan ferraris en rondas altas
        this.velocidadPEI *= (1 + (factorRonda - 1) * 0.2f) * factorVelocidad;

        // 2. VIDA (Escala MUCHO)
        // Multiplicamos por 1.2 extra porque son naves grandes que deben aguantar
        this.vidaActual = (int) (this.vidaActual * factorRonda * factorSalud * 1.2f);

        // 3. CADENCIA DE SPAWN (En vez de disparo)
        // Si factorRonda es 2.0, spawnean el doble de rápido.
        this.tiempoEntreSpawns = 2.0f / factorRonda; // (O 3.0f / factorRonda para el Pesado)

        // Tope de seguridad: Que no spawneen más rápido que cada 0.8 segundos
        if (this.tiempoEntreSpawns < 0.5f) {
            this.tiempoEntreSpawns = 0.5f;
        }
    }

}

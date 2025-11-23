package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class BossSummoner extends Boss {

    // Referencias para poder invocar a todos los tipos de enemigos
    private Texture txKamikaze;
    private Texture txTanque;
    private Texture txHealer;
    private Texture txThomas;
    private Texture txBlackShip;
    private Texture txEnemigoBase; // Para el minion de la BlackShip
    private Nave4 jugadorObjetivo;

    // Timers
    private float spawnTimer = 0;

    // [BALANCEO] Bajamos el tiempo base de 3.0 a 2.0 segundos para que empiece agresivo
    private float tiempoEntreSpawns = 2.0f;

    // [BALANCEO] Nueva variable: Cantidad de enemigos por oleada (Ráfaga)
    private int cantidadPorSpawn = 1;

    private boolean enPosicion = false;

    public BossSummoner(float x, float y, Texture txSelf, Texture txBala, int vida, String nombre,
                        Texture txKamikaze, Texture txTanque, Texture txHealer,
                        Texture txThomas, Texture txBlackShip, Texture txEnemigoBase, Nave4 jugador) {

        super(x, y, txSelf, txBala, vida, nombre);

        this.txKamikaze = txKamikaze;
        this.txTanque = txTanque;
        this.txHealer = txHealer;
        this.txThomas = txThomas;
        this.txBlackShip = txBlackShip;
        this.txEnemigoBase = txEnemigoBase;
        this.jugadorObjetivo = jugador;

        // Visual: Dorado Místico
        spr.setColor(1f, 0.8f, 0.2f, 1f);
        spr.setSize(200, 200);
        spr.setOriginCenter();
        hitbox.setSize(180, 180);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        super.update(delta, juego);

        // 1. MOVIMIENTO: Ir al centro superior y quedarse flotando
        if (!enPosicion) {
            float destinoX = 1200 / 2 - spr.getWidth() / 2;
            float destinoY = 800 / 2 + 150;

            // Lerp suave
            position.x += (destinoX - position.x) * 2f * delta;
            position.y += (destinoY - position.y) * 2f * delta;

            if (Math.abs(position.y - destinoY) < 5) enPosicion = true;
        } else {
            // Flotar (Respiración)
            position.y += MathUtils.sin(tiempoVida * 1.5f) * 0.5f;
        }

        spr.setPosition(position.x, position.y);

        // 2. INVOCACIÓN MÚLTIPLE (RÁFAGA)
        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;

            // [BALANCEO] Bucle para invocar varios enemigos simultáneos
            // En ronda 20, esto ejecutará 3 veces seguidas.
            for(int i = 0; i < cantidadPorSpawn; i++) {
                invocarEsbirro(juego);
            }
        }
    }

    // Sobrescribimos para que NO dispare balas normales
    @Override
    protected void disparar(float delta, PantallaJuego juego) { }

    private void invocarEsbirro(PantallaJuego juego) {
        // Límite de seguridad un poco más alto para soportar las hordas
        if (juego.getEnemigos().size() > 35) return;

        int dado = MathUtils.random(0, 100);

        // Posición aleatoria circular
        float angulo = MathUtils.random(0, 360);
        float distancia = 200f;
        float spawnX = (position.x + spr.getWidth()/2) + MathUtils.cosDeg(angulo) * distancia;
        float spawnY = (position.y + spr.getHeight()/2) + MathUtils.sinDeg(angulo) * distancia;

        EntidadJuego invocado = null;

        // --- TABLA DE PROBABILIDADES ---

        // 5% MINI-JEFE ESPECTRAL
        if (dado < 5) {
            if (MathUtils.randomBoolean()) {
                invocado = new BossThomas(spawnX, spawnY, txThomas, getTxBala(), 200, jugadorObjetivo, "THOMAS (ESPECTRO)");
                invocado.setColor(0.5f, 0.5f, 1f, 0.6f);
            } else {
                invocado = new BossBlackShip(spawnX, spawnY, txBlackShip, getTxBala(), txEnemigoBase, 250, "SOMBRA OSCURA");
                invocado.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            }
        }
        // 20% HEALER (Probabilidad aumentada para sostener al jefe)
        else if (dado < 25) {
            // Pasamos null en sonidos por ahora
            invocado = new EnemigoHealer(spawnX, spawnY, txHealer, null, null);
        }
        // 35% TANQUE
        else if (dado < 60) {
            invocado = new NaveTanque(spawnX, spawnY, txTanque, getTxBala());
        }
        // 40% KAMIKAZE (Carne de cañón rápida)
        else {
            invocado = new Kamikaze(spawnX, spawnY, jugadorObjetivo, txKamikaze, 400f);
        }

        if (invocado != null) {
            // Aplicamos dificultad de la ronda a los minions invocados
            if (!(invocado instanceof Boss)) {
                if (invocado instanceof EnemigoHealer) ((EnemigoHealer)invocado).aumentarDificultad(juego.getFactorDificultad());
                else if (invocado instanceof NaveTanque) ((NaveTanque)invocado).aumentarDificultad(juego.getFactorDificultad());
                else if (invocado instanceof Kamikaze) ((Kamikaze)invocado).aumentarDificultad(juego.getFactorDificultad());
            }
            juego.agregarEnemigo(invocado);
        }
    }

    @Override
    public void aumentarDificultad(float factorRonda) {
        super.aumentarDificultad(factorRonda);

        // [BALANCEO] Cálculo agresivo de velocidad de spawn
        // Ronda 20 (Factor ~2.9): 2.0 / 2.9 = 0.68 seg entre oleadas.
        this.tiempoEntreSpawns = 2.0f / factorRonda;

        // Límite de seguridad: Máximo 1 oleada cada 0.8 segundos
        if (this.tiempoEntreSpawns < 0.8f) this.tiempoEntreSpawns = 0.8f;

        // [BALANCEO] Cantidad de enemigos por oleada
        // Ronda 1-9: 1 enemigo
        // Ronda 10-19: 2 enemigos
        // Ronda 20+: 3 enemigos simultáneos
        this.cantidadPorSpawn = 1 + (int)(factorRonda * 0.7f);

        // Vida extra alta (x1.5) porque es un blanco estático
        this.vidaActual = (int)(this.vidaActual * 1.5f);
        this.vidaMax = this.vidaActual;
    }
}

package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class BossThomas extends Boss {

    // --- Máquina de Estados (Específica para Thomas) ---
    private enum EstadoThomas {
        ENTRANDO,            // Heredado de fase 0
        BUSCANDO,            // Se mueve lentamente hacia el jugador
        PREPARANDO_EMBESTIDA, // Se detiene y advierte (iluminación)
        EMBISTIENDO,         // Movimiento rápido
        REPOSICIONANDO       // Vuelve a la parte superior si choca contra borde
    }

    private EstadoThomas estadoThomas = EstadoThomas.ENTRANDO;

    // --- Propiedades del Jefe ---
    private Nave4 nave;
    private float velocidadBusqueda = 200f; // Más rápido que el Boss base
    private float velocidadEmbestida = 1000f; // Muy rápido
    private float tiempoPreparacion = 1.2f; // Tiempo de advertencia (1.2s)
    private float temporizadorAtaque = 0; // Temporizador para gestionar los estados

    private final float TIEMPO_MAX_BUSQUEDA = 4.0f; // Cuánto tiempo busca antes de embestir

    // Almacena el punto hacia donde va a embestir (donde estaba el jugador)
    private Vector2 objetivoEmbestida = new Vector2();
    // Guarda la dirección de la embestida una vez calculada
    private Vector2 direccionEmbestida = new Vector2();


    public BossThomas(float x, float y, Texture tx, Texture txBala, int vidaInicial, Nave4 nave) {
        super(x, y, tx, txBala, vidaInicial);
        this.nave = nave;
        // Ajustamos el sprite para que se parezca más a un tren lateral si es necesario
        // spr.setSize(250, 100);
    }

    // --- INVALICIÓN DEL UPDATE PARA INTEGRAR ESTADOS DE EMBESTIDA ---

    @Override
    public void update(float delta, PantallaJuego juego) {
        tiempoVida += delta;
        temporizadorAtaque += delta;

        // Heredamos la lógica de cambio de fase y feedback visual de golpe
        super.update(0, juego); // Llamada con delta 0 para que no ejecute el movimientoCombate y Disparar base

        // --- MÁQUINA DE ESTADOS ESPECÍFICA DE THOMAS ---
        switch (estadoThomas) {
            case ENTRANDO:
                // Hereda la lógica de entrada de la clase Boss (fase 0)
                if (position.y > 600) {
                    position.y -= 50 * delta;
                } else {
                    estadoThomas = EstadoThomas.BUSCANDO;
                    temporizadorAtaque = 0;
                }
                break;

            case BUSCANDO:
                movimientoBusqueda(delta); // Mueve lentamente al jugador
                if (temporizadorAtaque > TIEMPO_MAX_BUSQUEDA) {
                    prepararEmbestida();
                }
                break;

            case PREPARANDO_EMBESTIDA:
                // Se detiene y espera a que termine el tiempo de advertencia
                if (temporizadorAtaque > tiempoPreparacion) {
                    iniciarEmbestida();
                }
                break;

            case EMBISTIENDO:
                movimientoEmbestida(delta); // Mueve rápidamente
                comprobarLimitesMapa(Config.ALTO_MUNDO);
                break;

            case REPOSICIONANDO:
                reposicionarThomas(Config.ANCHO_MUNDO, Config.ALTO_MUNDO);
                break;
        }

        // --- COMPORTAMIENTO DE DISPARO (Aplica en BUSCANDO y EMBISTIENDO) ---
        if (estadoThomas == EstadoThomas.BUSCANDO || estadoThomas == EstadoThomas.PREPARANDO_EMBESTIDA) {
            // Se puede hacer que dispare mientras busca o prepara el ataque
            disparar(delta, juego);
        }

        // Actualiza la posición y el hitbox
        spr.setPosition(position.x, position.y);

        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco
            }
        }
    }

    // --- LÓGICA DE MOVIMIENTO DE THOMAS ---

    private void movimientoBusqueda(float delta) {
        // 1. Calcula la dirección al jugador
        Vector2 direccion = new Vector2(nave.getX() - position.x, nave.getY() - position.y).nor();

        // 2. Mueve a Thomas lentamente
        position.x += direccion.x * velocidadBusqueda * delta;
        position.y += direccion.y * velocidadBusqueda * delta;
    }

    private void prepararEmbestida() {
        // 1. Guarda la posición actual del jugador como objetivo
        objetivoEmbestida.set(nave.getX(), nave.getY());

        // 2. Calcula la dirección para la fase de embestida (normalizada)
        direccionEmbestida.set(objetivoEmbestida.x - position.x, objetivoEmbestida.y - position.y).nor();

        // 3. Transición
        temporizadorAtaque = 0;
        estadoThomas = EstadoThomas.PREPARANDO_EMBESTIDA;
    }

    private void iniciarEmbestida() {
        temporizadorAtaque = 0;
        estadoThomas = EstadoThomas.EMBISTIENDO;
        // Opcional: Cambiar sprite a uno más agresivo
        // spr.setColor(1f, 0, 0, 1f);
    }

    private void movimientoEmbestida(float delta) {
        // Mueve al jefe
        position.x += direccionEmbestida.x * velocidadEmbestida * delta;
        position.y += direccionEmbestida.y * velocidadEmbestida * delta;

        // *** 💡 NUEVA LÓGICA DE COLISIÓN ***
        // 1. Obtener la hitbox actualizada de Thomas
        this.getHitbox().setPosition(position.x + 25, position.y + 15); // Asumo los offsets de tu Boss.getHitbox()

        // 2. Verificar colisión con el jugador
        if (this.getHitbox().overlaps(nave.getHitbox())) {
            // Aplica un gran daño al jugador
            nave.recibirHit(50 , delta); // Ejemplo: 50 de daño por ser un jefe embistiendo

            // Detiene la embestida inmediatamente tras el impacto y vuelve a buscar
            estadoThomas = EstadoThomas.BUSCANDO;
            temporizadorAtaque = 0;

            // Opcional: añade un tiempo de invulnerabilidad temporal al jugador
            // jugador.setInvulnerable(true, 1.0f);
        }
    }

    private void comprobarLimitesMapa(int altoMapa) {
        int anchoMapa = Gdx.graphics.getWidth();

        // Comprobar si choca con el borde del mapa
        if (position.x < -spr.getWidth() || position.x > anchoMapa || position.y < -spr.getHeight() || position.y > altoMapa + spr.getHeight()) {
            // Chocó con cualquier borde o se fue de la pantalla
            estadoThomas = EstadoThomas.REPOSICIONANDO;
            temporizadorAtaque = 0;
            // Opcional: poner un efecto de choque
        }
    }

    private void reposicionarThomas(int anchoMapa, int altoMapa) {
        // 1. Teletransportar a una posición superior para reaparecer
        position.x = anchoMapa / 2f - spr.getWidth() / 2f;
        position.y = altoMapa + spr.getHeight(); // FUERA de la vista por arriba

        // 2. Espera un breve momento (1 segundo) para simular que sale de escena
        if (temporizadorAtaque > 1.0f) {
            temporizadorAtaque = 0;
            estadoThomas = EstadoThomas.BUSCANDO;
        }
    }

    // --- RENDERIZADO PARA LA ADVERTENCIA DEL CAMINO ---

    /**
     * Dibuja la línea de advertencia cuando Thomas se está preparando para embestir.
     * DEBE llamarse ANTES del SpriteBatch.begin() en PantallaJuego.render().
     */
    public void drawWarning(ShapeRenderer sr) {
        if (estadoThomas == EstadoThomas.PREPARANDO_EMBESTIDA) {

            sr.begin(ShapeRenderer.ShapeType.Filled);
            // Color de advertencia: Naranja/Rojo translúcido
            sr.setColor(1f, 0.5f, 0f, 0.6f);

            float centroX = position.x + spr.getWidth() / 2;
            float centroY = position.y + spr.getHeight() / 2;

            // Dibuja una línea gruesa desde Thomas hasta el punto objetivo
            // MathUtils.atan2 calcula el ángulo, MathUtils.cosDeg/sinDeg la dirección
            sr.rectLine(centroX, centroY,
                objetivoEmbestida.x, objetivoEmbestida.y,
                40f); // 40f es el grosor del camino

            sr.end();
        }
    }

    // --- INVALICIÓN DE MOVIMIENTO BASE ---


    protected void movimientoCombate(float delta) {
        // SOBREESCRIBIMOS el método de la clase Boss para que no ejecute
        // el movimiento de "Ocho" (Infinito) y use la máquina de estados de Thomas.
        // ¡No hacemos nada aquí!
    }

}

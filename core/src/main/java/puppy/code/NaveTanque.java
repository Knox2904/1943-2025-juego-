package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class NaveTanque extends EntidadJuego {

    private float fireTimer = 0;
    private Texture txBala;
    private boolean enPosicion = false;

    private float tiempoEntreDisparos = 2.0f;

    public NaveTanque(float x, float y, Texture tx, Texture txBala) {
        // Velocidad lenta: 80, Vida ALTA: 10
        super(tx, x, y, 80f, 10);
        this.txBala = txBala;

        // Ajuste de tamaño visual
        spr.setSize(180, 180); // (Bajé de 500 a 300 para que no sea monstruoso)
        spr.setOriginCenter();

        // Ajuste de tamaño físico (Hitbox)
        this.hitbox.setSize(180, 180); // (Más pequeño que el sprite para ser justo)
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        // 1. Movimiento: Bajar hasta Y = AltoPantalla - 150
        if (position.y > 400) {
            position.y -= velocidadPEI * delta;
        } else {
            enPosicion = true; // Ya llegó, se detiene y empieza a disparar
        }
        spr.setPosition(position.x, position.y);

        // --- 2. LÓGICA DE ROTACIÓN (QUE TE MIRE) ---
        Nave4 jugador = juego.getNave();

        // Calculamos los centros
        float myCenterX = position.x + spr.getWidth() / 2;
        float myCenterY = position.y + spr.getHeight() / 2;

        float playerCenterX = jugador.getX() + jugador.getHitbox().width / 2;
        float playerCenterY = jugador.getY() + jugador.getHitbox().height / 2;

        // Calculamos la diferencia
        float diffX = playerCenterX - myCenterX;
        float diffY = playerCenterY - myCenterY;

        // Obtenemos el ángulo
        float angleRad = MathUtils.atan2(diffY, diffX);
        float angleDeg = angleRad * MathUtils.radiansToDegrees;

        // Aplicamos la rotación (-90 asumiendo que tu imagen original mira hacia arriba)
        spr.setRotation(angleDeg + 90);



        // 3. Disparo
        if (enPosicion) {
            fireTimer += delta;


            if (fireTimer > tiempoEntreDisparos) {
                fireTimer = 0;
                disparar(juego);
            }
        }

        // 4. Feedback de Golpe
        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco (Normal)
            }
        }

    }

    @Override
    public Rectangle getHitbox() {
        // Centra la hitbox matemáticamente
        float diferenciaAncho = spr.getWidth() - hitbox.getWidth();
        float diferenciaAlto = spr.getHeight() - hitbox.getHeight();

        hitbox.setPosition(
            position.x + (diferenciaAncho / 2),
            position.y + (diferenciaAlto / 2)
        );

        return hitbox;
    }


    public void aumentarDificultad(float factor) {
        // 1. Velocidad de Movimiento (Sube poco, el tanque es pesado)
        this.velocidadPEI *= (1 + (factor - 1) * 0.3f);

        // 2. Vida
        this.vidaActual = (int) (this.vidaActual * factor);

        // 3. Cadencia de Disparo (Reducimos el tiempo entre disparos)
        // Si factor es 2.0 (doble dificultad), disparará cada 1.0 segundo.
        this.tiempoEntreDisparos = 2.0f / factor;

        // Tope de seguridad: Que no dispare mas rápido que una ametralladora (0.5s)
        if (this.tiempoEntreDisparos < 0.5f) {
            this.tiempoEntreDisparos = 0.5f;
        }
    }

    private void disparar(PantallaJuego juego) {
        // 1. Obtener posiciones (Origen y Destino)
        Nave4 jugador = juego.getNave();

        // Origen: El centro del Tanque
        float origenX = this.position.x + spr.getWidth() / 2;
        float origenY = this.position.y + spr.getHeight() / 2; // O +20 si quieres que salga de la boca

        // Destino: El centro del Jugador
        float destinoX = jugador.getX() + jugador.getHitbox().width / 2;
        float destinoY = jugador.getY() + jugador.getHitbox().height / 2;

        // 2. Calcular el ángulo base hacia el jugador
        float deltaX = destinoX - origenX;
        float deltaY = destinoY - origenY;

        // MathUtils.atan2 nos da el ángulo en Radianes
        float anguloRad = MathUtils.atan2(deltaY, deltaX);

        // Convertimos a Grados para poder sumar/restar el offset de las balas laterales
        float anguloGrados = anguloRad * MathUtils.radiansToDegrees;

        // Velocidad de las balas del tanque
        float velocidadBala = 7f;

        // 3. Disparar las 3 balas usando ese ángulo
        crearBalaDirigida(juego, origenX, origenY, anguloGrados, velocidadBala);       // Central (Directa)
        crearBalaDirigida(juego, origenX, origenY, anguloGrados - 15f, velocidadBala); // Derecha
        crearBalaDirigida(juego, origenX, origenY, anguloGrados + 15f, velocidadBala); // Izquierda
    }

    private void crearBalaDirigida(PantallaJuego juego, float x, float y, float anguloGrados, float velocidad) {

        float velX = velocidad * MathUtils.cosDeg(anguloGrados);
        float velY = velocidad * MathUtils.sinDeg(anguloGrados);


        juego.agregarBalaEnemiga(new Bullet(x, y, velX, velY, txBala));
    }

}

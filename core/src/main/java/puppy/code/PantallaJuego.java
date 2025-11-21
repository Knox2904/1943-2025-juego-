package puppy.code;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;



public class PantallaJuego implements Screen {

	private SpaceNavigation game;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Sound explosionSound;
	private Music gameMusic;
	private int score;
	private int ronda;
	private int velXAsteroides;
	private int velYAsteroides;
	private int cantAsteroides;

	private Nave4 nave;
    private ArrayList<Bullet> balasJugador = new ArrayList<>();
    private ArrayList<Bullet> balasEnemigas = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Texture powerUpTexture;
    private Texture texturaAliado;
    private Texture backgroundTexture;
    private float scrollSpeed = 100f; // Velocidad de scroll
    private float backgroundOffsetY = 0; // Posicion Y del fondo
    private Texture txBarra;
    private Texture txAsteroide;
    private Texture txEnemigo; // Textura para el kamikaze
    private Texture txBalaEnemiga;
    private Texture txTank;
    private Texture txKamikazeS; // Nuevo
    private OleadaFactory factory;
    //  Variables añadidas
    private ArrayList<EntidadJuego> enemigos = new ArrayList<>();
    private ArrayList<EntidadJuego> enemigosPendientes = new ArrayList<>();
    private float spawnTimerEnemigo = 0f; // Timer para el kamikaze (5 segundos)



	public PantallaJuego(SpaceNavigation game, int ronda, float combustible, int score,
			int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        this.game = game;
        this.ronda = ronda;
        this.score = score;
        this.velXAsteroides = velXAsteroides;
        this.velYAsteroides = velYAsteroides;
        this.cantAsteroides = cantAsteroides;

        batch = game.getBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 480, 640);
        //inicializar assets; musica de fondo y efectos de sonido
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.ogg"));
        explosionSound.setVolume(1, 0.25f);
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("piano-loops.wav"));

        gameMusic.setLooping(true);
        gameMusic.setVolume(0.5f);
        gameMusic.play();

        powerUpTexture = new Texture(Gdx.files.internal("powerUpDobleTiro.png"));
        texturaAliado = new Texture(Gdx.files.internal("MainShip3.png"));
        backgroundTexture = new Texture(Gdx.files.internal("fondoJuegoJava.jpg"));
        txEnemigo = new Texture(Gdx.files.internal("MainShip3.png")); // Kamikaze
        txAsteroide = new Texture(Gdx.files.internal("aGreyMedium4.png"));
        txBalaEnemiga = new Texture(Gdx.files.internal("Rocket2.png"));
        txTank = new Texture(Gdx.files.internal("aGreyLarge.png")); // Usa una imagen existente por ahora
        txKamikazeS = new Texture(Gdx.files.internal("MainShip3.png"));

        // Creamos un Pixmap de 1x1 píxel blanco puro
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        txBarra = new Texture(pixmap);
        pixmap.dispose();

        // cargar imagen de la nave, 64x64
        nave = new Nave4((float) Gdx.graphics.getWidth() / 2 - 50, 30f,
            new Texture(Gdx.files.internal("MainShip3.png")),
            Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
            new Texture(Gdx.files.internal("Rocket2.png")),
            Gdx.audio.newSound(Gdx.files.internal("pop-sound.mp3")),
            texturaAliado
        );
        //cambio a rondas, aqui se generan los enemigos por ronda.
        if (ronda == 1) {
            // Ronda 1: Fábrica "EnemigoT1" (Crea Ball2 y Kamikaze)
            factory = new EnemigoT1(txAsteroide, txEnemigo);
        } else {
            // Ronda 2+: Fábrica "EnemigoT2" (Crea KamikazeS y NaveTanque)
            factory = new EnemigoT2(txKamikazeS, txTank, txBalaEnemiga);
        }
        Random r = new Random();
        // 1. CALCULAR PRESUPUESTO DE LA RONDA
        // Ronda 1 = 10 puntos. Ronda 2 = 15 puntos. Ronda 3 = 20...
        int presupuesto = 10 + ((ronda - 1) * 5);

        // 2. COMPRAR ENEMIGOS HASTA AGOTAR PRESUPUESTO
        while (presupuesto > 0) {
            float x = r.nextInt(Gdx.graphics.getWidth() - 64);
            float y = Gdx.graphics.getHeight() + 50; // Todos spawnean arriba, la cola los administrará

            EntidadJuego nuevoEnemigo = null;
            int costo = 0;

            // logica de compra de rondas
            if (ronda == 1) {
                // Nivel 1: Solo Coste 1 (Asteroides y Kamikazes normales)
                if (r.nextBoolean()) {
                    nuevoEnemigo = factory.createEnemigoT1(x, y, this); // Asteroide
                    // Lógica de velocidad aleatoria para Asteroides
                    if (nuevoEnemigo instanceof Ball2) {
                        Ball2 b = (Ball2) nuevoEnemigo;
                        float velocidadLoca = 150f + r.nextInt((int) MathUtils.random(500f, 1000f));
                        float angulo = MathUtils.random(0f, 360f);
                        b.setXSpeed(MathUtils.cosDeg(angulo) * velocidadLoca);
                        b.setySpeed(MathUtils.sinDeg(angulo) * velocidadLoca);
                    }
                } else {
                    nuevoEnemigo = factory.createEnemigoT2(x, y, this); // Kamikaze
                }
                costo = 1;
            }
            else {
                // Nivel 2+: Variedad con Costos
                // Probabilidades ajustadas para gastar presupuesto
                int dado = r.nextInt(100);

                if (dado < 40 && presupuesto >= 3) {
                    // 40% Probabilidad: TANQUE (Coste 3)
                    nuevoEnemigo = factory.createEnemigoT2(x, y, this);
                    costo = 3;
                }
                else if (dado < 70 && presupuesto >= 2) {
                    // 30% Probabilidad: KAMIKAZE-S (Coste 2)
                    nuevoEnemigo = factory.createEnemigoT1(x, y, this);
                    costo = 2;
                }
                else {


                    // Por ahora, si no alcanza para tanque, compramos lo más barato disponible en esta factory
                    nuevoEnemigo = factory.createEnemigoT1(x, y, this);
                    costo = 2; //

                    //  si presupuesto es 1 y todo vale 2, rompemos el bucle
                    if (presupuesto < costo) break;
                }
            }

            // Añadir a la COLA DE PENDIENTES (No a la pantalla todavía)
            if (nuevoEnemigo != null) {
                enemigosPendientes.add(nuevoEnemigo);
                presupuesto -= costo;
            } else {
                break; // Evitar bucles infinitos si hay error
            }
        }

    }

        public void dibujaEncabezado() {
            float anchoBarraTotal = 200f;
            float altoBarra = 20f;
            float margenX = 10f;
            float margenY = Gdx.graphics.getHeight() - 40f;

            // Fondo de la barra (Gris)
            batch.setColor(0.2f, 0.2f, 0.2f, 1f);
            batch.draw(txBarra, margenX, margenY, anchoBarraTotal, altoBarra);

            // Barra de vida (Color según porcentaje)
            float porcentaje = nave.getCombustible() / nave.getMaxCombustible();
            float anchoActual = anchoBarraTotal * porcentaje;

            if (porcentaje > 0.6f) batch.setColor(0f, 1f, 0f, 1f); // Verde
            else if (porcentaje > 0.2f) batch.setColor(1f, 1f, 0f, 1f); // Amarillo
            else batch.setColor(1f, 0f, 0f, 1f); // Rojo

            batch.draw(txBarra, margenX, margenY, anchoActual, altoBarra);

            // Resetear color y dibujar textos
            batch.setColor(1f, 1f, 1f, 1f);
            game.getFont().getData().setScale(1.2f);
            game.getFont().draw(batch, "Score: " + this.score, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
            game.getFont().draw(batch, "High: " + game.getHighScore(), Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() - 20);
            game.getFont().getData().setScale(1.0f);
            game.getFont().draw(batch, "FUEL", margenX + 5, margenY + 15);
        }


    public Nave4 getNave() { return nave; }

    @Override
    public void render(float delta) {

        // --- 1. ACTUALIZACIONES ---
        nave.update(delta, this);

        // 1. Actualizar Balas JUGADOR
        for (Bullet b : balasJugador) {
            b.update();
            if (b.getX() < -10 || b.getX() > Gdx.graphics.getWidth() + 10 ||
                b.getY() < -10 || b.getY() > Gdx.graphics.getHeight() + 10) {
                b.recibirHit(1, 0);
            }
        }

        // 2. Actualizar Balas ENEMIGAS
        for (Bullet b : balasEnemigas) {
            b.update();
            // Check salida de pantalla
            if (b.getY() < -50 || b.getY() > Gdx.graphics.getHeight() + 50) {
                b.recibirHit(1, 0);
            }
        }
        // Actualizar TODOS los enemigos (Asteroides, Kamikazes, Tanques)
        for (EntidadJuego enemigo : enemigos) {
            enemigo.update(delta, this);
        }

        // Actualizar PowerUps
        for (PowerUp p : powerUps) {
            p.update(delta, this);
        }

        // --- SPAWNER PROGRESIVO (Sacar de la cola de pendientes) ---
        if (!enemigosPendientes.isEmpty()) {
            // Solo sacamos enemigos si hay en la cola
            spawnTimerEnemigo -= delta;

            if (spawnTimerEnemigo <= 0) {
                // 1. Sacar el siguiente enemigo de la lista de espera
                EntidadJuego enemigoASpawnear = enemigosPendientes.remove(0);

                // 2. Activarlo (meterlo al juego real)
                enemigos.add(enemigoASpawnear);

                // 3. Calcular cuándo sale el siguiente (ej: entre 0.5 y 1.5 segundos)
                //    Usamos el multiplicador de BuffManager para que sea más rápido si es difícil
                float spawnRate = BuffManager.getInstance().getEnemySpawnRateMultiplier();
                spawnTimerEnemigo = MathUtils.random(0.5f, 1.5f) / spawnRate;
            }
        }




        // --- 3. DIBUJADO ---
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        // Fondo y HUD
        dibujaEncabezado();

        // --- 4. COLISIONES ---
        if (!nave.estaHerido()) {
            // A. Balas vs Enemigos (Cualquier tipo)
            for (int i = 0; i < balasJugador.size(); i++) {
                Bullet b = balasJugador.get(i);
                if (b.estaDestruido()) continue;

                for (EntidadJuego enemigo : enemigos) {
                    if (!enemigo.estaDestruido()) {
                        if (b.checkCollision(enemigo.getHitbox())) {
                            explosionSound.play();
                            enemigo.recibirHit(1, delta); // Dañar enemigo
                            b.recibirHit(1, delta);       // Destruir bala
                            agregarScore(10);
                            break; // Bala choca solo una vez
                        }
                    }
                }
            }
            // B. Colisión: Balas ENEMIGAS contra JUGADOR
            for (Bullet b : balasEnemigas) {
                if (b.estaDestruido()) continue;
                if (b.checkCollision(nave.getHitbox())) {
                    nave.recibirHit(10, delta);
                    b.recibirHit(1, 0); // La bala enemiga se destruye
                }
            }

            // D. Colisión Asteroide vs Asteroide (Rebote)
            for (int i = 0; i < enemigos.size(); i++) {
                EntidadJuego e1 = enemigos.get(i);

                // Solo nos interesa si e1 es un Asteroide (Ball2)
                if (e1 instanceof Ball2) {
                    Ball2 b1 = (Ball2) e1;
                    if (b1.estaDestruido()) continue;

                    for (int j = i + 1; j < enemigos.size(); j++) {
                        EntidadJuego e2 = enemigos.get(j);

                        // Y si e2 TAMBIÉN es un Asteroide
                        if (e2 instanceof Ball2) {
                            Ball2 b2 = (Ball2) e2;
                            if (!b2.estaDestruido()) {
                                // ¡Hacemos que reboten!
                                b1.checkCollision(b2);
                            }
                        }
                    }
                }
            }

            // B. Nave vs Enemigos (Cualquier tipo)
            for (EntidadJuego enemigo : enemigos) {
                if (!enemigo.estaDestruido()) {
                    // Usamos el checkCollision de la nave
                    if (nave.checkCollision(enemigo, delta)) {
                        enemigo.recibirHit(10, delta); // El enemigo también recibe impacto
                    }
                }
            }

            // C. Nave vs PowerUps
            // Nave vs PowerUps
            for (int i = 0; i < powerUps.size(); i++) {
                PowerUp p = powerUps.get(i);
                p.update(delta, this); // Mover
                p.draw(batch);         // Dibujar

                if (p.getHitbox().overlaps(nave.getHitbox())) {

                    p.recoger(nave);

                    powerUps.remove(i);
                    i--;
                }
            }
        }

        // --- DIBUJAR SPRITES ---
        nave.draw(batch);
        for (Bullet b : balasJugador) b.draw(batch);
        for (Bullet b : balasEnemigas) b.draw(batch);
        for (EntidadJuego e : enemigos) e.draw(batch); // Dibuja todos los tipos
        for (PowerUp p : powerUps) p.draw(batch);

        batch.end();

        // --- 5. LIMPIEZA ---
        balasJugador.removeIf(Bullet::estaDestruido);
        balasEnemigas.removeIf(Bullet::estaDestruido);
        enemigos.removeIf(EntidadJuego::estaDestruido); // Limpia Asteroides, Kamikazes, etc.
        // (PowerUps se limpian al recogerlos, pero puedes añadir limpieza aquí si salen de pantalla)

        // --- 6. LÓGICA DE NIVEL ---
        if (nave.estaDestruido()) {
            // ... Game Over logic ...
            if (score > game.getHighScore()) game.setHighScore(score);
            Screen ss = new PantallaGameOver(game);
            ss.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.setScreen(ss);
            dispose();
            return;
        }

        // Si no quedan enemigos, pasamos a la siguiente ronda
        if (enemigos.isEmpty() && enemigosPendientes.isEmpty()) {
            // Crea la nueva pantalla con ronda + 1 y más enemigos
            Screen ss = new PantallaJuego(game,
                ronda + 1,
                nave.getCombustible(), // Mantiene el combustible actual
                score,velXAsteroides + 1,
                velYAsteroides + 1,
                cantAsteroides + 5);   // Aumenta dificultad

            ss.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.setScreen(ss);
            dispose();
        }
    }


    public boolean agregarBala(Bullet bb) {
    	return balasJugador.add(bb);
    }

    public boolean agregarBalaEnemiga(Bullet bb) {
        return balasEnemigas.add(bb);
    }

	@Override
	public void show() {
		// TODO Auto-generated method stub
		gameMusic.play();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		this.explosionSound.dispose();
		this.gameMusic.dispose();
        this.txBarra.dispose();
        this.txEnemigo.dispose();
        this.txAsteroide.dispose();
	}

    public void crearPowerUpEn(float x, float y, TipoPowerUp tipo) {
        PowerUp p = null;

        switch (tipo) {
            case MEJORA_ARMA:

                p = new PowerUpArma(x, y, powerUpTexture, this);
                break;
            case COMBUSTIBLE:

                p = new PowerUpCombustible(x, y, powerUpTexture);
                break;
            case NAVE_ALIADA:
                p = new PowerUpAliado(x, y, powerUpTexture);
                break;
        }

        if (p != null) {
            powerUps.add(p);
        }
    }


    public void agregarScore(int puntos){
        score+=puntos;
        if (this.score >= proximaMejora) {
            proximaMejora += 1000;
            mostrarPantallaMejoras();
        }
    }

    private int proximaMejora = 1000;

    /**
     * Pausa el juego actual y muestra la pantalla de selección de mejoras.
     */
    public void mostrarPantallaMejoras() {

        game.setScreen(new UpgradeScreen(game, this));
    }

}

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
	private  ArrayList<Ball2> balls1 = new ArrayList<>();
	private  ArrayList<Ball2> balls2 = new ArrayList<>();
	private  ArrayList<Bullet> balas = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Texture powerUpTexture;
    private Texture texturaAliado;
    private Texture backgroundTexture;
    private float scrollSpeed = 100f; // Velocidad de scroll
    private float backgroundOffsetY = 0; // Posicion Y del fondo
    private Texture txBarra;

    // --- (MERGE) --- Variables añadidas del compañero
    private ArrayList<Kamikaze> kamikazes = new ArrayList<>();
    private Texture txEnemigo; // Textura para el kamikaze
    private float spawnTimerEnemigo = 2f; // Timer para el kamikaze (5 segundos)

    private Texture txAsteroide;

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
		explosionSound.setVolume(1,0.25f);
		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("piano-loops.wav"));

		gameMusic.setLooping(true);
		gameMusic.setVolume(0.5f);
		gameMusic.play();

        powerUpTexture = new Texture(Gdx.files.internal("powerUpDobleTiro.png"));
        texturaAliado = new Texture(Gdx.files.internal("MainShip3.png"));
        backgroundTexture = new Texture(Gdx.files.internal("fondoJuegoJava.jpg"));
        txEnemigo = new Texture(Gdx.files.internal("MainShip3.png"));
        txAsteroide = new Texture(Gdx.files.internal("aGreyMedium4.png"));

        // Creamos un Pixmap de 1x1 píxel blanco puro
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        txBarra = new Texture(pixmap);
        pixmap.dispose();

	    // cargar imagen de la nave, 64x64
	    nave = new Nave4((float)Gdx.graphics.getWidth()/2-50,30f,
                        new Texture(Gdx.files.internal("MainShip3.png")),
	    				Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
	    				new Texture(Gdx.files.internal("Rocket2.png")),
	    				Gdx.audio.newSound(Gdx.files.internal("pop-sound.mp3")),
                        texturaAliado
                        );

        //crear asteroides
        Random r = new Random();
        for (int i = 0; i < cantAsteroides; i++) {

            Ball2 bb = new Ball2(
                (float) r.nextInt((int)Gdx.graphics.getWidth()),
                (float) (50+r.nextInt((int)Gdx.graphics.getHeight()-50)),
                20+r.nextInt(10),
                (float)velXAsteroides+r.nextInt(150),
                new Texture(Gdx.files.internal("aGreyMedium4.png")));
            balls1.add(bb);
            balls2.add(bb);
        }
	}

    public void dibujaEncabezado() {

        float anchoBarraTotal = 200f;
        float altoBarra = 20f;
        float margenX = 10f;
        float margenY = Gdx.graphics.getHeight() - 40f; // Arriba a la izquierda


        batch.setColor(0.2f, 0.2f, 0.2f, 1f); // Gris oscuro
        batch.draw(txBarra, margenX, margenY, anchoBarraTotal, altoBarra);

        float porcentaje = nave.getCombustible() / nave.getMaxCombustible();
        float anchoActual = anchoBarraTotal * porcentaje;


        if (porcentaje > 0.6f) {
            batch.setColor(0f, 1f, 0f, 1f); // Verde
        } else if (porcentaje > 0.2f) {
            batch.setColor(1f, 1f, 0f, 1f); // Amarillo
        } else {
            batch.setColor(1f, 0f, 0f, 1f); // Rojo (Peligro)
        }

        batch.draw(txBarra, margenX, margenY, anchoActual, altoBarra);


        batch.setColor(1f, 1f, 1f, 1f);


        game.getFont().getData().setScale(1.2f); // Un poco más pequeño para que quepa
        game.getFont().draw(batch, "Score: " + this.score, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
        game.getFont().draw(batch, "High: " + game.getHighScore(), Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() - 20);


        game.getFont().getData().setScale(1.0f);
        game.getFont().draw(batch, "FUEL", margenX + 5, margenY + 15);
    }



    @Override
    public void render(float delta) {

        // --- 1. LÓGICA DE ACTUALIZACIÓN ---
        nave.update(delta, this);

        // Mueve balas y comprueba si salieron de pantalla
        for (int i = 0; i < balas.size(); i++) {
            Bullet b = balas.get(i);
            b.update(); // (Bullet no usa 'delta' en tu versión)
            if (b.getX() < -10 || b.getX() > Gdx.graphics.getWidth() + 10 ||
                b.getY() < -10 || b.getY() > Gdx.graphics.getHeight() + 10) {
                b.recibirHit(1,0);
            }
        }

        // Mueve asteroides
        for (Ball2 ball : balls1) {
            ball.update(delta , this);
        }

        // Mueve Power-ups
        for (PowerUp p : powerUps) {
            p.update(delta, this);
        }

        // --- (MERGE) --- Mueve Kamikazes
        for (Kamikaze k : kamikazes) {
            k.update(delta , this);
        }


        // --- (MERGE) --- Spawner de Kamikazes (Ejemplo simple)
        spawnTimerEnemigo -= delta;
        if (spawnTimerEnemigo <= 0) {

            float spawnMod = BuffManager.getInstance().getEnemySpawnRateMultiplier();
            float tiempoBase = MathUtils.random(1f, 3f);

            // 1. Resetea el timer para el *próximo* enemigo
            spawnTimerEnemigo = tiempoBase / spawnMod;

            // 2. Prepara las variables de spawn
            Random r = new Random();
            float spawnX = (float) r.nextInt(Gdx.graphics.getWidth());
            float spawnY = (float) Gdx.graphics.getHeight() + 50; // Arriba de la pantalla

            // 3. DECIDE QUÉ ENEMIGO CREAR (Tu "array" de opciones)
            //    Usamos un 'if' para 50/50 de probabilidad.

            if (MathUtils.randomBoolean()) {
                // --- Opción A: Crear un Kamikaze ---
                kamikazes.add(new Kamikaze(
                    spawnX,
                    spawnY,
                    nave,
                    txEnemigo, // <-- Usa la textura pre-cargada
                    MathUtils.random(250f,500f)
                ));
            } else {
                // --- Opción B: Crear un Asteroide ---
                Ball2 bb = new Ball2(
                    spawnX,
                    spawnY,
                    20 + r.nextInt(10),
                    (float)velXAsteroides + r.nextInt((int)MathUtils.random(500f, 10000f)),
                    txAsteroide // <-- Usa la textura pre-cargada
                );
                balls1.add(bb);
                balls2.add(bb); // No olvides añadirlo a ambas listas
            }
        }

        // --- 2. DIBUJADO Y COLISIONES ---
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        // Dibuja el fondo primero
        backgroundOffsetY -= scrollSpeed * delta;
        if (backgroundOffsetY <= -Gdx.graphics.getHeight()) {
            backgroundOffsetY = 0;
        }
        batch.draw(backgroundTexture, 0, backgroundOffsetY, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(backgroundTexture, 0, backgroundOffsetY + Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Dibuja el HUD
        dibujaEncabezado();


        // --- Colisiones de Balas ---
        for (int i = 0; i < balas.size(); i++) {
            Bullet b = balas.get(i);
            if (b.estaDestruido()) continue;

            // Bala vs Asteroide
            for (Ball2 asteroide : balls1) {
                if (!asteroide.estaDestruido() && b.checkCollision(asteroide)) {
                    explosionSound.play();
                    asteroide.recibirHit(1, delta);
                    b.recibirHit(1, delta);
                    agregarScore(10);
                    break; // La bala choca solo una vez
                }
            }
            if (b.estaDestruido()) continue;

            // --- (MERGE) --- Bala vs Kamikaze
            for (Kamikaze kamikaze : kamikazes) {
                if (!kamikaze.estaDestruido() && b.checkCollision(kamikaze)) {
                    explosionSound.play();
                    kamikaze.recibirHit(1, delta);
                    b.recibirHit(1, delta);
                    agregarScore(25);
                    break;
                }
            }
        }

        // --- Colisiones de Nave ---

        // Nave vs Asteroide
        for (Ball2 asteroide : balls1) {
            if (!asteroide.estaDestruido()) {
                // --- (MERGE) --- CORREGIDO: Usando el checkCollision correcto
                if (nave.checkCollision(asteroide, delta)) {
                    asteroide.recibirHit(1, delta);
                }
            }
        }

        // --- (MERGE) --- Nave vs Kamikaze
        for (Kamikaze kamikaze : kamikazes) {
            if (!kamikaze.estaDestruido()) {

                if (nave.checkCollision(kamikaze, delta)) {

                    kamikaze.recibirHit(10, delta);
                }
            }
        }

        // Asteroide vs Asteroide
        for (int i = 0; i < balls1.size(); i++) {
            Ball2 ball1 = balls1.get(i);
            if (ball1.estaDestruido()) continue;
            for (int j = i + 1; j < balls1.size(); j++) {
                Ball2 ball2 = balls1.get(j);
                if (!ball2.estaDestruido()) {
                    ball1.checkCollision(ball2); // (Este método es de Ball2)
                }
            }
        }

        // Nave vs PowerUps
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp p = powerUps.get(i);


            p.update(delta, this);
            p.draw(batch);


            if (p.getHitbox().overlaps(nave.getHitbox())) {


                p.recoger(nave);

            }
        }

        // --- 3. DIBUJADO FINAL ---
        // Dibuja todo (los que estén destruidos no se dibujarán si lo manejas en su 'draw')

        for (Bullet b : balas) { b.draw(batch); }
        for (Ball2 ball : balls1) { ball.draw(batch); }
        for (Kamikaze k : kamikazes) { k.draw(batch); }
        for (PowerUp p : powerUps) { p.draw(batch); }

        nave.draw(batch); // Dibuja la nave al final (encima de todo)

        batch.end();

        // --- 4. LIMPIEZA DE OBJETOS MUERTOS ---
        balas.removeIf(Bullet::estaDestruido);

        ArrayList<Ball2> asteroidesARemover = new ArrayList<>();
        for (Ball2 ball : balls1) {
            if (ball.estaDestruido()) {
                asteroidesARemover.add(ball);
            }
        }
        balls1.removeAll(asteroidesARemover);
        balls2.removeAll(asteroidesARemover);


        for (int i = 0; i < powerUps.size(); i++) {
            if (powerUps.get(i).estaDestruido()) {
                powerUps.remove(i);
                i--;
            }
        }

        // --- (MERGE) ---
        kamikazes.removeIf(Kamikaze::estaDestruido);



        // --- 5. FIN DE JUEGO / SIGUIENTE NIVEL ---

        if (nave.estaDestruido()) {
            if (score > game.getHighScore()) game.setHighScore(score);
            Screen ss = new PantallaGameOver(game);
            ss.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.setScreen(ss);
            dispose();
            return;
        }


        if (balls1.isEmpty() && kamikazes.isEmpty()) {
            Screen ss = new PantallaJuego(game, ronda + 1, (int)nave.getCombustible(), score,
                velXAsteroides + 2, velYAsteroides + 2, cantAsteroides + 10);
            ss.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.setScreen(ss);
            dispose();
        }
    }


    public boolean agregarBala(Bullet bb) {
    	return balas.add(bb);
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

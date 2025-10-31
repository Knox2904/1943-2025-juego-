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


public class PantallaJuego implements Screen {

	private SpaceNavigation game;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Sound explosionSound;
	private Music gameMusic;
	private int score;
	private int ronda;
	private int cantAsteroides;

	private Nave4 nave;
	private  ArrayList<Ball2> balls1 = new ArrayList<>();
	private  ArrayList<Ball2> balls2 = new ArrayList<>();
	private  ArrayList<Bullet> balas = new ArrayList<>();
    private ArrayList<Kamikaze> kamikazes = new ArrayList<>();
    private Texture txEnemigo; // Textura para el kamikaze
    private float spawnTimerKamikaze = 0f; // Timer para el kamikaze
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Texture powerUpTexture;
    private Texture texturaAliado;


	public PantallaJuego(SpaceNavigation game, int ronda, int vidas, int score, int cantAsteroides) {
		this.game = game;
		this.ronda = ronda;
		this.score = score;
		this.cantAsteroides = cantAsteroides;

		batch = game.getBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 640);
		//inicializar assets; musica de fondo y efectos de sonido
		explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.ogg"));
		explosionSound.setVolume(1,0.25f);
		gameMusic = Gdx.audio.newMusic(Gdx.files.internal("piano-loops.wav")); //

		gameMusic.setLooping(true);
		gameMusic.setVolume(0.5f);
		gameMusic.play();

        powerUpTexture = new Texture(Gdx.files.internal("powerUpDobleTiro.png"));
        texturaAliado = new Texture(Gdx.files.internal("MainShip3.png"));
        txEnemigo = new Texture(Gdx.files.internal("MainShip3.png"));

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

        //velocidad de los asteroides definida
        float velocidadA= 150f;

        for (int i = 0; i < cantAsteroides; i++) {

            // 2. Genera las velocidades finales como float, se cambio para que ahora se ocupen con fps, por que su velocidad era erratica, esta es una velocidad especifica para el asteroide, de 150 - 200.
            float velocidadPEI = velocidadA + r.nextInt(50);


            Ball2 bb = new Ball2(
                r.nextInt(Gdx.graphics.getWidth()),
                50 + r.nextInt(Gdx.graphics.getHeight() - 50),
                20 + r.nextInt(10),
                velocidadPEI,
                new Texture(Gdx.files.internal("aGreyMedium4.png"))
            );

            balls1.add(bb);
            balls2.add(bb);
        }

        int cantKamikazes = 5 + (ronda * 2);
        float velKamikaze = 375.0f;

        for (int i = 0; i < cantKamikazes; i++) {

            float spawnX = r.nextInt(Gdx.graphics.getWidth());
            float spawnY = Gdx.graphics.getHeight() + r.nextInt(300) + 30;
            float targetX = r.nextInt(Gdx.graphics.getWidth());
            float targetY = r.nextInt(Gdx.graphics.getHeight() / 2);

            Kamikaze k = new Kamikaze(spawnX, spawnY, targetX, targetY, txEnemigo, velKamikaze);
            kamikazes.add(k);
        }
	}

	public void dibujaEncabezado() {
        String fuelTexto = "FUEL: " + (int)nave.getCombustible();
        game.getFont().draw(batch, fuelTexto, 10, 30);
		game.getFont().getData().setScale(2f);
		game.getFont().draw(batch, fuelTexto, 10, 30);
		game.getFont().draw(batch, "Score:"+this.score, Gdx.graphics.getWidth()-150, 30);
		game.getFont().draw(batch, "HighScore:"+game.getHighScore(), Gdx.graphics.getWidth()/2-100, 30);
	}


    @Override
	public void render(float delta) {


        //se cambio todo para que ahora se actualizaran primero las balas, asteroides, etc.
        //Esto para que haya un orden y que sea mas entendible.
        nave.update(delta, this);

        // Mueve balas y comprueba si salieron de pantalla
        for (Bullet b : balas) {
            b.update(); // (Sigue sin delta, como lo tenías)
            if (b.getX() < -10 || b.getX() > Gdx.graphics.getWidth() + 10 ||
                b.getY() < -10 || b.getY() > Gdx.graphics.getHeight() + 10) {
                b.destruir();
            }
        }

        // Mueve asteroides
        for (Ball2 ball : balls1) {
            ball.update(delta);
        }

        for (PowerUp p : powerUps) {
            p.update(delta, this);
        }

        for (Kamikaze k : kamikazes) {
            k.update(delta);
        }
        //ahora el dibujado, buffer y colisiones.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        dibujaEncabezado();
        if (!nave.estaHerido()) {

            // colisiones entre balas y enemigos.
            for (int i = 0; i < balas.size(); i++) {
                Bullet b = balas.get(i);
                if (b.isDestroyed()) continue;

                //colisiones entre bala y asteroides
                for (Ball2 asteroide : balls1) {
                    if (!asteroide.isDestroyed()) {
                        // Llama al método de colisión original de Bullet
                        if (b.checkCollision(asteroide)) {
                            explosionSound.play();
                            asteroide.destruir(); // Marca el asteroide
                            b.destruir();         // (Asumo que checkCollision(Ball2) hace esto)
                            score += 10;
                            break; // La bala choca solo una vez
                        }
                    }
                }
                if (b.isDestroyed()) continue; // Si chocó, no sigas

                //ahora para revisar si colisiono con kamikaze
                for (Kamikaze kamikaze : kamikazes) {
                    if (!kamikaze.isDestroyed()) {
                        // (Esto asume que añadiste checkCollision(Kamikaze k) a tu clase Bullet)
                        if (b.checkCollision(kamikaze)) {
                            explosionSound.play();
                            kamikaze.destruir(); // Marca el kamikaze
                            b.destruir();        // (Asumo que checkCollision(Kamikaze) hace esto)
                            score += 25;
                            break;
                        }
                    }
                }

            } // Fin del bucle que revisa todo de bullets

            //ahora se comprueba las colisiones de los enemigos vs el jugador, por ahora es solo colisiones, por que despues...
            //se implementaran los enemigos que disparan

            // Nave vs Asteroide
            for (Ball2 asteroide : balls1) {
                if (!asteroide.isDestroyed()) {
                    if (nave.checkCollision(asteroide.getArea())) {
                        asteroide.destruir();
                    }
                }
            }

            // Nave vs Kamikaze
            for (Kamikaze kamikaze : kamikazes) {
                if (!kamikaze.isDestroyed()) {
                    if (nave.checkCollision(kamikaze.getArea())) {
                        kamikaze.destruir();
                    }
                }
            }

            //  Asteroide vs Asteroide, para ver si rebotan entre si.
            for (int i = 0; i < balls1.size(); i++) {
                Ball2 ball1 = balls1.get(i);
                if (ball1.isDestroyed()) continue;
                for (int j = i + 1; j < balls1.size(); j++) {
                    Ball2 ball2 = balls1.get(j);
                    if (!ball2.isDestroyed()) {
                        ball1.checkCollision(ball2);
                    }
                }
            }

            //  Nave vs PowerUps, en verdad es confirmar para que el jugador tome los powerups
            for (int i = 0; i < powerUps.size(); i++) {
                PowerUp p = powerUps.get(i);
                if (p.getHitbox().overlaps(nave.getHitbox())) {
                    aplicarEfectoPowerUp(p.getTipo());
                    powerUps.remove(i);
                    i--;
                }
            }

        }


        nave.draw(batch);

        for (Bullet b : balas) { b.draw(batch); }
        for (Ball2 ball : balls1) { ball.draw(batch); }
        for (Kamikaze k : kamikazes) { k.draw(batch); }
        for (PowerUp p : powerUps) { p.draw(batch); }

        batch.end();
        // Limpia balas
        for (int i = 0; i < balas.size(); i++) {
            if (balas.get(i).isDestroyed()) {
                balas.remove(i);
                i--;
            }
        }

        // Limpia asteroides
        ArrayList<Ball2> asteroidesARemover = new ArrayList<>();
        for (Ball2 ball : balls1) {
            if (ball.isDestroyed()) {
                asteroidesARemover.add(ball);
            }
        }
        balls1.removeAll(asteroidesARemover);
        balls2.removeAll(asteroidesARemover); // Limpia ambas listas


        kamikazes.removeIf(EntidadJuego::isDestroyed);
        // ------------------------------------

        // --- 6. LÓGICA DE FIN DE JUEGO / SIGUIENTE NIVEL ---
        if (nave.estaDestruido()) {
            if (score > game.getHighScore()) game.setHighScore(score);
            Screen ss = new PantallaGameOver(game);
            ss.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            game.setScreen(ss);
            dispose();
            return; // Sal del render
        }

        // --- CAMBIADO: Comprueba AMBAS listas de enemigos ---
        if (balls1.isEmpty() && kamikazes.isEmpty()) {
            Screen ss = new PantallaJuego(game, ronda + 1, nave.getVidas(), score,
                cantAsteroides + 10);
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
	}

    public void crearPowerUpEn(float x, float y, TipoPowerUp tipo) {
        powerUps.add(new PowerUp(x, y, powerUpTexture, tipo));
    }

    private void aplicarEfectoPowerUp(TipoPowerUp tipo) {
        switch (tipo) {
            case MEJORA_ARMA:
                nave.mejorarArma(this);
                // sonidoPowerUpMejorarArma.play(); a crear
                break;
            case COMBUSTIBLE:
                nave.agregarCombustible(40f);
                // sonidoPowerUpAgregarCombustible.play(); a crear
                break;

            case NAVE_ALIADA:
                nave.agregarAliado();
                // sonidoPowerUpAliado.play(); a crear
                break;
        }
    }

    public void agregarScore(int puntos){
        score+=puntos;
    }

}

package puppy.code;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;




public class SpaceNavigation extends Game {
	private String nombreJuego = "Space Navigation";
	private SpriteBatch batch;
	private BitmapFont font;
	private int highScore;

    private Music gameMusic ;



	public void create() {
		highScore = 0;
		batch = new SpriteBatch();
		font = new BitmapFont(); // usa Arial font x defecto
		font.getData().setScale(2f);
		Screen ss = new PantallaMenu(this);
		this.setScreen(ss);
	}

    public void playMusic() {

        if (gameMusic != null && gameMusic.isPlaying()) {
            return;
        }


        String[] listaCanciones = {
            "Slum Lord.mp3",
            "The Gauntlet.mp3",
            "Payday 2 - Dirt & Dust (Border Crossing Heist Track).mp3",
            "Payday 2 - Blastaway (Brooklyn Bank Track).mp3"


        };


        int indice = MathUtils.random(0, listaCanciones.length - 1);

        // Cargar y reproducir
        if (gameMusic != null) gameMusic.dispose(); // Limpia la anterior si existía
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal(listaCanciones[indice]));

        gameMusic.setLooping(true);
        gameMusic.setVolume(0.7f);
        gameMusic.play();
    }


    public void stopMusic() {
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.dispose();
            gameMusic = null;
        }
    }



	public void render() {
		super.render(); // important!
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
        if (gameMusic != null) gameMusic.dispose();
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public BitmapFont getFont() {
		return font;
	}

	public int getHighScore() {
		return highScore;
	}

	public void setHighScore(int highScore) {
		this.highScore = highScore;
	}



}

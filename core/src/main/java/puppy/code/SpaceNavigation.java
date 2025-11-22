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
    private int lastSongIndex = -1;



	public void create() {
		highScore = 0;
		batch = new SpriteBatch();
		font = new BitmapFont(); // usa Arial font x defecto
		font.getData().setScale(2f);
		Screen ss = new PantallaMenu(this);
		this.setScreen(ss);
        playMenuMusic();
	}

    public void playMusic() {

        if (gameMusic != null && gameMusic.isPlaying()) {
            return;
        }
        if (gameMusic != null) gameMusic.dispose();


        String[] listaCanciones = {
            //juego
            "Slum Lord.mp3",
            "The Gauntlet.mp3",
            "Payday 2 - Dirt & Dust (Border Crossing Heist Track).mp3",
            "Payday 2 - Blastaway (Brooklyn Bank Track).mp3",
            "Acid Spit.mp3",
            "Payday 2 Official Soundtrack - #38 Backstab.mp3",
            "Nightsider.mp3",
            "Warp Traveller.mp3",
            "NARC.mp3",
            "Light of the Imperium.mp3"
        };


        int indice;
        do {
            indice = MathUtils.random(0, listaCanciones.length - 1);
        } while (indice == lastSongIndex);

        lastSongIndex = indice;

        // Cargar y reproducir
        if (gameMusic != null) gameMusic.dispose(); // Limpia la anterior si existía

        gameMusic = Gdx.audio.newMusic(Gdx.files.internal(listaCanciones[indice]));

        gameMusic.setLooping(false);
        gameMusic.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                playMusic();
            }
        });

        gameMusic.setVolume(0.7f);
        gameMusic.play();
    }

    public void playMenuMusic() {

        if (gameMusic != null && gameMusic.isPlaying()) {
            return;
        }


        String[] listaCanciones = {
            "Legio Symphonica - Praetores De Sanctus Pella  Warhammer 40K Music.mp3",
            "Warhammer 40,000 Mechanicus Soundtrack - 1. Children of the Omnissiah.mp3"
        };


        int indice;
        indice = MathUtils.random(0, listaCanciones.length - 1);

        lastSongIndex = indice;

        // Cargar y reproducir
        if (gameMusic != null) gameMusic.dispose();

        gameMusic = Gdx.audio.newMusic(Gdx.files.internal(listaCanciones[indice]));

        gameMusic.setLooping(false);
        gameMusic.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                // Al terminar, llama a playMenuMusic() de nuevo (para que siga con música de menú)
                playMenuMusic();
            }
        });

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

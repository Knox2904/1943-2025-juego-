package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;


public class PantallaMenu implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private Texture imagenPortada; // 1. Variable para la imagen

    public PantallaMenu(SpaceNavigation game) {
        this.game = game;

        camera = new OrthographicCamera();
        // CAMBIO: Usamos la misma resolución vertical que el juego (480x640)
        camera.setToOrtho(false, 1200, 800);

        // 2. Cargar la imagen (Asegúrate de tener "portada.png" en tu carpeta assets)
        // Puedes usar la misma del fondo del juego si no tienes una portada específica
        imagenPortada = new Texture(Gdx.files.internal("menu.png"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);

        game.getBatch().begin();

        // 3. DIBUJAR LA IMAGEN (Siempre primero para que quede de fondo)
        // La dibujamos estirada para que cubra toda la pantalla (480x640)
        game.getBatch().draw(imagenPortada, 0, 0, 1200, 800);

        // Dibujar el texto encima
        // Ajusté un poco las coordenadas para la resolución vertical
        game.getFont().getData().setScale(2f); // Hacemos la letra un poco más grande
        game.getFont().draw(game.getBatch(), "Bienvenido a 2025!", 125, 500);

        game.getFont().getData().setScale(1f);
        game.getFont().draw(game.getBatch(), "Presiona cualquier tecla para iniciar ...", 250, 375);

        game.getBatch().end();

        if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            BuffManager.getInstance().resetBuffs();

            // Aquí usamos los 7 parámetros correctos que definimos antes
            Screen ss = new PantallaJuego(game, 1, 100f, 0, 10, 10, 10);
            ss.resize(480, 640);
            game.setScreen(ss);
            dispose();
        }
    }


    @Override
    public void show() { }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        // 4. IMPORTANTE: Limpiar la memoria de la imagen al salir
        imagenPortada.dispose();
    }

}

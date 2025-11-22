package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // Necesario si draw() se quedara
import com.badlogic.gdx.math.Rectangle;

// EntidadJuego hereda TODO de GameObject (spr, position, hitbox, draw)
public abstract class EntidadJuego extends GameObject implements IDestruible {



    // --- VARIABLES PROPIAS DE ENTIDADJUEGO ---
    protected boolean destroyed = false;
    protected float velocidadPEI;
    protected int vidaActual;
    protected boolean enHit = false;
    protected float tiempoHit = 0;

    // Constructor base
    public EntidadJuego(Texture tx, float x, float y, float velocidad, int vidaI) {
        // 1. Llama al padre. Esto crea 'position', 'spr' y 'hitbox'
        //    y también llama a spr.setPosition(x,y)
        super(x, y, tx);


        this.velocidadPEI = velocidad;
        this.vidaActual = vidaI;

    }


    // --- MÉTODOS DE LA INTERFAZ  ---

    @Override
    public void recibirHit(int cantidad, float delta){
        if(estaDestruido())return;

        this.vidaActual -= cantidad;

        this.enHit = true;
        this.tiempoHit = 0.1f;
        this.spr.setColor(1, 0, 0, 1);

        if(this.vidaActual <= 0){
            this.vidaActual = 0;
            this.destruir();
        }
    }

    @Override
    public boolean estaDestruido() {
        return this.destroyed;
    }

    @Override
    public int getVidas(){
        return this.vidaActual;
    }

    public void destruir(){
        this.vidaActual = 0;
        this.destroyed = true;
    }

    @Override
    public abstract void update(float delta, PantallaJuego juego);

    // --- GETTERS (Modificados para usar 'position' del padre) ---

    public float getX() {
        return this.position.x;
    }

    public float getY() {
        return this.position.y;
    }

    public void curar(int cantidad) {
        this.vidaActual += cantidad;
        // Efecto visual verde al curarse
        this.spr.setColor(0, 1, 0, 1);
        this.enHit = true;
        this.tiempoHit = 0.2f;
    }
}

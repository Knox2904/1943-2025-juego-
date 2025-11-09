package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // Necesario si draw() se quedara
import com.badlogic.gdx.math.Rectangle;

// EntidadJuego hereda TODO de GameObject (spr, position, hitbox, draw)
public abstract class EntidadJuego extends GameObject implements IDestruible {

    // --- VARIABLES ELIMINADAS ---
    // protected float x, y; // <-- BORRADO (Ya existe 'position' en GameObject)
    // protected Sprite spr; // <-- BORRADO (¡Esta era la causa del crash!)

    // --- VARIABLES PROPIAS DE ENTIDADJUEGO ---
    protected boolean destroyed = false;
    protected float velocidadPEI;
    protected int vidaActual;

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

    // --- GETTERS (Modificados para usar 'position' del padre) ---

    public float getX() {
        return this.position.x;
    }

    public float getY() {
        return this.position.y;
    }
}

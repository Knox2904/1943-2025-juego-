package puppy.code;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;



public class Nave4 extends GameObject implements IDestruible {


	private boolean destruida = false;
    private int vidas = 3;
    private Sprite spr;
    private Sound sonidoHerido; //cambiar
    private Sound soundBala; //cambiar
    private Texture txBala;
    private boolean herido = false;
    private float tiempoHeridoMax = 0.5f;
    private float tiempoHerido;
    private float stateTime = 0 ;
    private int nivelArma = 0; // 0=base, 1=doble, 2=triple ... hasta le n nivle :D
    private final int MAX_NIVEL_ARMA = 2; // El nivel máximo que queremos (0, 1, 2 ... n) , n = 2 por ahora , testar
    private IFireStrategy fireStrategy;


    public Nave4(float x, float y, Texture tx, Sound soundChoque, Texture txBala, Sound soundBala) {
        super(x ,y,tx) ;
        sonidoHerido = soundChoque;
    	this.soundBala = soundBala;
    	this.txBala = txBala;
    	spr = new Sprite(tx);
    	spr.setPosition(x, y);
    	//spr.setOriginCenter();
    	spr.setBounds(x, y, 45, 45);
        this.fireStrategy = new OffsetFireStrategy(new float[]{ 0f });


    }

    @Override
    public void recibirHit(int cantidad) {
        if (!herido) {
            vidas -= cantidad;
            herido = true;
            tiempoHerido = tiempoHeridoMax;
            sonidoHerido.play();

            nivelArma = 0 ;
            fireStrategy = new OffsetFireStrategy(new float[]{ 0f });


            if (vidas <= 0) {
                destruida = true;
            }
        }
    }

    @Override
    public boolean estaDestruido() {
        return !herido && destruida;
    }

    @Override
    public int getVidas() {
        return this.vidas;
    }


    //-------- logica propia -----------

    @Override
    public void update(float delta, PantallaJuego juego){

        stateTime += delta;


        //--------------movimiento-----------------

        if(!herido){

            final float PLAYER_SPEED = 500f ; // testear para ver la velocidad


            float x = spr.getX();
            float y = spr.getY();


            if(Gdx.input.isKeyPressed(Input.Keys.A)){
                x-=PLAYER_SPEED * delta;
            }

            if(Gdx.input.isKeyPressed(Input.Keys.D)){
                x+=PLAYER_SPEED * delta;
            }

            if(Gdx.input.isKeyPressed(Input.Keys.W)){
                y+=PLAYER_SPEED * delta;
            }

            if(Gdx.input.isKeyPressed(Input.Keys.S)){
                y-=PLAYER_SPEED * delta;
            }


            //---------------limites para el jugador y la pantalla------------


            // izquierdo
            if(x < 0 ) { x = 0 ; }


            //derecho

            float screenWidth = Gdx.graphics.getWidth();
            if(x > screenWidth - spr.getWidth()) {
                x = screenWidth - spr.getWidth() ;
            }

            // inferior
            if(y < 0 ) { y = 0 ; }


            //superior
            float screenHeight = Gdx.graphics.getHeight();
            if(y > screenHeight - spr.getHeight()) {
                y = screenHeight - spr.getHeight() ;
            }

            spr.setPosition(x, y);




        }

        else{

            //invencibilidad para no ser oneshoteado , Iframes

            spr.setX(spr.getX() + MathUtils.random(-2, 2));

            spr.setX(spr.getX());

            tiempoHerido -= delta ;


            if(tiempoHerido <= 0 ){
                herido = false ;
            }



        }

            //------------- disparos ------------

        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            float spawnX = spr.getX() + spr.getWidth() / 2 - 5;
            float spawnY = spr.getY() + spr.getHeight() - 5;

            fireStrategy.fire(juego, txBala, spawnX, spawnY);
            soundBala.play();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            System.out.println("CHEAT: Generando PowerUp de ARMA!");
            juego.crearPowerUpEn(position.x, position.y + 100, TipoPowerUp.MEJORA_ARMA);
        }




    }

    @Override
    public void draw(SpriteBatch batch) {
        if (herido) {


            float flicker = (float) Math.sin(stateTime * 30);

            float alpha = 0.5f + 0.5f * flicker;

            spr.setColor(1, 1, 1, alpha);

        }
        else {

            spr.setColor(1, 1, 1, 1);
        }

        spr.draw(batch);
    }

    public boolean checkCollision(Ball2 b) {
        if(!herido && b.getArea().overlaps(spr.getBoundingRectangle())){

        	//actualizar vidas y herir

            vidas--;
            herido = true;
  		    tiempoHerido=tiempoHeridoMax;
  		    sonidoHerido.play();
            if (vidas<=0)
          	    destruida = true;
            return true;
        }
        return false;
    }

    public boolean estaHerido() {
 	   return herido;
    }

    public void mejorarArma(PantallaJuego juego) {

        nivelArma++;

        switch (nivelArma) {
            case 1: // Nivel 1: Doble
                fireStrategy = new OffsetFireStrategy(new float[]{ -10f, 10f });
                break;
            case 2: // Nivel 2: Triple
                fireStrategy = new OffsetFireStrategy(new float[]{ -15f, 0f, 15f });
                break;
            default: // Maximo alcanzado
                nivelArma = MAX_NIVEL_ARMA;
                juego.agregarScore(100);
                break;
        }
    }





    //public boolean isDestruida() {return destruida;}
    public int getX() {return (int) spr.getX();}
    public int getY() {return (int) spr.getY();}
	public void setVidas(int vidas2) {vidas = vidas2;}

}

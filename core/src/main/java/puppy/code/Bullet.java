package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class Bullet implements IDestruible {

	private int xSpeed;
	private int ySpeed;
	private boolean destroyed = false;
	private Sprite spr;

	    public Bullet(float x, float y, int xSpeed, int ySpeed, Texture tx) {
	    	spr = new Sprite(tx);
	    	spr.setPosition(x, y);
	        this.xSpeed = xSpeed;
	        this.ySpeed = ySpeed;
	    }
	    public void update() {
	        spr.setPosition(spr.getX()+xSpeed, spr.getY()+ySpeed);
	        if (spr.getX() < 0 || spr.getX()+spr.getWidth() > Gdx.graphics.getWidth()) {
	            destroyed = true;
	        }
	        if (spr.getY() < 0 || spr.getY()+spr.getHeight() > Gdx.graphics.getHeight()) {
	        	destroyed = true;
	        }

	    }

	    public void draw(SpriteBatch batch) {
	    	spr.draw(batch);
	    }

	    public boolean checkCollision(Ball2 b2) {
	        if(spr.getBoundingRectangle().overlaps(b2.getHitbox())){
	        	// Se destruyen ambos
	            this.destroyed = true;
	            return true;

	        }
	        return false;
	    }


        public boolean checkCollision(Kamikaze k) {
            if(spr.getBoundingRectangle().overlaps(k.getHitbox())){
                // Se destruyen ambos
                this.destroyed = true;
                return true;
            }
            return false;
        }
        @Override
	    public boolean estaDestruido() {return this.destroyed;}

        @Override
        public void recibirHit(int cantidad, float delta) {
            this.destroyed = true;
        }

        @Override
        public int getVidas() {
            // Operador ternario: (condición ? si_es_verdad : si_es_falso)
            return this.destroyed ? 0 : 1;
        }


        public float getX() {
        return spr.getX();
    }



        public float getY() {
        return spr.getY();
    }
}

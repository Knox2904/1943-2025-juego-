package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;


public class Bullet implements IDestruible {

	private float xSpeed;
	private float ySpeed;
	private boolean destroyed = false;
	private Sprite spr;
    private int damage = 1;

	    public Bullet(float x, float y, float xSpeed, float ySpeed, Texture tx) {
	    	spr = new Sprite(tx);

            spr.setPosition(x, y);

            float anguloRadianes = MathUtils.atan2(ySpeed, xSpeed);
            float anguloGrados = anguloRadianes * MathUtils.radiansToDegrees;
            spr.setRotation(anguloGrados - 90);

            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;

            spr.getBoundingRectangle().setPosition(x, y);


	    }
	    public void update() {
	        spr.setPosition(spr.getX()+xSpeed, spr.getY()+ySpeed);
            if (spr.getX() < -50 || spr.getX() > Config.ANCHO_MUNDO + 50) {
                destroyed = true;
            }
            if (spr.getY() < -50 || spr.getY() > Config.ALTO_MUNDO + 50) {
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

            return this.destroyed ? 0 : 1;
        }


        public float getX() {
        return spr.getX();
    }

        public float getY() {
        return spr.getY();
    }


    // ESTE METODO FUNCIONA PARA CUALQUIER ENEMIGO (Kamikaze, KamikazeS, Tank, etc.)
    public boolean checkCollision(com.badlogic.gdx.math.Rectangle rect) {
        if (spr.getBoundingRectangle().overlaps(rect)) {
            // Se destruye la bala
            this.destroyed = true;
            return true;
        }
        return false;
    }

    public void setRotation(float degrees) {
        this.spr.setRotation(degrees);
    }
    public void setDamage(int damage) {
        this.damage = damage;
    }
    public int getDamage() {
        return this.damage;
    }

}

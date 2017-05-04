package at.walterje.koalio;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

    public float WIDTH;
    public float HEIGHT;
    public float MAX_VELOCITY = 5f;
    //static float JUMP_VELOCITY = 40f;
    static float DAMPING = 0.87f;


    final Vector2 position = new Vector2();
    final Vector2 velocity = new Vector2();
    float stateTime = 0;
    boolean facesRight = true;
    //boolean grounded = false;
    private Rectangle bounds;

    public void setBounds(float width, float height) {
        this.bounds = new Rectangle(position.x - width / 2, position.y - height/2, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void updateBounds(float x, float y) {
        this.bounds.x = x;
        this.bounds.y = y;
    }

    public void updateBounds() {
        this.bounds.x = position.x;
        this.bounds.y = position.y;
    }
}

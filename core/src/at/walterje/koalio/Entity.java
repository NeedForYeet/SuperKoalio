package at.walterje.koalio;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by user on 04.05.17.
 */
public class Entity {

    final Vector2 position = new Vector2();
    final Vector2 velocity = new Vector2();
    boolean facesRight = true;
    private Rectangle bounds;
    float DAMPING = 0.87f;


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

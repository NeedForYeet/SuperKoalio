package at.walterje.koalio;

import com.badlogic.gdx.graphics.Texture;

public class Bullet extends Entity{
    public float WIDTH;
    public float HEIGHT;
    float MAX_VELOCITY = 20f;
    static float DAMPING = 0.87f;

    /*private Texture bulletTexture = new Texture("assets/data/tank_explosion3.png");

    public Texture getTexture() {
        return bulletTexture;
    }*/

}

package at.walterje.koalio;

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
}

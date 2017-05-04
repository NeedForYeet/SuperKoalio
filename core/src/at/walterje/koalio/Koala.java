package at.walterje.koalio;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

/** The player character, has state and state time, */
public class Koala extends Entity{

        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 10f;
        static float JUMP_VELOCITY = 40f;
        static float DAMPING = 0.87f;

        int jumpsRemaining = 2;

        enum State {
            Standing, Walking, Jumping
        }


        State state = State.Walking;
        float stateTime = 0;
        boolean grounded = false;

}

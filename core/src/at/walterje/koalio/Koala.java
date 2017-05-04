package at.walterje.koalio;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

/** The player character, has state and state time, */
public class Koala {

        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 10f;
        static float JUMP_VELOCITY = 40f;
        static float DAMPING = 0.87f;

        int jumpsRemaining = 2;

        enum State {
            Standing, Walking, Jumping
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;
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

package at.walterje.koalio;

public class Enemy extends Entity{

    public float WIDTH;
    public float HEIGHT;
    public float MAX_VELOCITY = 5f;
    static float DAMPING = 0.87f;

    public long lastFireTime = 0;

}

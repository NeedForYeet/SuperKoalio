package at.walterje.koalio;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Super Mario Brothers-like very basic platformer, using a tile map built using <a href="http://www.mapeditor.org/">Tiled</a> and a
 * tileset and sprites by <a href="http://www.vickiwenderlich.com/">Vicky Wenderlich</a></p>
 * <p>
 * Shows simple platformer collision detection as well as on-the-fly map modifications through destructible blocks!
 *
 * @author mzechner
 */
public class SuperKoalio extends ApplicationAdapter {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Koala koala;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();

    private static final float GRAVITY = -2.5f;

    private Animation<TextureRegion> enemyWalk;
    private Enemy enemy1;
    private Enemy enemy2;
    private Enemy enemy3;
    private Enemy enemy4;

    private Enemy initEnemy(int x, int y) {
        TextureAtlas textureAtlas = new TextureAtlas("assets/data/alienBlue.atlas");
        TextureRegion[] textureRegions = new TextureRegion[2];
        textureRegions[0] = textureAtlas.findRegion("alienBlue_walk1");
        textureRegions[1] = textureAtlas.findRegion("alienBlue_walk2");

        enemyWalk = new Animation<TextureRegion>(0.2f, textureRegions);

        Enemy enemy = new Enemy();

        enemy.WIDTH = 1 / 16f * 35;
        enemy.HEIGHT = 1 / 16f * 35;

        enemy.position.set(x, y);
        enemy.stateTime = 0;

        enemy.setBounds(enemy.WIDTH, enemy.HEIGHT);

        return enemy;
    }

    private Koala initKoala() {
        // load the koala frames, split them, and assign them to Animations
        Texture koalaTexture = new Texture("assets/data/koalio.png");
        TextureRegion[] regions = TextureRegion.split(koalaTexture, 18, 26)[0];
        stand = new Animation(0, regions[0]);
        jump = new Animation(0, regions[1]);
        walk = new Animation(0.15f, regions[2], regions[3], regions[4]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        Koala.WIDTH = 1 / 16f * regions[0].getRegionWidth();
        Koala.HEIGHT = 1 / 16f * regions[0].getRegionHeight();

        // create the Koala we want to move around the world
        Koala koala = new Koala();
        koala.position.set(20, 20);
        koala.setBounds(Koala.WIDTH, Koala.HEIGHT);
        return koala;
    }

    @Override
    public void create() {

        enemy1 = initEnemy(30, 5);
        enemy2 = initEnemy(45, 5);
        enemy3 = initEnemy(122, 3);
        enemy4 = initEnemy(175, 3);

        koala = initKoala();

        // load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
        map = new TmxMapLoader().load("assets/data/level2.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();



    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update the koala (process input, collision detection, position update)
        updateKoala(deltaTime);

        // let the camera follow the koala on both axes
        camera.position.x = koala.position.x;
        camera.position.y = koala.position.y;
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();

        // render the koala
        renderKoala(deltaTime);
        checkBelowGround();

        // update and render enemies
        updateEnemy(enemy1, deltaTime);
        updateEnemy(enemy2, deltaTime);
        updateEnemy(enemy3, deltaTime);
        updateEnemy(enemy4, deltaTime);

        renderEnemy(enemy1);
        renderEnemy(enemy2);
        renderEnemy(enemy3);
        renderEnemy(enemy4);
    }

    /**
     * Check if Koala is below the ground.
     */
    private void checkBelowGround() {
        if (koala.position.y < -10) {
            System.out.println("Koala Died");
            create();
        }
    }


    private void updateKoala(float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        koala.stateTime += deltaTime;

        koalaMovement();

        // multiply by delta time so we know how far we go
        // in this frame
        koala.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the koala is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle koalaRect = rectPool.obtain();
        koalaRect.set(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        int startX, startY, endX, endY;
        if (koala.velocity.x > 0) {
            startX = endX = (int) (koala.position.x + Koala.WIDTH + koala.velocity.x);
        } else {
            startX = endX = (int) (koala.position.x + koala.velocity.x);
        }
        startY = (int) (koala.position.y);
        endY = (int) (koala.position.y + Koala.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.x += koala.velocity.x;
        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                koala.velocity.x = 0;
                break;
            }
        }
        koalaRect.x = koala.position.x;

        // if the koala is moving upwards, check the tiles to the top of its
        // top bounding box edge, otherwise check the ones to the bottom
        if (koala.velocity.y > 0) {
            startY = endY = (int) (koala.position.y + Koala.HEIGHT + koala.velocity.y);
        } else {
            startY = endY = (int) (koala.position.y + koala.velocity.y);
        }
        startX = (int) (koala.position.x);
        endX = (int) (koala.position.x + Koala.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.y += koala.velocity.y;
        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                // we actually reset the koala y-position here
                // so it is just below/above the tile we collided with
                // this removes bouncing :)
                if (koala.velocity.y > 0) {
                    koala.position.y = tile.y - Koala.HEIGHT;
                    // we hit a block jumping upwards, let's destroy it!
                    TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("breakable");
                    layer.setCell((int) tile.x, (int) tile.y, null);
                } else {
                    koala.position.y = tile.y + tile.height;
                    // if we hit the ground, mark us as grounded so we can jump
                    koala.grounded = true;
                }
                koala.velocity.y = 0;
                break;
            }
        }
        rectPool.free(koalaRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        koala.position.add(koala.velocity);
        koala.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        koala.velocity.x *= Koala.DAMPING;

        //update its bounds
        koala.updateBounds();
    }

    private void updateEnemy(Enemy enemy, float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        enemy.stateTime += deltaTime;


        // reset the game if koala collides with an enemy
        if (enemy.getBounds().overlaps(koala.getBounds())) {
            create();
        }

        // enemyMovement
        moveEnemy(enemy);

        // multiply by delta time so we know how far we go
        // in this frame
        enemy.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the koala is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle enemyRect = rectPool.obtain();
        enemyRect.set(enemy.position.x, enemy.position.y, enemy.WIDTH, enemy.HEIGHT);
        int startX, startY, endX, endY;
        if (enemy.velocity.x > 0) {
            startX = endX = (int) (enemy.position.x + enemy.WIDTH + enemy.velocity.x);
        } else {
            startX = endX = (int) (enemy.position.x + enemy.velocity.x);
        }
        startY = (int) (enemy.position.y);
        endY = (int) (enemy.position.y + enemy.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        enemyRect.x += enemy.velocity.x;
        for (Rectangle tile : tiles) {
            if (enemyRect.overlaps(tile)) {
                enemy.velocity.x = 0;
                //System.out.println("ENEMY COLLIDED WITH WALL");
                // make enemy face into the other direction
                enemy.facesRight = !enemy.facesRight;
                break;
            }
        }
        enemyRect.x = enemy.position.x;

        // check bounds on the bottom
        startY = endY = (int) (enemy.position.y + enemy.velocity.y);

        startX = (int) (enemy.position.x);
        endX = (int) (enemy.position.x + enemy.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        enemyRect.y += enemy.velocity.y;
        for (Rectangle tile : tiles) {
            if (enemyRect.overlaps(tile)) {
                enemy.position.y = tile.y + tile.height;
                enemy.velocity.y = 0;
                break;
            }
        }
        rectPool.free(enemyRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        enemy.position.add(enemy.velocity);
        enemy.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        enemy.velocity.x *= enemy.DAMPING;

        // update its bounds
        enemy.updateBounds();
    }


    private void koalaMovement() {
        // check input and apply to velocity & state
        // also check for double jump
        if ((Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.SPACE) || isTouched(0.5f, 1)) && koala.jumpsRemaining > 0) {
            koala.velocity.y = Koala.JUMP_VELOCITY;
            koala.state = Koala.State.Jumping;
            koala.grounded = false;
            koala.jumpsRemaining--;
        }

        // reset jumps when on ground
        if (koala.grounded) {
            koala.jumpsRemaining = 2;
        }

        // shift to sprint, ctrl to sneak
        if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
            Koala.MAX_VELOCITY = 30;
        } else if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
            Koala.MAX_VELOCITY = 5;
        } else {
            Koala.MAX_VELOCITY = 10;
        }


        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
            koala.velocity.x = -Koala.MAX_VELOCITY;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = false;
        }

        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
            koala.velocity.x = Koala.MAX_VELOCITY;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = true;
        }

        // apply gravity if we are falling
        koala.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        koala.velocity.x = MathUtils.clamp(koala.velocity.x,
                -Koala.MAX_VELOCITY, Koala.MAX_VELOCITY);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(koala.velocity.x) < 1) {
            koala.velocity.x = 0;
            if (koala.grounded) koala.state = Koala.State.Standing;
        }

        // Reset game if R is pressed
        if (Gdx.input.isKeyJustPressed(Keys.R)) {
            create();
        }
    }

    private void moveEnemy(Enemy enemy) {

        if (enemy.facesRight) {
            enemy.velocity.x = enemy.MAX_VELOCITY;
            enemy.facesRight = true;
        } else {
            enemy.velocity.x = -enemy.MAX_VELOCITY;
            enemy.facesRight = false;
        }

        // apply gravity if we are falling
        enemy.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        enemy.velocity.x = MathUtils.clamp(enemy.velocity.x,
                -enemy.MAX_VELOCITY, enemy.MAX_VELOCITY);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(enemy.velocity.x) < 1) {
            enemy.velocity.x = 0;
        }

    }


    private boolean isTouched(float startX, float endX) {
        // Check for touch inputs between startX and endX
        // startX/endX are given between 0 (left edge of the screen) and 1 (right edge of the screen)
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float) Gdx.graphics.getWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }


    private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layerWalls = (TiledMapTileLayer) map.getLayers().get("breakable");
        TiledMapTileLayer layerUnbreakable = (TiledMapTileLayer) map.getLayers().get("unbreakable");
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Cell cell = layerWalls.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }

                Cell cellUnbreakable = layerUnbreakable.getCell(x, y);
                if (cellUnbreakable != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }

    private void renderKoala(float deltaTime) {
        // based on the koala state, get the animation frame
        TextureRegion frame = null;
        switch (koala.state) {
            case Standing:
                frame = stand.getKeyFrame(koala.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(koala.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(koala.stateTime);
                break;
        }

        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right
        // or left
        Batch batch = renderer.getBatch();
        batch.begin();
        if (koala.facesRight) {
            batch.draw(frame, koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        } else {
            batch.draw(frame, koala.position.x + Koala.WIDTH, koala.position.y, -Koala.WIDTH, Koala.HEIGHT);
        }
        batch.end();
    }

    private void renderEnemy(Enemy enemy) {
        Batch batch = renderer.getBatch();
        enemy.stateTime += Gdx.graphics.getDeltaTime();

        batch.begin();

        if (enemy.facesRight) {
            batch.draw(enemyWalk.getKeyFrame(enemy.stateTime, true), enemy.position.x, enemy.position.y, enemy.WIDTH, enemy.HEIGHT);
        } else {
            batch.draw(enemyWalk.getKeyFrame(enemy.stateTime, true), enemy.position.x + enemy.WIDTH, enemy.position.y, -enemy.WIDTH, enemy.HEIGHT);
        }

        batch.end();
    }

    @Override
    public void dispose() {
    }
}

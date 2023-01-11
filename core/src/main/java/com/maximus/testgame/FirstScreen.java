package com.maximus.testgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.maximus.testgame.billiard.Ball;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class FirstScreen implements Screen {

    Skin skin = new Skin(Gdx.files.internal("commodore_skin/uiskin.json"));
    Stage stage = new Stage(new ScreenViewport());

    ShapeRenderer shapeRenderer = new ShapeRenderer();
    ArrayList<Ball> balls = new ArrayList<>();
    Ball selectedBall = null;

    public FirstScreen() {
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        table.left();

        Label selectBallHint = new Label("Select ball: mouse left click", skin);
        table.add(selectBallHint).row();
        Label spawnBallHint = new Label("Create ball: mouse right click", skin);
        table.add(spawnBallHint).row();

        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        spawnBall();
        selectBall();

        clearScreen();
        drawBalls();
        if (selectedBall != null) drawBallTrajectoryPrediction(selectedBall);

        stage.act();
        stage.draw();
    }

    void spawnBall() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            Ball ball = new Ball();
            ball.position.x = Gdx.input.getX();
            ball.position.y = Gdx.graphics.getHeight() - Gdx.input.getY();
            balls.add(ball);
        }
    }

    void selectBall() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (Ball ball : balls) {
                if (ball.position.dst(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()) < ball.radius) {
                    selectedBall = ball;
                }
            }
        }
    }

    void drawBalls() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Ball ball : balls) {
            shapeRenderer.setColor(Color.WHITE);
            if (ball == selectedBall) shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.circle(ball.position.x, ball.position.y, ball.radius);
        }
        shapeRenderer.end();
    }

    void drawBallTrajectoryPrediction(Ball ballA) {
        Vector2 dir = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

        for (int i = 0; i < 2; i++) {
            ArrayList<Ball> predictBalls = new ArrayList<>();
            for (Ball ballB : balls) {
                if (ballA == ballB) continue;

                Vector2 ball2dir = getBallDir(ballA, ballB, dir);
                if (ball2dir != null) {
                    predictBalls.add(ballB);
                }
            }

            if (predictBalls.size() > 0) {
                Ball b = ballA;
                predictBalls.sort((o1, o2) -> {
                    float dst1 = b.position.dst(o1.position);
                    float dst2 = b.position.dst(o2.position);
                    return Float.compare(dst1, dst2);
                });
                drawBallDir(ballA, predictBalls.get(0), dir);
                dir = getBallDir(ballA, predictBalls.get(0), dir);
                ballA = predictBalls.get(0);
            }
        }
    }

    Vector2 getBallDir(Ball ballA, Ball ballB, Vector2 dir) {
        // Уравнение прямой (Движение первого шара)
        float a = ballA.position.y - dir.y;
        float b = dir.x - ballA.position.x;
        float c = -a * ballA.position.x - b * ballA.position.y;

        // Расстояние от второго шара до этой прямой
        float d = Math.abs(a * ballB.position.x + b * ballB.position.y + c) / (float) Math.sqrt(a * a + b * b);
        // Позиция столкновения двух шаров
        Vector2 circlePos = ballA.position.cpy().add(new Vector2(b, -a).setLength(
                (float) Math.sqrt(ballA.position.dst2(ballB.position) - d * d)
                        - (float) Math.sqrt((ballA.radius + ballB.radius) * (ballA.radius + ballB.radius) - d * d)));

        if (d < ballA.radius + ballB.radius && circlePos.dst(ballB.position) < (ballA.radius + ballB.radius) * 1.2) {
            Vector2 ballBTrajectory = ballB.position.cpy().sub(circlePos).setLength(60);
            return ballB.position.cpy().add(ballBTrajectory);
        }
        return null;
    }

    void drawBallDir(Ball ballA, Ball ballB, Vector2 dir) {
        // Уравнение прямой (Движение первого шара)
        float a = ballA.position.y - dir.y;
        float b = dir.x - ballA.position.x;
        float c = -a * ballA.position.x - b * ballA.position.y;

        // Расстояние от второго шара до этой прямой
        float d = Math.abs(a * ballB.position.x + b * ballB.position.y + c) / (float) Math.sqrt(a * a + b * b);
        // Позиция столкновения двух шаров
        Vector2 circlePos = ballA.position.cpy().add(new Vector2(b, -a).setLength(
                (float) Math.sqrt(ballA.position.dst2(ballB.position) - d * d)
                        - (float) Math.sqrt((ballA.radius + ballB.radius) * (ballA.radius + ballB.radius) - d * d)));

        if (d < ballA.radius + ballB.radius && circlePos.dst(ballB.position) < (ballA.radius + ballB.radius) * 1.2) {
            Vector2 ballBTrajectory = ballB.position.cpy().sub(circlePos).setLength(60);

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.line(ballA.position, circlePos);
            shapeRenderer.circle(circlePos.x, circlePos.y, ballA.radius);

            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.line(ballB.position, ballB.position.cpy().add(ballBTrajectory));
            shapeRenderer.end();
        }
    }

    void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        skin.dispose();
        stage.dispose();
    }

}
/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.geomfx;

import eu.hansolo.fx.geomfx.tools.GradientLookup;
import eu.hansolo.fx.geomfx.tools.PathGradient;
import eu.hansolo.fx.geomfx.tools.ShapeConverter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;


public class Main extends Application {
    private GradientLookup          gradientLookup;
    private Canvas                  canvas;
    private GraphicsContext         ctx;
    private javafx.scene.shape.Path fxPath;
    private Path                    path;


    @Override public void init() {
        canvas = new Canvas(400, 400);
        ctx    = canvas.getGraphicsContext2D();

        fxPath = new javafx.scene.shape.Path();
        fxPath.getElements().add(new MoveTo(91, 36));
        fxPath.getElements().add(new LineTo(182, 124));
        fxPath.getElements().add(new CubicCurveTo(248, 191, 92, 214, 92, 214));
        fxPath.getElements().add(new CubicCurveTo(-26, 248, 200, 323, 200, 323));
        fxPath.getElements().add(new CubicCurveTo(303, 355, 383, 141, 383, 141));

        gradientLookup = new GradientLookup();
        gradientLookup.setStops(new Stop(0.0, Color.BLUE),
                                new Stop(0.25, Color.LIME),
                                new Stop(0.5, Color.YELLOW),
                                new Stop(0.75, Color.ORANGE),
                                new Stop(1.0, Color.RED));

        path = ShapeConverter.convert(fxPath);
        path.setLineWidth(10);
        path.setLineJoin(StrokeLineJoin.ROUND);
        path.setLineCap(StrokeLineCap.ROUND);
    }

    private void initOnFxApplicationThread(final Stage stage) {

        registerListeners();
    }

    private void registerListeners() {

    }

    @Override public void start(final Stage stage) {
        StackPane pane = new StackPane(canvas);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("Title");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();

        draw(ctx);
    }

    @Override public void stop() {
        Platform.exit();
        System.exit(0);
    }

    public void draw(final GraphicsContext ctx) {
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        PathGradient.strokePathWithGradient(ctx, path, gradientLookup);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

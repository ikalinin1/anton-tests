/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.vividus.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

import org.imgscalr.Scalr;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.When;
import org.vividus.testcontext.TestContext;
import org.vividus.util.ResourceUtils;

public class ViewerSteps
{
    private static final String KEY = "viewer";

    private final TestContext testContext;

    public ViewerSteps(TestContext testContext)
    {
        this.testContext = testContext;
    }

    @When("I open image `$path`")
    public void openImage(Path path) throws IOException, URISyntaxException
    {
        closeFrame();
        JFrame viewer = new JFrame();
        testContext.put(KEY, viewer);
        viewer.setExtendedState(JFrame.MAXIMIZED_BOTH);
        viewer.setLocationRelativeTo(null);
        viewer.setUndecorated(true);
        viewer.setLayout(new BorderLayout());

        BufferedImage image = ImageIO.read(path.toFile().exists() ? path.toFile()
                : new File(ResourceUtils.findResource(getClass(), path.toString()).toURI()));
        JComponent pane = new Viewer(image, viewer::getSize);
        pane.setLayout(new BorderLayout());
        viewer.add(pane);
        viewer.setVisible(true);
    }

    @AfterStories
    public void afterStory()
    {
        closeFrame();
    }

    private int calculateCoordinate(int border, int size)
    {
        return border / 2 - size / 2;
    }

    private void closeFrame()
    {
        JFrame viewer = testContext.get(KEY);
        if (viewer != null)
        {
            viewer.dispatchEvent(new WindowEvent(viewer, WindowEvent.WINDOW_CLOSING));
            testContext.remove(KEY);
        }
    }

    private final class Viewer extends JComponent
    {
        private static final long serialVersionUID = 6829157714132186489L;
        private final BufferedImage image;
        private final Supplier<Dimension> sizeProvider;

        private Viewer(BufferedImage image, Supplier<Dimension> sizeProvider)
        {
            this.image = image;
            this.sizeProvider = sizeProvider;
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Dimension viewSize = sizeProvider.get();
            BufferedImage toDraw = Scalr.resize(image, Scalr.Method.SPEED, Scalr.Mode.AUTOMATIC,
                    (int) viewSize.getWidth(), (int) viewSize.getHeight());
            g.drawImage(toDraw, calculateCoordinate((int) viewSize.getWidth(), toDraw.getWidth()),
                    calculateCoordinate((int) viewSize.getHeight(), toDraw.getHeight()), null);
        }
    }
}

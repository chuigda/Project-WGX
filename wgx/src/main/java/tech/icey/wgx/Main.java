package tech.icey.wgx;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import tech.icey.r77.window.Initialiser;
import tech.icey.r77.window.R77Window;

public class Main {
    public static void main(String[] args) {
        Initialiser.initialise();
        R77Window wnd = new R77Window("Zdravstvuyte, mir!", 640, 480);

        wnd.makeCurrent();
        GL.createCapabilities();
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        while (!wnd.requestedToClose()) {
            wnd.beforePaint();
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glBegin(GL11.GL_TRIANGLES);

            GL11.glColor3f(1.0f, 0.0f, 0.0f);
            GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

            GL11.glColor3f(0.0f, 1.0f, 0.0f);
            GL11.glVertex3f(0.5f, -0.5f, 0.0f);

            GL11.glColor3f(0.0f, 0.0f, 1.0f);
            GL11.glVertex3f(0.0f, 0.5f, 0.0f);

            GL11.glEnd();
            wnd.afterPaint();
        }
    }
}

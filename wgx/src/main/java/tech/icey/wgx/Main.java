package tech.icey.wgx;

import org.lwjgl.opengl.GL11;
import tech.icey.r77.window.GLWindow;
import tech.icey.r77.Init;

import javax.swing.*;

final class MyWindow extends GLWindow {
    public MyWindow(String title, int width, int height) {
        super(title, width, height);
    }

    @Override
    public void initialiseGL() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void paintGL() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        rotation += 0.05f;
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(rotation, 0.0f, 0.0f, 1.0f);

        GL11.glBegin(GL11.GL_TRIANGLES);

        GL11.glColor3f(1.0f, 0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

        GL11.glColor3f(0.0f, 1.0f, 0.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glColor3f(0.0f, 0.0f, 1.0f);
        GL11.glVertex3f(0.0f, 0.5f, 0.0f);

        GL11.glEnd();
        GL11.glFinish();
    }

    @Override
    public void resizeGL(int width, int height) {
        GL11.glViewport(0, 0, width, height);
    }

    private float rotation = 0;
}

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Zdravstvuyte, mir!");
        frame.setSize(600, 600);
        JButton button = new JButton("Click to show thread id");
        button.addActionListener(e -> System.err.println("The \"swing event handling\" thred id is: " + Thread.currentThread().getId()));
        frame.getContentPane().add(button);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        System.err.println("The \"main\" thread id is: " + Thread.currentThread().getId());
        Init.initialise();
        try (var window = new MyWindow("Hello World", 600, 600)) {
            while (window.poll()) {}
        }
    }
}

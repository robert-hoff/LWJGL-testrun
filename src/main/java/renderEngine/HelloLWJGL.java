package renderEngine;

import java.nio.IntBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class HelloLWJGL {

  private long window;

  public void run() {
    System.out.printf("Starting LWJGL %s! \n", Version.getVersion());
    init();
    loop();
    cleanup();
  }

  private void init() {
    // Setup an error callback
    GLFWErrorCallback.createPrint(System.err).set();

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    // Configure GLFW
    GLFW.glfwDefaultWindowHints();
    GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

    // Request a multisampled framebuffer: 4x MSAA is a good default
    GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);

    // Create the window
    window = GLFW.glfwCreateWindow(800, 600, "Hello LWJGL", MemoryUtil.NULL, MemoryUtil.NULL);
    if (window == MemoryUtil.NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    // Center the window
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);
      GLFW.glfwGetWindowSize(window, pWidth, pHeight);
      GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
      GLFW.glfwSetWindowPos(
          window,
          (vidmode.width() - pWidth.get(0)) / 2,
          (vidmode.height() - pHeight.get(0)) / 2
          );
    }

    // Make OpenGL context current
    GLFW.glfwMakeContextCurrent(window);
    // Enable v-sync
    GLFW.glfwSwapInterval(1);
    // Make window visible
    GLFW.glfwShowWindow(window);
    // initialize OpenGL bindings

    // which binds OpenGL to the current context defined by glfwMakeContextCurrent
    GL.createCapabilities();
    // Enable multisampling (must be after context creation)
    GL11.glEnable(GL13.GL_MULTISAMPLE);

    // GL11.glClearColor(0.0f, 0.5f, 1.0f, 0.0f);
    // GL11.glClearColor(0,0,0,0); // black
    GL11.glClearColor(1,1,1,1); // white

    // backface culling enabled
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);

    // after creating the context and calling GL.createCapabilities()
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer w = stack.mallocInt(1);
      IntBuffer h = stack.mallocInt(1);
      GLFW.glfwGetFramebufferSize(window, w, h); // use *framebuffer* size (accounts for HiDPI)
      GL11.glViewport(0, 0, w.get(0), h.get(0));
    }

    // update glViewport when window is resized
    GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
      GL11.glViewport(0, 0, width, height);
    });

    // do a render pass also when the screen resizes
    GLFW.glfwSetWindowRefreshCallback(window, win -> {
      drawScene();
    });
  }

  private static final boolean SHOW_TIMESTAMP_EACH_DRAW = false;

  private void loop() {
    while (!GLFW.glfwWindowShouldClose(window)) {
      drawScene();
      // process window events, must be called every frame
      GLFW.glfwPollEvents();
    }
  }

  private void drawScene() {
    if (SHOW_TIMESTAMP_EACH_DRAW) {
      String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS"));
      System.out.println(ts);
    }
    // clear framebuffer
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    // Draw a simple triangle
    GL11.glBegin(GL11.GL_TRIANGLES);
    GL11.glColor3f(1.0f, 0.0f, 0.0f); // Red
    GL11.glVertex2f(-0.5f, -0.5f);
    GL11.glColor3f(0.0f, 1.0f, 0.0f); // Green
    GL11.glVertex2f(0.5f, -0.5f);
    GL11.glColor3f(0.0f, 0.0f, 1.0f); // Blue
    GL11.glVertex2f(0.0f, 0.5f);
    GL11.glEnd();
    GLFW.glfwSwapBuffers(window);
  }

  private void cleanup() {
    Callbacks.glfwFreeCallbacks(window);
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
    GLFW.glfwSetErrorCallback(null).free();
  }

  public static void main(String[] args) {
    new HelloLWJGL().run();
  }
}




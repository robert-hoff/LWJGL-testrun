package renderEngine;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;


public class HelloLWJGL {

  // the long var for the window substitutes as a pointer
  private long window;
  private int winWidth = 800;
  private int winHeight = 600;

  public void run() {
    System.out.printf("Starting LWJGL %s! \n", Version.getVersion());
    init();
    loop();
    cleanup();
  }


  private double lastPressTime;
  private double pressX, pressY;

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
    window = GLFW.glfwCreateWindow(winWidth, winHeight, "Hello LWJGL", MemoryUtil.NULL, MemoryUtil.NULL);
    if (window == MemoryUtil.NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }


    GLFW.glfwSetWindowPos(window, 300, 50);


    // Make OpenGL context current
    GLFW.glfwMakeContextCurrent(window);
    // Enable v-sync
    GLFW.glfwSwapInterval(1);
    // Make window visible
    GLFW.glfwShowWindow(window);
    // initialize OpenGL bindings





    // Esc, and other keypresses
    GLFW.glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
      if (action == GLFW.GLFW_PRESS) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
          GLFW.glfwSetWindowShouldClose(win, true); // close on ESC
        }
      }
    });


    // Mouse presses
    GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
      if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
        if (action == GLFW.GLFW_PRESS) {
          lastPressTime = GLFW.glfwGetTime();
          DoubleBuffer xb = BufferUtils.createDoubleBuffer(1);
          DoubleBuffer yb = BufferUtils.createDoubleBuffer(1);
          GLFW.glfwGetCursorPos(win, xb, yb);
          pressX = xb.get(0);
          pressY = yb.get(0);
        } else if (action == GLFW.GLFW_RELEASE) {
          DoubleBuffer xb = BufferUtils.createDoubleBuffer(1);
          DoubleBuffer yb = BufferUtils.createDoubleBuffer(1);
          GLFW.glfwGetCursorPos(win, xb, yb);
          double releaseX = xb.get(0);
          double releaseY = yb.get(0);
          double dt = GLFW.glfwGetTime() - lastPressTime;
          double dx = releaseX - pressX;
          double dy = releaseY - pressY;
          if (dt < 0.5 && Math.hypot(dx, dy) < 5.0) {
            System.out.println("Click!");
          }
        }
      }
    });






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
      this.winWidth = width;
      this.winHeight = height;
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

    drawTextMessage("(1,100)", winWidth, winHeight);
    GLFW.glfwSwapBuffers(window);
  }

  void drawTextMessage(String text, int winW, int winH) {
    // 1) Switch to 2D (orthographic) and prep state
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    // GL11.glOrtho(0, winW, winH, 0, -1, 1);  // <— notice winH, 0
    GL11.glOrtho(0, winW, 0, winH, -1, 1);

    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    // float textScale = 2.0f;
    // GL11.glScalef(textScale, textScale, 1f);
    GL11.glTranslatef(0f, winH, 0f);
    GL11.glScalef(1f, -1f, 1f);

    float textScale = 1.2f;
    GL11.glScalef(textScale, textScale, 1f);


    GL11.glDisable(GL11.GL_DEPTH_TEST);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_CULL_FACE);

    // 2) Generate quads for the text
    // Each character needs up to ~270 bytes; allocate once and reuse if you like.
    ByteBuffer buffer = BufferUtils.createByteBuffer(16 * 270);
    float x = 8f;
    // float y = winH - 18f; // 8px margin from bottom edge (origin is bottom-left in this ortho)
    float y = 8f; // 8px margin from bottom edge (origin is bottom-left in this ortho)
    int numQuads = STBEasyFont.stb_easy_font_print(x, y, text, null, buffer);

    // 3) Draw as GL_QUADS (compat profile)
    // Vertex format is 2D floats packed by stb_easy_font. Each quad = 4 vertices.
    // GL11.glColor3f(1f, 1f, 1f); // white text
    GL11.glColor3f(0,0,0); // black text
    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
    GL11.glVertexPointer(2, GL11.GL_FLOAT, 16, buffer); // stride 16 bytes (x,y,z? easy font packs per-vertex stride=16)
    GL11.glDrawArrays(GL11.GL_QUADS, 0, numQuads * 4);
    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

    // 4) Restore state
    GL11.glEnable(GL11.GL_DEPTH_TEST);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPopMatrix();
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPopMatrix();
  }

  private void cleanup() {

    int[] dim = getWindowDimensions();
    System.out.printf("(%d,%d) \n", dim[0], dim[1]);

    Callbacks.glfwFreeCallbacks(window);
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
    GLFW.glfwSetErrorCallback(null).free();
  }




  @SuppressWarnings("unused")
  private int[] getWindowDimensions() {
    // this is a 'try-with-resources' block
    // requires the resource to implement AutoCloseble
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);
      GLFW.glfwGetWindowSize(window, pWidth, pHeight);
      return new int[] {pWidth.get(0), pHeight.get(0)};
    }
  }


  @SuppressWarnings("unused")
  private static void showSwingPanel() {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Swing Control Panel");
      frame.setSize(300, 200);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    });
  }

  public static void main(String[] args) {
    // creates a Swing panel that exists alongside the GLWF window
    // showSwingPanel();
    new HelloLWJGL().run();
  }
}


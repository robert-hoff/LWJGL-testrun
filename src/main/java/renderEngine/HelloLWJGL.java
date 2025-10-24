package renderEngine;

import java.nio.ByteBuffer;
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
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import graphics.Camera;
import input.CursorEvent;
import input.InputSystem;
import input.KeyEvent;
import input.MouseButtonEvent;
import input.ScrollEvent;
import state.AppState;
import state.GameState;
import state.Scene;


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


  //  private double lastPressTime;
  //  private double pressX, pressY;
  
  private Camera camera = new Camera();
  private Scene scene = new Scene();
  private InputSystem input = new InputSystem();
  private AppState state = new GameState(camera, scene);
  

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


    // GLFW callbacks just enqueue
    GLFW.glfwSetCursorPosCallback(window, (w, x, y) ->
    input.enqueue(new CursorEvent(w, GLFW.glfwGetTime(), x, y)));

    GLFW.glfwSetMouseButtonCallback(window, (w, button, action, mods) ->
    input.enqueue(new MouseButtonEvent(w, GLFW.glfwGetTime(), button, action, mods)));

    GLFW.glfwSetScrollCallback(window, (w, dx, dy) ->
    input.enqueue(new ScrollEvent(w, GLFW.glfwGetTime(), dx, dy)));

    GLFW.glfwSetKeyCallback(window, (w, key, sc, action, mods) ->
    input.enqueue(new KeyEvent(w, GLFW.glfwGetTime(), key, sc, action, mods)));

    lastTime = GLFW.glfwGetTime();
    

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
  final int TARGET_FPS = 10;
  final double FRAME_TIME = 1.0 / TARGET_FPS;

  private void loop() {
    while (!GLFW.glfwWindowShouldClose(window)) {
      if (state.shutDown()) {
        GLFW.glfwSetWindowShouldClose(window, true);
      }
      
      // process window events, must be called every frame
      GLFW.glfwPollEvents();
      double dt = deltaTime();
      // System.out.printf("dt=%5.3f \n", dt);
      input.update(dt, state::onAction);
      drawScene();
      GLFW.glfwSwapBuffers(window);
      
      // limit the frame rate
      double elapsed = GLFW.glfwGetTime() - lastTime;
      double sleepTime = FRAME_TIME - elapsed;
      if (sleepTime > 0) {
        try {
          Thread.sleep((long) (sleepTime * 1000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }
  }
  
  private double lastTime = 0;
  
  double deltaTime()
  {
    double currentTime = GLFW.glfwGetTime();
    double dt = currentTime - lastTime;
    lastTime = currentTime;
    return dt;
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
    Callbacks.glfwFreeCallbacks(window);
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
    GLFW.glfwSetErrorCallback(null).free();
  }

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


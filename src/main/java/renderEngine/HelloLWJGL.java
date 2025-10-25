package renderEngine;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import graphics.Camera;
import graphics.Mesh;
import graphics.OrbitCamera;
import graphics.Shader;
import input.CursorEvent;
import input.InputSystem;
import input.KeyEvent;
import input.MouseButtonEvent;
import input.ScrollEvent;
import model_loader.BlueprintLoader;
import model_loader.MyFile;
import model_loader.SubBlueprint;
import state.GameState;
import state.Scene;


public class HelloLWJGL {

  // the long var for the window substitutes as a pointer
  private long window;
  private int winWidth = 800;
  private int winHeight = 600;

  public void run() throws Exception {
    System.out.printf("Starting LWJGL %s! \n", Version.getVersion());
    init();
    loop();
    cleanup();
  }


  //  private double lastPressTime;
  //  private double pressX, pressY;

  // private OrbitCamera camera = new OrbitCamera();
  // private Scene scene = new Scene();
  private InputSystem input = new InputSystem();
  private GameState gameState;


  // Mesh mesh = Mesh.create(modelFloatArray);
  Mesh mesh;
  Shader shader;

  private void init() throws Exception {


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

    gameState = new GameState(window);
    int[] winD = gameState.getWin();
    GLFW.glfwSetWindowPos(window, winD[0], winD[1]);
    GLFW.glfwSetWindowSize(window, winD[2], winD[3]);


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

    // Set callback to detect movement
    GLFW.glfwSetWindowPosCallback(window, new GLFWWindowPosCallback() {
      @Override
      public void invoke(long window, int xpos, int ypos) {
        gameState.winXPos = xpos;
        gameState.winYPos = ypos;
      }
    });

    lastTime = GLFW.glfwGetTime();


    // ** GL CONTEXT **
    // which binds OpenGL to the current context defined by glfwMakeContextCurrent
    GL.createCapabilities();


    // LOAD MODEL
    // List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\89_Beaver.txt"));
    // List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\64_Sparrow.txt"));
    // List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\55_Butterfly.txt"));
    List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\43_BananaTree.txt"));

    mesh = Mesh.create(bps.get(1).getFullModelData());

    String vertexSource = Files.readString(Paths.get(
        Shader.class.getResource("/glsl/mesh.vert").toURI()));
    String fragmentSource = Files.readString(Paths.get(
        Shader.class.getResource("/glsl/mesh.frag").toURI()));
    shader = new Shader(vertexSource, fragmentSource);


    // Enable multisampling (must be after context creation)
    GL11.glEnable(GL13.GL_MULTISAMPLE);

    // GL11.glClearColor(0.0f, 0.5f, 1.0f, 0.0f);
    GL11.glClearColor(0,0,0,0); // black
    // GL11.glClearColor(1,1,1,1); // white

    // backface culling enabled
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);
    GL11.glEnable(GL11.GL_DEPTH_TEST);

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
    //    GLFW.glfwSetWindowRefreshCallback(window, win -> {
    //      drawScene();
    //    });
  }


  private static final boolean SHOW_TIMESTAMP_EACH_DRAW = false;
  final int TARGET_FPS = 10;
  final double FRAME_TIME = 1.0 / TARGET_FPS;

  private void loop() {
    while (!GLFW.glfwWindowShouldClose(window)) {
      if (gameState.shutDown()) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
          IntBuffer xpos = stack.mallocInt(1);
          IntBuffer ypos = stack.mallocInt(1);
          IntBuffer width = stack.mallocInt(1);
          IntBuffer height = stack.mallocInt(1);
          GLFW.glfwGetWindowPos(window, xpos, ypos);
          GLFW.glfwGetWindowSize(window, width, height);
          gameState.saveState(xpos.get(0), ypos.get(0), width.get(0), height.get(0));
        }
        GLFW.glfwSetWindowShouldClose(window, true);
      }

      // process window events, must be called every frame
      GLFW.glfwPollEvents();
      GL11.glClearColor(1, 1, 1, 1);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
      // GL11.glDisable(GL11.GL_CULL_FACE);
      // GL11.glFrontFace(GL11.GL_CCW);

      double dt = deltaTime();
      // System.out.printf("dt=%5.3f \n", dt);
      input.update(dt, gameState::onAction);

      // drawScene();
      // each frame:
      shader.bind();
      Matrix4f viewProjMatrix = gameState.camera.getViewProj(gameState.winWidth, gameState.winHeight);
      // Matrix4f modelMatrix = new Matrix4f();

      shader.set("uViewProj", viewProjMatrix);
      shader.set("uModel", modelMatrix());
      // float timeSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0f;
      shader.set("uTime", 0.0f);
      mesh.draw();
      shader.unbind();
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


  Matrix4f viewProjMatrix() {
    Vector3f cameraPos = new Vector3f(0, 2, 5);
    Vector3f cameraTarget = new Vector3f(0, 0, 0);
    Vector3f up = new Vector3f(0, 1, 0);
    Matrix4f viewMatrix = new Matrix4f().lookAt(cameraPos, cameraTarget, up);
    Matrix4f projMatrix = new Matrix4f().perspective(
        (float) Math.toRadians(60.0f),  // FOV
        // (float)windowWidth / windowHeight,
        (float) 6.0 / 4,
        0.1f, 100.0f);                 // near/far planes
    Matrix4f viewProjMatrix = new Matrix4f(projMatrix).mul(viewMatrix);
    return viewProjMatrix;
  }

  Matrix4f modelMatrix() {
    Vector3f position = new Vector3f(0, 0, 0);
    Vector3f rotation = new Vector3f(0, 0, 0); // radians
    float scale = 1.0f;
    Matrix4f modelMatrix = new Matrix4f()
        .translation(position)
        .rotateXYZ(rotation.x, rotation.y, rotation.z)
        .scale(scale);
    return modelMatrix;
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

    mesh.dispose();
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


  public static void main(String[] args) throws Exception {
    // creates a Swing panel that exists alongside the GLWF window
    // showSwingPanel();
    new HelloLWJGL().run();
  }
}


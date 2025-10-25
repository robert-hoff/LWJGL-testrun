package renderEngine;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
import graphics.Axes;
import graphics.Mesh;
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


public class HelloLWJGL {

  private long glfwWindow;

  public void run() throws Exception {
    System.out.printf("Starting LWJGL %s! \n", Version.getVersion());
    init();
    loop();
    cleanup();
  }

  private double startTime;
  private InputSystem input = new InputSystem();
  private GameState gameState;

  Mesh mesh;
  Axes axes;
  Shader shaderMesh;
  Shader shaderAxes;

  private void init() throws Exception {

    // error callback
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

    // Create a size (100,100) window, resized by gameState
    glfwWindow = GLFW.glfwCreateWindow(100, 100, "Hello LWJGL", MemoryUtil.NULL, MemoryUtil.NULL);
    if (glfwWindow == MemoryUtil.NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    gameState = new GameState(glfwWindow);
    int[] winD = gameState.getWin();
    GLFW.glfwSetWindowPos(glfwWindow, winD[0], winD[1]);
    GLFW.glfwSetWindowSize(glfwWindow, winD[2], winD[3]);


    // Make OpenGL context current
    GLFW.glfwMakeContextCurrent(glfwWindow);
    // Enable v-sync
    GLFW.glfwSwapInterval(1);
    // Make window visible
    GLFW.glfwShowWindow(glfwWindow);
    // initialize OpenGL bindings


    // GLFW callbacks just enqueue
    GLFW.glfwSetCursorPosCallback(glfwWindow, (w, x, y) ->
    input.enqueue(new CursorEvent(w, GLFW.glfwGetTime(), x, y)));

    GLFW.glfwSetMouseButtonCallback(glfwWindow, (w, button, action, mods) ->
    input.enqueue(new MouseButtonEvent(w, GLFW.glfwGetTime(), button, action, mods)));

    GLFW.glfwSetScrollCallback(glfwWindow, (w, dx, dy) ->
    input.enqueue(new ScrollEvent(w, GLFW.glfwGetTime(), dx, dy)));

    GLFW.glfwSetKeyCallback(glfwWindow, (w, key, sc, action, mods) ->
    input.enqueue(new KeyEvent(w, GLFW.glfwGetTime(), key, sc, action, mods)));

    // Set callback to detect movement
    GLFW.glfwSetWindowPosCallback(glfwWindow, new GLFWWindowPosCallback() {
      @Override
      public void invoke(long window, int xpos, int ypos) {
        gameState.winXPos = xpos;
        gameState.winYPos = ypos;
      }
    });

    startTime = GLFW.glfwGetTime();
    lastTime = GLFW.glfwGetTime();


    // ** GL CONTEXT **
    // -> which binds OpenGL to the current context defined by glfwMakeContextCurrent
    GL.createCapabilities();


    // LOAD MODEL
    // List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\89_Beaver.txt"));
    // List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\64_Sparrow.txt"));
    // List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\55_Butterfly.txt"));
    List<SubBlueprint> bps = BlueprintLoader.loadBlueprint(new MyFile("\\blueprints\\43_BananaTree.txt"));

    mesh = Mesh.create(bps.get(1).getFullModelData());
    axes = Axes.create();

    String vShaderMesh = Files.readString(Paths.get(
        Shader.class.getResource("/glsl/mesh.vert").toURI()));
    String fShaderMesh = Files.readString(Paths.get(
        Shader.class.getResource("/glsl/mesh.frag").toURI()));
    String vShaderAxes = Files.readString(Paths.get(
        Shader.class.getResource("/glsl/axes.vert").toURI()));
    String fShaderAxes = Files.readString(Paths.get(
        Shader.class.getResource("/glsl/axes.frag").toURI()));

    shaderMesh = new Shader(vShaderMesh, fShaderMesh);
    shaderAxes = new Shader(vShaderAxes, fShaderAxes);


    // Enable multisampling (must be after context creation)
    GL11.glEnable(GL13.GL_MULTISAMPLE);

    // backface culling enabled
    GL11.glEnable(GL11.GL_CULL_FACE);
    GL11.glCullFace(GL11.GL_BACK);
    GL11.glEnable(GL11.GL_DEPTH_TEST);

    // after creating the context and calling GL.createCapabilities()
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer w = stack.mallocInt(1);
      IntBuffer h = stack.mallocInt(1);
      GLFW.glfwGetFramebufferSize(glfwWindow, w, h); // use *framebuffer* size (accounts for HiDPI)
      GL11.glViewport(0, 0, w.get(0), h.get(0));
    }

    // update glViewport when window is resized
    GLFW.glfwSetFramebufferSizeCallback(glfwWindow, (win, width, height) -> {
      GL11.glViewport(0, 0, width, height);
      gameState.updateViewport(width, height);
    });

    // do a render pass also when the screen resizes
    // -> keyboard and mouse events are by default paused
    // -> additional renders are recommended from here
    GLFW.glfwSetWindowRefreshCallback(glfwWindow, win -> {
      drawScene();
    });
  }

  final int TARGET_FPS = 60;
  final double FRAME_TIME = 1.0 / TARGET_FPS;

  private void loop() {
    while (!GLFW.glfwWindowShouldClose(glfwWindow)) {
      if (gameState.shutDown()) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
          IntBuffer xpos = stack.mallocInt(1);
          IntBuffer ypos = stack.mallocInt(1);
          IntBuffer width = stack.mallocInt(1);
          IntBuffer height = stack.mallocInt(1);
          GLFW.glfwGetWindowPos(glfwWindow, xpos, ypos);
          GLFW.glfwGetWindowSize(glfwWindow, width, height);
          gameState.saveState(xpos.get(0), ypos.get(0), width.get(0), height.get(0));
        }
        GLFW.glfwSetWindowShouldClose(glfwWindow, true);
      }

      // process mouse and keyboard events every frame
      GLFW.glfwPollEvents();

      // *draw scene*
      drawScene();

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

  Matrix4f modelMatrix(float x, float y, float z) {
    Vector3f position = new Vector3f(x, y, z);
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
    GL11.glClearColor(1, 1, 1, 1);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    // GL11.glDisable(GL11.GL_CULL_FACE);
    // GL11.glFrontFace(GL11.GL_CCW);

    double dt = deltaTime();
    // System.out.printf("dt=%5.3f \n", dt);
    input.update(dt, gameState::onAction);

    Matrix4f viewProjMatrix = gameState.camera.viewProjMat();

    shaderMesh.bind();
    shaderMesh.set("uViewProj", viewProjMatrix);
    shaderMesh.set("uModel", modelMatrix(2,0,2));
    float timeSeconds = (float) ((System.nanoTime() - startTime) / 1_000_000_000.0f);
    // shaderMesh.set("uTime", timeSeconds);
    mesh.draw();
    shaderMesh.unbind();

    if (gameState.showAxis) {
      shaderAxes.bind();
      shaderAxes.set("uLen", 3.0f); // scale the axis
      Matrix4f mvp = new Matrix4f(viewProjMatrix).mul(new Matrix4f().identity());
      shaderAxes.set("uMVP", mvp);
      axes.draw();
      shaderAxes.unbind();
    }

    // drawTextMessage("(1,100)", winWidth, winHeight);
    GLFW.glfwSwapBuffers(glfwWindow);
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
    Callbacks.glfwFreeCallbacks(glfwWindow);
    GLFW.glfwDestroyWindow(glfwWindow);
    GLFW.glfwTerminate();
    GLFW.glfwSetErrorCallback(null).free();
    mesh.dispose();
  }

  public static void main(String[] args) throws Exception {
    // creates a Swing panel that exists alongside the GLWF window
    // showSwingPanel();
    new HelloLWJGL().run();
  }
}


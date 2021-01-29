package game;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import animation.Animation;
import animation.Animator;
import animation.MaskedAnimation;
import engine.Keyboard;
import engine.Mouse;
import engine.Renderer;
import engine.Window;
import entities.Camera;
import entities.GameItem;
import graphics.AnimatedModel;
import loaders.ModelLoader;
import shaders.ShaderProgram;
import utilities.Timer;
import utilities.TransformationMatrix;

public class Main {
	public static void main(String[] args) {
		Window window = new Window(1600, 900, "OpenGL Skeletal Animation | 3.0", true);
		Camera camera = new Camera(new Vector3f(0, 5, 10));
		Renderer renderer = new Renderer(window);
		Timer timer = new Timer();
		window.init();
		ShaderProgram shaderProgram = new ShaderProgram();
		
		
		AnimatedModel model = null;
		try {
			model = ModelLoader.loadGameItem("src/res/character_2/model/character_2.dae", "src/res/character_2/animations", "src/res/character_2/textures");
		} catch (Exception e) {
			e.printStackTrace();
		}List<GameItem> gameItems = new ArrayList<GameItem>();
		GameItem gameItem = new GameItem(model);
		gameItem.getModel().getCurrentAnimation().playAnimation();
		gameItems.add(gameItem);
		
		Animator animator = new Animator(gameItems);
		Mouse mouse = new Mouse(gameItems);
		Keyboard keyboard = new Keyboard(gameItems);
		mouse.init(window);
		keyboard.init(window);
		
		

		Matrix4f view = TransformationMatrix.createViewMatrix(camera);
		Matrix4f projection = TransformationMatrix.createProjectionMatrix(45.0f, window.getWidth()/window.getHeight(), 0.1f, 1500f);
		
		shaderProgram.use();
		shaderProgram.loadMatrix("view", view);
		shaderProgram.loadMatrix("projection", projection);
		
		float rotY = 0.0f;
		while(!window.windowShouldClose()) {
			renderer.init();
			
			animator.update(timer.getElapsedTime());
			
			rotY += timer.getTime();
			Matrix4f modelMat = TransformationMatrix.createModelMatrix(new Vector3f(0, 0, 0), 0, rotY / 1000000, 0, 1.0f);
			shaderProgram.loadMatrix("model", modelMat);
			
			renderer.render(animator, shaderProgram, gameItem);
			
			renderer.update();
		}
		
		window.close();
	}
}

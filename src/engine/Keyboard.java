package engine;

import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

import entities.GameItem;

public class Keyboard {
	private List<GameItem> gameItems;
	
	public Keyboard(List<GameItem> gameItems) {
		this.gameItems = gameItems;
	}
	
	public void init(Window window) {
		glfwSetKeyCallback(window.getWindowHandle(), (windowHandle, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(windowHandle, true);
			}
			if(key == GLFW_KEY_W && action == GLFW_PRESS) {
				gameItems.get(0).getModel().transitionToAnimation(3, 0.5f);
			}
			if(key == GLFW_KEY_W && action == GLFW_RELEASE) {
				gameItems.get(0).getModel().transitionToAnimation(4, 0.5f);
			}
		});
	}
}

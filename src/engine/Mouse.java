package engine;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

import entities.GameItem;

public class Mouse {
	private List<GameItem> gameItems;
	
	public Mouse(List<GameItem> gameItems) {
		this.gameItems = gameItems;
	}
	
	public void init(Window window) {
		glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
			if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
				List<String> maskBoneNames = new ArrayList<String>();
				maskBoneNames.add("Armature_Chest");
				gameItems.get(0).getModel().transitionToMaskedAnimation(1, maskBoneNames, false, 0.5f);
			}
		});
	}
}

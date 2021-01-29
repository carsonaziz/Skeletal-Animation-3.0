package entities;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import animation.Animation;
import animation.Bone;
import animation.MaskedAnimation;
import animation.Node;
import graphics.Mesh;
import graphics.AnimatedModel;

public class GameItem {
	private static int numGameItems = 0;
	
	private final int id;
	
	private final AnimatedModel model;
	
	private Vector3f position;
	private Vector3f rotation;
	private float scale;
	
	public GameItem(AnimatedModel model) {
		this.id = numGameItems++;
		this.model = model;
		this.position = new Vector3f(0, 0, 0);
		this.rotation = new Vector3f(0, 0, 0);
		this.scale = 1;
	}
	
	public int getID() {
		return id;
	}
	
	public AnimatedModel getModel() {
		return model;
	}
}

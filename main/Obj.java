package main;


public class Obj {
	private int id;
	private int poids;
	private int utils;
	private float ratio;

	Obj(int i, int pi, int ui) {
		id = i;
		poids = pi;
		utils = ui;
		ratio = utils / (float) poids;
	}

	public int getId() {
		return id;
	}

	public int getPoids() {
		return poids;
	}

	public int getUtils() {
		return utils;
	}

	public int getInvertUtils() {
		return -utils;
	}

	public float getRatio() {
		return ratio;
	}

	public float getInvertRatio() {
		return -ratio;
	}

	@Override
	public String toString() {
		return "Objet " + id + " { Poids : " + poids + " | utils : " + utils + " | ratio : " + ratio + " }";
	}

}

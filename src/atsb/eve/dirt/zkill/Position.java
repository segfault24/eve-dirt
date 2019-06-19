package atsb.eve.dirt.zkill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Position {

	@SerializedName("x")
	@Expose
	private double x;
	@SerializedName("y")
	@Expose
	private double y;
	@SerializedName("z")
	@Expose
	private double z;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

}
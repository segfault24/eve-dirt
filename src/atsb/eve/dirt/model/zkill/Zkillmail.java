package atsb.eve.dirt.model.zkill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Zkillmail {

	@SerializedName("package")
	@Expose
	private Package _package;

	public Package getPackage() {
		return _package;
	}

	public void setPackage(Package _package) {
		this._package = _package;
	}

}

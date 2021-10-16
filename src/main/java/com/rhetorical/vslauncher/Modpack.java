package com.rhetorical.vslauncher;

public class Modpack {

	public Modpack(String json) {
		packJson = json;
	}

	// The pack ID for identification purposes
	public String packId;
	// A direct link to json that identifies updates for this pack
	public String packJson;
	// The version of this pack
	public int packVersion;
	// The author of this pack
	public String author;
	// The direct download link to the .zip file containing this pack
	public String packDownloadLink;

}

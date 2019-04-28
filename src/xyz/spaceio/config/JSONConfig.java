package xyz.spaceio.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;

import org.bukkit.plugin.Plugin;

import com.google.gson.Gson;

/**
 * A simple class for serializing/deserializing objects using Gson and managing
 * its file locations. The object class must be serializable
 * 
 * @author MasterCake
 *
 */
public class JSONConfig {
	private File dataFile;
	private Type typeToken;
	private Object objectToSerialize;

	/**
	 * Creates a new JSONConfig
	 * 
	 * @param type              type of the object to store
	 * @param objectToSerialize the actual object to store
	 * @param parentPlugin      the plugin instance calling this method
	 */
	public JSONConfig(Type type, Object objectToSerialize, Plugin parentPlugin) {
		this.dataFile = new File(parentPlugin.getDataFolder().getPath() + "/" + "data.json");
		this.typeToken = type;
		this.objectToSerialize = objectToSerialize;
	}

	/**
	 * Returns the JSON string representing the object
	 * 
	 * @param obj
	 * @return a JSON string
	 */
	public String getJson() {
		Gson gson = new Gson();
		return gson.toJson(objectToSerialize, typeToken);
	}

	/**
	 * Saves the object to disk, using the given file {@link #dataFile}
	 * 
	 * @return the final file size in bytes
	 */
	public long saveToDisk() {
		Gson gson = new Gson();
		try {
			FileOutputStream fout = new FileOutputStream(dataFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);

			oos.writeObject(gson.toJson(objectToSerialize, typeToken));
			fout.close();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataFile.length();
	}

	/**
	 * Reads the JSON string from the file and serializes it to an object
	 * 
	 * @return the object
	 */
	public Object getObject() {
		Object c = null;
		if (dataFile.exists()) {
			try {
				FileInputStream fin = new FileInputStream(dataFile);
				ObjectInputStream ois = new ObjectInputStream(fin);
				Gson gson = new Gson();
				c = gson.fromJson((String) ois.readObject(), typeToken);
				ois.close();
				fin.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return c;
		} else {
			return null;
		}
	}
}

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


public class JSONConfig {
	File data;
	Type type;
	Object obj;
	
	public JSONConfig(Object obj, Type type, Plugin pl){
		this.data = new File(pl.getDataFolder().getPath() + "/" + "data.json");
		this.type = type;
	}
	public String getJson(Object obj){
		Gson gson = new Gson();
		return gson.toJson(obj, type);
	}
	public long saveToDisk(Object obj){
		Gson gson = new Gson();
		try {
			FileOutputStream fout= new FileOutputStream (data);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			
			oos.writeObject(gson.toJson(obj, type));
			fout.close();
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data.length();
	}
	public Object get(){
		Object  c = null;
		if(data.exists()){
			try {
				FileInputStream fin = new FileInputStream(data);
				ObjectInputStream ois = new ObjectInputStream(fin);
				Gson gson = new Gson();
				c = gson.fromJson((String) ois.readObject(), type);
				ois.close();
				fin.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return c;
		}else{
			return null;
		}
	}
}

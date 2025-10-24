package model_loader;

// import org.lwjgl.util.vector.Vector3f;
import org.joml.Vector3f;


public class LineSplitter {

  private int pointer = 0;
  private String[] data;

  public LineSplitter(String string){
    data = string.split(FileUtils.SEPARATOR);
  }

  public LineSplitter(String string, String separator){
    data = string.split(separator);
  }

  public String getNextString(){
    return data[pointer++];
  }

  public int getNextInt(){
    return Integer.parseInt(data[pointer++]);
  }

  public long getNextLong(){
    return Long.parseLong(data[pointer++]);
  }

  public float getNextFloat(){
    return Float.parseFloat(data[pointer++]);
  }

  public Vector3f getNextVector(){
    float x = getNextFloat();
    float y = getNextFloat();
    float z = getNextFloat();
    return new Vector3f(x, y, z);
  }

  public double getNextDouble(){
    return Double.parseDouble(data[pointer++]);
  }

  public boolean getNextBool(){
    return FileUtils.readBoolean(data[pointer++]);
  }

  public boolean hasMoreValues(){
    return pointer < data.length;
  }






}

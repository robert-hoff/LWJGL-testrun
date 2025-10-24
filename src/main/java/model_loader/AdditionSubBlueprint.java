package model_loader;

// import toolbox.Maths;

public class AdditionSubBlueprint extends SubBlueprint{

  private SubBlueprint base;

  public AdditionSubBlueprint(SubBlueprint base, float[] extraData) {
    super(extraData, base.getAABB(), base.getExtraAabbs(), 1);
    this.base = base;
  }

  @Override
  public int getDataLength() {
    return super.getDataLength() + base.getDataLength();
  }

  @Override
  public float[] getFullModelData() {
    return concatenateArrays(base.getFullModelData(), super.getUniqueStageData());
  }

  public static float[] concatenateArrays(float[]... arrays) {
    int totalLength = 0;
    for (float[] array : arrays) {
      totalLength += array.length;
    }
    float[] bigArray = new float[totalLength];
    int pointer = 0;
    for (float[] array : arrays) {
      for (int i = 0; i < array.length; i++) {
        bigArray[pointer++] = array[i];
      }
    }
    return bigArray;
  }
}


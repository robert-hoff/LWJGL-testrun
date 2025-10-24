package model_loader;


public class SubBlueprint {

  private float[] data;
  private AABB boundingBox;
  private AABB[] extraAabbs;
  private float increaseFactor;
  private float minGrowth;
  private float maxGrowth;

  public SubBlueprint(float[] data, AABB aabb, AABB[] aabbs, float increaseFactor){
    this.boundingBox = aabb;
    this.data = data;
    this.extraAabbs = aabbs;
    this.increaseFactor = increaseFactor;
  }

  public AABB[] getExtraAabbs(){
    return extraAabbs;
  }

  public void calculateGrowths(boolean isFirst, SubBlueprint nextStage){
    calculateMaxGrowth(nextStage);
    calculateMinGrowth(isFirst);
  }

  public float getIncreaseFactor(){
    return increaseFactor;
  }

  public float getMinGrowth() {
    return minGrowth;
  }

  public float getMaxGrowth() {
    return maxGrowth;
  }

  public float[] getFullModelData(){
    return data;
  }

  public float[] getUniqueStageData(){
    return data;
  }

  public int getDataLength(){
    return data.length;
  }

  public int getVertexCount(){
    return getDataLength() / MemorySlot.VERTEX_FLOAT_COUNT;
  }

  public AABB getAABB(){
    return boundingBox;
  }

  private void calculateMaxGrowth(SubBlueprint nextStage){
    if(nextStage == null){
      float half = (increaseFactor - 1)/2f;
      float part = (1 + half)/increaseFactor;
      this.maxGrowth = 2 - part;
    }else{
      float half = (nextStage.increaseFactor - 1)/2;
      this.maxGrowth = 1 + half;
    }
  }

  private void calculateMinGrowth(boolean isFirst){
    if(isFirst){
      float half = maxGrowth - 1;
      this.minGrowth = 1 - half;
    }else{
      float half = (increaseFactor - 1)/2f;
      this.minGrowth = (1 + half)/increaseFactor;
    }
  }


}

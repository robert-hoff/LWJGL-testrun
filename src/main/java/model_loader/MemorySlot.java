package model_loader;

import java.util.List;

public class MemorySlot {

  public static final int VERTEX_FLOAT_COUNT = 4 + 3 + 3;

  private int startIndex;
  private int endIndex;
  private boolean isGap;
  private MemorySlot previousSlot = null;
  private MemorySlot nextSlot = null;
  private float[] data;

  protected MemorySlot(int start, int length, boolean isGap) {
    this.startIndex = start;
    this.endIndex = start + length;
    this.isGap = isGap;
  }

  protected void clear(){
    data = null;
  }

  protected int getStartIndex() {
    return startIndex;
  }

  protected int getEndIndex(){
    return endIndex;
  }

  protected float[] getData(){
    return data;
  }

  protected MemorySlot getNextSlot() {
    return nextSlot;
  }

  protected int getLength() {
    return endIndex - startIndex;
  }

  protected MemorySlot getPreviousSlot() {
    return previousSlot;
  }

  protected boolean isGap() {
    return isGap;
  }

  protected void connectToPrevious(MemorySlot previous){
    this.previousSlot = previous;
    if(previous!=null){
      previous.nextSlot = this;
    }
  }

  protected void connectToNext(MemorySlot next){
    this.nextSlot = next;
    if(next!=null){
      next.previousSlot = this;
    }
  }

  protected void shiftLeft(int amount){
    this.startIndex -= amount;
    endIndex -= amount;
  }

  protected void increaseStartIndex(int filledLength){
    this.startIndex += filledLength;
  }

  protected void increaseEndIndex(int length){
    this.endIndex += length;
  }

  protected static void append(MemorySlot newSlot, MemorySlot currentEnd) {
    if(currentEnd!=null){
      currentEnd.connectToNext(newSlot);
    }
  }

  protected static void insertInGap(MemorySlot newSlot, MemorySlot gap, List<MemorySlot> gaps) {
    if(gap.previousSlot!=null){
      gap.previousSlot.connectToNext(newSlot);
    }
    if(gap.getLength() == newSlot.getLength()){
      newSlot.connectToNext(gap.nextSlot);
      gaps.remove(gap);
    }else{
      newSlot.connectToNext(gap);
      gap.increaseStartIndex(newSlot.getLength());
    }
  }

  protected static MemorySlot createGap(int start, int length) {
    return new MemorySlot(start, length, true);
  }

  protected static MemorySlot createDataSlot(int start, float[] data){
    MemorySlot slot = new MemorySlot(start, data.length, false);
    slot.data = data;
    return slot;
  }
}


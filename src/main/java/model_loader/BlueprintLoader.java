package model_loader;

import java.util.ArrayList;
import java.util.List;

// import org.lwjgl.util.vector.Vector3f;
import org.joml.Vector3f;
// import classification.Classifier;
// import componentArchitecture.ComponentBlueprint;
// import componentArchitecture.ComponentType;
// import languages.GameText;


public class BlueprintLoader {

  public static List<SubBlueprint> loadBlueprint(/* Blueprint blueprint,*/ MyFile blueprintFile) throws Exception {
    CSVReader reader = new CSVReader(blueprintFile);
    reader.nextLine();
    float size = reader.getNextFloat();
    String extraName = loadOverrideName(reader);
    Integer overrideIndex = loadOverrideModelIndex(reader);
    boolean randomize = loadPotentialBoolean(reader);
    boolean alwaysVis = loadPotentialBoolean(reader);
    loadIconInfo(reader /*, blueprint*/);
    // blueprint.alwaysVisible = alwaysVis;
    loadBlueprintInfo(/*blueprint,*/ reader);
    reader.nextLine();
    int subBlueprintCount = reader.getNextInt();
    List<SubBlueprint> subBlueprints = new ArrayList<SubBlueprint>();
    for (int i = 0; i < subBlueprintCount; i++) {
      loadSubBlueprint(reader, subBlueprints, size);
    }
    calculateGrowthFactors(subBlueprints);
    // blueprint.setSubBlueprints(subBlueprints);
    // blueprint.setOverrideName(extraName);
    // blueprint.setRandomizeModelStages(randomize);
    // blueprint.setOverrideMainSubBlueprintIndex(overrideIndex);
    loadComponents(/*blueprint,*/ reader);
    // blueprint.indicateLoaded();
    reader.close();
    return subBlueprints;
  }

  private static void loadSubBlueprint(CSVReader reader, List<SubBlueprint> subBlueprints, float size) {
    reader.nextLine();
    Vector3f mins = reader.getNextVector();
    // mins.scale(size);
    mins.mul(size);
    Vector3f maxs = reader.getNextVector();
    // maxs.scale(size);
    maxs.mul(size);
    float increaseFactor = reader.getNextFloat();
    boolean additive = false;
    AABB[] extraAabbs = null;
    if (!reader.isEndOfLine()) {
      additive = reader.getNextBool();
      extraAabbs = loadExtraAabbs(reader, size);
    }
    float[] modelData = ModelLoader.loadModel(reader, size);
    if (additive) {
      SubBlueprint base = subBlueprints.get(subBlueprints.size()-1);
      subBlueprints.add(new AdditionSubBlueprint(base, modelData));
    } else {
      subBlueprints.add(new SubBlueprint(modelData, new AABB(mins, maxs), extraAabbs, increaseFactor));
    }
  }

  private static AABB[] loadExtraAabbs(CSVReader reader, float size){
    if(reader.isEndOfLine()){
      return null;
    }
    int count = reader.getNextLabelInt();
    AABB[] aabbs = new AABB[count];
    for(int i=0;i<count;i++){
      Vector3f mins = reader.getNextVector();
      // mins.scale(size);
      mins.mul(size);
      Vector3f maxs = reader.getNextVector();
      // maxs.scale(size);
      maxs.mul(size);
      aabbs[i] = new AABB(mins, maxs);
    }
    return aabbs;
  }

  private static String loadOverrideName(CSVReader reader){
    if(!reader.isEndOfLine()){
      int index = reader.getNextLabelInt();
      if(index == -1){
        return null;
      }
      // return GameText.getText(index);
      return "game-text";
    }else{
      return null;
    }
  }

  private static void loadIconInfo(CSVReader reader /*, Blueprint blueprint*/){
    if(reader.isEndOfLine()){
      return;
    }
    float size = reader.getNextLabelFloat();
    float y = reader.getNextLabelFloat();
    // blueprint.setOverrideIconValues(size, y);
  }

  private static boolean loadPotentialBoolean(CSVReader reader){
    if(!reader.isEndOfLine()){
      return reader.getNextLabelBool();
    }
    return false;
  }

  private static Integer loadOverrideModelIndex(CSVReader reader){
    if(!reader.isEndOfLine()){
      int value = reader.getNextLabelInt();
      if(value >= 0){
        return value;
      }else{
        return null;
      }
    }else{
      return null;
    }
  }

  private static void calculateGrowthFactors(List<SubBlueprint> subBlueprints) {
    for (int i = 0; i < subBlueprints.size(); i++) {
      SubBlueprint nextStage = i < subBlueprints.size() - 1 ? subBlueprints.get(i + 1) : null;
      subBlueprints.get(i).calculateGrowths(i == 0, nextStage);
    }
  }

  private static void loadComponents(/*Blueprint blueprint,*/ CSVReader reader) {
    reader.nextLine();
    int componentCount = reader.getNextInt();
    for (int i = 0; i < componentCount; i++) {
      reader.nextLine();
      // ComponentType type = ComponentType.valueOf(reader.getNextString());
      String component_string = reader.getNextString();
      // ComponentBlueprint component = type.loadComponent(reader, blueprint);
      // blueprint.addComponent(component);
    }
  }

  private static void loadBlueprintInfo(/*Blueprint blueprint,*/ CSVReader reader) {
    reader.nextLine();
    String classification = reader.getNextString();
    // blueprint.setClassification(Classifier.getClassification(classification));
    reader.nextLine();
    boolean underwater = reader.getNextBool();
    boolean overwater = reader.getNextBool();
    float offset = 0;
    if(!reader.isEndOfLine()){
      offset = reader.getNextFloat();
    }
    // blueprint.setWaterRequirements(underwater, overwater, offset);
  }

}










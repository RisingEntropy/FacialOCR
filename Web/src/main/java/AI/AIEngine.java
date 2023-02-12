package AI;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import io.jhdf.HdfFile;
import io.jhdf.api.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class AIEngine {
    private String modelPath;
    private Base64Translator translator;

    private Criteria<String, float[]> criteria;
    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;
    private boolean initialized = false;
    private float[][] database;
    private ArrayList<Integer> unicodes;
    private boolean databse_loaded = false;
    public AIEngine(){

    }
    public boolean initialize(String modelPath){
        this.modelPath = modelPath;
        this.translator = new Base64Translator();
        if(initialized)
            return true;
        this.criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelPath(Paths.get(this.modelPath))
                .optOption("mapLocation", "true")
                .optTranslator(translator)
                .build();
        try {
            this.model = criteria.loadModel();
            this.predictor = this.model.newPredictor();
            this.initialized = true;
            return true;
        } catch (IOException | MalformedModelException | ModelNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isDatabaseLoaded(){
        return this.databse_loaded;
    }
    public boolean engineOK(){
        return this.initialized&&this.databse_loaded;
    }
    public boolean loadDatabase(String path){
        try(HdfFile file = new HdfFile(Paths.get(path))){
            this.unicodes = new ArrayList<>();
            for(Node node:file){
                unicodes.add(Integer.valueOf(node.getName()));
            }
            this.database = new float[unicodes.size()][];
            for (int i = 0;i<unicodes.size();i++){
                database[i] = (float[]) file.getDatasetByPath("/"+unicodes.get(i)).getData();
            }
            this.databse_loaded = true;
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public boolean isInitialized(){
        return this.initialized;
    }
    public Result infer(String base64, float threshold) {

        if(!initialized || !databse_loaded)
            return null;

        Result result = new Result();
        float[] embedding = new float[0];
        try {
            embedding = this.predictor.predict(base64);
        } catch (TranslateException e) {
            return result;
        }
        float bestMatch = 1e9f;
        int bestUnicode = 0;
        for(int i = 0;i <database.length; i++){
            float norm = l2_norm(database[i], embedding);
            if(norm<bestMatch){
                bestMatch = norm;
                bestUnicode = this.unicodes.get(i);
            }else if(norm<threshold){
                result.addCandidate(Character.toString(this.unicodes.get(i)),norm);
            }
        }
        result.setBestMatch(Character.toString(bestUnicode), bestMatch);
        return result;
    }
    private static float l2_norm(float[] v1, float[] v2){
        if(v1.length!=v2.length)
            throw new IndexOutOfBoundsException("length of input vector v1,v2 does not match each other");
        float norm = 0.f;
        for(int i = 0; i<v1.length; i++)
            norm += (v1[i]-v2[i])*(v1[i]-v2[i]);
        return norm;
    }


}
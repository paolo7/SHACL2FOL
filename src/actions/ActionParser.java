package actions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

public class ActionParser {

    public static List<Action> readActionsFromFile(String filename) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filename), new TypeReference<List<Action>>() {});
    }

    public static void main(String[] args) throws Exception {
        List<Action> actions = readActionsFromFile("actions.json");
        for (Action action : actions) {
            System.out.println(action.getClass().getSimpleName() + ": " + action);
        }
    }
}
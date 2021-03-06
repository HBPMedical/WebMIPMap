/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.users.recommendation;

import gr.aueb.users.ActionGetUsers;
import gr.aueb.users.recommendation.mappingmodel.MappingScenario;
import gr.aueb.users.recommendation.mappingmodel.Schema;
import it.unibas.spicy.persistence.DAOException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import net.sf.jsqlparser.JSQLParserException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author ioannisxar
 */
public class ActionFindCommonMappingTasks {
    
    private String user, mappingName, mappingType;
    
    public ActionFindCommonMappingTasks(String user, String mappingName, String mappingType){
        this.user = user;
        this.mappingName = mappingName;
        this.mappingType = mappingType;
    }
    
    public HashMap<MappingScenario, String> findCommonScenarions() throws DAOException, IOException, FileNotFoundException, JSQLParserException{
        OpenMappingScenario scenarioToMatch = new OpenMappingScenario(user, mappingName);
        Schema sourceSchemaToCheck = scenarioToMatch.getScenarioSchema("source", mappingType);
        Schema targetSchemaToCheck = scenarioToMatch.getScenarioSchema("target", mappingType);
        ArrayList<MappingScenario> trustedUserPublicMappings = trustedMappingsToCheck();
        HashMap<MappingScenario, String> commonScenarios = new HashMap<>();
        for(MappingScenario scenario: trustedUserPublicMappings){
            //checks if both source and target schemata are common in both scenarios
            if(sourceSchemaToCheck.compareSchemata(scenario, "source") && targetSchemaToCheck.compareSchemata(scenario, "target")){
                commonScenarios.put(scenario, scenario.getMappingTaskName());
            }
        }
        return commonScenarios;
    }
    
    private ArrayList<MappingScenario> trustedMappingsToCheck() throws DAOException, IOException, FileNotFoundException, FileNotFoundException, JSQLParserException, JSQLParserException, JSQLParserException, JSQLParserException, JSQLParserException{
        ArrayList<MappingScenario> trustedUserPublicMappings = new ArrayList<>();
        ActionGetUsers actionGetUsers = new ActionGetUsers();
        actionGetUsers.performAction(user);
        JSONObject outputObject = actionGetUsers.getJSONObject();
        JSONArray trustedUsers = (JSONArray) outputObject.get("trustUsers");
        Iterator<JSONObject> iterator = trustedUsers.iterator();
        while (iterator.hasNext()) {
            JSONObject innerObject = iterator.next();
            JSONArray publicTasks = (JSONArray) innerObject.get("publicTasks");
            Iterator<JSONObject> publicTasksIterator = publicTasks.iterator();
            while (publicTasksIterator.hasNext()) {
                String userName = (String) innerObject.get("userName");
                String mappingTaskName = (String) publicTasksIterator.next().get("taskName");
                OpenMappingScenario scenario = new OpenMappingScenario(userName, mappingTaskName);
                Schema sourceSchema = scenario.getScenarioSchema("source", "public");
                Schema targetSchema = scenario.getScenarioSchema("target", "public");
                trustedUserPublicMappings.add(new MappingScenario(userName, mappingTaskName, sourceSchema, targetSchema));
            }
        }
        return trustedUserPublicMappings;
    }
    
}

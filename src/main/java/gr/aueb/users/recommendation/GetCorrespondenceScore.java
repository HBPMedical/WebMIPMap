/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aueb.users.recommendation;

import gr.aueb.context.ApplicationContextProvider;
import gr.aueb.users.recommendation.mappingmodel.UserMappingCorrespondences;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author ioannisxar
 */
public class GetCorrespondenceScore {
    private ArrayList<UserMappingCorrespondences> umc;
    private JdbcTemplate jdbcTemplate = (JdbcTemplate) ApplicationContextProvider.getApplicationContext().getBean("jdbcTemplate");
    
    
    public GetCorrespondenceScore(ArrayList<UserMappingCorrespondences> umc){
        this.umc = umc;
    }
    
    public void performAction(){
        //feature - users' Pagerank
        HashMap<String, Double> usersPRank = getUsersPagerank();
        //feature - users' Credibility
        HashMap<String, Double> usersCredibility = getUsersCredibility();
        //feature - users' Total Connection normalized
        HashMap<String, Double> usersTotalConnectionsNormalized = getUsersTotalConnectionsNormalized();
    }
    
    private HashMap<String, Double> getUsersPagerank(){
        DirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        HashMap<String, String> userIds = new HashMap<>();
        jdbcTemplate.query(
            "SELECT \"id\", \"username\" "
            + "FROM mipmapuser;",
            (rs, rowNum) ->  new Object[] { rs.getString("id"), rs.getString("username") }
        ).stream().forEach((user) -> { 
            userIds.put(String.valueOf(user[0]), String.valueOf(user[1]));
        });
        userIds.forEach((id, user)->{
            directedGraph.addVertex(user);
        });
        jdbcTemplate.query(
            "SELECT \"userA\", \"userB\" "
            + "FROM user_user "
            + "WHERE status = 1;",
            (rs, rowNum) ->  new Object[] { rs.getString("userA"), rs.getString("userB") }
        ).stream().forEach((user) -> { 
            directedGraph.addEdge(userIds.get(user[1]), userIds.get(user[0])); 
        });
        
        PageRank a = new PageRank(directedGraph);
        HashMap<String, Double> usersPRank = new HashMap<>();
        Set<String> users = new HashSet<>();
        umc.forEach((umcObject)->{
            users.add(umcObject.getUser());
        });
        for(String user: users){
            usersPRank.put(user, a.getVertexScore(user));
        }
        return usersPRank;
    }
    
    private double getUsersAverageMappings(){
        return 0.0;
    }
    
    private HashMap<String, Double> getUsersCredibility(){
        HashMap<String, Double> usersCredibility = new HashMap<>();
        jdbcTemplate.query(
            "SELECT \"username\", \"mappings_accepted\", \"mappings_total\" "
            + "FROM mipmapuser;",
            (rs, rowNum) ->  new Object[] { rs.getString("username"), rs.getString("mappings_accepted"), rs.getString("mappings_total") }
        ).stream().forEach((obj) -> { 
            usersCredibility.put(String.valueOf(obj[0]), (double)Integer.parseInt(String.valueOf(obj[1]))/Integer.parseInt(String.valueOf(obj[2])));
        });
        return usersCredibility;
    }
    
    private HashMap<String, Double> getUsersTotalConnectionsNormalized(){
        HashMap<String, Double> usersTotalConnectionsNormalized = new HashMap<>(); 
        String query = "SELECT min(\"mappings_total\") as min, max(\"mappings_total\") as max FROM mipmapuser;";
        Map<String, Object> row = jdbcTemplate.queryForMap(query);
        int min = Integer.parseInt(String.valueOf(row.get("min")));
        int max = Integer.parseInt(String.valueOf(row.get("max")));
        jdbcTemplate.query(
            "SELECT \"username\", \"mappings_total\" "
            + "FROM mipmapuser;",
            (rs, rowNum) ->  new Object[] { rs.getString("username"), rs.getString("mappings_total") }
        ).stream().forEach((obj) -> {
            double score = (double)(Integer.valueOf(String.valueOf(obj[1])) - min)/(double)(max-min);
            usersTotalConnectionsNormalized.put(String.valueOf(obj[0]), score);
        });
        return usersTotalConnectionsNormalized;
    }
}

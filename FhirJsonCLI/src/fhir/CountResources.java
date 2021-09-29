package fhir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Parses command line arguments to get ID or first/last name of a patient.
 * Find all resources in the .ndjson files that reference that patient, directly or indirectly.
 * Output summary of findings that includes the resource type and number of unique resources.
 * 
 *  @author Alan
 *  @version Sep 29, 2021
 */
public class CountResources
{

    /**
     * Main method to count number of FHIR resources given patient resource. 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException
    {
        String fullName = "";
        String fName = "";
        String lName = "";
        String id = "";
        String userDir = System.getProperty("user.dir");
        File patFile = new File(userDir+"\\Patient.ndjson");
        ArrayList<File> files = new ArrayList<File>();
        HashMap <String, Integer> resourceMap = new HashMap<String, Integer>();
        
        if (args.length == 1) {
            id = args[0];
            
            try
            {
                fullName = findName(id, patFile);
            } catch (IOException | ParseException e)
            {
                e.printStackTrace();
            }
            
        } else if (args.length == 2) {
            fName = args[0];
            lName = args[1];
            fullName = fName + " " + lName;
            
            try
            {
                id = findId(lName, fName, patFile);
            }
            catch (IOException | ParseException e)
            {
                e.printStackTrace();
            }
            
            if (id == "") {
                System.out.println("Unable to find ID for patient: "+ fName + " " + lName);
            }
        } else {
            System.out.println("Please enter FirstName LastName or ID as args");
            System.out.println("For example: CountResources.java Rosamond509 Lynch190");
            return;
        }
        
        File folder = new File(userDir);
        files.addAll(Arrays.asList(folder.listFiles((d, name) -> name.endsWith(".ndjson"))));
                
        if (files.size() == 0) {
            System.out.println("No files were found in: " + userDir);
            return;
        }
        
        try
        {
            resourceMap = findNumOfRef(files, id);
        }
        catch (IOException | ParseException e1)
        {
            e1.printStackTrace();
        }

        resourceMap = sortDesc(resourceMap);
        
        printMap(resourceMap, id, fullName);
    }

    /**
     * Find patient's name from the Patient ndjson.
     * @param patient id
     * @param patFile
     * @return patient full name
     * @throws ParseException 
     * @throws IOException 
     */
    private static String findName(String id, File patFile) throws IOException, ParseException
    {
        String fullName = "";
        
        if (patFile.isFile()) {
            BufferedReader inputStream = null;
            String line;
            JSONParser parser = new JSONParser();
            
            try {
                inputStream = new BufferedReader(new FileReader(patFile));
                while ((line = inputStream.readLine()) != null) {
                    if(line.contains(id)) {
                        JSONObject json = (JSONObject) parser.parse(line);
                        JSONArray nameArr = (JSONArray) json.get("name");
                        JSONObject firstJson = (JSONObject)nameArr.get(0);
                        JSONArray given = (JSONArray) firstJson.get("given");
                        fullName = (String) firstJson.get("family") + (String) given.get(0); 
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }   
        }
        return fullName;
    }

    /**
     * Find patient's ID from the Patient ndjson.
     * @param patient last name
     * @param patient first name
     * @param userDir
     * @return patient Id
     * @throws IOException 
     * @throws ParseException 
     */
    private static String findId(String lName, String fName, File patFile) throws IOException, ParseException
    {
        String id = "";
        
        if (patFile.isFile()) {
            BufferedReader inputStream = null;
            String line;
            JSONParser parser = new JSONParser();
            
            try {
                inputStream = new BufferedReader(new FileReader(patFile));
                while ((line = inputStream.readLine()) != null) {
                    
                    if(line.contains(lName) && line.contains(fName)) {
                        JSONObject json = (JSONObject) parser.parse(line);
                        id = (String) json.get("id");
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }   
        }
        return id;
    }

    /**
     * Prints contents of the hashmap in a formatted output
     * @param resourceMap
     */
    private static void printMap(HashMap<String, Integer> resourceMap, String id, String name)
    {
        System.out.println("Patient Name: " + name);
        System.out.println("Patient ID: " + id + "\n");
        System.out.format("%-22s%-22s\n", "RESOURCE_TYPE", "COUNT");
        System.out.println("------------------------------");
        for(HashMap.Entry<String, Integer>resource : resourceMap.entrySet()) {
            System.out.format("%-22s%-22d\n", resource.getKey(), resource.getValue());
        }
        
    }

    /**
     * Sorts hashmap based on its value (count) in descending order
     * @param resourceList
     * @return Sorted
     */
    private static HashMap<String, Integer> sortDesc(HashMap<String, Integer> resourceList)
    {
        HashMap<String,Integer>  sortedDecMap =  resourceList.entrySet().
            stream().
            sorted(HashMap.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(HashMap.Entry::getKey, HashMap.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return sortedDecMap;
    }

    /**
     * Returns a sorted descending hashmap containing the resource type and the number of times
     * the patient ID was referenced in the file.
     * 
     * @param files
     * @param id
     * @return hashmap <ResourceType, Count of references of patient ID>
     * @throws IOException 
     * @throws ParseException 
     */
    private static HashMap<String, Integer> findNumOfRef(ArrayList<File> files, String id) throws IOException, ParseException
    {
        HashMap<String, Integer> returnHash = new HashMap<String, Integer>();
        
        for (File file : files) {
            if (file.isFile()) {
                BufferedReader inputStream = null;
                String line;
                JSONParser parser = new JSONParser();
                
                try {
                    inputStream = new BufferedReader(new FileReader(file));
                    while ((line = inputStream.readLine()) != null) {
                        if(line.contains(id)) {
                            JSONObject json = (JSONObject) parser.parse(line);
                            String resource = (String) json.get("resourceType");
                            returnHash.put(resource, returnHash.getOrDefault(resource, 0) + 1);
                        }
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }   
            }
        }
        
        return returnHash;
    }
}

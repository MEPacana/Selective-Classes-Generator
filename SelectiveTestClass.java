import java.io.File;  // Import the File class
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.regex.*;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class SelectiveTestClass {
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);  
        clearScreen();
        System.out.println("*****************************************************************************************************");
        System.out.println("****************************** Selective Test Class Generator ***************************************");
        System.out.println("*****************************************************************************************************");
        System.out.println("Please input path of the file containing main classes list ");
        String fileLocation = sc.nextLine();
        List<String> selectiveMainClassFileNames = getSelectiveMainClass(fileLocation);
        System.out.println("\n\nPlease input project file location ");
        String projectFileLocation = sc.nextLine();
        sc.close();

        ProjectFileName folderClassFileNames = getProjectClassesNames(projectFileLocation);
        SelectiveTestClassResult selectiveTestClassResult = getSelectiveTestClassResult(selectiveMainClassFileNames, folderClassFileNames.projectAllTestClasses, projectFileLocation);
        selectiveTestClassResult.processSelectiveClasses();
        createOutputFile(selectiveTestClassResult);
        clearScreen();
        System.out.println("*****************************************************************************************************");
        System.out.println("************ Selective Test Class Successful. Please check SelectiveTestClasses.txt file ************");
        System.out.println("*****************************************************************************************************");
    }

    public static void clearScreen() {   
        System.out.print("\033[H\033[2J");   
        System.out.flush();   
    } 

    public static void createOutputFile(SelectiveTestClassResult selectiveTestClassResult ){
        try {
            File outFile = new File("SelectiveTestClasses.txt");
            if(!outFile.isFile()){
                outFile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream("SelectiveTestClasses.txt", false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-16");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            List<String> allSelectiveTestClasses = new ArrayList<String>(selectiveTestClassResult.allSelectiveTestClasses);
            if(allSelectiveTestClasses.size() > 0){
                Collections.sort(allSelectiveTestClasses);
                for(String s : allSelectiveTestClasses){
                    bufferedWriter.write(s);
                    bufferedWriter.newLine();
                }
            }
            List<String> allMainClassesWithNoTestClass = new ArrayList<>(selectiveTestClassResult.allMainClassesWithNoTestClass);
            if(allMainClassesWithNoTestClass.size() > 0){
                Collections.sort(allMainClassesWithNoTestClass);
                bufferedWriter.write("NO TEST CLASSES FOUND:\n");
                for(String s : allMainClassesWithNoTestClass){
                    bufferedWriter.write(s);
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         
    }
    public static SelectiveTestClassResult getSelectiveTestClassResult(List<String> selectiveMainClassFileNames, String allTestClasses, String fileLocation){
        SelectiveTestClassResult selectiveTestClassResult = new SelectiveTestClassResult();
        Set<String> tempTestClass;
        String tempMainFileFileLocation; 
        for(String mainClassFileName : selectiveMainClassFileNames){
            tempTestClass = new HashSet<String>();
            tempTestClass.addAll(getTestStrings(mainClassFileName, allTestClasses));
            tempMainFileFileLocation = getFileLocationOfMainClass(mainClassFileName, fileLocation);
            tempTestClass.addAll(readMainClass(tempMainFileFileLocation));
            selectiveTestClassResult.mainClassToTestClass.put(mainClassFileName, tempTestClass);
        }
        return selectiveTestClassResult;
    }

    public static class SelectiveTestClassResult{
        Map<String, Set<String>> mainClassToTestClass;
        Set<String> allSelectiveTestClasses;
        Set<String> allMainClassesWithNoTestClass;

        public SelectiveTestClassResult(){
            this.mainClassToTestClass = new HashMap<>();
            this.allSelectiveTestClasses = new HashSet<String>();
            this.allMainClassesWithNoTestClass = new HashSet<>();
        }

        public void processSelectiveClasses(){
            for(String mainClass : mainClassToTestClass.keySet()){
                if(mainClassToTestClass.get(mainClass) != null && mainClassToTestClass.get(mainClass).size() > 0){
                    allSelectiveTestClasses.addAll(mainClassToTestClass.get(mainClass));
                }else{
                    allMainClassesWithNoTestClass.add(mainClass);
                }
            }
        }
    }
    
    public static Set<String> getTestClassesFromMainClassFolder(List<String> selectiveMainClassFileNames,String allTestClasses){
        Set<String> allSelectiveTestClasses = new HashSet<String>();
        for(String mainClassFileName : selectiveMainClassFileNames){
            allSelectiveTestClasses.addAll(getTestStrings(mainClassFileName, allTestClasses));
        }
        return allSelectiveTestClasses;
    }

    public static Set<String> getTestClassesFromMainClassFile(List<String> selectiveMainClassFileNames, String fileLocation){
        Set<String> testClassesFromMainClassFile = new HashSet<String>();
        String tempMainFileFileLocation ; 

        for(String selectiveMainClass : selectiveMainClassFileNames){
            tempMainFileFileLocation = getFileLocationOfMainClass(selectiveMainClass, fileLocation);
            testClassesFromMainClassFile.addAll(readMainClass(tempMainFileFileLocation));
        }
        return testClassesFromMainClassFile;
    }
    public static Set<String> readMainClass(String mainClassFileLocation){
        
        Set<String> tempTestClassStrings;
        Set<String>  allSelectiveTestClassesFromFile = new HashSet<String>();
        try {
            File myObj = new File(mainClassFileLocation);
            if(myObj.isFile()){
                FileReader reader = new FileReader(mainClassFileLocation); 
                BufferedReader bufferedReader = new BufferedReader(reader);
            
                String line, regex = "(\\w)";
                Integer count = 0, limit = 15;
                while ((line = bufferedReader.readLine()) != null) {
                    count++;
                    tempTestClassStrings = getTestStrings(regex, line);
                    allSelectiveTestClassesFromFile.addAll(tempTestClassStrings);
                    if(count > limit){
                        break;
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  allSelectiveTestClassesFromFile;
    }
    public static String getFileLocationOfMainClass(String selectiveMainClass, String fileLocation){
        return fileLocation + "\\"+ selectiveMainClass + ".cls";
    }

    public static class ProjectFileName{
        String projectAllTestClasses;
        String projectAllMainClasses;
        public  ProjectFileName(String projectAllTestClasses, String projectAllMainClasses){
            this.projectAllTestClasses = projectAllTestClasses;
            this.projectAllMainClasses = projectAllMainClasses;
        }
    }
    public static ProjectFileName getProjectClassesNames(String projectFileLocation){
        String projectAllTestClasses = new String();
        String projectAllMainClasses = new String();
        File folder = new File(projectFileLocation);
        File[] listOfFiles = folder.listFiles();
        String tempString;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                tempString = listOfFiles[i].getName();
                if(!tempString.contains("meta")){
                    if(tempString.toLowerCase().contains("test")){
                        projectAllTestClasses += tempString + " ";
                    }else{
                        projectAllMainClasses += tempString + " ";
                    }
                }
            }
        }
        return new ProjectFileName(projectAllTestClasses, projectAllMainClasses);
    }
    public static List<String>  getSelectiveMainClass(String fileLocation){
        List<String> mainClassFileNames = new ArrayList<String>();
        try {
            File myObj = new File(fileLocation);
            if(myObj.isFile()){
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    mainClassFileNames.add(data);
                }
                myReader.close();
            }
        }catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return mainClassFileNames;
    }

    public static Set<String> getTestStrings(String token, String input){
        String regexSearchToken = token+"+(_)*(T|t)(E|e)(S|s)(T|t)";

        Pattern p = Pattern.compile(regexSearchToken);
        Matcher m = p.matcher(input);   // get a matcher object
        Set<String> testStrings = new HashSet<String>();        
  
        while(m.find()) {
            if(m.start() - 1 > 0 && input.charAt(m.start() - 1) != '.' || m.start() - 1 <= 0){
                testStrings.add(input.substring(m.start(),m.end()));
            }
        }
        return testStrings;
    }

    public static List<String> getAllFileName(String folderName){
        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();
        String delims = "[.]";
        List<String> fileNameList = new ArrayList<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileNameList.add(listOfFiles[i].getName().split(delims)[0]);
            }
        }
        return fileNameList;
    }
}

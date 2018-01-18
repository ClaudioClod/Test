/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prova;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.text.Annotation;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import static javax.tools.StandardLocation.SOURCE_OUTPUT;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author admin
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.hpe.ict.activator.nodes.provisioning.base.HPESANodeAttributes")
public class HPESANodeAttributesProcessor  extends AbstractProcessor{

     /** public for ServiceLoader */
    public HPESANodeAttributesProcessor() {
    }
    
    private String xmlStart="<?xml version='1.0' encoding='utf-8'?>\n" +
"<!DOCTYPE WorkflowComponent SYSTEM 'workflowComponent.dtd'>\n" +
"<!-- (c) Copyright 2010 Hewlett-Packard Development Company, L.P. -->\n" +
"\n" +
"<WorkflowComponent>\n" +
" <Name>{nodeName}</Name>\n" +
" <NodeDescription>{nodeName}</NodeDescription>\n" +
" <ClassName>{nodeFullName}</ClassName>\n" +
" <Type>{nodeType}</Type>\n" +
" <DisablePersistence>{nodeDisablePersistence}</DisablePersistence>\n" +
" <Params>";
    
    private String xmlStartH="<?xml version='1.0' encoding='utf-8'?>\n" +
"<!-- (c) Copyright 2010 Hewlett-Packard Development Company, L.P. -->\n" +
"\n" +
"<WorkflowComponent>\n" +
" <Name>{nodeName}</Name>\n" +
" <NodeDescription>{nodeName}</NodeDescription>\n" +
" <ClassName>{nodeFullName}</ClassName>\n" +
" <Type>{nodeType}</Type>\n" +
" <DisablePersistence>{nodeDisablePersistence}</DisablePersistence>\n" +
" <Params>";
    
    private String ParamNode="<Param Style=\"either\" Type=\"{nodeParamType}\" Required=\"{nodeRequired}\" Multiple=\"{nodeParamMultiple}\">\n" +
"     <Name>{paramName}</Name>\n" +
"     <Description>{paramDescription}</Description>\n" +
"    </Param>";
            
     private String ParamNodeEmpty="<Param Style=\"either\"  Required=\"{nodeRequired}\" Multiple=\"{nodeParamMultiple}\">\n" +
"     <Name>{paramName}</Name>\n" +
"     <Description>{paramDescription}</Description>\n" +
"    </Param>";
     
    private String xmlEnd="<Param Multiple=\"false\" Style=\"either\" Type=\"Boolean\" Required=\"false\">\n" +
"      <Name>throw_excep</Name>\n" +
"      <Description>Controls whether the node should throw exceptions upon failures, or if the framework should handle them. If set to 'false' the framework will handle the failure by setting the RET_VALUE case packet variable to -1. The RET_TEXT variable will hold the failure text. (Default is 'true')</Description>\n" +
"    </Param>\n" +
" </Params>\n" +
"</WorkflowComponent>";
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        
        boolean hasAnn=false;
             String newXml="";
             String  ParamNodeItem="";
             Writer w=null;
             PrintWriter pw=null;
             FileObject f=null;
             
        boolean isFirstAnn=true;
        
        boolean skipAddNodeToContainer=false;
        String fileName="";
       
        //workflownodes
         
         String groupName="NodeICTCompanyData";//simpleclassname--> clazz.getSimpleName()
         String groupNameDescription="NodeICTCompanyData";
         String groupNameImageDefault="Database.gif";
         String nodeFullName="";
         String nodeNameImageDefault="Database.gif";
        
        for (Element e : roundEnv.getElementsAnnotatedWith(HPESANodeAttributes.class)) {
            
             try {
                
              skipAddNodeToContainer=false;
              HPESANodeAttributes annotation = e.getAnnotation(HPESANodeAttributes.class);
                  
//               processingEnv.getMessager().printMessage(
//                        Diagnostic.Kind.NOTE,
//                        " ELEMENT ***", e);
               
               
            if (e.getKind() != ElementKind.FIELD) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        "Not a field", e);
                continue;
            }
            
           
            
            //String name = capitalize(e.getSimpleName().toString());
            TypeElement clazz = (TypeElement) e.getEnclosingElement();
           
                if(isFirstAnn)
                    fileName=clazz.getSimpleName().toString();
                 
                System.out.println("fileName:"+fileName + ", isFirstAnn:"+isFirstAnn);
                
                if(isFirstAnn || 
                        !fileName.equalsIgnoreCase(clazz.getSimpleName().toString()))
                {
                 
                    if(
                            !isFirstAnn && 
                            hasAnn 
                            && !fileName.equalsIgnoreCase(clazz.getSimpleName().toString())
                        )
                    {
                            fileName=clazz.getSimpleName().toString();
                        try{
                              processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                              "Closing " + f.toUri());
                              
                             pw.println("");
                             pw.println(xmlEnd);

                              pw.flush();
                              isFirstAnn=true;
                            } finally {
                            try {
                                w.close();
                            } catch (IOException ex) {
                                Logger.getLogger(HPESANodeAttributesProcessor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            }
                    }
               
                f = processingEnv.getFiler().createResource(SOURCE_OUTPUT, 
                        "nodes",
                        //clazz.getQualifiedName(), 
                        clazz.getSimpleName()+".xml", (Element) null);
                
                        //createSourceFile(clazz.getQualifiedName() + "Extras");
                
                   
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Creating " + f.toUri());
                
                   isFirstAnn=false;
                   
                   w = f.openWriter();
                   pw = new PrintWriter(w);
                   
                   if(annotation.nodeType()==HPESANodeAttributes.NodeType.EndHandler
                      ||  annotation.nodeType()==HPESANodeAttributes.NodeType.ErrorHandler    )
                   {
                       newXml=xmlStartH.replace("{nodeName}", clazz.getSimpleName().toString());
                       skipAddNodeToContainer=true;
                   }
                   else
                    newXml=xmlStart.replace("{nodeName}", clazz.getSimpleName().toString());
                   
                    newXml=newXml.replace("{nodeFullName}", clazz.getQualifiedName().toString());
                    newXml=newXml.replace("{nodeType}", annotation.nodeType().toString());
                    newXml=newXml.replace("{nodeDisablePersistence}", String.valueOf(annotation.nodeDisablePersistence()));
                    
                            
                    
                     pw.println(newXml);
                     
                     
                      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                   "File:"+clazz.getSimpleName().toString()+".xml"+ ", generate HPE Service Activator Node Param Elem ***" + e.getSimpleName());
              
                       System.out.println("annotation.nodeParamType() is Empty?:"+annotation.nodeParamType().Empty);
                       
                     //if(annotation.nodeParamType().Empty.equals(HPESANodeAttributes.NodeParamType.Empty))
                     if(annotation.nodeParamType().equals(HPESANodeAttributes.NodeParamType.Empty))
                     {
                         if(annotation.nodeParamName().isEmpty())
                         ParamNodeItem=ParamNodeEmpty.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", e.getSimpleName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                         else
                         ParamNodeItem=ParamNodeEmpty.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", annotation.nodeParamName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                     
                     }else
                     {
                        if(annotation.nodeParamName().isEmpty())
                        ParamNodeItem=ParamNode.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", e.getSimpleName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                        else
                        ParamNodeItem=ParamNode.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", annotation.nodeParamName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                     }
                       
                     pw.println("");
                     pw.println(ParamNodeItem);
                    
                     hasAnn=true;
                     
                     
                    if(!skipAddNodeToContainer)
                     {
                         File file = new File(f.toUri());
                         String absolutePath = file.getParentFile().getAbsolutePath();

                         System.out.println("absolutePath:"+absolutePath);
                         
                         foldercontainer=absolutePath+File.separator;
                         
                         
                         System.out.println("foldercontainer:"+foldercontainer);
                          
                      //workflownodes
                        CreateIfNotExists();
                        groupName=extractNodeName(clazz.getSimpleName().toString());
                        groupNameDescription=groupName;//clazz.getSimpleName().toString();
                        groupNameImageDefault=String.valueOf(annotation.nodeImageParamType())+".gif";//"Database.gif";//from annotation

                        nodeFullName=clazz.getQualifiedName().toString();
                        nodeNameImageDefault=String.valueOf(annotation.nodeImageParamType())+".gif";

                        System.out.println("groupName:"+groupName);
                        
                        System.out.println("nodeFullName:"+nodeFullName);
                          
                        
                           
                         if(!ExistsGroupNameInXml(groupName))
                            CreateGroupInRoot(groupName, groupNameDescription, groupNameImageDefault);

                         if(!ExistsNodeNameInXml(nodeFullName))
                            CreateNodeInGroup(groupName, nodeFullName, nodeNameImageDefault);
                     }else
                     {
                        System.out.println("skipped ParamNodeItem:"+ParamNodeItem); 
                     }
                     
                    continue;
                 }
                
                
                 processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                   "File:"+clazz.getSimpleName().toString()+".xml"+ ", gGenerate HPE Service Activator Node Param Elem ***" + e.getSimpleName());
              
                 System.out.println("annotation.nodeParamType() is Empty?:"+annotation.nodeParamType());
                       
                 if(annotation.nodeParamType().equals(HPESANodeAttributes.NodeParamType.Empty))
                     {
                         
                         if(annotation.nodeParamName().isEmpty())
                        ParamNodeItem=ParamNodeEmpty.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", e.getSimpleName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                        else
                        ParamNodeItem=ParamNodeEmpty.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", annotation.nodeParamName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                    
                     }
                 else 
                 {
                     if(annotation.nodeParamName().isEmpty())
                    ParamNodeItem=ParamNode.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", e.getSimpleName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                    else
                    ParamNodeItem=ParamNode.replace("{nodeParamMultiple}", (String.valueOf(annotation.nodeParamMultiple()))).replace("{nodeRequired}", (String.valueOf(annotation.nodeParamRequired()))).replace("{paramName}", annotation.nodeParamName()).replace("{paramDescription}", annotation.nodeParamDescription()).replace("{nodeParamType}", annotation.nodeParamType().toString());
                    
                 }
                   
                        
                     pw.println("");
                     pw.println(ParamNodeItem);
                     
                   
//                     if(!skipAddNodeToContainer)
//                     {
//                         File file = new File(f.toUri());
//                         String absolutePath = file.getParentFile().getAbsolutePath();
//
//                         System.out.println("absolutePath:"+absolutePath);
//                         
//                         foldercontainer=absolutePath+File.separator;
//                         
//                         
//                         System.out.println("foldercontainer:"+foldercontainer);
//                          
//                      //workflownodes
//                        CreateIfNotExists();
//                        groupName=extractNodeName(clazz.getSimpleName().toString());
//                        groupNameDescription=groupName;//clazz.getSimpleName().toString();
//                        groupNameImageDefault=String.valueOf(annotation.nodeImageParamType())+".gif";//"Database.gif";//from annotation
//
//                        nodeFullName=clazz.getQualifiedName().toString();
//                        nodeNameImageDefault=String.valueOf(annotation.nodeImageParamType())+".gif";
//
//                        System.out.println("groupName:"+groupName);
//                        
//                        System.out.println("nodeFullName:"+nodeFullName);
//                          
//                        
//                           
//                         if(!ExistsGroupNameInXml(groupName))
//                            CreateGroupInRoot(groupName, groupNameDescription, groupNameImageDefault);
//
//                         if(!ExistsNodeNameInXml(nodeFullName))
//                            CreateNodeInGroup(groupName, nodeFullName, nodeNameImageDefault);
//                     }else
//                     {
//                        System.out.println("skipped ParamNodeItem:"+ParamNodeItem); 
//                     }
                     
                     
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        x.toString());
            }
        }
        
        
        if(hasAnn)
        {
            try{
                 pw.println("");
                 pw.println(xmlEnd);
                 
                  pw.flush();
                } finally {
                try {
                    w.close();
                } catch (IOException ex) {
                    Logger.getLogger(HPESANodeAttributesProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
        }
        return true;
    }

    private static String capitalize(String name) {
        char[] c = name.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }
    
    private static String foldercontainer="";
    private static String buildFileName="workflowNodeGroups.xml";
    public static String BuildFileName()
     {
         return foldercontainer+buildFileName;
     }
    
   
    
    public static String BuildFileNameDtd()
     {
         return "workflowNodeGroups.dtd";
     }
      
    public static void WriteFile(String xml)
    {
        
       BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter( BuildFileName() ));
            writer.write( xml);
                
                

        }
        catch ( IOException e)
        {
        }
        finally
        {
            try
            {
                if ( writer != null)
                writer.close( );
            }
            catch ( IOException e)
            {
            }
        }

                
    }
    
    public static void CreateDtdIfNotExists()
    {
        
        
        String xmlDtd="<!-- The root for workflow node groups definitions -->\n" +
"<!ELEMENT WorkflowNodeGroups (Group*)>\n" +
"\n" +
"<!ELEMENT Group (Name, Image?, Description?, Node*)>\n" +
"\n" +
"<!ELEMENT Name            (#PCDATA)>\n" +
"<!ELEMENT Image           (#PCDATA)>\n" +
"<!ELEMENT Description     (#PCDATA)>\n" +
"<!ELEMENT Node            (ClassName, Image?)>\n" +
"<!ELEMENT ClassName       (#PCDATA)>";
        
        
         BufferedWriter writer = null;
        try
        {
            
            File f = new File(BuildFileNameDtd());
            if(!f.exists() && !f.isDirectory()) { 
                // do something

            writer = new BufferedWriter( new FileWriter( BuildFileNameDtd()));
            
            
             writer.write( xmlDtd);
             
             }

        }
        catch ( IOException e)
        {
        }
        finally
        {
            try
            {
                if ( writer != null)
                writer.close( );
            }
            catch ( IOException e)
            {
            }
        }

        

    }
    
    public static void CreateIfNotExists()
    {
        String workflowNodeGroupsStartOld="<?xml version='1.0' encoding='utf-8'?>\n" +
"<!DOCTYPE WorkflowNodeGroups SYSTEM 'workflowNodeGroups.dtd'>\n" +
"<!-- (c) Copyright 2010 Hewlett-Packard Development Company, L.P. -->\n" +
"<WorkflowNodeGroups>";
        
         String workflowNodeGroupsStart="<?xml version='1.0' encoding='utf-8'?>\n" +
"<!-- (c) Copyright 2010 Hewlett-Packard Development Company, L.P. -->\n" +
"<WorkflowNodeGroups>";
         
        String groupStart="<Group>\n" +
"    <Name>{groupName}</Name>\n" +
"    <Image>{defaulImageGroupFileGif}.gif</Image>\n" +
"    <Description>{groupDescription}</Description>";
        
        String nodeStart="<Node>\n" +
"      <ClassName>{nodeFullName}</ClassName>\n" +
"      <Image>{imageGroupFileGif}.gif</Image>\n" +
"    </Node>";
        
 
        
        String groupEnd="</Group>";
        
       String workflowNodeGroupsEnd=" </WorkflowNodeGroups>";
       
       
       BufferedWriter writer = null;
        try
        {
            
            File f = new File(BuildFileName());
            if(!f.exists() && !f.isDirectory()) { 
                // do something
            

            writer = new BufferedWriter( new FileWriter( BuildFileName()));
            writer.write( workflowNodeGroupsStart);
                
//                writer.write( groupStart);
//                    
//                writer.write( groupEnd);
            
             writer.write( workflowNodeGroupsEnd);
             
             }

        }
        catch ( IOException e)
        {
        }
        finally
        {
            try
            {
                if ( writer != null)
                writer.close( );
            }
            catch ( IOException e)
            {
            }
        }

                
    }
    
  public static boolean ExistsGroupNameInXml(String groupName)
    {
         boolean retValue=false;
         try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(new File(BuildFileName()));

            XPathFactory xFactory = XPathFactory.newInstance();
            XPath xPath = xFactory.newXPath();
            XPathExpression exp = xPath.compile("//Group/Name[text()='"+groupName+"']");

            NodeList nl = (NodeList)exp.evaluate(doc.getFirstChild(), XPathConstants.NODESET);//getFirstChild getChildNodes
            for (int index = 0; index < nl.getLength(); index++) {

                Node node = nl.item(index);
                System.out.println(node.getNodeName());//.getTextContent());
                retValue=true;
                break;
            }


        } catch (Exception ex) {
           System.out.println(ex);
        }

         return retValue;
    }
    
    
     
  public static boolean ExistsNodeNameInXml(String nodeName)
    {
         boolean retValue=false;
         try {

            
             //CreateIfNotExists();
             
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(new File(BuildFileName()));

            XPathFactory xFactory = XPathFactory.newInstance();
            XPath xPath = xFactory.newXPath();
            XPathExpression exp = xPath.compile("//Node/ClassName[text()='"+nodeName+"']");

            NodeList nl = (NodeList)exp.evaluate(doc.getFirstChild(), XPathConstants.NODESET);//getFirstChild getChildNodes
            for (int index = 0; index < nl.getLength(); index++) {

                Node node = nl.item(index);
                System.out.println(node.getNodeName());//.getTextContent());
                retValue=true;
                break;
            }


        } catch (Exception ex) {
           System.out.println(ex);
        }

         return retValue;
    }
    
     public static void CreateNodeInGroup(
             String groupName,
             String nodeFullName,
             String nodeImageFileGif)
    {
        
         try {

             //CreateIfNotExists();
             
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(new File(BuildFileName()));

            
            
            
             org.w3c.dom.Element newelement = doc.createElement("Node");
                     org.w3c.dom.Element newelementClassName = doc.createElement("ClassName");
                             org.w3c.dom.Element newelementImage = doc.createElement("Image");

                           newelementClassName.appendChild(doc.createTextNode(nodeFullName));
                           
                           if(nodeImageFileGif!=null && !nodeImageFileGif.isEmpty())
                           newelementImage.appendChild(doc.createTextNode(nodeImageFileGif));
                           
                             newelement.appendChild(newelementClassName);
                        newelement.appendChild(newelementImage);
                     System.out.println("New Attribute Created");
                     
                     
             //string xx = @"//tr[.//td[1]/span[contains(text(), '{2}')]
            XPathFactory xFactory = XPathFactory.newInstance();
            XPath xPath = xFactory.newXPath();
            XPathExpression exp = xPath.compile("//Group[.//Name[text()='"+groupName+"']]");

            NodeList nl = (NodeList)exp.evaluate(doc.getFirstChild(), XPathConstants.NODESET);//getFirstChild getChildNodes
            for (int index = 0; index < nl.getLength(); index++) {

                Node node = nl.item(index);
                System.out.println(node.getNodeName());//.getTextContent());

                node.appendChild(newelement);
                      
                break;
                     
            }

            //WriteFile(doc.);
            // write the content into xml file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(BuildFileName()));

    // Output to console for testing
    // StreamResult result = new StreamResult(System.out);

    transformer.transform(source, result);

    System.out.println("File saved!");
                

        } catch (Exception ex) {
           System.out.println(ex);
        }

    }
    
        public static void CreateGroupInRoot(
                String groupName, 
            String groupDescription,
            String groupImageDefault)
    {
        
         try {

             //CreateIfNotExists();
             
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(new File(BuildFileName()));

            
            
            
             org.w3c.dom.Element newelement = doc.createElement("Group");
                org.w3c.dom.Element newelementname = doc.createElement("Name");
                     org.w3c.dom.Element newelementDescription =doc.createElement("Description");
                             org.w3c.dom.Element newelementImage = doc.createElement("Image");

                                    newelementname.appendChild(doc.createTextNode(groupName));
                                  newelementDescription.appendChild(doc.createTextNode(groupDescription));
                                  
                                  if(groupImageDefault!=null && !groupImageDefault.isEmpty())
                                 newelementImage.appendChild(doc.createTextNode(groupImageDefault));
                                
                                 newelement.appendChild(newelementname);
                             newelement.appendChild(newelementDescription);
                        newelement.appendChild(newelementImage);
                     
                     System.out.println("New Attribute Created");
                     
                     
             //string xx = @"//tr[.//td[1]/span[contains(text(), '{2}')]
            XPathFactory xFactory = XPathFactory.newInstance();
            XPath xPath = xFactory.newXPath();
            XPathExpression exp = xPath.compile("//WorkflowNodeGroups");

           
            NodeList nl = (NodeList)exp.evaluate(doc.getFirstChild(), XPathConstants.NODESET);//getFirstChild getChildNodes
            for (int index = 0; index < nl.getLength(); index++) {

                Node node = nl.item(index);
                System.out.println(node.getNodeName());//.getTextContent());
                
                
              
                      node.appendChild(newelement);
                      
                   break;
                     
            }
            
            //WriteFile(doc.);
            // write the content into xml file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new File(BuildFileName()));

    // Output to console for testing
    // StreamResult result = new StreamResult(System.out);

    transformer.transform(source, result);

    System.out.println("File saved!");
                

        } catch (Exception ex) {
           System.out.println(ex);
        }

    }
        
        public static String extractNodeName(String name)
    {
        //String result="NodeICTCompanyDataCreate";
        
        name=name.replace("Node", "");
        
        String result="";
        
        for (String w : name.split("(?<=[a-z])(?=[A-Z])")) {
        System.out.println("w:"+w);
        if(w.chars().count()>8)
        {
             result=w;
             break;
        }
        else if((result+w).chars().count()>8)
        {
             result=result+w;
             break;
        }
        else
        result=result+w;
        
      
        }
        
        System.out.println("result:"+result);
        return result.replace("Node", "");
    }
    
        
    
}

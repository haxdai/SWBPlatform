/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software, todo
 * ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * de SemanticWebBuilder 4.0.
 *
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 *
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica: http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.codegen;

import org.semanticwb.Logger;
import org.semanticwb.SWBPlatform;
import org.semanticwb.SWBUtils;
import org.semanticwb.platform.SemanticClass;
import org.semanticwb.platform.SemanticMgr;
import org.semanticwb.platform.SemanticObject;
import org.semanticwb.platform.SemanticProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class CodeGenerator {
    /** The Constant SEMANTIC_ITERATOR_FULL_NAME. */
    private static final String SEMANTIC_ITERATOR_FULL_NAME="org.semanticwb.platform.SemanticIterator";

    /** The Constant MODEL_FULL_NAME. */
    private static final String MODEL_FULL_NAME = "org.semanticwb.platform.SemanticModel";

    /** The Constant SEMANTIC_MANANGER_FULL_NAME. */
    private static final String SEMANTIC_MANANGER_FULL_NAME = "org.semanticwb.platform.SemanticMgr";

    /** The Constant SEMANTIC_MODEL_FULL_NAME. */
    private static final String SEMANTIC_MODEL_FULL_NAME = "org.semanticwb.model.SWBModel";

    /** The Constant SEMANTIC_PROPERTY_FULL_NAME. */
    private static final String SEMANTIC_PROPERTY_FULL_NAME = "org.semanticwb.platform.SemanticProperty";

    /** The Constant GET_SEMANTIC_CLASS. */
    private static final String GET_SEMANTIC_CLASS = ".getSemanticMgr().getVocabulary().getSemanticClass(\"";

    /** The Constant SEMANTIC_PLATFORM_FULL_NAME. */
    private static final String SEMANTIC_PLATFORM_FULL_NAME="org.semanticwb.SWBPlatform";

    /** The Constant GENERIC_ITERATOR_FULL_NAME. */
    private static final String GENERIC_ITERATOR_FULL_NAME="org.semanticwb.model.GenericIterator";

    /** The Constant SEMANTIC_LITERAL_FULL_NAME. */
    private static final String SEMANTIC_LITERAL_FULL_NAME="org.semanticwb.platform.SemanticLiteral";

    /** The Constant UTIL_ITERATOR_FULL_NAME. */
    private static final String UTIL_ITERATOR_FULL_NAME="java.util.Iterator";

    /** The Constant GENERIC_OBJECT_FULL_NAME. */
    private static final String GENERIC_OBJECT_FULL_NAME="org.semanticwb.model.GenericObject";

    /** The Constant JENA_ITERATOR_FULL_NAME. */
    private static final String JENA_ITERATOR_FULL_NAME="com.hp.hpl.jena.rdf.model.StmtIterator";

    /** The Constant SEMANTIC_OBJECT_FULL_NAME. */
    private static final String SEMANTIC_OBJECT_FULL_NAME="org.semanticwb.platform.SemanticObject";

    /** The Constant SEMANTIC_CLASS_FULL_NAME. */
    private static final String SEMANTIC_CLASS_FULL_NAME="org.semanticwb.platform.SemanticClass";

    /** The Constant GLOBAL_CLASS_NAME. */
    private static final String GLOBAL_CLASS_NAME = "ClassMgr";
    /** The Constant CLOSE_BLOCK. */
    private static final String CLOSE_BLOCK = "    }";
    /** The Constant ENTER. */
    private static final String ENTER = "\r\n";
    /** The Constant OPEN_BLOCK. */
    private static final String OPEN_BLOCK = "    {";
    /** The Constant PUBLIC. */
    private static final String PUBLIC = "    public ";
    /** The Constant TYPE_BOOLEAN. */
    private static final String TYPE_BOOLEAN = "boolean";
    /** The Constant TYPE_BYTE. */
    private static final String TYPE_BYTE = "byte";
    /** The Constant TYPE_DATE_TIME. */
    private static final String TYPE_DATE_TIME = "dateTime";
    /** The Constant TYPE_BINARY. */
    private static final String TYPE_BINARY = "base64Binary";
    /** The Constant TYPE_DATE. */
    private static final String TYPE_DATE = "date";
    /** The Constant TYPE_DOUBLE. */
    private static final String TYPE_DOUBLE = "double";
    /** The Constant TYPE_FLOAT. */
    private static final String TYPE_FLOAT = "float";
    /** The Constant TYPE_INT. */
    private static final String TYPE_INT = "int";
    /** The Constant TYPE_LONG. */
    private static final String TYPE_LONG = "long";
    /** The Constant TYPE_SHORT. */
    private static final String TYPE_SHORT = "short";
    /** The Constant TYPE_VOID. */
    private static final String TYPE_VOID = "void";
    /** The Constant TYPE_STRING. */
    private static final String TYPE_STRING = "String";
    /** The Constant TYPE_JAVADATE. */
    private static final String TYPE_JAVADATE = "java.util.Date";
    /** The Constant TYPE_JAVAINPUTSTREAM. */
    private static final String TYPE_JAVAINPUTSTREAM = "java.io.InputStream";
    /** The log. */
    private static Logger log = SWBUtils.getLogger(SemanticObject.class);
    /** Flag to set generation of virtual classes from ontology definition */
    private boolean generateVirtualClasses=false;

    /**
     * Instantiates a new code generator.
     */
    public CodeGenerator() {

    }

    /**
     * Tells the {@CodeGenerator} to output source code for virtual (compiled in memory) classes.
     * @param generateVirtualClasses
     */
    public void setGenerateVirtualClasses(boolean generateVirtualClasses) {
        this.generateVirtualClasses = generateVirtualClasses;
    }

    /**
     * Gets the flag for virtual class generation from the {@CodeGenerator}.
     * @return true if the {@CodeGenerator} is set to output source code from virtual (compiled in memory) classes, false otherwise.
     */
    public boolean isGenerateVirtualClasses() {
        return generateVirtualClasses;
    }

    /**
     * Creates the package folders in file system.
     *
     * @param spackage the package full namespace
     * @param pDirectory the directory where to output surce code for the package
     * @return the file
     * @throws CodeGeneratorException the code generator exception
     */
    private File createPackage(String spackage, File pDirectory) throws CodeGeneratorException {
        File createPackage = pDirectory;
        if (spackage != null && !spackage.isEmpty()) {
            File dir = new File(pDirectory.getPath() +
                    File.separatorChar + spackage.replace('.', File.separatorChar));

            if (!dir.exists() && !dir.mkdirs()) {
                throw new CodeGeneratorException("The directory " + dir.getPath() + " was not possible to create");
            }
            createPackage = dir;
        }
        return createPackage;
    }

    /**
     * Writes source code to a file.
     *
     * @param file the target {@File}.
     * @param code Source code to write.
     * @throws CodeGeneratorException the code generator exception
     */
    private void saveFile(File file, String code) throws CodeGeneratorException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(code.getBytes());
        } catch (IOException ioe) {
            throw new CodeGeneratorException("The File " + file.getPath() + " was not possible to create", ioe);
        }
    }

    /**
     * Generate code by namespace.
     *
     * @param namespace the namespace
     * @param createSWBcontent whether to create a SWBContext
     * @param pDirectory the souce code output directory.
     * @throws CodeGeneratorException the code generator exception
     */
    public void generateCodeByNamespace(String namespace, boolean createSWBcontent, File pDirectory) throws CodeGeneratorException {
        String prefix = null;
        SemanticMgr mgr = SWBPlatform.getSemanticMgr();
        Iterator<SemanticClass> tpcit = mgr.getVocabulary().listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            boolean create = false;
            if (namespace == null) {
                create = true;
            } else {
                if (tpc.getURI() != null) {
                    String strnamespace = namespace;
                    if (!strnamespace.endsWith("#")) {
                        strnamespace += "#";
                    }
                    strnamespace += tpc.getName();
                    if (tpc.getURI().equals(strnamespace)) {
                        prefix = tpc.getPrefix();
                        create = true;
                    }
                }
            }
            if (create) {
                if (pDirectory.exists() && pDirectory.isFile()) {
                    throw new CodeGeneratorException("The path " + pDirectory.getPath() + " is not a directory");
                }
                if (!pDirectory.exists() && !pDirectory.mkdirs()) {
                    throw new CodeGeneratorException("The path " + pDirectory.getPath() + " was not possible to create");
                }
                createElementCode(tpc, pDirectory);
            }
        }
        if (createSWBcontent && prefix != null) {
            createSWBContextBase(prefix, pDirectory);
            createSWBContext(prefix, pDirectory);
        }
    }

    /**
     * Checks whether a class definition in the input ontology meets the conditions to generate source code.
     * Throws an exception if one of the conditions is not met.
     *
     * @param sclass the {@SemanticClass} wrapper object from the ontology class definition.
     * @throws CodeGeneratorException the code generator exception
     */
    private void checkClass(SemanticClass sclass) throws CodeGeneratorException {
        if(sclass.getCodePackage() == null) {
            throw new CodeGeneratorException("The code package for Semantic Class " +
                    sclass.getURI() + " is not defined\r\n");
        }

        Iterator<SemanticClass> tpcit = sclass.listSuperClasses(true);
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            if (sclass.isSWBInterface() && tpc.isSWBClass()) {
                throw new CodeGeneratorException("The Semantic Class " + sclass.getURI() +
                        " is an interface and its parent is a class\r\n");
            }
        }

        Iterator<SemanticProperty> properties=sclass.listProperties();
        while(properties.hasNext()) {
            SemanticProperty tpp=properties.next();
            if(tpp.isObjectProperty()) {
                try {
                    if (tpp.isString() || tpp.isXML() || tpp.isInt() || tpp.isFloat() || tpp.isDouble() ||
                            tpp.isLong() || tpp.isByte() || tpp.isShort() || tpp.isBoolean() || tpp.isDateTime() ||
                            tpp.isDate()) {
                        throw new CodeGeneratorException("The property " + tpp + " for semantic class " +
                                sclass + " is defined as Object Property, but the type is "+ tpp.getRange() +" \r\n");
                    }
                } catch(Exception e) {
                    throw new CodeGeneratorException("The property " + tpp + " has an error\r\n",e);
                }
            }

            if(tpp.isDataTypeProperty() && !(tpp.isBinary() ||  tpp.isString() || tpp.isXML() || tpp.isInt() ||
                    tpp.isFloat() || tpp.isDouble() || tpp.isLong() || tpp.isByte() || tpp.isShort() ||
                    tpp.isBoolean() || tpp.isDateTime() || tpp.isDate())) {
                throw new CodeGeneratorException("The property " + tpp + " for Semantic Class " +
                        sclass + " is defined as DataType Property, but the type is "+ tpp.getRange() +" \r\n");
            }
        }
    }

    /**
     * Generates the source code from an input ontology definition.
     *
     * @param prefix the ontology prefix used to generate source code.
     * @param createSWBContext whether to create a SWBContext.
     * @param pDirectory the target directory for the source code.
     * @throws CodeGeneratorException the code generator exception
     */
    public void generateCode(String prefix, boolean createSWBContext, File pDirectory) throws CodeGeneratorException {
        SemanticMgr mgr = SWBPlatform.getSemanticMgr();
        Iterator<SemanticClass> tpcit = mgr.getVocabulary().listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            if (prefix == null || (prefix.equals(tpc.getPrefix()))) {
                checkClass(tpc);
            }
        }

        tpcit = mgr.getVocabulary().listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            boolean create = (prefix == null) ? true : prefix.equals(tpc.getPrefix());

            if (create) {
                if (pDirectory.exists() && pDirectory.isFile()) {
                    throw new CodeGeneratorException("The path " + pDirectory.getPath() + " is not a directory");
                }
                if (!pDirectory.exists() && !pDirectory.mkdirs()) {
                    throw new CodeGeneratorException("The path " + pDirectory.getPath() + " was not possible to create");
                }
                createElementCode(tpc, pDirectory);
            }
        }

        if (createSWBContext) {
            createSWBContextBase(prefix, pDirectory);
            createSWBContext(prefix, pDirectory);
        }
    }

    /**
     * Gets the {@SemanticClass}es that a {@SemanticClass} inherits, according to the ontology definition.
     *
     * @param tpc the {@SemanticClass} wrapper for a class definition in the input ontology.
     * @return the interfaces
     */
    private HashSet<SemanticClass> getInterfaces(SemanticClass tpc) {
        HashSet<SemanticClass> interfaces = new HashSet<>();
        Iterator<SemanticClass> it = tpc.listSuperClasses();
        while (it.hasNext()) {
            SemanticClass sclass = it.next();
            if (sclass.isSWBInterface()) {
                interfaces.add(sclass);
            }
        }
        return interfaces;
    }

    /**
     * Gets a string with the {@SemanticClass}es that a {@SemanticClass} inherits, according to the ontology definition.
     *
     * @param tpc the {@SemanticClass} wrapper for a class definition in the input ontology.
     * @param isextends whether to prefix the result string with "extends". Defaults to "implements".
     * @return the interfaces as a string
     */
    private String  getInterfacesAsString(SemanticClass tpc, boolean isextends) {
        StringBuilder interfaces = new StringBuilder();
        Iterator<SemanticClass> it = tpc.listSuperClasses();
        while (it.hasNext()) {
            SemanticClass clazz = it.next();
            if (clazz.isSWBInterface()) {
                interfaces.append(clazz.getCanonicalName()).append(",");
            }
        }

        if (interfaces.length() > 0) {
            if (isextends) {
                interfaces.insert(0, "extends ");
            } else {
                interfaces.insert(0, "implements ");
            }
            interfaces.deleteCharAt(interfaces.length() - 1);
        }
        return interfaces.toString();
    }

    /**
     * Creates the SWBContext class.
     *
     * @param prefix the ontology prefix to use in code generation.
     * @param pDirectory the target directory for source code.
     * @throws CodeGeneratorException the code generator exception
     */
    private void createSWBContext(String prefix, File pDirectory) throws CodeGeneratorException {
        // si existe no debe reemplazarlo
        String SWBContextClsName = "SWBContext";
        SemanticMgr mgr = SWBPlatform.getSemanticMgr();
        String spackage = mgr.getCodePackage().getPackage(prefix);
        File dir = createPackage(spackage, pDirectory);
        dir = new File(dir.getPath() + File.separatorChar);
        File fileClass = new File(dir.getPath() + File.separatorChar + SWBContextClsName + ".java");
        if (!fileClass.exists()) {
            StringBuilder javaClassContent = new StringBuilder();
            if (null != spackage && !spackage.isEmpty()) {
                javaClassContent.append("package ").append(spackage).append(";").append(ENTER);
                javaClassContent.append(ENTER);
            }
            javaClassContent.append("import org.semanticwb.SWBUtils;").append(ENTER);
            javaClassContent.append("import org.semanticwb.model.base.SWBContextBase;").append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(0,
                    "Singleton that holds methods to get contextual information for a SWBPortal instance."));

            javaClassContent.append("public class SWBContext extends SWBContextBase").append("{").append(ENTER);

            javaClassContent
                    .append("    private static org.semanticwb.Logger log = SWBUtils.getLogger(SWBContext.class);")
                    .append(ENTER);

            javaClassContent.append("    private static SWBContext instance = null;").append(ENTER).append(ENTER)   ;


            javaClassContent.append(buildJavaDocBlock(1,
                    "Gets the instance of {@link SWBContext}.",
                    "@return {@link SWBContext} instance."));
            javaClassContent.append(buildMethodDefinition("public static synchronized",
                    SWBContextClsName,
                    "createInstance",
                    null,
                    null,
                    1,
                    "if (instance == null) {",
                    "    instance = new SWBContext();",
                    "}",
                    "return instance;"
            ));
            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(1,
                    "Constructor."));
            javaClassContent.append(buildMethodDefinition("private",
                    null,
                    SWBContextClsName,
                    null,
                    null,
                    1,
                    "log.event(\"Initializing SemanticWebBuilder Context...\");"));

            javaClassContent.append(ENTER);

            javaClassContent.append("}");
            saveFile(fileClass, javaClassContent.toString());
        }
    }

    /**
     * Creates the SWBContextBase class.
     *
     * @param prefix the ontology prefix to use in code generation.
     * @param pDirectory the output directory for the source code.
     * @throws CodeGeneratorException the code generator exception
     */
    private void createSWBContextBase(String prefix, File pDirectory) throws CodeGeneratorException
    {
        // Debe reemplazarlo siempre
        SemanticMgr mgr = SWBPlatform.getSemanticMgr();
        String spackage = mgr.getCodePackage().getPackage(prefix);
        String ClsName = "SWBContextBase";
        File dir = createPackage(spackage, pDirectory);
        dir = new File(dir.getPath() + File.separatorChar + "base");
        StringBuilder javaClassContent = new StringBuilder();
        File fileClass = new File(dir.getPath() + File.separatorChar + ClsName + ".java");
        if (null != spackage && !spackage.isEmpty()) {
            javaClassContent.append("package ").append(spackage).append(".base;").append(ENTER);
            javaClassContent.append(ENTER);
        }
        javaClassContent.append("import ").append(SEMANTIC_CLASS_FULL_NAME).append(";").append(ENTER);


        javaClassContent.append(buildJavaDocBlock(0,
                "Class definition for the " + ClsName + " object."));
        javaClassContent.append("public class SWBContextBase").append(" {").append(ENTER);

        javaClassContent.append("    private static ").append(SEMANTIC_MANANGER_FULL_NAME).append(" mgr = ")
                .append(SEMANTIC_PLATFORM_FULL_NAME).append(".getSemanticMgr();").append(ENTER);

        Iterator<SemanticClass> tpcit = mgr.getVocabulary().listSemanticClasses();
        //Define class fields
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            if (tpc.isSWBModel()) {
                javaClassContent.append(buildJavaDocBlock(1,
                        "The SemanticClass associated to the " + tpc.getUpperClassName() + "."));

                javaClassContent.append("    public static final SemanticClass ").append(tpc.getPrefix()).append("_")
                        .append(tpc.getUpperClassName()).append(" = ").append(SEMANTIC_PLATFORM_FULL_NAME)
                        .append(GET_SEMANTIC_CLASS).append(tpc.getURI()).append("\");").append(ENTER);
                javaClassContent.append(ENTER);
            }
        }

        //Define class methods
        tpcit = mgr.getVocabulary().listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();

            if (tpc.isSWBModel()) {

                javaClassContent.append(buildJavaDocBlock(1,
                        "Gets a " + tpc.getCanonicalName() + " with a given id.",
                        "@param id The id of the " + tpc.getCanonicalName() + " to get.",
                        "@return the " + tpc.getCanonicalName()));
                javaClassContent.append(buildMethodDefinition("public static",
                        tpc.getCanonicalName(),
                        "get"+tpc.getUpperClassName(),
                        "String id",
                        null,
                        1,
                        "return " + tpc.getCanonicalName() + "." + GLOBAL_CLASS_NAME + ".get" +
                                tpc.getUpperClassName() + "(id);"));
                javaClassContent.append(ENTER).append(ENTER);


                javaClassContent.append(buildJavaDocBlock(1,
                        "List all " + tpc.getNameInPlural(),
                        "@return Iterator to all " + tpc.getNameInPlural()));
                javaClassContent.append(buildMethodDefinition("public static",
                        UTIL_ITERATOR_FULL_NAME + "<" + tpc.getCanonicalName() + ">",
                        "list" + tpc.getNameInPlural(),
                        null,
                        null,
                        1,
                        "return (" + UTIL_ITERATOR_FULL_NAME + "<" + tpc.getCanonicalName() + ">)" +
                                tpc.getPrefix() + "_" + tpc.getUpperClassName() + ".listGenericInstances();"));
                javaClassContent.append(ENTER).append(ENTER);


                javaClassContent.append(buildJavaDocBlock(1,
                        "Removes a " + tpc.getCanonicalName() + " with a given id.",
                        "@param id The id of the " + tpc.getCanonicalName() + " to remove."));

                javaClassContent.append(buildMethodDefinition("public static",
                        TYPE_VOID,
                        "remove" + tpc.getUpperClassName(),
                        "String id",
                        null,
                        1,
                        tpc.getCanonicalName() + "." + GLOBAL_CLASS_NAME + ".remove" + tpc.getUpperClassName() +
                                "(id);"));
                javaClassContent.append(ENTER).append(ENTER);

                javaClassContent.append(buildJavaDocBlock(1,
                        "Creates a new " + tpc.getCanonicalName() + " with the given id on the given namespace.",
                        "@param id Identifier for the new " + tpc.getCanonicalName(),
                        "@param namespace Namespace for the new " + tpc.getCanonicalName()));


                javaClassContent.append(buildMethodDefinition("public static",
                        tpc.getCanonicalName(),
                        "create" + tpc.getUpperClassName(),
                        "String id, String namespace",
                        null,
                        1,
                        "return " + tpc.getCanonicalName() + "." + GLOBAL_CLASS_NAME + ".create" +
                                tpc.getUpperClassName() + "(id, namespace);"));

                javaClassContent.append(ENTER);
            }
        }
        javaClassContent.append("}" + ENTER);

        saveFile(fileClass, javaClassContent.toString());
    }

    /**
     * Creates a SemanticResource source code.
     *
     * @param tpc the {@SemanticClass} wrapper for the {@SemanticResource} definition in the input ontology.
     * @param pDirectory the target directory for the source code.
     * @throws CodeGeneratorException the code generator exception
     */
    private void createSemanticResource(SemanticClass tpc, File pDirectory) throws CodeGeneratorException {
        File dir = createPackage(tpc.getCodePackage(), pDirectory);
        File fileClass = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + ".java");
        File fileClass2 = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + ".groovy");
        if (!fileClass.exists() && !fileClass2.exists()) {
            StringBuilder javaClassContent = new StringBuilder();
            if (tpc.getCodePackage()!=null && !tpc.getCodePackage().equals("")) {
                javaClassContent.append("package ").append(tpc.getCodePackage()).append(";" + ENTER);
            }
            javaClassContent.append(ENTER);
            javaClassContent.append("import org.semanticwb.portal.api.SWBParamRequest;" + ENTER);
            javaClassContent.append("import org.semanticwb.portal.api.SWBResourceException;" + ENTER);
            javaClassContent.append("import javax.servlet.http.HttpServletRequest;" + ENTER);
            javaClassContent.append("import javax.servlet.http.HttpServletResponse;" + ENTER);
            javaClassContent.append("import java.io.IOException;" + ENTER);
            javaClassContent.append("import java.io.PrintWriter;").append(ENTER).append(ENTER);

            javaClassContent.append("public class ").append(tpc.getUpperClassName()).append(" extends ")
                    .append(tpc.getCodePackage()).append(".base.").append(tpc.getUpperClassName())
                    .append("Base ").append(" {" + ENTER);

            javaClassContent.append(buildJavaDocBlock(1,
                    "Constructor.",
                    "Creates a new instance of " + tpc.getUpperClassName()));

            javaClassContent.append(buildMethodDefinition("public",
                    null,
                    tpc.getUpperClassName(),
                    null,
                    null,
                    1,
                    ""));

            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(1, "Constructor.",
                    "Creates a new instance of "+tpc.getUpperClassName()+ " with a SemanticObject as base for creation",
                    "@param base The SemanticObject with the properties for the "+tpc.getUpperClassName()));

            javaClassContent.append(buildMethodDefinition("public",
                    null,
                    tpc.getUpperClassName(),
                    SEMANTIC_OBJECT_FULL_NAME + " base",
                    null,
                    1,
                    "super(base);"));

            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append("    @Override" + ENTER);
            javaClassContent.append(buildMethodDefinition("public",
                    TYPE_VOID,
                    "doView",
                    "HttpServletRequest request, HttpServletResponse response, SWBParamRequest paramRequest",
                    "SWBResourceException, IOException",
                    1,
                    "//TODO: Change method implementation",
                    "PrintWriter out = response.getWriter();",
                    "out.print(\"Hello " + tpc.getUpperClassName() + "\");"));
            javaClassContent.append(ENTER);
            javaClassContent.append("}" + ENTER);
            saveFile(fileClass, javaClassContent.toString());
        }
    }

    /**
     * Creates a SemanticResourceBase source code for a SemanticResource.
     *
     * @param tpc the {@SemanticClass} wrapper for the {@SemanticResource} definition in the input ontology.
     * @param pDirectory the target directory for the source code.
     * @throws CodeGeneratorException the code generator exception
     */
    private void createSemanticResourceBase(SemanticClass tpc, File pDirectory) throws CodeGeneratorException {
        String exts = "org.semanticwb.portal.api.GenericSemResource";
        File dir = createPackage(tpc.getCodePackage(), pDirectory);
        SemanticClass parent = null;
        Iterator<SemanticClass> it = tpc.listSuperClasses(true);
        while (it.hasNext()) {
            parent = it.next();
            if (parent.isSWBSemanticResource()) {
                exts = parent.getCanonicalName();
                break;
            }
        }
        dir = new File(dir.getPath() + File.separatorChar + "base");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        StringBuilder javaClassContent = new StringBuilder();
        if (tpc.getCodePackage() != null && !tpc.getCodePackage().equals("")) {
            javaClassContent.append("package ").append(tpc.getCodePackage()).append(".base;" + ENTER);
        }
        javaClassContent.append(ENTER);
        javaClassContent.append("public abstract class ").append(tpc.getUpperClassName()).append("Base extends ")
                .append(exts).append(" ").append(getInterfacesAsString(tpc, false))
                .append(" {").append(ENTER).append(ENTER);

        HashSet<SemanticClass> staticClasses = new HashSet<>();
        HashSet<SemanticProperty> staticProperties = new HashSet<>();
        Iterator<SemanticProperty> properties = tpc.listProperties();

        while (properties.hasNext()) {
            SemanticProperty tpp = properties.next();
            boolean isInClass = isPropertyInClass(tpp, tpc);

            if (!isInClass) {
                SemanticClass range = tpp.getRangeClass();
                if (range != null && !staticClasses.contains(range)) {
                    javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ")
                            .append(range.getPrefix()).append("_").append(range.getUpperClassName())
                            .append("=" + SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS)
                            .append(range.getURI()).append("\");" + ENTER);

                    staticClasses.add(range);
                }

                if (!staticProperties.contains(tpp)) {
                    javaClassContent.append("    public static final "+SEMANTIC_PROPERTY_FULL_NAME+" ")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName())
                            .append("="+SEMANTIC_PLATFORM_FULL_NAME +
                                    ".getSemanticMgr().getVocabulary().getSemanticProperty(\"")
                            .append(tpp.getURI()).append("\");" + ENTER);
                }
            }
        }

        if (!staticClasses.contains(tpc)) {
            javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ")
                    .append(tpc.getPrefix()).append("_").append(tpc.getUpperClassName())
                    .append("=" + SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS).append(tpc.getURI())
                    .append("\");" + ENTER);

            staticClasses.add(tpc);
        }

        javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " sclass = " +
                SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS).append(tpc.getURI()).append("\");" + ENTER);

        javaClassContent.append(ENTER);

        javaClassContent.append(buildJavaDocBlock(1,
                "Constructor.",
                "Creates a new instance of " + tpc.getUpperClassName()));

        javaClassContent.append(buildMethodDefinition("public",
                null,
                tpc.getUpperClassName() + "Base",
                null,
                null,
                1,
                ""));
        javaClassContent.append(ENTER).append(ENTER);

        javaClassContent.append(buildJavaDocBlock(1, "Constructor.",
                "Creates a new instance of "+tpc.getUpperClassName()+ "Base with a SemanticObject as base for creation",
                "@param base The SemanticObject with the properties for the "+tpc.getUpperClassName() + "Base"));

        javaClassContent.append(buildMethodDefinition("public",
                null,
                tpc.getUpperClassName() + "Base",
                SEMANTIC_OBJECT_FULL_NAME + " base",
                null,
                1,
                "super(base);"));
        javaClassContent.append(ENTER).append(ENTER);

        javaClassContent.append(buildJavaDocBlock(1, "@see java.lang.Object#hashCode()"));
        javaClassContent.append("    @Override" + ENTER);
        javaClassContent.append(buildMethodDefinition("public",
                TYPE_INT,
                "hashCode",
                null,
                null,
                1,
                "return getSemanticObject().hashCode();"
        ));
        javaClassContent.append(ENTER).append(ENTER);

        javaClassContent.append(buildJavaDocBlock(1, "@see java.lang.Object#equals(java.lang.Object)"));
        javaClassContent.append("    @Override" + ENTER);

        javaClassContent.append(buildMethodDefinition("public",
                TYPE_BOOLEAN,
                "equals",
                "Object obj",
                null,
                1,
                "return (obj != null && hashCode() == obj.hashCode());"));
        javaClassContent.append(ENTER).append(ENTER);

        insertPropertiesToClass(tpc, javaClassContent, null, "SemanticObject");

        javaClassContent.append("}" + ENTER);
        File fileClass = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + "Base.java");
        saveFile(fileClass, javaClassContent.toString());
        createSemanticResource(tpc, pDirectory);
    }

    /**
     * Builds a String for a JavaDoc code block.
     * @param indentLevel Indent level for the comment block.
     * @param comment One or more comment lines to be added to the block.
     * @return String with the JavaDoc code block.
     */
    private String buildJavaDocBlock(int indentLevel, String... comment) {
        if (null != comment && comment.length > 0) {
            String indentFormat = "%1$" + (indentLevel * 4) + "s";
            String indentSpaces = indentLevel > 0 ? String.format(indentFormat, "") : "";

            StringBuilder javadoc = new StringBuilder();
            javadoc.append(indentSpaces).append("/**" + ENTER);
            for (String c : comment) {
                javadoc.append(indentSpaces).append(" * ").append(c).append(" " + ENTER);
            }
            javadoc.append(indentSpaces).append(" */" + ENTER);
            return javadoc.toString();
        }

        return null;
    }

    /**
     * Buids a String for a Method declaration and body.
     * @param modifiers Method modifiers string
     * @param returnType Method return type
     * @param name Method name
     * @param params Method parameters list
     * @param throwsDefinition Method throws definition
     * @param indentLevel Indent level
     * @param body One or more code lines for method body
     * @return String with the method declaration and body.
     */
    public String buildMethodDefinition(String modifiers, String returnType, String name, String params, String throwsDefinition, int indentLevel, String... body) {
        StringBuilder sb = new StringBuilder();
        String indentFormat = "%1$" + (indentLevel * 4) + "s";
        String indentSpaces = indentLevel > 0 ? String.format(indentFormat, "") : "";

        sb.append(indentSpaces).append(modifiers).append(" ");
        if (null != returnType) {
            sb.append(returnType).append(" ");
        }

        sb.append(name+"(");
        if (null != params && !params.isEmpty()) {
            sb.append(params);
        }
        sb.append(") ");

        if (null != throwsDefinition && !throwsDefinition.isEmpty()) {
            sb.append("throws ").append(throwsDefinition+" ");
        }

        sb.append("{").append(ENTER);
        if (null != body && body.length > 0) {
            for (String bodyLine : body) {
                sb.append(indentSpaces).append("    ").append(bodyLine).append(ENTER);
            }
        }
        sb.append(indentSpaces).append("}");

        return sb.toString();
    }

    /**
     * Creates the source code for an object's base class.
     *
     * @param tpc the {@SemanticClass} wrapper for the object definition in the input ontology.
     * @param usesufix whether to use "base" suffix in code generation.
     * @return a string with the source code for the base class.
     * @throws CodeGeneratorException the code generator exception
     */
    public String createClassBase(SemanticClass tpc,boolean usesufix) throws CodeGeneratorException {
        String exts = "org.semanticwb.model.base.GenericObjectBase";
        if (tpc.isSWBFormElement()) {
            exts = "org.semanticwb.model.base.FormElementBase";
        }

        SemanticClass parent = null;
        Iterator<SemanticClass> it = tpc.listSuperClasses(true);
        while (it.hasNext()) {
            parent = it.next();
            //EHSP-31032016 - Check parent chain for virtual classes and add them to extends
            if (parent.isSWBClass() || parent.isSWBModel() || parent.isSWBFormElement() ||
                    (parent.isSWBVirtualClass() && isGenerateVirtualClasses())) {
                exts = parent.getCanonicalName();
                break;
            } else {
                parent = null;
            }
        }

        StringBuilder javaClassContent = new StringBuilder();
        if (tpc.getCodePackage()!=null && !tpc.getCodePackage().equals("")) {
            if(usesufix) {
                javaClassContent.append("package ").append(tpc.getCodePackage()).append(".base;" + ENTER);
            } else {
                javaClassContent.append("package ").append(tpc.getCodePackage()).append(";" + ENTER);
            }

            javaClassContent.append("" + ENTER);
        }
        javaClassContent.append(ENTER);

        if(tpc.getComment(null) != null) {
            String javadoc = buildJavaDocBlock(1, tpc.getComment(null));
            if (null != javadoc) {
                javaClassContent.append(javadoc);
            }
        }

        if(usesufix) {
            javaClassContent.append("public abstract class ").append(tpc.getUpperClassName()).append("Base extends ")
                    .append(exts).append(" ").append(getInterfacesAsString(tpc, false)).append("" + ENTER);
        } else {
            javaClassContent.append("public class ").append(tpc.getUpperClassName()).append(" extends ")
                    .append(exts).append(" ").append(getInterfacesAsString(tpc, false)).append("" + ENTER);
        }

        javaClassContent.append("{" + ENTER);
        HashSet<SemanticClass> staticClasses = new HashSet<>();
        HashSet<SemanticProperty> staticProperties = new HashSet<>();
        Iterator<SemanticProperty> properties = tpc.listProperties();
        HashSet<SemanticClass> interfaces = getInterfaces(tpc);

        while (properties.hasNext()) {
            SemanticProperty tpp = properties.next();
            boolean isInClass = isPropertyInClass(tpp, tpc);

            if (!isInClass) {
                isInClass = isPropertyInSuperInterface(tpp, interfaces);
            }

            if (!isInClass) {
                SemanticClass range = tpp.getRangeClass();
                if (range != null && !staticClasses.contains(range)) {
                    String propJavaDoc = null;
                    if(range.getComment() != null) {
                        propJavaDoc = buildJavaDocBlock(1, range.getComment());
                        if (null != propJavaDoc) {
                            javaClassContent.append(propJavaDoc);
                        }
                    }
                    String propName = range.getPrefix() + "_" + range.getUpperClassName();
                    String initialVal = SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS + range.getURI() + "\")";
                    javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ")
                            .append(propName).append(" = ").append(initialVal).append(";" + ENTER);

                    staticClasses.add(range);
                }

                if (!staticProperties.contains(tpp)) {
                    String propJavaDoc = null;
                    if(tpp.getComment() != null) {
                        propJavaDoc = buildJavaDocBlock(1, tpp.getComment());
                        if (null != propJavaDoc) {
                            javaClassContent.append(propJavaDoc);
                        }
                    }
                    String propName = tpp.getPrefix()+"_"+tpp.getName();
                    String initialVal = SEMANTIC_PLATFORM_FULL_NAME +
                            ".getSemanticMgr().getVocabulary().getSemanticProperty(\"" + tpp.getURI() + "\")";

                    javaClassContent.append("    public static final " + SEMANTIC_PROPERTY_FULL_NAME + " ")
                            .append(propName).append(" = ").append(initialVal).append(";" + ENTER);

                    staticProperties.add(tpp);
                }
            }
        }

        if (tpc.isSWBModel()) {
            Iterator<SemanticClass> classesOfModel = tpc.listModelClasses();
            while (classesOfModel.hasNext()) {
                SemanticClass clazzOfModel = classesOfModel.next();
                if (clazzOfModel != null && !staticClasses.contains(clazzOfModel)) {
                    String clsJavaDoc = "";
                    if(clazzOfModel.getComment(null)!=null) {
                        clsJavaDoc = buildJavaDocBlock(1, clazzOfModel.getComment(null));
                        if (null != clsJavaDoc) {
                            javaClassContent.append(clsJavaDoc);
                        }
                    }
                    String propName = clazzOfModel.getPrefix()+"_"+clazzOfModel.getUpperClassName();
                    String initialVal = SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS + clazzOfModel.getURI() + "\")";
                    javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ")
                            .append(propName).append(" = ").append(initialVal).append(";" + ENTER);

                    staticClasses.add(clazzOfModel);
                }
            }
        }

        if (!staticClasses.contains(tpc)) {
            String clsJavaDoc = null;
            if(tpc.getComment(null) != null) {
                clsJavaDoc = buildJavaDocBlock(1, tpc.getComment(null));
                if (null != clsJavaDoc) {
                    javaClassContent.append(clsJavaDoc);
                }
            }
            String propName = tpc.getPrefix()+ "_" + tpc.getUpperClassName();
            String initialVal = SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS + tpc.getURI() + "\")";
            javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ").append(propName)
                    .append(" = ").append(initialVal).append(";" + ENTER);

            staticClasses.add(tpc);
        }

        javaClassContent.append(buildJavaDocBlock(1, "The semantic class that represents the currentObject"));
        String propName = "sclass";
        String initialVal = SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS + tpc.getURI() + "\")";
        javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " " + propName + " = " +
                initialVal).append(";" + ENTER);

        javaClassContent.append(ENTER);
        javaClassContent.append("    public static class " + GLOBAL_CLASS_NAME + ENTER);
        javaClassContent.append("    {" + ENTER);

        String fullpathClass = tpc.getCanonicalName();

        javaClassContent.append(buildJavaDocBlock(2, "Returns a list of " +
                        tpc.getUpperClassName()+" for a model",
                "@param model Model to find",
                "@return Iterator of "+fullpathClass));

        javaClassContent.append(buildMethodDefinition("public static", UTIL_ITERATOR_FULL_NAME +
                        "<" + fullpathClass + ">", "list" + tpc.getNameInPlural(),
                SEMANTIC_MODEL_FULL_NAME+" model",
                null, 2,
                UTIL_ITERATOR_FULL_NAME +
                        " it = model.getSemanticObject().getModel().listInstancesOfClass(sclass);",
                "return new " + GENERIC_ITERATOR_FULL_NAME + "<" + fullpathClass + ">(it, true);"));
        javaClassContent.append(ENTER).append(ENTER);

        javaClassContent.append(buildJavaDocBlock(2, "Returns a list of " +
                        fullpathClass + " for all models",
                "@return Iterator of "+fullpathClass));

        javaClassContent.append(buildMethodDefinition("public static",
                UTIL_ITERATOR_FULL_NAME + "<" + fullpathClass + ">",
                "list"+tpc.getNameInPlural(), null, null, 2,
                UTIL_ITERATOR_FULL_NAME+" it = sclass.listInstances();",
                "return new " + GENERIC_ITERATOR_FULL_NAME + "<" + fullpathClass + ">(it, true);"));
        javaClassContent.append(ENTER).append(ENTER);


        if (tpc.isAutogenId()) {
            javaClassContent.append(buildJavaDocBlock(2, "Creates a " +
                            fullpathClass + " in the given model",
                    "@param model Model for the new "+fullpathClass,
                    "@return A "+fullpathClass));

            javaClassContent.append(buildMethodDefinition("public static",
                    fullpathClass,
                    "create" + tpc.getUpperClassName(),
                    SEMANTIC_MODEL_FULL_NAME+" model", null, 2,
                    "long id = model.getSemanticObject().getModel().getCounter(sclass);",
                    "return "+fullpathClass+"."+ GLOBAL_CLASS_NAME + ".create" + tpc.getUpperClassName() +
                            "(String.valueOf(id), model);"));

            javaClassContent.append(ENTER).append(ENTER);

        }

        if (tpc.isSWBModel()) {
            javaClassContent.append(buildJavaDocBlock(2, "Gets a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@return A "+fullpathClass));

            javaClassContent.append(buildMethodDefinition("public static",
                    fullpathClass,
                    "get"+tpc.getUpperClassName(),
                    "String id",
                    null,
                    2,
                    SEMANTIC_MANANGER_FULL_NAME + " mgr = " + SEMANTIC_PLATFORM_FULL_NAME + ".getSemanticMgr();",
                    fullpathClass+" ret = null;",
                    MODEL_FULL_NAME + " model = mgr.getModel(id);",
                    "if (model != null) {",
                    "    " + SEMANTIC_OBJECT_FULL_NAME + " obj = model.getSemanticObject(model.getObjectUri(id,sclass));",
                    "    if (obj != null) {",
                    "        org.semanticwb.model.GenericObject gobj = obj.createGenericInstance();",
                    "        if (gobj instanceof " + fullpathClass + ") {",
                    "          ret = (" + fullpathClass + ")gobj;",
                    "        }",
                    "    }",
                    "}",
                    "return ret;"));

            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(2, "",
                    "Create a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@return A "+fullpathClass));


            javaClassContent.append(buildMethodDefinition("public static",
                    fullpathClass,
                    "create"+tpc.getUpperClassName(),
                    "String id, String namespace",
                    null,
                    2,
                    SEMANTIC_MANANGER_FULL_NAME+" mgr = "+SEMANTIC_PLATFORM_FULL_NAME +".getSemanticMgr();",
                    MODEL_FULL_NAME + " model = mgr.createModel(id, namespace);",
                    "return (" + fullpathClass + ")model.createGenericObject(model.getObjectUri(id,sclass),sclass);"));

            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(2, "Remove a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass));

            javaClassContent.append(buildMethodDefinition("public static",
                    TYPE_VOID,
                    "remove"+tpc.getUpperClassName(),
                    "String id",
                    null,
                    2,
                    fullpathClass + " obj = get" + tpc.getUpperClassName() + "(id);",
                    "if (obj != null) {",
                    "    obj.remove();",
                    "}"));

            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(2, "Returns true if exists a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@return true if the "+fullpathClass+" exists, false otherwise."));


            javaClassContent.append(buildMethodDefinition("public static",
                    TYPE_BOOLEAN,
                    "has"+tpc.getUpperClassName(),
                    "String id",
                    null,
                    2,
                    "return (get" + tpc.getUpperClassName() + "(id) != null);"));
        } else {
            javaClassContent.append(buildJavaDocBlock(2, "Gets a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@param model Model of the "+fullpathClass,
                    "@return A "+fullpathClass));

            javaClassContent.append(buildMethodDefinition("public static",
                    fullpathClass,
                    "get"+tpc.getUpperClassName(),
                    "String id, " + SEMANTIC_MODEL_FULL_NAME + " model",
                    null,
                    2,
                    "return (" + fullpathClass + ")" +
                            "model.getSemanticObject().getModel().getGenericObject(model.getSemanticObject().getModel().getObjectUri(id, sclass), sclass);"));
            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(2, "Create a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@param model Model of the "+fullpathClass,
                    "@return A " + fullpathClass));

            javaClassContent.append(buildMethodDefinition("public static",
                    fullpathClass,
                    "create"+tpc.getUpperClassName(),
                    "String id, " + SEMANTIC_MODEL_FULL_NAME + " model",
                    null,
                    2,
                    "return (" + fullpathClass + ")" +
                            "model.getSemanticObject().getModel().createGenericObject(model.getSemanticObject().getModel().getObjectUri(id, sclass), sclass);"));
            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(2, "Remove a "+fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@param model Model of the "+fullpathClass));


            javaClassContent.append(buildMethodDefinition("public static",
                    TYPE_VOID,
                    "remove"+tpc.getUpperClassName(),
                    "String id, "+SEMANTIC_MODEL_FULL_NAME+" model",
                    null,
                    2,
                    "model.getSemanticObject().getModel().removeSemanticObject(model.getSemanticObject().getModel().getObjectUri(id, sclass));"));

            javaClassContent.append(ENTER).append(ENTER);

            javaClassContent.append(buildJavaDocBlock(2, "Returns true if exists a " + fullpathClass,
                    "@param id Identifier for "+fullpathClass,
                    "@param model Model of the "+fullpathClass,
                    "@return true if the "+fullpathClass+" exists, false otherwise."));

            javaClassContent.append(buildMethodDefinition("public static",
                    TYPE_BOOLEAN,
                    "has"+tpc.getUpperClassName(),
                    "String id, "+SEMANTIC_MODEL_FULL_NAME+" model",
                    null,
                    2,
                    "return (get" + tpc.getUpperClassName() + "(id, model) != null);"));
            javaClassContent.append(ENTER).append(ENTER);

        }

        Iterator<SemanticProperty> tppit = tpc.listProperties();
        while (tppit.hasNext()) {
            SemanticProperty tpp = tppit.next();
            if (tpp.isObjectProperty() && !tpp.isNotCodeGeneration()) {
                SemanticClass tpcToReturn = tpp.getRangeClass();
                if (tpcToReturn != null && tpcToReturn.getURI() != null && tpcToReturn.isSWB()) {
                    String nameList = tpp.getPropertyCodeName();
                    if (nameList.startsWith("has")) {
                        nameList = nameList.substring(3);
                    }

                    javaClassContent.append(buildJavaDocBlock(2, "Gets all " + fullpathClass +
                                    " with a determined "+SWBUtils.TEXT.toUpperCase(nameList),
                            "@param value "+SWBUtils.TEXT.toUpperCase(nameList)+ " of the type " +
                                    tpcToReturn.getCanonicalName(),
                            "@param model Model of the "+fullpathClass,
                            "@return Iterator with all the "+fullpathClass));

                    javaClassContent.append(buildMethodDefinition("public static",
                            UTIL_ITERATOR_FULL_NAME + "<" + tpc.getCanonicalName() + ">",
                            "list" + tpc.getUpperClassName() + "By" + SWBUtils.TEXT.toUpperCase(nameList),
                            tpcToReturn.getCanonicalName() + " value, " + SEMANTIC_MODEL_FULL_NAME + " model",
                            null,
                            2,
                            GENERIC_ITERATOR_FULL_NAME + "<" + tpc.getCanonicalName() + "> it = new " +
                                    GENERIC_ITERATOR_FULL_NAME + "(model.getSemanticObject().getModel().listSubjectsByClass(" +
                                    tpp.getPrefix() + "_" + tpp.getName()+ ", value.getSemanticObject(), sclass));",
                            "return it;"));
                    javaClassContent.append(ENTER).append(ENTER);

                    javaClassContent.append(buildJavaDocBlock(2, "Gets all " + fullpathClass +
                                    " with a determined "+SWBUtils.TEXT.toUpperCase(nameList),
                            "@param value "+SWBUtils.TEXT.toUpperCase(nameList)+ " of the type " + tpcToReturn.getCanonicalName(),
                            "@return Iterator with all the "+fullpathClass));


                    javaClassContent.append(buildMethodDefinition("public static",
                            UTIL_ITERATOR_FULL_NAME + "<" + tpc.getCanonicalName() + ">",
                            "list" + tpc.getUpperClassName() + "By" + SWBUtils.TEXT.toUpperCase(nameList),
                            tpcToReturn.getCanonicalName() + " value",
                            null,
                            2,
                            GENERIC_ITERATOR_FULL_NAME + "<" + tpc.getCanonicalName() + "> it = new " +
                                    GENERIC_ITERATOR_FULL_NAME + "(value.getSemanticObject().getModel().listSubjectsByClass(" +
                                    tpp.getPrefix() + "_" + tpp.getName()+ ", value.getSemanticObject(), sclass));",
                            "return it;"
                    ));
                    javaClassContent.append(ENTER);
                }
            }
        }

        javaClassContent.append("    }").append(ENTER).append(ENTER); // ennd ClassMgr

        String base = usesufix ? "Base" : "";
        javaClassContent.append(buildMethodDefinition("public static",
                tpc.getUpperClassName() + base + ".ClassMgr",
                "get" + tpc.getUpperClassName() + "ClassMgr",
                null,
                null,
                1,
                "return new " + tpc.getUpperClassName() + base + ".ClassMgr();"));
        javaClassContent.append(ENTER).append(ENTER);

        javaClassContent.append(buildJavaDocBlock(1, "Constructor.",
                "Creates a "+tpc.getUpperClassName()+base+" with a SemanticObject",
                "@param base The SemanticObject with the properties for the "+tpc.getUpperClassName()));

        javaClassContent.append(buildMethodDefinition("public",
                null,
                tpc.getUpperClassName() + base,
                SEMANTIC_OBJECT_FULL_NAME + " base",
                null,
                1,
                "super(base);"
        ));
        javaClassContent.append(ENTER).append(ENTER);

        insertPropertiesToClass(tpc, javaClassContent, parent);

        if (tpc.isSWBModel()) {
            insertPropertiesToModel(tpc, javaClassContent);
        } else {
            if (parent == null) {
                javaClassContent.append(buildMethodDefinition("public",
                        TYPE_VOID,
                        "remove",
                        null,
                        null,
                        1,
                        "getSemanticObject().remove();"));

                javaClassContent.append(ENTER).append(ENTER);

                javaClassContent.append(buildMethodDefinition("public",
                        UTIL_ITERATOR_FULL_NAME + "<" + GENERIC_OBJECT_FULL_NAME + ">",
                        "listRelatedObjects",
                        null,
                        null,
                        1,
                        "return new " + GENERIC_ITERATOR_FULL_NAME + "(getSemanticObject().listRelatedObjects(), true);"));
                javaClassContent.append(ENTER).append(ENTER);
            }
            insertLinkToClass4Model(tpc, javaClassContent, parent);
        }
        javaClassContent.append("}" + ENTER);
        return javaClassContent.toString();
    }

    /**
     * Writes to a file the source code for an object's base class.
     *
     * @param tpc the {@SemanticClass} wrapper for the object definition in the input ontology.
     * @param pDirectory the target directory for source code.
     * @throws CodeGeneratorException the code generator exception
     */
    private void createClassBase(SemanticClass tpc, File pDirectory) throws CodeGeneratorException {
        String code = createClassBase(tpc,true);
        File dir = createPackage(tpc.getCodePackage(), pDirectory);
        dir = new File(dir.getPath() + File.separatorChar + "base");

        if (!dir.exists()) {
            dir.mkdirs();
        }
        File fileClass = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + "Base.java");
        try {
            log.info("Creando clase "+fileClass.getCanonicalPath());
        } catch(Exception e) {
            log.error(e);
        }
        saveFile(fileClass, code);
        SemanticClass parent = null;
        Iterator<SemanticClass> it = tpc.listSuperClasses(true);
        while (it.hasNext()) {
            parent = it.next();
            if (parent.isSWBClass() || parent.isSWBModel() || parent.isSWBFormElement()) {
                break;
            } else {
                parent = null;
            }
        }
        createClass(tpc, parent, pDirectory);
    }

    /**
     * Writes the source code for an object's class.
     *
     * @param tpc the {@SemanticClass} wrapper for the object definition in the input ontology.
     * @param parent the parent
     * @param pDirectory the target directory for the source code
     * @throws CodeGeneratorException the code generator exception
     */
    private void createClass(SemanticClass tpc, SemanticClass parent, File pDirectory) throws CodeGeneratorException {
        String sPackage = tpc.getCodePackage();
        File dir = createPackage(sPackage, pDirectory);
        File fileClass = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + ".java");
        File fileClass2 = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + ".groovy");
        if (!fileClass.exists() && !fileClass2.exists()) {
            StringBuilder javaClassContent = new StringBuilder();
            if (null != sPackage && !sPackage.isEmpty()) {
                javaClassContent.append("package ").append(sPackage).append(";" + ENTER);
                javaClassContent.append("" + ENTER);
            }

            javaClassContent.append(ENTER);
            if(tpc.getComment(null)!=null) {
                javaClassContent.append(buildJavaDocBlock(1, tpc.getComment(null)));
            }
            javaClassContent.append("public class ").append(tpc.getUpperClassName()).append(" extends ")
                    .append(tpc.getCodePackage()).append(".base.").append(tpc.getUpperClassName()).append("Base " + ENTER);
            javaClassContent.append("{" + ENTER);
            javaClassContent.append(PUBLIC).append(tpc.getUpperClassName()).append("("+SEMANTIC_OBJECT_FULL_NAME+" base)" + ENTER);
            javaClassContent.append(OPEN_BLOCK + ENTER);
            javaClassContent.append("        super(base);" + ENTER);
            javaClassContent.append(CLOSE_BLOCK + ENTER);
            javaClassContent.append("}" + ENTER);
            saveFile(fileClass, javaClassContent.toString());
        }
    }

    /**
     * Writes the source code for a {@SWBInterface} object.
     *
     * @param tpc the {@SemanticClass} wrapper for the {@SWBInterface} definition in the input ontology.
     * @param pDirectory the target directory for the source code
     * @throws CodeGeneratorException the code generator exception
     */
    private void createInterface(SemanticClass tpc, File pDirectory) throws CodeGeneratorException {
        File dir = createPackage(tpc.getCodePackage(), pDirectory);
        StringBuilder javaClassContent = new StringBuilder();

        if (tpc.getCodePackage()!=null && !tpc.getCodePackage().equals("")) {
            javaClassContent.append("package ").append(tpc.getCodePackage()).append(";" + ENTER);
            javaClassContent.append("" + ENTER);
        }

        if(tpc.getComment(null) != null) {
            javaClassContent.append(buildJavaDocBlock(1, tpc.getComment(null)));
        }

        javaClassContent.append("public interface ").append(tpc.getUpperClassName()).append(" extends ")
                .append(tpc.getCodePackage()).append(".base.").append(tpc.getUpperClassName()).append("Base" + ENTER);

        javaClassContent.append("{" + ENTER);
        javaClassContent.append("}" + ENTER);
        File fileClass = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + ".java");
        boolean exists = false;
        if (fileClass.exists()) {
            exists = true;
        }

        if (!exists) {
            saveFile(fileClass, javaClassContent.toString());
        }
    }

    /**
     * Writes the source code for a {@SWBInterface} base class.
     *
     * @param tpc the {@SemanticClass} wrapper for the {@SWBInterface} definition in the input ontology.
     * @param pDirectory the target directory for the source code
     * @throws CodeGeneratorException the code generator exception
     */
    private void createInterfaceBase(SemanticClass tpc, File pDirectory) throws CodeGeneratorException {
        File dir = createPackage(tpc.getCodePackage() + ".base", pDirectory);
        StringBuilder javaClassContent = new StringBuilder();
        if (tpc.getCodePackage()!=null && !tpc.getCodePackage().equals("")) {
            javaClassContent.append("package ").append(tpc.getCodePackage()).append(".base;" + ENTER);
            javaClassContent.append("" + ENTER);
        }

        HashSet<SemanticClass> interfaces = getInterfaces(tpc);
        if(tpc.getComment(null) != null) {
            javaClassContent.append(buildJavaDocBlock(1, tpc.getComment(null)));
        }

        if (interfaces.isEmpty()) {
            javaClassContent.append("public interface ").append(tpc.getUpperClassName()).append("Base extends "+GENERIC_OBJECT_FULL_NAME+ ENTER);
        } else {
            javaClassContent.append("public interface ").append(tpc.getUpperClassName()).append("Base ")
                    .append(getInterfacesAsString(tpc, true)).append(ENTER);
        }
        javaClassContent.append("{" + ENTER);
        HashSet<SemanticClass> staticClasses = new HashSet<>();
        Iterator<SemanticProperty> properties = tpc.listProperties();
        HashSet<String> rangeNames = new HashSet<>();
        while (properties.hasNext()) {
            SemanticProperty tpp = properties.next();
            SemanticClass range = tpp.getRangeClass();
            if (range != null && !staticClasses.contains(range)) {
                boolean isInSuperInterface = false;
                for (SemanticClass cInterface : interfaces) {
                    Iterator<SemanticProperty> propertiesInterface = cInterface.listProperties();
                    while (propertiesInterface.hasNext()) {
                        SemanticProperty tppInterface = propertiesInterface.next();
                        SemanticClass rangeInterface = tppInterface.getRangeClass();
                        if (rangeInterface != null && rangeInterface.equals(range)) {
                            isInSuperInterface = true;
                            break;
                        }
                    }

                    if (isInSuperInterface) {
                        break;
                    }
                }

                if (!isInSuperInterface) {
                    String name = range.getPrefix() + "_" + range.getUpperClassName();
                    if (!rangeNames.contains(name)) {
                        if(range.getComment(null) != null) {
                            javaClassContent.append(buildJavaDocBlock(1, range.getComment(null)));
                        }
                        javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ")
                                .append(name).append("=" + SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS).append(range.getURI()).append("\");" + ENTER);
                        rangeNames.add(name);
                    }
                }
                staticClasses.add(range);
            }

            if (!isPropertyInSuperInterface(tpp, interfaces)) {
                String name = tpp.getPrefix() + "_" + tpp.getName();
                if (!rangeNames.contains(name)) {
                    if(tpp.getComment(null)!=null) {
                        javaClassContent.append(buildJavaDocBlock(1, tpp.getComment(null)));
                    }
                    javaClassContent.append("    public static final " + SEMANTIC_PROPERTY_FULL_NAME + " ")
                            .append(name).append("=" + SEMANTIC_PLATFORM_FULL_NAME + ".getSemanticMgr().getVocabulary().getSemanticProperty(\"").append(tpp.getURI()).append("\");" + ENTER);
                    rangeNames.add(name);
                }

            }
        }
        String name = tpc.getPrefix() + "_" + tpc.getUpperClassName();
        if (!rangeNames.contains(name)) {
            if(tpc.getComment(null) != null) {
                javaClassContent.append(buildJavaDocBlock(1, tpc.getComment(null)));
            }
            javaClassContent.append("    public static final " + SEMANTIC_CLASS_FULL_NAME + " ")
                    .append(name).append("=" + SEMANTIC_PLATFORM_FULL_NAME + GET_SEMANTIC_CLASS).append(tpc.getURI()).append("\");" + ENTER);
            rangeNames.add(name);
        }

        insertPropertiesToInterface(tpc, javaClassContent);
        javaClassContent.append("}" + ENTER);
        File fileClass = new File(dir.getPath() + File.separatorChar + tpc.getUpperClassName() + "Base" + ".java");
        try {
            log.info("Creando interface "+fileClass.getCanonicalPath());
        } catch(Exception e) {
            log.error(e);
        }
        saveFile(fileClass, javaClassContent.toString());
        createInterface(tpc, pDirectory);
    }

    /**
     * Injects method definitions to manage object properties to an interface.
     *
     * @param tpc the {@SemanticClass} wrapper for the {@SWBInterface} definition in the input ontology.
     * @param javaClassContent the java class content
     */
    private void insertPropertiesToInterface(SemanticClass tpc, StringBuilder javaClassContent) {
        Iterator<SemanticProperty> tppit = tpc.listProperties();
        while (tppit.hasNext()) {
            SemanticProperty tpp = tppit.next();
            boolean isInClass = false;
            Iterator<SemanticClass> classes = tpc.listSuperClasses(true);
            while (classes.hasNext()) {
                SemanticClass superclass = classes.next();
                if (superclass.isSWBInterface()) {
                    Iterator<SemanticProperty> propInterfaces = superclass.listProperties();
                    while (propInterfaces.hasNext()) {
                        SemanticProperty propSuperClass = propInterfaces.next();
                        if (propSuperClass.equals(tpp)) {
                            isInClass = true;
                            break;
                        }
                    }

                    if (isInClass) {
                        break;
                    }
                }
            }

            if (!isInClass) {
                if (tpp.isObjectProperty()) {
                    String classToReturn=null;
                    SemanticClass cls = tpp.getRangeClass();
                    if (cls != null && cls.isSWB()) {
                        SemanticClass tpcToReturn = tpp.getRangeClass();
                        classToReturn=tpcToReturn.getCanonicalName();
                    } else if (tpp.getRange() != null) {
                        classToReturn=SEMANTIC_OBJECT_FULL_NAME;
                    }
                    String objectName = tpp.getPropertyCodeName();
                    if (objectName == null) {
                        objectName = tpp.getName();
                    }
                    objectName = SWBUtils.TEXT.toUpperCase(objectName);

                    if (objectName.toLowerCase().startsWith("has")) {
                        SemanticClass clsrange = tpp.getRangeClass();
                        if (clsrange != null && clsrange.getURI() != null && clsrange.isSWB()) {
                            // son varios
                            objectName = objectName.substring(3);
                            javaClassContent.append(ENTER);
                            javaClassContent.append("    public " + GENERIC_ITERATOR_FULL_NAME + "<")
                                    .append(classToReturn).append("> list").append(SWBUtils.TEXT.getPlural(objectName))
                                    .append("();" + ENTER);
                            javaClassContent.append("    public boolean has").append(objectName).append("(")
                                    .append(classToReturn).append(" " + "value" + ");" + ENTER);
                            if (tpp.isInheritProperty()) {
                                javaClassContent.append("    public " + GENERIC_ITERATOR_FULL_NAME + "<")
                                        .append(classToReturn).append("> listInherit").append(SWBUtils.TEXT.getPlural(objectName)).append("();" + ENTER);
                            }
                        } else {
                            // son varios
                            objectName = objectName.substring(3);
                            javaClassContent.append(ENTER);
                            javaClassContent.append("    public " + SEMANTIC_ITERATOR_FULL_NAME + "<")
                                    .append(classToReturn).append("> list").append(SWBUtils.TEXT.getPlural(objectName))
                                    .append("();" + ENTER);
                            if (tpp.isInheritProperty()) {
                                javaClassContent.append("    public " + SEMANTIC_ITERATOR_FULL_NAME + "<")
                                        .append(classToReturn).append("> listInherit")
                                        .append(SWBUtils.TEXT.getPlural(objectName)).append("();" + ENTER);
                            }
                        }

                        if (!tpp.hasInverse()) {
                            javaClassContent.append(ENTER);

                            javaClassContent.append(buildJavaDocBlock(1, "Adds the "+objectName,
                                    "@param value An instance of "+classToReturn));

                            javaClassContent.append("    public void add").append(objectName).append("(")
                                    .append(classToReturn).append(" " + "value" + ");" + ENTER);
                            javaClassContent.append(ENTER);

                            javaClassContent.append(buildJavaDocBlock(1, "Remove all the values for the property "+objectName));

                            javaClassContent.append("    public void removeAll").append(objectName).append("();" + ENTER);
                            javaClassContent.append(ENTER);

                            javaClassContent.append(buildJavaDocBlock(1, "Remove a value from the property "+objectName,
                                    "@param value An instance of "+classToReturn));

                            javaClassContent.append("    public void remove").append(objectName).append("(")
                                    .append(classToReturn).append(" " + "value" + ");" + ENTER);
                            javaClassContent.append(ENTER);

                            javaClassContent.append(buildJavaDocBlock(1, "Gets the "+objectName,
                                    "@return An instance of "+classToReturn));

                            javaClassContent.append(PUBLIC).append(classToReturn).append(" get").append(objectName)
                                    .append("();" + ENTER);
                        }
                    } else {
                        javaClassContent.append(ENTER);
                        String parameterName = "value";

                        javaClassContent.append(buildJavaDocBlock(1, "Sets a value from the property "+objectName,
                                "@param value An instance of "+classToReturn));

                        javaClassContent.append("    public void set").append(objectName).append("(")
                                .append(classToReturn).append(" ").append(parameterName).append(");" + ENTER);
                        javaClassContent.append(ENTER);

                        javaClassContent.append(buildJavaDocBlock(1, "Remove the value from the property "+objectName));

                        javaClassContent.append("    public void remove").append(objectName).append("();" + ENTER);
                        javaClassContent.append(ENTER);
                        javaClassContent.append(PUBLIC).append(classToReturn).append(" get")
                                .append(objectName).append("();" + ENTER);
                    }
                } else if (tpp.isDataTypeProperty()) {
                    String objectName = tpp.getPropertyCodeName();
                    if (objectName == null) {
                        objectName = tpp.getName();
                    }
                    objectName = SWBUtils.TEXT.toUpperCase(objectName);
                    if (objectName.toLowerCase().startsWith("has")) {
                        String type = getSemanticPropertyType(tpp);
                        if (null == type) {
                            throw new IllegalArgumentException("Data type '" + tpp.getRange() + "' is no supported");
                        }

                        // son varios
                        objectName = objectName.substring(3);
                        javaClassContent.append(ENTER);
                        javaClassContent.append("    public " + UTIL_ITERATOR_FULL_NAME + "<").append(type)
                                .append("> list").append(SWBUtils.TEXT.getPlural(objectName)).append("();" + ENTER);

                        if (!tpp.hasInverse()) {
                            javaClassContent.append(ENTER);
                            javaClassContent.append("    public void add").append(objectName).append("(").append(type)
                                    .append(" value);" + ENTER);
                            javaClassContent.append("    public void removeAll").append(objectName).append("();" + ENTER);
                            javaClassContent.append("    public void remove").append(objectName).append("(")
                                    .append(type).append(" value);" + ENTER);
                        }
                    } else {
                        String type = getSemanticPropertyType(tpp);
                        if (null == type) {
                            throw new IllegalArgumentException("Data type '" + tpp.getRange() + "' is no supported");
                        }

                        String prefix = TYPE_BOOLEAN.equals(type) ? "is" : "get";
                        String label = tpp.getPropertyCodeName();
                        if (label == null) {
                            label = tpp.getName();
                        }
                        String methodName = SWBUtils.TEXT.toUpperCase(label);
                        String propertyName = tpp.getName();
                        if (propertyName.equals("protected")) {
                            propertyName = "_" + propertyName;
                        }
                        javaClassContent.append(ENTER);
                        if (TYPE_JAVAINPUTSTREAM.equals(type)) {
                            javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append(methodName)
                                    .append("() throws Exception;" + ENTER);
                        } else {
                            javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append(methodName)
                                    .append("();" + ENTER);
                        }

                        javaClassContent.append(ENTER);
                        if (TYPE_JAVAINPUTSTREAM.equals(type)) {
                            javaClassContent.append(PUBLIC + "void set").append(methodName).append("(").append(type)
                                    .append(" " + "value" + ",String name) throws Exception;" + ENTER);
                        } else {
                            javaClassContent.append(PUBLIC + "void set").append(methodName).append("(").append(type)
                                    .append(" " + "value" + ");" + ENTER);
                        }

                        if (tpp.isLocaleable()) {
                            javaClassContent.append(ENTER);
                            javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append(methodName)
                                    .append("(String lang);" + ENTER);
                            if (TYPE_STRING.equals(type)) {
                                javaClassContent.append(ENTER);
                                javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append("Display")
                                        .append(methodName).append("(String lang);" + ENTER);
                            }

                            javaClassContent.append(ENTER);
                            javaClassContent.append(PUBLIC + "void set").append(methodName).append("(").append(type)
                                    .append(" ").append(tpp.getName()).append(", String lang);" + ENTER);
                        }
                    }
                }
            }
        }
    }

    /**
     * Insert link to class4 model.
     *
     * @param tpcls the tpcls
     * @param javaClassContent the java class content
     * @param parent the parent
     */
    private void insertLinkToClass4Model(SemanticClass tpcls, StringBuilder javaClassContent, SemanticClass parent) {
        Iterator<SemanticClass> tpcit = tpcls.listOwnerModels();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            javaClassContent.append(ENTER);
            javaClassContent.append(buildJavaDocBlock(1, "Gets the "+tpc.getUpperClassName(),
                    "@return a instance of "+tpc.getCanonicalName()));

            javaClassContent.append(buildMethodDefinition("public",
                    tpc.getCanonicalName(),
                    "get" + tpc.getUpperClassName(),
                    null,
                    null,
                    1,
                    "return (" + tpc.getCanonicalName() + ") getSemanticObject().getModel().getModelObject().createGenericInstance();"));
            javaClassContent.append(ENTER);
        }
    }

    /**
     * Insert properties to model.
     *
     * @param tpcls the tpcls
     * @param javaClassContent the java class content
     */
    private void insertPropertiesToModel(SemanticClass tpcls, StringBuilder javaClassContent) {
        Iterator<SemanticClass> tpcit = tpcls.listModelClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            javaClassContent.append(ENTER);

            javaClassContent.append("    public ").append(tpc.getCanonicalName()).append(" get")
                    .append(tpc.getUpperClassName()).append("(String id)" + ENTER);
            javaClassContent.append("    {" + ENTER);
            javaClassContent.append("        return ").append(tpc.getCanonicalName()).append("." +
                    GLOBAL_CLASS_NAME + ".get").append(tpc.getUpperClassName()).append("(id, this);" + ENTER);
            javaClassContent.append("    }" + ENTER);
            javaClassContent.append(ENTER);

            javaClassContent.append("    public " + UTIL_ITERATOR_FULL_NAME + "<").append(tpc.getCanonicalName())
                    .append("> list").append(tpc.getNameInPlural()).append("()" + ENTER);
            javaClassContent.append("    {" + ENTER);
            javaClassContent.append("        return ").append(tpc.getCanonicalName()).append("." + GLOBAL_CLASS_NAME +
                    ".list").append(tpc.getNameInPlural()).append("(this);" + ENTER);
            javaClassContent.append("    }" + ENTER);

            javaClassContent.append(ENTER);
            javaClassContent.append("    public ").append(tpc.getCanonicalName()).append(" create")
                    .append(tpc.getUpperClassName()).append("(String id)" + ENTER);
            javaClassContent.append("    {" + ENTER);
            javaClassContent.append("        return ").append(tpc.getCanonicalName()).append("." + GLOBAL_CLASS_NAME +
                    ".create").append(tpc.getUpperClassName()).append("(id,this);" + ENTER);
            javaClassContent.append("    }" + ENTER);

            if (tpc.isAutogenId()) {
                javaClassContent.append(ENTER);
                javaClassContent.append("    public ").append(tpc.getCanonicalName()).append(" create")
                        .append(tpc.getUpperClassName()).append("()" + ENTER);
                javaClassContent.append("    {" + ENTER);
                javaClassContent.append("        long id=getSemanticObject().getModel().getCounter(")
                        .append(tpc.getPrefix()).append("_").append(tpc.getUpperClassName()).append(");" + ENTER);
                javaClassContent.append("        return ").append(tpc.getCanonicalName()).append("." +
                        GLOBAL_CLASS_NAME + ".create").append(tpc.getUpperClassName()).append("(String.valueOf(id),this);" + ENTER);
                javaClassContent.append("    } " + ENTER);
            }
            javaClassContent.append(ENTER);
            javaClassContent.append("    public void remove").append(tpc.getUpperClassName()).append("(String id)" + ENTER);
            javaClassContent.append("    {" + ENTER);
            javaClassContent.append("        ").append(tpc.getCanonicalName()).append("." + GLOBAL_CLASS_NAME +
                    ".remove").append(tpc.getUpperClassName()).append("(id, this);" + ENTER);
            javaClassContent.append("    }" + ENTER);

            javaClassContent.append("    public boolean has").append(tpc.getUpperClassName()).append("(String id)" + ENTER);
            javaClassContent.append("    {" + ENTER);
            javaClassContent.append("        return ").append(tpc.getCanonicalName()).append("." + GLOBAL_CLASS_NAME +
                    ".has").append(tpc.getUpperClassName()).append("(id, this);" + ENTER);
            javaClassContent.append("    }" + ENTER);
        }
    }

    /**
     * Checks if is property of parent.
     *
     * @param tpp the tpp
     * @param parent the parent
     * @return true, if is property of parent
     */
    private boolean isPropertyOfParent(SemanticProperty tpp, SemanticClass parent) {
        boolean isPropertyOfParent = false;
        if (parent != null) {
            Iterator<SemanticProperty> properties = parent.listProperties();
            while (properties.hasNext()) {
                SemanticProperty propertyOfParent = properties.next();
                if (getNameOfProperty(propertyOfParent).equals(getNameOfProperty(tpp))) {
                    isPropertyOfParent = true;
                    break;
                }
            }
        }
        return isPropertyOfParent;
    }

    /**
     * Gets the name of property.
     *
     * @param tpp the tpp
     * @return the name of property
     */
    private String getNameOfProperty(SemanticProperty tpp) {
        String objectName = tpp.getPropertyCodeName();
        if (objectName == null) {
            objectName = tpp.getName();
        }
        return objectName;
    }

    /**
     * Insert data type property.
     *
     * @param tpc the tpc
     * @param tpp the tpp
     * @param javaClassContent the java class content
     * @param semanticObject the semantic object
     */
    private void insertDataTypeProperty(SemanticClass tpc, SemanticProperty tpp, StringBuilder javaClassContent, String semanticObject) {
        String objectName = tpp.getPropertyCodeName();
        if (objectName == null) {
            objectName = tpp.getName();
        }
        objectName = SWBUtils.TEXT.toUpperCase(objectName);

        if (objectName.toLowerCase().startsWith("has")) {
            String type = getSemanticPropertyType(tpp);
            if (null == type) {
                throw new IllegalArgumentException("Data type '" + tpp.getRange() + "' is no supported");
            }

            // son varios
            objectName = objectName.substring(3);
            javaClassContent.append(ENTER);

            javaClassContent.append(buildMethodDefinition("public",
                    UTIL_ITERATOR_FULL_NAME + "<" + type + ">",
                    "list" + SWBUtils.TEXT.getPlural(objectName),
                    null,
                    null,
                    1,
                    "java.util.ArrayList<" + type + "> values = new java.util.ArrayList<" + type + ">();",
                    UTIL_ITERATOR_FULL_NAME + "<" + SEMANTIC_LITERAL_FULL_NAME + "> it = getSemanticObject().listLiteralProperties(" +
                            tpp.getPrefix() + "_" + tpp.getName() + ");",
                    "while(it.hasNext()) {",
                    "    " + SEMANTIC_LITERAL_FULL_NAME + " literal = it.next();",
                    "    values.add(literal.getString());",
                    "}",
                    "return values.iterator();"));

            javaClassContent.append(ENTER).append(ENTER);

            if (!tpp.hasInverse()) {
                javaClassContent.append(ENTER);
                javaClassContent.append("    public void add").append(objectName).append("(").append(type)
                        .append(" value)" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        get").append(semanticObject).append("().addLiteralProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(", new "+
                        SEMANTIC_LITERAL_FULL_NAME+"(value));" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
                javaClassContent.append(ENTER);
                javaClassContent.append("    public void removeAll").append(objectName).append("()" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        get").append(semanticObject).append("().removeProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
                javaClassContent.append(ENTER);
                javaClassContent.append("    public void remove").append(objectName).append("(").append(type)
                        .append(" value)" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        get").append(semanticObject).append("().removeLiteralProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName())
                        .append(",new org.semanticwb.platform.SemanticLiteral(value));" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
            }
        } else {
            String type = getSemanticPropertyType(tpp);
            if (null == type) {
                throw new IllegalArgumentException("Data type '" + tpp.getRange() + "' is no supported, from class:" +
                        tpc + ", property:" + tpp);
            }

            String prefix = TYPE_BOOLEAN.equals(type) ? "is" : "get";
            String signature = getPropertyMethodSignature(tpp);
            String getMethod = "get" + signature;
            String setMethod = "set" + signature;
            getMethod = "get" + semanticObject + "()." + getMethod;
            setMethod = "get" + semanticObject + "()." + setMethod;

            String label = tpp.getPropertyCodeName();
            if (label == null) {
                label = tpp.getName();
            }
            String methodName = SWBUtils.TEXT.toUpperCase(label);
            String propertyName = tpp.getName();
            if (propertyName.equals("protected")) {
                propertyName = "_" + propertyName;
            }
            javaClassContent.append(ENTER);
            if (TYPE_JAVAINPUTSTREAM.equals(type)) {
                javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append(methodName)
                        .append("() throws Exception" + ENTER);
            } else {
                javaClassContent.append(buildJavaDocBlock(1, "Gets the "+methodName+ " property",
                        "@return "+type+" with the "+methodName));

                javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append(methodName)
                        .append("()" + ENTER);
            }
            javaClassContent.append(OPEN_BLOCK + ENTER);
            if (tpp.isExternalInvocation()) {
                javaClassContent.append("        //Override this method in ").append(tpc.getUpperClassName())
                        .append(" object" + ENTER);
                javaClassContent.append("        return ").append(getMethod).append("(").append(tpp.getPrefix())
                        .append("_").append(tpp.getName()).append(",false);" + ENTER);
            } else {
                javaClassContent.append("        return ").append(getMethod).append("(").append(tpp.getPrefix())
                        .append("_").append(tpp.getName()).append(");" + ENTER);
            }
            javaClassContent.append(CLOSE_BLOCK + ENTER);

            javaClassContent.append(ENTER);
            if (TYPE_JAVAINPUTSTREAM.equals(type)) {
                javaClassContent.append(PUBLIC + "void set").append(methodName).append("(").append(type)
                        .append(" " + "value" + ",String name) throws Exception" + ENTER);
            } else {
                javaClassContent.append(buildJavaDocBlock(1, "Sets the "+methodName+ " property",
                        "@param value "+type+" with the "+methodName));
                javaClassContent.append(PUBLIC + "void set").append(methodName).append("(").append(type)
                        .append(" " + "value" + ")" + ENTER);
            }
            javaClassContent.append(OPEN_BLOCK + ENTER);
            if (tpp.isExternalInvocation()) {
                javaClassContent.append("        //Override this method in ").append(tpc.getUpperClassName())
                        .append(" object" + ENTER);
                if (TYPE_JAVAINPUTSTREAM.equals(type)) {
                    javaClassContent.append("        throw new org.semanticwb.SWBMethodImplementationRequiredException();" + ENTER);
                } else {
                    javaClassContent.append("        ").append(setMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", " + "value" + ",false);" + ENTER);
                }
            } else {
                if (TYPE_JAVAINPUTSTREAM.equals(type)) {
                    javaClassContent.append("        ").append(setMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", " + "value" + ",name);" + ENTER);
                } else {
                    javaClassContent.append("        ").append(setMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", " + "value" + ");" + ENTER);
                }
            }
            javaClassContent.append(CLOSE_BLOCK + ENTER);

            if (tpp.isLocaleable()) {
                javaClassContent.append(ENTER);
                javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append(methodName)
                        .append("(String lang)" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                if (tpp.isExternalInvocation()) {
                    javaClassContent.append("        //Override this method in ").append(tpc.getUpperClassName())
                            .append(" object" + ENTER);
                    javaClassContent.append("        return ").append(getMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", null, lang,false);" + ENTER);

                } else {
                    javaClassContent.append("        return ").append(getMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", null, lang);" + ENTER);
                }
                javaClassContent.append(CLOSE_BLOCK + ENTER);

                if (TYPE_STRING.equals(type)) {
                    javaClassContent.append(ENTER);
                    javaClassContent.append(PUBLIC).append(type).append(" ").append(prefix).append("Display")
                            .append(methodName).append("(String lang)" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        return " + "get").append(semanticObject)
                            .append("().getLocaleProperty" + "(").append(tpp.getPrefix()).append("_")
                            .append(tpp.getName()).append(", lang);" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);
                }

                javaClassContent.append(ENTER);
                javaClassContent.append(PUBLIC + "void set").append(methodName).append("(").append(type).append(" ")
                        .append(tpp.getName()).append(", String lang)" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                if (tpp.isExternalInvocation()) {
                    javaClassContent.append("        //Override this method in ").append(tpc.getUpperClassName())
                            .append(" object" + ENTER);
                    javaClassContent.append("        ").append(setMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", ").append(tpp.getName()).append(", lang,false);" + ENTER);
                } else {
                    javaClassContent.append("        ").append(setMethod).append("(").append(tpp.getPrefix())
                            .append("_").append(tpp.getName()).append(", ").append(tpp.getName()).append(", lang);" + ENTER);
                }
                javaClassContent.append(CLOSE_BLOCK + ENTER);
            }
        }

    }

    /**
     * Insert object property.
     *
     * @param tpc the tpc
     * @param tpp the tpp
     * @param javaClassContent the java class content
     * @param semanticObject the semantic object
     */
    private void insertObjectProperty(SemanticClass tpc, SemanticProperty tpp, StringBuilder javaClassContent, String semanticObject) {
        SemanticClass cls = tpp.getRangeClass();
        if (cls != null && cls.getURI() != null && (cls.isSWB() || (cls.isSWBVirtualClass() && isGenerateVirtualClasses()))) {
            String objectName = tpp.getPropertyCodeName();
            if (objectName == null) {
                objectName = tpp.getName();
            }
            objectName = SWBUtils.TEXT.toUpperCase(objectName);
            SemanticClass tpcToReturn = tpp.getRangeClass();
            if (objectName.toLowerCase().startsWith("has")) {
                // son varios
                objectName = objectName.substring(3);

                javaClassContent.append(buildJavaDocBlock(1, "Gets all the " +
                                tpcToReturn.getCanonicalName(),
                        "@return A GenericIterator with all the "+tpcToReturn.getCanonicalName()));

                javaClassContent.append("    public " + GENERIC_ITERATOR_FULL_NAME + "<")
                        .append(tpcToReturn.getCanonicalName()).append("> list")
                        .append(SWBUtils.TEXT.getPlural(objectName)).append("()" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        return new " + GENERIC_ITERATOR_FULL_NAME + "<")
                        .append(tpcToReturn.getCanonicalName()).append(">(getSemanticObject().listObjectProperties(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append("));" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
                javaClassContent.append(ENTER);

                javaClassContent.append(buildJavaDocBlock(1, "Gets true if has a "+objectName,
                        "@param value "+tpcToReturn.getCanonicalName()+" to verify",
                        "@return true if the "+tpcToReturn.getCanonicalName()+" exists, false otherwise."));

                javaClassContent.append("    public boolean has").append(objectName).append("(")
                        .append(tpcToReturn.getCanonicalName()).append(" " + "value" + ")" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        boolean ret=false;" + ENTER);
                javaClassContent.append("        if(" + "value" + "!=null)" + ENTER);
                javaClassContent.append("        {" + ENTER);
                javaClassContent.append("           ret=get").append(semanticObject).append("().hasObjectProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append("," + "value" +
                        ".getSemanticObject());" + ENTER);
                javaClassContent.append("        }" + ENTER);
                javaClassContent.append("        return ret;" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
                if (tpp.isInheritProperty()) {
                    javaClassContent.append(ENTER);

                    javaClassContent.append(buildJavaDocBlock(1, "Gets all the "+
                                    SWBUtils.TEXT.getPlural(objectName)+" inherits",
                            "@return A GenericIterator with all the "+tpcToReturn.getCanonicalName()));

                    javaClassContent.append("    public " + GENERIC_ITERATOR_FULL_NAME + "<")
                            .append(tpcToReturn.getCanonicalName()).append("> listInherit")
                            .append(SWBUtils.TEXT.getPlural(objectName)).append("()" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        return new " + GENERIC_ITERATOR_FULL_NAME + "<")
                            .append(tpcToReturn.getCanonicalName()).append(">(getSemanticObject().listInheritProperties(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append("));" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);
                }

                if (!tpp.hasInverse()) {
                    javaClassContent.append(buildJavaDocBlock(1, "Adds a "+objectName,
                            "@param value "+(tpcToReturn.getCanonicalName()+" to add")));

                    javaClassContent.append(ENTER);
                    javaClassContent.append("    public void add").append(objectName).append("(")
                            .append(tpcToReturn.getCanonicalName()).append(" " + "value" + ")" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        get").append(semanticObject).append("().addObjectProperty(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(", " + "value" +
                            ".getSemanticObject());" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);

                    javaClassContent.append(buildJavaDocBlock(1, "Removes all the "+objectName));

                    javaClassContent.append(ENTER);
                    javaClassContent.append("    public void removeAll").append(objectName).append("()" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        get").append(semanticObject).append("().removeProperty(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);

                    javaClassContent.append(buildJavaDocBlock(1, "Removes a "+objectName,
                            "@param value "+tpcToReturn.getCanonicalName()+" to remove"));

                    javaClassContent.append(ENTER);
                    javaClassContent.append("    public void remove").append(objectName).append("(")
                            .append(tpcToReturn.getCanonicalName()).append(" " + "value" + ")" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        get").append(semanticObject).append("().removeObjectProperty(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName())
                            .append("," + "value" + ".getSemanticObject());" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);
                }
            } else {
                javaClassContent.append(buildJavaDocBlock(1, "Sets the value for the property "+
                                objectName,
                        "@param value "+objectName+" to set"));

                javaClassContent.append("    public void set").append(objectName).append("(")
                        .append(tpcToReturn.getCanonicalName()).append(" " + "value" + ")" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        if(value!=null)" + ENTER);
                javaClassContent.append("        {" + ENTER);
                javaClassContent.append("            get").append(semanticObject).append("().setObjectProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName())
                        .append(", " + "value" + ".getSemanticObject());" + ENTER);
                javaClassContent.append("        }else" + ENTER);
                javaClassContent.append("        {" + ENTER);
                javaClassContent.append("            remove").append(objectName).append("();" + ENTER);
                javaClassContent.append("        }" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);


                javaClassContent.append(buildJavaDocBlock(1, "Removes the value for the property "+objectName));
                javaClassContent.append("    public void remove").append(objectName).append("()" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        get").append(semanticObject).append("().removeProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
            }

            String nameList = tpp.getPropertyCodeName();
            if (nameList.startsWith("has")) {
                nameList = nameList.substring(3);
            }

            javaClassContent.append(ENTER);

            javaClassContent.append(buildJavaDocBlock(1, "Gets the "+objectName,
                    "@return A "+tpcToReturn.getCanonicalName()));

            javaClassContent.append(PUBLIC).append(tpcToReturn.getCanonicalName()).append(" get")
                    .append(objectName).append("()" + ENTER);
            javaClassContent.append(OPEN_BLOCK + ENTER);
            javaClassContent.append("         ").append(tpcToReturn.getCanonicalName()).append(" ret=null;" + ENTER);
            javaClassContent.append("         " + SEMANTIC_OBJECT_FULL_NAME + " obj=getSemanticObject().getObjectProperty(")
                    .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
            javaClassContent.append("         if(obj!=null)" + ENTER);
            javaClassContent.append("         {" + ENTER);
            javaClassContent.append("             ret=(").append(tpcToReturn.getCanonicalName())
                    .append(")obj.createGenericInstance();" + ENTER);
            javaClassContent.append("         }" + ENTER);
            javaClassContent.append("         return ret;" + ENTER);
            javaClassContent.append(CLOSE_BLOCK + ENTER);
        } else if (tpp.getRange() != null) {
            String classToReturn=SEMANTIC_OBJECT_FULL_NAME;
            String objectName = tpp.getPropertyCodeName();
            if (objectName == null) {
                objectName = tpp.getName();
            }
            objectName = SWBUtils.TEXT.toUpperCase(objectName);

            if (objectName.toLowerCase().startsWith("has")) {
                // son varios
                objectName = objectName.substring(3);
                javaClassContent.append(ENTER);
                javaClassContent.append("    public " + SEMANTIC_ITERATOR_FULL_NAME + "<").append(classToReturn)
                        .append("> list").append(SWBUtils.TEXT.getPlural(objectName)).append("()" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);

                javaClassContent.append("        " + JENA_ITERATOR_FULL_NAME + " stit=getSemanticObject().getRDFResource().listProperties(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(".getRDFProperty());" + ENTER);
                javaClassContent.append("        return new " + SEMANTIC_ITERATOR_FULL_NAME + "<")
                        .append(classToReturn).append(">(stit);" + ENTER);

                javaClassContent.append(CLOSE_BLOCK + ENTER);

                if (!tpp.hasInverse()) {
                    javaClassContent.append(ENTER);
                    javaClassContent.append("    public void add").append(objectName).append("(").append(classToReturn)
                            .append(" " + "value" + ")" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        get").append(semanticObject).append("().addObjectProperty(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(", " + "value" + ");" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);

                    javaClassContent.append(ENTER);
                    javaClassContent.append("    public void removeAll").append(objectName).append("()" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        get").append(semanticObject).append("().removeProperty(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);

                    javaClassContent.append(ENTER);
                    javaClassContent.append("    public void remove").append(objectName).append("(").append(classToReturn)
                            .append(" value)" + ENTER);
                    javaClassContent.append(OPEN_BLOCK + ENTER);
                    javaClassContent.append("        get").append(semanticObject).append("().removeObjectProperty(")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(",value);" + ENTER);
                    javaClassContent.append(CLOSE_BLOCK + ENTER);
                }
            } else {
                javaClassContent.append(ENTER);
                javaClassContent.append("    public void set").append(objectName).append("(").append(classToReturn)
                        .append(" " + "value" + ")" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        get").append(semanticObject).append("().setObjectProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(", " + "value" + ");" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);

                javaClassContent.append(ENTER);
                javaClassContent.append("    public void remove").append(objectName).append("()" + ENTER);
                javaClassContent.append(OPEN_BLOCK + ENTER);
                javaClassContent.append("        get").append(semanticObject).append("().removeProperty(")
                        .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
                javaClassContent.append(CLOSE_BLOCK + ENTER);
            }

            javaClassContent.append(buildJavaDocBlock(0, "Gets the " + objectName+ " property",
                    " @return the value for the property as "+classToReturn));
            javaClassContent.append(ENTER);

            javaClassContent.append(PUBLIC).append(classToReturn).append(" get").append(objectName).append("()" + ENTER);
            javaClassContent.append(OPEN_BLOCK + ENTER);
            javaClassContent.append("         ").append(classToReturn).append(" ret=null;" + ENTER);
            javaClassContent.append("         ret=get").append(semanticObject).append("().getObjectProperty(")
                    .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(");" + ENTER);
            javaClassContent.append("         return ret;" + ENTER);
            javaClassContent.append(CLOSE_BLOCK + ENTER);
        }
    }

    /**
     * Insert properties to class.
     *
     * @param tpc the tpc
     * @param javaClassContent the java class content
     * @param parent the parent
     */
    private void insertPropertiesToClass(SemanticClass tpc, StringBuilder javaClassContent, SemanticClass parent) {
        insertPropertiesToClass(tpc, javaClassContent, parent, null);
    }

    /**
     * Insert properties to class.
     *
     * @param tpc the tpc
     * @param javaClassContent the java class content
     * @param parent the parent
     * @param semanticObject the semantic object
     */
    private void insertPropertiesToClass(SemanticClass tpc, StringBuilder javaClassContent, SemanticClass parent,
                                         String semanticObject) {
        if (semanticObject == null) {
            semanticObject = "SemanticObject";
        }
        Iterator<SemanticProperty> tppit = tpc.listProperties();
        while (tppit.hasNext()) {
            SemanticProperty tpp = tppit.next();
            if (!isPropertyOfParent(tpp, parent)) {
                if (tpp.isObjectProperty()) {
                    insertObjectProperty(tpc, tpp, javaClassContent, semanticObject);
                } else if (tpp.isDataTypeProperty()) {
                    insertDataTypeProperty(tpc, tpp, javaClassContent, semanticObject);
                }
            }
        }
    }

    /**
     * Creates the vocabulary.
     *
     * @param pPackage the package
     * @param pDirectory the directory
     * @throws CodeGeneratorException the code generator exception
     */
    public void createVocabulary(String pPackage, File pDirectory) throws CodeGeneratorException {
        StringBuilder javaClassContent = new StringBuilder();
        String sPackage = pPackage;
        javaClassContent.append("package ").append(sPackage).append(";\r\n" + ENTER);
        javaClassContent.append("import "+SEMANTIC_PLATFORM_FULL_NAME+";" + ENTER);
        javaClassContent.append("import org.semanticwb.platform.SemanticVocabulary;" + ENTER);
        javaClassContent.append("import "+SEMANTIC_CLASS_FULL_NAME+";" + ENTER);
        javaClassContent.append("import "+SEMANTIC_PROPERTY_FULL_NAME+";" + ENTER);
        javaClassContent.append("import java.util.Hashtable;" + ENTER);

        javaClassContent.append("public class SWBVocabulary" + ENTER);
        javaClassContent.append("{" + ENTER);
        javaClassContent.append("\r\n\r\n    //Classes" + ENTER);
        SemanticMgr mgr = SWBPlatform.getSemanticMgr();
        Iterator<SemanticClass> tpcit = mgr.getVocabulary().listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            javaClassContent.append("    public final SemanticClass ").append(tpc.getPrefix()).append("_")
                    .append(tpc.getUpperClassName()).append(";" + ENTER);
        }

        javaClassContent.append("\r\n\r\n\r\n    //Properties" + ENTER);
        HashSet<String> properties = new HashSet<>();
        tpcit = mgr.getVocabulary().listSemanticClasses();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            Iterator<SemanticProperty> tppit = tpc.listProperties();
            while (tppit.hasNext()) {
                SemanticProperty tpp = tppit.next();
                if (!properties.contains(tpp.getPrefix() + "_" + tpp.getName())) {
                    properties.add(tpp.getPrefix() + "_" + tpp.getName());
                    javaClassContent.append("    public final " + SEMANTIC_PROPERTY_FULL_NAME + " ")
                            .append(tpp.getPrefix()).append("_").append(tpp.getName()).append(";" + ENTER);
                }
            }
        }
        javaClassContent.append("\r\n" + ENTER);
        javaClassContent.append("    public SWBVocabulary()" + ENTER);
        javaClassContent.append("    {\r\n" + ENTER);
        javaClassContent.append("         SemanticVocabulary vocabulary=SWBPlatform.getSemanticMgr().getVocabulary();" + ENTER);
        javaClassContent.append("        // Classes" + ENTER);
        tpcit = mgr.getVocabulary().listSemanticClasses();
        HashMap<String, String> namespaces = new HashMap<>();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            int pos = tpc.getURI().indexOf('#');
            String uri = tpc.getURI();
            if (pos != -1) {
                uri = uri.substring(0, pos + 1);
            }
            namespaces.put(tpc.getPrefix(), uri);
            javaClassContent.append("        ").append(tpc.getPrefix()).append("_").append(tpc.getUpperClassName())
                    .append("=vocabulary.getSemanticClass(\"").append(tpc.getURI()).append("\");" + ENTER);
        }

        javaClassContent.append("\r\n\r\n\r\n        //Properties" + ENTER);
        tpcit = mgr.getVocabulary().listSemanticClasses();
        properties = new HashSet<>();
        while (tpcit.hasNext()) {
            SemanticClass tpc = tpcit.next();
            Iterator<SemanticProperty> tppit = tpc.listProperties();
            while (tppit.hasNext()) {
                SemanticProperty tpp = tppit.next();
                String propertyName = tpp.getPrefix() + "_" + tpp.getName();
                if (!properties.contains(propertyName)) {
                    properties.add(propertyName);
                    if (!tpp.getURI().equals("#")) {
                        int pos = tpp.getURI().indexOf('#');
                        String uri = tpp.getURI();
                        if (pos != -1) {
                            uri = uri.substring(0, pos + 1);
                        }
                        namespaces.put(tpp.getPrefix(), uri);
                    }
                    javaClassContent.append("        ").append(propertyName).append("=vocabulary.getSemanticProperty(\"")
                            .append(tpp.getURI()).append("\");" + ENTER);
                }
            }
        }
        javaClassContent.append(CLOSE_BLOCK + ENTER);
        javaClassContent.append("\r\n\r\n\r\n        //ListUris" + ENTER);
        javaClassContent.append("              public Hashtable<String,String> listUris()" + ENTER);
        javaClassContent.append("              {" + ENTER);
        javaClassContent.append("                     Hashtable<String,String> namespaces=new Hashtable<String, String>();" + ENTER);
        for (String prefix : namespaces.keySet()) {
            String uri = namespaces.get(prefix);
            javaClassContent.append("                 namespaces.put(\"").append(prefix).append("\",\"").append(uri).append("\");" + ENTER);
        }
        javaClassContent.append("                     return namespaces;" + ENTER);
        javaClassContent.append("              }" + ENTER);
        javaClassContent.append("}" + ENTER);

        File dir = createPackage(sPackage, pDirectory);
        File fileClass = new File(dir.getPath() + File.separatorChar + "SWBVocabulary.java");
        saveFile(fileClass, javaClassContent.toString());
    }

    /**
     * Creates source code for a {@link SemanticClass}.
     * @param sclass {@link SemanticClass}
     * @param pDirectory Parent directory.
     * @throws CodeGeneratorException if an error occurs on code generation.
     */
    private void createElementCode(SemanticClass sclass, File pDirectory) throws CodeGeneratorException {
        if (sclass.isSWBInterface()) {
            createInterfaceBase(sclass, pDirectory);
        } else if (sclass.isSWBSemanticResource()) {
            createSemanticResourceBase(sclass, pDirectory);
        } else if (sclass.isSWBClass() || sclass.isSWBModel() || sclass.isSWBFormElement()) {
            createClassBase(sclass, pDirectory);
        }
    }

    /**
     * Checks whether a {@link SemanticProperty} is part of a {@link SemanticClass}'s list of properties.
     * This method is called for {@link SemanticClass}es used as interfaces.
     * @param sp {@link SemanticProperty}
     * @param interfaces Hash of interfaces to check.
     * @return true if sp is a property of one of the {@link SemanticClass}es in interfaces set.
     */
    private boolean isPropertyInSuperInterface(SemanticProperty sp, HashSet<SemanticClass> interfaces) {
        boolean isInSuperInterface = false;
        for (SemanticClass cInterface : interfaces) {
            Iterator<SemanticProperty> propertiesInterface = cInterface.listProperties();
            while (propertiesInterface.hasNext()) {
                SemanticProperty tppInterface = propertiesInterface.next();
                if (tppInterface.equals(sp)) {
                    isInSuperInterface = true;
                    break;
                }
            }

            if (isInSuperInterface) {
                break;
            }
        }

        return isInSuperInterface;
    }

    /**
     * Checks whether a {@link SemanticProperty} is part of a {@link SemanticClass}'s list of properties.
     * @param sp {@link SemanticProperty}
     * @param sclass {@link SemanticClass}
     * @return true if sp is a property of sclass or one of its parent classes.
     */
    public boolean isPropertyInClass(SemanticProperty sp, SemanticClass sclass) {
        boolean isInClass = false;
        Iterator<SemanticClass> classes = sclass.listSuperClasses(true);
        while (classes.hasNext()) {
            SemanticClass superclass = classes.next();
            if (superclass.isSWBClass() || superclass.isSWBModel() || superclass.isSWBFormElement()) {
                Iterator<SemanticProperty> propInterfaces = superclass.listProperties();
                while (propInterfaces.hasNext()) {
                    SemanticProperty propSuperClass = propInterfaces.next();
                    if (propSuperClass.equals(sp)) {
                        isInClass = true;
                        break;
                    }
                }

                if (isInClass) {
                    break;
                }
            }
        }
        return isInClass;
    }

    /**
     * Gets String of method signature for a {@link SemanticProperty}.
     * @param sp {@link SemanticProperty}
     * @return String of method signature.
     * @throws IllegalArgumentException if type is not supported for property.
     */
    private String getPropertyMethodSignature(SemanticProperty sp) throws IllegalArgumentException {
        String type = getSemanticPropertyType(sp);
        if (null == type) {
            throw new IllegalArgumentException("Data type '" + sp.getRange() + "' is no supported, property:" + sp);
        }

        String signature = null;
        switch (type) {
            case TYPE_STRING:
                signature = "Property";
                break;
            case TYPE_INT:
                signature = "IntProperty";
                break;
            case TYPE_FLOAT:
                signature = "FloatProperty";
                break;
            case TYPE_DOUBLE:
                signature = "DoubleProperty";
                break;
            case TYPE_LONG:
                signature = "LongProperty";
                break;
            case TYPE_BYTE:
                signature = "ByteProperty";
                break;
            case TYPE_SHORT:
                signature = "ShortProperty";
                break;
            case TYPE_BOOLEAN:
                signature = "BooleanProperty";
                break;
            case TYPE_JAVADATE:
                signature = "DateProperty";
                break;
            case TYPE_BINARY:
                signature = "InputStreamProperty";
                break;
        }

        return signature;
    }

    /**
     * Gets the Java type name for a {@link SemanticProperty}
     * @param sp {@link SemanticProperty}
     * @return String of Java datatype class name.
     */
    private String getSemanticPropertyType(SemanticProperty sp) {
        String type = null;
        if (sp.isString() || sp.isXML()) {
            type = TYPE_STRING;
        } else if (sp.isInt()) {
            type = TYPE_INT;
        } else if (sp.isFloat()) {
            type = TYPE_FLOAT;
        } else if (sp.isDouble()) {
            type = TYPE_DOUBLE;
        } else if (sp.isLong()) {
            type = TYPE_LONG;
        } else if (sp.isByte()) {
            type = TYPE_BYTE;
        } else if (sp.isShort()) {
            type = TYPE_SHORT;
        } else if (sp.isBoolean()) {
            type = TYPE_BOOLEAN;
        } else if (sp.isDateTime() || sp.isDate()) {
            type = TYPE_JAVADATE;
        } else if (sp.isBinary()) {
            type = TYPE_JAVAINPUTSTREAM;
        }

        return type;
    }
}
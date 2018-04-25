package core.loader;

import core.common.*;
import core.generator.ReportGenerator;
import core.render.LiteralRender;
import org.apache.logging.log4j.Logger;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 统一Word报告生成系统（UWR）
 * Jar数据加载类

 *
 */
public class JarLoader extends DataLoader {
    private static DataLoader jarLoader = new JarLoader();
    private Logger logger = ReportGenerator.getLogger();

    private JarLoader() {};

    public static DataLoader newInstance() {
        return jarLoader;
    }

    private String queryResult(DataHolder dh) {
        String stubClass = "StubClass";
        String stubCode =
                "import " + ((JarDataSource)dh.getDataSource()).getFullClassName() + ";\n" +
                "public class StubClass {" + "\n" + "    public static String getData(String n) {" + "\n" +
                "        return " + dh.getExpr() + "; } \n"  + "}" + "\n";

        RuntimeCompiler rc = new RuntimeCompiler();
        rc.addClass(stubClass, stubCode);
        rc.compile();

        String db = (String) MethodInvocationUtils.invokeStaticMethod(rc.getCompiledClass(stubClass), "getData", "empty");
        List<DataHolder> nodedhs = new ArrayList<>();
        CollectionHolder ch = new ListHolder(dh.getDataSource(), "nodes", nodedhs, LiteralRender.newInstance());
        dh.setValue(ch);

        List<DataHolder> attrdhs = new ArrayList<>();
        DataHolder mapdh = new MapHolder(dh.getDataSource(), "item_1" , attrdhs, LiteralRender.newInstance());
        mapdh.setHolderRender(LiteralRender.newInstance());
        nodedhs.add(mapdh);
        attrdhs.add(new VarHolder(dh.getDataSource(), "value", String.valueOf(db), LiteralRender.newInstance()));
        return "success";
    }

    @Override
    public String fill(DataHolder dh) throws Exception {
        String res = null;
        String expr = dh.getExpr();
        String oexpr = expr;

        CollectionHolder val = (CollectionHolder)dh.getValue();

        if (dh == null || expr == null || "".equals(expr) || val!=null) return String.valueOf(0);

        //是否存在变量引用？
        logger.debug(expr);
        String tmpexpr;
        tmpexpr = expr;
        while(expr.matches(".*?\\$\\{.*")) {
            tmpexpr = expr.replaceFirst(".*?\\$\\{", "");
            tmpexpr = tmpexpr.replaceFirst("\\}.*", "");
            DataHolder dhh = DataSourceConfig.newInstance().getDataHolder(tmpexpr);
            if ( dhh != null) {
                if (dhh.getValue() == null) dhh.fillValue();
                String tmpval = dhh.getValue().toString();
                tmpval = tmpval.replace("\\","\\\\");
                oexpr = oexpr.replaceAll(java.util.regex.Pattern.quote("${"+tmpexpr+"}"), "\""+Matcher.quoteReplacement(tmpval)+"\"");
            }
            expr=expr.replaceFirst("\\$\\{", "");
        }
        dh.setExpr(oexpr);
        logger.debug(dh.getExpr());
        res = queryResult(dh);
        logger.debug(res);
        return res;
    }
}
class RuntimeCompiler {
    private JavaCompiler javaCompiler;
    private Map <String, byte[]> classData;
    private MapClassLoader mapClassLoader;
    private ClassDataFileManager classDataFileManager;
    private List<JavaFileObject> compilationUnits;

    RuntimeCompiler() {
        this.javaCompiler = ToolProvider.getSystemJavaCompiler();
        if (javaCompiler == null) {
            throw new NullPointerException( "No JavaCompiler found. Make sure to run this with "
                            + "a JDK, and not only with a JRE");
        }
        this.classData = new LinkedHashMap<>();
        this.mapClassLoader = new MapClassLoader();
        this.classDataFileManager = new ClassDataFileManager(
                        javaCompiler.getStandardFileManager(null, null, null));
        this.compilationUnits = new ArrayList<>();
    }

    public void addClass(String className, String code) {
        String javaFileName = className + ".java";
        JavaFileObject javaFileObject = new MemoryJavaSourceFileObject(javaFileName, code);
        compilationUnits.add(javaFileObject);
    }

    boolean compile() {
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        CompilationTask task = javaCompiler.getTask(null, classDataFileManager,
                        diagnosticsCollector, null, null, compilationUnits);
        boolean success = task.call();
        compilationUnits.clear();
        for (Diagnostic<?> diagnostic : diagnosticsCollector.getDiagnostics()) {
            System.out.println( diagnostic.getKind() + " : " + diagnostic.getMessage(null));
            System.out.println( "Line " + diagnostic.getLineNumber() + " of " + diagnostic.getSource());
            System.out.println();
        }
        return success;
    }

    public Class<?> getCompiledClass(String className) {
        return mapClassLoader.findClass(className);
    }

    private static final class MemoryJavaSourceFileObject extends SimpleJavaFileObject {
        private final String code;

        private MemoryJavaSourceFileObject(String fileName, String code) {
            super(URI.create("string:///" + fileName), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private class MapClassLoader extends ClassLoader {
        @Override
        public Class<?> findClass(String name) {
            byte[] b = classData.get(name);
            return defineClass(name, b, 0, b.length);
        }
    }

    private class MemoryJavaClassFileObject extends SimpleJavaFileObject {
        private final String className;

        private MemoryJavaClassFileObject(String className) {
            super(URI.create("string:///" + className + ".class"), Kind.CLASS);
            this.className = className;
        }

        @Override
        public OutputStream openOutputStream() {
            return new ClassDataOutputStream(className);
        }
    }

    private class ClassDataFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private ClassDataFileManager( StandardJavaFileManager standardJavaFileManager) {
            super(standardJavaFileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(final Location location, final String className, Kind kind, FileObject sibling) {
            return new MemoryJavaClassFileObject(className);
        }
    }

    private class ClassDataOutputStream extends OutputStream {
        private final String className;
        private final ByteArrayOutputStream baos;

        private ClassDataOutputStream(String className) {
            this.className = className;
            this.baos = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) {
            baos.write(b);
        }

        @Override
        public void close() throws IOException {
            classData.put(className, baos.toByteArray());
            super.close();
        }
    }
}

class MethodInvocationUtils {
    public static Object invokeStaticMethod( Class<?> c, String methodName, Object... args) {
        Method m = findFirstMatchingStaticMethod(c, methodName, args);
        if (m == null) {
            throw new RuntimeException("No matching method found");
        } try {
            return m.invoke(null, args);
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method findFirstMatchingStaticMethod( Class<?> c, String methodName, Object ... args) {
        Method methods[] = c.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers())) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (areAssignable(parameterTypes, args)) {
                    return m;
                }
            }
        }
        return null;
    }

    private static boolean areAssignable(Class<?> types[], Object ...args) {
        if (types.length != args.length) {
            return false;
        } for (int i=0; i<types.length; i++) {
            Object arg = args[i];
            Class<?> type = types[i];
            if (arg != null && !type.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }
}

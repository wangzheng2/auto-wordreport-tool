package core.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import core.common.DataSource;
import core.common.DataSourceConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.aspose.words.License;

/**
 * 统一Word报告生成系统（UWR）
 *  @author 王铮 18640548252
 * 
 */
public class ReportGenerator {
	public static String version = "1.10";
	public static Logger logger = null;
	public static String template = null;
	public static String config = null;
	public static String prefix= null;
	public static String output = "resource" + File.separator +"output";
	public static boolean isDebug = false;
	public static String format = "doc";
	private static InputStream license;
	private static String usage = "Usage: uwr -c configfile -o output.doc template.doc";
	private static String opt10 = "-o <arg>\t\t\t\t生成的WORD/PDF位置";
	private static String opt20 = "-d <on|off>\t\t\t工具运行模式：on-调试模式，off-正常模式（默认）";
	private static String opt30 = "-c <arg>\t\t\t\t数据源配置文件，配置所要使用的数据源";
	private static String opt40 = "-f <doc|pdf>\t\t\t结果报告的输出格式，默认为doc格式";
	private static String opt50 = "-p <prefix>\t\t\t指定标签匹配前缀，默认匹配任何前缀";
	private static String opt60 = "-v\t\t\t\t\t显示版本信息";
	private static String opt70 = "-h\t\t\t\t\t显示使用提示";
	private List<String> tmpFiles = new ArrayList<String>();

	public ReportGenerator() {
		if (logger == null) ReportGenerator.getLogger();
	}

	public static void main(String[] args) {
		ReportGenerator rg = new ReportGenerator();
		Logger logger = ReportGenerator.getLogger();

		logger.info("===== Starting Universal Word Report Generator ("+version+") =====");
		getLicense();
		rg.argsRead(args);
		TemplateProcessor tp = new TemplateProcessor();

		long startTime=System.currentTimeMillis();
		try {
			//处理配置文件
			logger.info("===== Step 1: Reading Configurations =====");
			DataSourceConfigProcessor dsch = new DataSourceConfigProcessor(ReportGenerator.config);		
			dsch.parseConfigFile();
			//处理模板：标签重写
			logger.info("===== Step 2: Template Labels Rewriting =====");
			rg.tmpFiles.add(tp.foreachRewrite(ReportGenerator.template));
			//处理模板：静态变量扫描与处理
			logger.info("===== Step 3: Template Variables Parsing =====");
			rg.tmpFiles.add(tp.staticVarExtract(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			//处理模板：<foreach>标签擦除
			logger.info("===== Step 4: Data Initializing and Loading =====");
			rg.tmpFiles.add(tp.foreachEraser(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			//处理模板：<foreach>深度擦除
			while ( tp.getHasProcessed() > 0) {
				rg.tmpFiles.add(tp.foreachEraser(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			}
			//处理模板：<out>标签处理
			logger.info("===== Step 5: Report Generating =====");
			rg.tmpFiles.add(tp.outVarReplace(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			tp.cleanUpReplace(rg.tmpFiles.get(rg.tmpFiles.size() - 1));
			//报告生成：收尾
			logger.info("===== Done =====");
			rg.cleanUp();		
		} catch (Exception e) {
			logger.error("===== Something Wrong =====", e);
		}
		long endTime=System.currentTimeMillis();
        logger.info("Total Time Consumed: "+(endTime-startTime)+"ms");

	}

	//命令行参数处理
	private void argsRead(String[] args) {
		List<String> argsList = new ArrayList<String>();
		List<Option> optsList = new ArrayList<Option>();
		List<String> doubleOptsList = new ArrayList<String>();

		if (args.length <= 0) {
			logger.info(usage);
			System.exit(0);
		} else {
			for (int i = 0; i < args.length; i++) {
				switch (args[i].charAt(0)) {
				case '-':
					if (args[i].length() < 2)
						throw new IllegalArgumentException("Not a valid argument: " + args[i]);
					if (args[i].charAt(1) == '-') {
						if (args[i].length() < 3)
							throw new IllegalArgumentException("Not a valid argument: " + args[i]);
						// --opt
						doubleOptsList.add(args[i].substring(2, args[i].length()));
					} else {
						if (args.length - 1 == i) {
							if ("-v".equals(args[i])) {
								logger.info("Version "+version+" and all rights reserved!");
								System.exit(0);
							}
							if ("-h".equals(args[i])) {
								logger.info(usage+"\n");
								logger.info(opt10);
								logger.info(opt20);
								logger.info(opt30);
								logger.info(opt40);
								logger.info(opt50);
								logger.info(opt60);
								logger.info(opt70);
								System.exit(0);
							}
							throw new IllegalArgumentException("Expected argument after: " + args[i]);
						}
						// -opt
						optsList.add(new Option(args[i], args[i + 1]));
						i++;
					}
					break;
				default:
					// arg
					argsList.add(args[i]);
					break;
				}
			}
		}

		logger.debug("----- commandline -----");	
		for (int i = 0; i < argsList.size(); i++) {		
			// the last template will be processed only
			template = argsList.get(i);
		}
		
		checkArg("template", template);
		
		logger.debug("arguments:");
		logger.debug("    " + template);
		
		for (int i = 0; i < optsList.size(); i++) {
			Option opt = (Option) optsList.get(i);
			logger.debug("    " + opt);
			if ("-c".equals(opt.flag))
				config = opt.opt;
			if ("-p".equals(opt.flag))
				prefix = opt.opt;
			if ("-o".equals(opt.flag)) {
				opt.opt = opt.opt.replaceAll("\\.[a-zA-Z]{1,4}$", "");
				output = opt.opt;
			}
				
			if ("-f".equals(opt.flag)) {
				if ("pdf".equalsIgnoreCase(opt.opt)) 
					format = "pdf";
				else
					format = "doc";
			}
			if ("-d".equals(opt.flag) && "on".equalsIgnoreCase(opt.opt))
				isDebug = true;
		}
		
		checkArg("config", config);
		
		logger.debug("parameters:");
		logger.debug("template: " + template);
		logger.debug("config: " + config);
		logger.debug("format: " + format);
		logger.debug("output: " + output);
		logger.debug("debug: " + isDebug);
		logger.debug("prefix: " + prefix);

	}

	//日志记录器
	public static Logger getLogger() {
		if (logger == null) {
			String log4jConfigFile = System.getProperty("user.dir") + File.separator + "resource" + File.separator + "log4j2.xml";
			ConfigurationSource source = null;
			try {
				source = new ConfigurationSource(new FileInputStream(log4jConfigFile));
				Configurator.initialize(null, source);
				logger = LogManager.getRootLogger();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logger;
	}
	
    //Aspose的许可
	public static boolean getLicense() {
		boolean result = false;
		try {
			license = new FileInputStream("resource"+File.separator + "license.xml");
			License aposeLic = new License();
			aposeLic.setLicense(license);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//收尾工作
	private void cleanUp() {	
		Iterator<String> itr = tmpFiles.iterator();
		if(!ReportGenerator.isDebug)
		while(itr.hasNext()) {
			String filename = itr.next();
			File file = new File(filename);
			//if (file.exists()) file.delete();
		}

		List<DataSource> dss = DataSourceConfig.newInstance().getDataSources();
		Iterator<DataSource> itrds = dss.iterator();
		while(itrds.hasNext()){
			DataSource ds = itrds.next();
			ds.cleanUp();
		}
	}

	//参数检查
	private void checkArg(String name, String argvalue) {
		if (argvalue == null || "".equals(argvalue)) {
			logger.info("missing " + name + " file name! ");
			logger.info(usage);
			System.exit(0);
		}
	}
	
	//参数结构
	static class Option {
		String flag, opt;

		public Option(String flag, String opt) {
			this.flag = flag;
			this.opt = opt;
		}

		public String toString() {
			return flag + " " + opt;
		}
	}
}

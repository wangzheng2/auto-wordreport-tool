package core.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.aspose.words.License;

public class ReportGenerator {
	public static Logger logger = null;
	public static String template = null;
	public static String config = null;
	public static String output = null;
	private static boolean isDebug = false;
	private static InputStream license;
	private List<String> tmpFiles = new ArrayList<String>();

	public ReportGenerator() {
		if (logger == null) ReportGenerator.getLogger();
	}

	public static void main(String[] args) {
		ReportGenerator rg = new ReportGenerator();
		Logger logger = ReportGenerator.getLogger();
		TemplateProcessor tp = new TemplateProcessor();

		logger.info("===== Starting Universal Word Report Generator =====");
		getLicense();
		rg.argsRead(args);	
		try {
			logger.info("===== Step 1: Reading Configurations =====");
			DataSourceConfigProcessor dsch = new DataSourceConfigProcessor(ReportGenerator.config);		
			dsch.parseConfigFile();
			logger.info("===== Step 2: Template Labels Rewriting =====");
			rg.tmpFiles.add(tp.foreachRewrite(ReportGenerator.template));
			logger.info("===== Step 3: Template Variables Parsing =====");
			rg.tmpFiles.add(tp.staticVarExtract(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			logger.info("===== Step 4: Data Initializing and Loading =====");
			rg.tmpFiles.add(tp.foreachEraser(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			while ( tp.getHasProcessed() > 0) {
				rg.tmpFiles.add(tp.foreachEraser(rg.tmpFiles.get(rg.tmpFiles.size() - 1)));
			}
			logger.info("===== Step 5: Report Generating =====");
			tp.outVarReplace(rg.tmpFiles.get(rg.tmpFiles.size() - 1));
			logger.info("===== Done =====");
			rg.cleanUp();		
		} catch (Exception e) {
			logger.error("===== Something Wrong =====", e);
		}	
	}

	private void argsRead(String[] args) {
		List<String> argsList = new ArrayList<String>();
		List<Option> optsList = new ArrayList<Option>();
		List<String> doubleOptsList = new ArrayList<String>();

		if (args.length <= 0) {
			logger.info("Usage: uwr -c configfile -o output.doc template.doc ");
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
						if (args.length - 1 == i)
							throw new IllegalArgumentException("Expected argument after: " + args[i]);
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
		logger.debug("arguments:");
		for (int i = 0; i < argsList.size(); i++) {
			logger.debug("    " + argsList.get(i));
			// the last template will be processed only
			template = argsList.get(i);
		}
		logger.debug("parameters:");
		for (int i = 0; i < optsList.size(); i++) {
			Option opt = (Option) optsList.get(i);
			logger.debug("    " + opt);
			if ("-c".equals(opt.flag))
				config = opt.opt;
			if ("-o".equals(opt.flag))
				output = opt.opt;
			if ("-d".equals(opt.flag) && "on".equalsIgnoreCase(opt.opt))
				isDebug = true;
		}
		logger.debug("template: " + template);
		logger.debug("config: " + config);
		logger.debug("output: " + output);
		logger.debug("debug: " + isDebug);
	}

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
	
	private void cleanUp() {	
		Iterator<String> itr = tmpFiles.iterator();
		if(!ReportGenerator.isDebug)
		while(itr.hasNext()) {
			String filename = itr.next();
			File file = new File(filename);
			if (file.exists()) file.delete();
		}
		
		//关闭所有打开的文件以及数据库的连接
		//to be finished!!!
	}

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

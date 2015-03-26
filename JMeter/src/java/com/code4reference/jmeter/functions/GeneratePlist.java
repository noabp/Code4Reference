package com.code4reference.jmeter.functions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class GeneratePlist extends AbstractFunction {

	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__GeneratePlist";
	private static final int MIN_PARAM_COUNT = 2;
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	static {
		desc.add("Generate plist from the given file path. Returns the full path of the generated plist.");
	}
	
	private Object[] values;
	
	/**
	 * No-arg constructor.
	 */
	public GeneratePlist() {
        super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {
		JMeterVariables vars = getVariables();
		
		String originalFile = ((CompoundVariable) values[0]).execute();
		String location = ((CompoundVariable) values[1]).execute();
		String filename = location.trim() + "/" + UUID.randomUUID().toString() + ".plist";
		
		log.info("Got original file: " + originalFile + ", location: " + location);
		
		final Path source = Paths.get(originalFile);
		final Path target = Paths.get(filename);
		PrintWriter out = null;
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			out = new PrintWriter(new BufferedWriter(new FileWriter(target.toFile(), true)));
			out.println(" //" + filename);
			
		} catch (final IOException e) {
			log.error("Got the following error while generating plist, original plist: " + originalFile, e);
		} finally {
			if(out != null) {
				out.close();
			}
		}

		return target.toFile().getAbsolutePath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
		checkMinParameterCount(parameters, MIN_PARAM_COUNT);
		values = parameters.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReferenceKey() {
		return KEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getArgumentDesc() {
		return desc;
	}
}

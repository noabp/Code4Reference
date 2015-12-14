package com.code4reference.jmeter.functions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.log4j.lf5.util.StreamUtils;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class GenerateApp extends AbstractFunction {

	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__GenerateApp";
	private static final int MIN_PARAM_COUNT = 6;
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	static {
		desc.add("Generate app from the given file path. Returns the full path of the generated app.");
	}
	
	private Object[] values;
	
	/**
	 * No-arg constructor.
	 */
	public GenerateApp() {
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
        String appListDelay = ((CompoundVariable) values[2]).execute();
        String numAppsPerDevice = ((CompoundVariable) values[3]).execute();
        String numOriginalApps = ((CompoundVariable) values[4]).execute();
        String numDevices = ((CompoundVariable) values[5]).execute();

		String filename = location.trim() + "/" + UUID.randomUUID().toString() + ".apk";
		
		log.info("Got original file: " + originalFile + ", location: " + location);
		ZipArchiveOutputStream append = null;
		try {
			
		final ZipFile zipfile = new ZipFile(originalFile);
		append = new ZipArchiveOutputStream(new FileOutputStream(filename));

		
			// first, copy contents from existing file
			final Enumeration<? extends ZipArchiveEntry> entries = zipfile.getEntries();
			while (entries.hasMoreElements()) {
				final ZipArchiveEntry e = entries.nextElement();
				append.putArchiveEntry(e);
				if (!e.isDirectory()) {
					StreamUtils.copy(zipfile.getInputStream(e), append);
				}
				append.closeArchiveEntry();
			}

            long mean = new DateTime(DateTimeZone.UTC).getMillis() / Long.valueOf(appListDelay.trim());
            double std = (Long.valueOf(numDevices.trim()) * Long.valueOf(numAppsPerDevice.trim())) / (30 * Long.valueOf(numOriginalApps.trim()));

            double randComment = new Random().nextGaussian() * std + mean;
			append.setComment(String.valueOf(randComment));
			zipfile.close();
		} catch(Exception e) {
			// handle error
			log.error("Got the following error while generating app, original app: " + originalFile, e);
		} finally {
			if(append != null)
				try {
				append.close();
				} catch(Exception e) {
					// ignore
				}
		}

		return new File(filename).getAbsolutePath();
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

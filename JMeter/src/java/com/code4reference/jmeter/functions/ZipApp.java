package com.code4reference.jmeter.functions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ZipApp extends AbstractFunction {

	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__ZipApp";
	private static final int MIN_PARAM_COUNT = 2;
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	static {
		desc.add("Prepare zip file to upload to server, including the basic info of the app and the app itself. Return the full path of the created zip file");
	}
	
	private Object[] values;
	
	/**
	 * No-arg constructor.
	 */
	public ZipApp() {
        super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {
		JMeterVariables vars = getVariables();
		
		String basicInfo = ((CompoundVariable) values[0]).execute();
		String appFilePath = ((CompoundVariable) values[1]).execute();
		appFilePath = appFilePath.trim();
		
		try {
		final String tempDir = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString();
		final List<String> filesToZip = new ArrayList<String>();

		final File tmpDirFile = new File(tempDir);
		tmpDirFile.mkdir();
		
		final File jsonFile = new File(tempDir + File.separator + "input.json");
		final File zipFile = new File(tempDir + File.separator + "upload.zip");
		FileUtils.writeStringToFile(jsonFile, basicInfo);
		filesToZip.add(jsonFile.getAbsolutePath());
		filesToZip.add(appFilePath);
		createZip(filesToZip, zipFile.getAbsolutePath());
		return zipFile.getAbsolutePath();
		} catch(Exception e) {
			log.error("Got error when trying to zip. App file: " + appFilePath + ", basic info: " + basicInfo, e);
		}
		
		return null;
	}
	
	public static void createZip(final List<String> filesToZip, final String outputZipFile) throws IOException {

		final FileOutputStream fos = new FileOutputStream(outputZipFile);
		final ZipOutputStream zos = new ZipOutputStream(fos);
	
		for (final String file : filesToZip) {
			addToZipFile(file, zos);
		}
		zos.close();
		fos.close();

	}

	public static void addToZipFile(final String fileName, final ZipOutputStream zos) throws FileNotFoundException, IOException {

		final File file = new File(fileName);
		final FileInputStream fis = new FileInputStream(file);
		final ZipEntry zipEntry = new ZipEntry(file.getName());
		zos.putNextEntry(zipEntry);

		final byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
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

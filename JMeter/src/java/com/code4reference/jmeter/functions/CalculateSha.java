package com.code4reference.jmeter.functions;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class CalculateSha extends AbstractFunction {

	private static final List<String> desc = new LinkedList<String>();
	private static final String KEY = "__CalculateSha";
	private static final int MIN_PARAM_COUNT = 1;
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	static {
		desc.add("Calculate sha256sum on the given file. Returns the calculated sha");
	}
	
	private Object[] values;
	
	/**
	 * No-arg constructor.
	 */
	public CalculateSha() {
        super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {
		JMeterVariables vars = getVariables();
		
		String file = ((CompoundVariable) values[0]).execute();
		final StringBuffer hexString = new StringBuffer();
		FileInputStream fis = null;
		try {
		final MessageDigest md = MessageDigest.getInstance("SHA-256");
		fis = new FileInputStream(file);
		
			final byte[] dataBytes = new byte[1024];

			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}

			final byte[] mdbytes = md.digest();
			for (int i = 0; i < mdbytes.length; i++) {
				hexString.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch(Exception e){ 
			log.error("Got the following error while calculating sha on file: " + file, e);
		} finally {
			try {
			if (fis != null)
				fis.close();
			} catch(Exception e) {
				// ignore
			}
		}
		return hexString.toString();
		
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

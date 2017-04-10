/*****************************************************************************
 * Copyright (c) 2017 Minh Duc Cao (minhduc.cao@gmail.com).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the names of the institutions nor the names of the contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 ****************************************************************************/
/*                           Revision History                                
 * 28-02-2017 - Minh Duc Cao: Created       
 *                                
 ****************************************************************************/
package org.micromanage.pipeline.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.micromanage.pipeline.MicroManageConfig;
import org.micromanage.pipeline.sample.BacterialSample;

import japsa.seq.SequenceOutputStream;
import japsa.util.ProcessManagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJob {
	private static final Logger LOG = LoggerFactory.getLogger(BacterialSample.class);
	
	public static final int JOB_FAILED = 0;
	public static final int JOB_CREATED = 1;
	public static final int JOB_WAIT = 2;
	public static final int JOB_SUBMITED = 3;
	public static final int JOB_RUNNING = 4;
	public static final int JOB_COMPLETED = 5;
	
	public static final String [] STATUS_STRING = new String[6];
	static{		
		STATUS_STRING[JOB_FAILED]    = "Job failed";
		STATUS_STRING[JOB_CREATED]   = "Job created";
		STATUS_STRING[JOB_WAIT]      = "Job waiting to submit";
		STATUS_STRING[JOB_SUBMITED]  = "Job submitted";
		STATUS_STRING[JOB_RUNNING]   = "Job running";
		STATUS_STRING[JOB_COMPLETED] = "Job completed";		
	}
	
	String fileSuccess = "";
	ArrayList<String> listInFiles = new ArrayList<String>();
		
	
	protected int status = JOB_CREATED;
	
	protected int numTried = 0;	
	
	private int memReq = 8;//memory required
	private int cpuReq = 1;//CPU required
	private int hourReq = 160;//hours required
	
	BacterialSample sample;
	
	//HPCConfig hpcConfig;
	
	long timeSleep = 30 * 60 *1000;

	public AbstractJob(BacterialSample sample){
		this.sample = sample;
	}
	
	
	public boolean checkCompleted(){
		if (status >= JOB_COMPLETED)
			return true;
		
		boolean finished = Files.exists(Paths.get(fileSuccess));		
		if (finished){
			LOG.info("Job " + jobName() +" completed");
			status = JOB_COMPLETED;
		}
		LOG.info("Job " + jobName() +" = " + status);
		return finished;
	}
	
	public boolean submit(){		
		try {
			//Prepare the script
			String script = MicroManageConfig.getConfig().scriptDir + "/" + jobName()+ ".slm";
			SequenceOutputStream sos = SequenceOutputStream.makeOutputStream(script);
			sos.print(prepareScript());
			sos.close();
			
			//submit the script
			
			ProcessBuilder pb = new ProcessBuilder("sbatch", script); 
			int returnRun = ProcessManagement.runProcess(pb);
			if (returnRun == 0 ){
				LOG.info("Submit " + jobName() + " successfully");
				//continue;
			}else{
				LOG.info("Submit " + jobName() + " FAIL");
				//TODO: get the error message
				status = JOB_FAILED;
				return false;
			}
			status = JOB_SUBMITED;
			return true;
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = JOB_FAILED;
			return false;
		}
	}

	public String prepareScript(){
		String fileContent = 
				"#!/bin/sh\n"
				+ "#SBATCH --job-name=" + jobName() + "\n"
				+ "#SBATCH --output=scripts/logs/"+jobName() + ".out\n"
				+ "#SBATCH --error=scripts/logs/"+jobName() + ".err\n"
				+ "#SBATCH --nodes=1\n"
				+ "#SBATCH --ntasks=" + cpuReq + "\n"
//				+ "##SBATCH --ntasks-per-node=4\n"				
//				+ "##SBATCH --mem="+memReq+"000 # mb\n\n"
				+ command();				
				;
		
		return fileContent;		
	}
	/**
	 * Execute this
	 * @return
	 */
	public abstract String command();
	public abstract String jobName();
	
	
	//The boring stuff set and get

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the numTried
	 */
	public int getNumTried() {
		return numTried;
	}


	/**
	 * @param numTried the numTried to set
	 */
	public void setNumTried(int numTried) {
		this.numTried = numTried;
	}

	/**
	 * @return the memReq
	 */
	public int getMemReq() {
		return memReq;
	}


	/**
	 * @param memReq the memReq to set
	 */
	public void setMemReq(int memReq) {
		this.memReq = memReq;
	}


	/**
	 * @return the cpuReq
	 */
	public int getCpuReq() {
		return cpuReq;
	}


	/**
	 * @param cpuReq the cpuReq to set
	 */
	public void setCpuReq(int cpuReq) {
		this.cpuReq = cpuReq;
	}

	/**
	 * @return the hourReq
	 */
	public int getHourReq() {
		return hourReq;
	}

	/**
	 * @param hourReq the hourReq to set
	 */
	public void setHourReq(int hourReq) {
		this.hourReq = hourReq;
	}


	/**
	 * @return the sample
	 */
	public BacterialSample getSample() {
		return sample;
	}		

}

/*****************************************************************************
 * Copyright (c) 2006-2017 Minh Duc Cao (minhduc.cao@gmail.com).
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
 * 02-03-2017 - Minh Duc Cao: Created
 *
 ****************************************************************************/

package org.micromanage.pipeline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import org.micromanage.pipeline.job.AbstractJob;
import org.micromanage.pipeline.sample.BacterialSample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author minhduc
 *
 */
public class PipelineThread implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(PipelineThread.class);


	private boolean running = true;	
	private long sleepTime = 30 * 60 * 1000;
	MicroManageConfig mmConfig;

	public PipelineThread(MicroManageConfig mmConfig){
		this.mmConfig = mmConfig;
	}

	/**
	 * Run the pipeline:
	 * 1. Check the queue for the status of all the submited jobs
	 * 2. Go through the pipeline for every sample, and to see a. if the job
	 * has completed (checking the success file), b. check the job is still in
	 * the queue, and if it is running of queueing
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */

	public void runPipeline() throws IOException, InterruptedException{
		ArrayList<BacterialSample>  samples = mmConfig.getSamples();
		HashMap<String,String> slurmStatus = checkQueue();

		LOG.info(LocalDate.now().toString() + ": there are " + samples.size() + " samples");
		for (BacterialSample sample:samples){
			ArrayList<AbstractJob>  jobs = sample.getJobs();
			for (AbstractJob job:jobs){
				if (!job.checkCompleted()){
					//submit job if not yet submitted
					int status = job.getStatus(); 
					if ( status < AbstractJob.JOB_SUBMITED && status > AbstractJob.JOB_FAILED){
						if (!job.submit())
							break;//TODO: something wrong, please handle this
					}	
					if (status > AbstractJob.JOB_SUBMITED){
						String jobQueueStatus = slurmStatus.get(job.jobName());
						if (jobQueueStatus == null){
							//dont see in the queue, it must have failed
							job.setStatus(AbstractJob.JOB_FAILED);
							//TODO: redo if failed less than certain number???
						}else if ("R".equals("jobQueueStatus")){
							job.setStatus(AbstractJob.JOB_RUNNING);
						}
					}
					//else{SUBMITED}
					//up to here, the job should have been submitted but not completed
					break;//break this inner loop (for this sample) regarding the status					
				}//if
			}//for job
		}//for sample
	}

	public synchronized void setRunning(boolean running){
		this.running = running;
	}

	public boolean isRunning(){
		return this.running;
	}

	public HashMap<String,String> checkQueue() throws IOException, InterruptedException{

		HashMap<String,String> slurmStatus = new HashMap<String, String>();		
		ProcessBuilder pb = new ProcessBuilder("squeue", 
				"-u",
				"$USER",
				"-a",
				"-h"
				);
		Process bwaProcess  = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(bwaProcess.getInputStream()));
		String line = "";		
		while ((line = reader.readLine()) != null){
			line = line.trim();
			if (line.length() < 1)
				continue;

			String [] toks = line.split("\\s+");
			String existingName = slurmStatus.put(toks[5], toks[6]);
			if (existingName != null){
				LOG.warn("Job  " + toks[5] + " is duplicated");
			}
		}
		bwaProcess.waitFor();
		return slurmStatus;
	}

	public void run(){
		while (running){
			try {
				runPipeline();
				LOG.info("Pipeline thread sleeps for " + (sleepTime / 1000.0) + " seconds");
				Thread.sleep(sleepTime);
				LOG.info("Pipeline thread wakes up");
			} catch (IOException | InterruptedException e) {
				LOG.info("Pipeline thread interrupted while sleeping");
				e.printStackTrace();
			}
		}
	}
}

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

import java.io.File;

import org.micromanage.pipeline.MicroManageConfig;
import org.micromanage.pipeline.sample.BacterialSample;


public class Assembly extends AbstractJob{
	/**
	 * @param sample
	 */
	public Assembly(BacterialSample sample) {
		super(sample);
		this.setCpuReq(16);
		this.setMemReq(62);	

		MicroManageConfig config = MicroManageConfig.getConfig();
		String outPath = config.baseDir + "/" + config.assemblyDir + "/" + sample.samplePath();
		//listOutFiles.add(outPath + "/" + sample.getSampleID() + ".fasta");
		fileSuccess = outPath + "/" + this.jobName() + ".success";		
		File file = new File(outPath);
		if (!file.exists()){
			file.mkdirs();
		}
	}

	@Override
	public String command() {
		MicroManageConfig config = MicroManageConfig.getConfig();
		String inPath = config.baseDir + "/" + config.trimmedDir + "/" + sample.samplePath();		
		String outPath = config.baseDir + "/" + config.assemblyDir + "/" + sample.samplePath();

		String cmd = "echo START AT `date`\n"
				+ config.exeSpades + " -m " + (getMemReq() - 2) + " -t " + getCpuReq() + " -k 21,33,55,77,99,127 --careful \\\n"
				+ " --pe1-1 " + inPath + "_P1.fastq.gz --pe1-2 " + inPath + "_P2.fastq.gz \\\n"						 
				+ " -o " + outPath + "/spades  && \\\n"
				+ "jsa.amra.assppro --sample " + sample.getSampleID() + " --input " + outPath + "/spades/contigs" +
				".fasta --output " + outPath + "/" + sample.getSampleID() + ".fasta --summary " + fileSuccess + "\n"
				+ "echo $? AT `date`";
		return cmd;
	}

	@Override
	public String jobName() {		 
		return "sp" + sample.getSampleID();
	}

}

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
import org.micromanage.pipeline.sample.Gram;


public class AnnotationJob extends AbstractJob{
	/**
	 * @param sample
	 */
	public AnnotationJob(BacterialSample sample) {
		super(sample);
		this.setCpuReq(8);
		this.setMemReq(32);	
		
		MicroManageConfig config = MicroManageConfig.getConfig();
		String outPath = config.baseDir + "/" + config.annotationDir + "/" + sample.samplePath();
		fileSuccess = outPath + "/" + this.jobName() + ".success";
		
		new File(outPath).mkdirs();		
	}
		

	@Override
	public String command() {
		MicroManageConfig config = MicroManageConfig.getConfig();
		String inPath = config.baseDir + "/" + config.assemblyDir + "/" + sample.samplePath();
		
		String outPath = config.baseDir + "/" + config.annotationDir + "/" + sample.samplePath();		
		String cmd = "prokka --force --mincontiglen 200 --cpus " + getCpuReq() + " \\\n "
				+ "  --genus  " + sample.getGenus().getName() + " \\\n "
				+ "  --species  " + sample.getSpecies().getSpeciesName() + " \\\n "				
				+ "  --outdir " + outPath +  " \\\n " 
				+ "  --prefix " + sample.getSampleID() +  " \\\n "
				+ "  --locus " + sample.getSampleID() +  " \\\n "
                + "  --proteins dbs/card/CARD \\\n "
				+ ((sample.getGenus().getGram() == Gram.NEGATIVE)?"  --gram neg \\\n":"")
				+ ((sample.getGenus().getGram() == Gram.POSITIVE)?"  --gram pos \\\n":"")
				+ "  " + inPath + "/" + sample.getSampleID() + ".fasta && \\\n"
				+ "touch " + fileSuccess + "\n"
				+ "echo $? AT `date`\n";
		return cmd;
	}

	@Override
	public String jobName() {		 
		return "pr" + sample.getSampleID();
	}

}

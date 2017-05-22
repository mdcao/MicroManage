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

public class TrimIlluminaJob extends AbstractJob{

	/**
	 * @param sample
	 */
	public TrimIlluminaJob(BacterialSample sample) {
		super(sample);
		this.setCpuReq(8);
		this.setMemReq(8);
		MicroManageConfig config = MicroManageConfig.getConfig();

        outFolderPath = config.baseDir + "/" + config.trimmedDir + "/" + sample.samplePath() + "/";
        File folderFile = new File(outFolderPath);
        if (!folderFile.exists()){
            folderFile.mkdirs();
        }
        fileSuccess = outFolderPath + sample.getSampleID() + ".success";
	}

	@Override
	public String command() {
		MicroManageConfig config = MicroManageConfig.getConfig();

        String datPath = config.baseDir + "/" + config.rawDir + "/" + sample.samplePath() + "/" + sample.getSampleID();
        String outPath = outFolderPath + sample.getSampleID();

        String cmd = "echo START AT `date`\n"
                + config.exeJava + " -jar " + config.pathTrimmomatic + "/trimmomatic.jar PE \\\n"
                + "  -threads " + this.getCpuReq() + " \\\n"
                + "  -phred33 \\\n"
                + "  -trimlog " + outPath+ "_trim.log \\\n"
                + "  " + datPath + "_R1.fastq.gz " + datPath + "_R2.fastq.gz \\\n"
                + "  " + outPath + "_P1.fastq.gz " + outPath + "_U1.fastq.gz "
                +        outPath + "_P2.fastq.gz " + outPath + "_U2.fastq.gz \\\n"
                + "  ILLUMINACLIP:" + config.pathTrimmomatic + "/adapters/adapters.fa:3:30:10 SLIDINGWINDOW:4:15 LEADING:10 TRAILING:10 MINLEN:36 &&  \\\n"
                + "rm -f " + outPath + "_U1.fastq.gz " + outPath + "_U2.fastq.gz " + outPath+ "_trim.log &&  \\\n"
                + "touch  " + fileSuccess + "\n"
                + "echo $? AT `date`";
        return cmd;
	}

	@Override
	public String jobName() {		 
		return "tr" + sample.getSampleID();
	}

}

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

import org.micromanage.pipeline.MicroManageConfig;
import org.micromanage.pipeline.job.AbstractJob;
import org.micromanage.pipeline.sample.BacterialSample;

import java.io.File;


public class Alignment2AssemblyJob extends AbstractJob{
	/**
	 * @param sample
	 */
	public Alignment2AssemblyJob(BacterialSample sample) {
		super(sample);
		this.setCpuReq(4);
		this.setMemReq(8);

		MicroManageConfig config = MicroManageConfig.getConfig();
        outFolderPath = config.baseDir + "/" + config.variationDir + "/" + sample.samplePath() + "/";
        File folderFile = new File(outFolderPath);
        if (!folderFile.exists()){
            folderFile.mkdirs();
        }
        fileSuccess = outFolderPath + sample.getSampleID() + ".success";
	}

	@Override
	public String command() {
		MicroManageConfig config = MicroManageConfig.getConfig();
        String outPath = outFolderPath;
        String annoPath = config.baseDir + "/" + config.annotationDir + "/" + sample.samplePath() + "/";
        String trimPath = config.baseDir + "/" + config.trimmedDir + "/" + sample.samplePath() + "/" + sample.getSampleID();

        String cmd = "echo START AT `date`\n"
                ///////////////////////////////////////////////////////////////////
                + config.exeBwa + " mem -t " + getCpuReq()
                + " -R \"@RG\\\\tID:" + sample.getSampleID() + "\\\\tSM:" + sample.getSampleID() + "\" "
                + annoPath + "bwaIndex/"+sample.getSampleID() + " "
                + trimPath + "_P1.fastq.gz " + trimPath + "_P2.fastq.gz > "
                + outPath  + "assembly_" + sample.getSampleID() + ".sam && \\\n"
                ///////////////////////////////////////////////////////////////////
                + config.exeSamtools + " view -buS " + outPath  + "assembly_" + sample.getSampleID() + ".sam | "
                + config.exeSamtools + " sort -o " + outPath  + "assembly_" + sample.getSampleID() + ".bam - && \\\n"
                ///////////////////////////////////////////////////////////////////
                + config.exeSamtools + " index " + outPath  + "assembly_" + sample.getSampleID() + ".bam && \\\n"
                + "rm -f " + outPath  + "assembly_" + sample.getSampleID() + ".sam && \\\n"
                ///////////////////////////////////////////////////////////////////
                + config.exeFreeBayes + " -f " + annoPath + sample.getSampleID() + ".fna -F 0.1 -C 2 "
                + outPath  + "assembly_" + sample.getSampleID() + ".bam -v "
                + outPath  + "assembly_" + sample.getSampleID() + ".vcf && \\\n"
                ///////////////////////////////////////////////////////////////////
                + "touch  " + fileSuccess + "\n"
                + "echo $? AT `date`";
        return cmd;
	}
	//freebayes -f ref.fa -F 0.01 -C 1 --pooled-continuous aln.bam >var.vcf


	@Override
	public String jobName() {		 
		return "av" + sample.getSampleID();
	}

}

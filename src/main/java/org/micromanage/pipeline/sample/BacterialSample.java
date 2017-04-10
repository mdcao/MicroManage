/*
* Copyright (c) 2017 Minh Duc Cao (minhduc.cao@gmail.com).
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
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

package org.micromanage.pipeline.sample;

import java.util.ArrayList;

import javax.json.JsonObject;

import org.micromanage.pipeline.MicroManageConfig;
import org.micromanage.pipeline.job.AbstractJob;
import org.micromanage.pipeline.job.AnnotationJob;
import org.micromanage.pipeline.job.Assembly;
import org.micromanage.pipeline.job.TrimIlluminaReads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author minhduc
 *         <p>
 *         Implementation of a sample
 */
public class BacterialSample {
    private static final Logger LOG = LoggerFactory.getLogger(BacterialSample.class);

    String sampleID;
    String desc = "";


    Species species;
    Genus genus;
    Strain strain;


    String readFile1 = null,
            readFile2 = null;
    String longRead = null;
    boolean isReference = false;
    String genome = null;
    String annotation = null;

    ArrayList<AbstractJob> jobs;

    /**
     * Construct a sample
     */
    public BacterialSample(JsonObject sampleObject) {
        try {
            MicroManageConfig config = MicroManageConfig.getConfig();
            sampleID = sampleObject.getString("sampleID");
            genus = config.getGenus(sampleObject.getString("genus"));
            species = config.getSpecies(sampleObject.getString("species"));
            desc = sampleObject.getString("desc");

            //add jobs
            jobs = new ArrayList<AbstractJob>();

            jobs.add(new TrimIlluminaReads(this));
            jobs.add(new Assembly(this));
            jobs.add(new AnnotationJob(this));

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    public String getSampleID() {
        return sampleID;
    }


    /**
     * @return the species
     */
    public Species getSpecies() {
        return species;
    }


    /**
     * @return the genus
     */
    public Genus getGenus() {
        return genus;
    }


    public String samplePath() {
        return genus.getId() + "/" + species.getId() + "/" + sampleID;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String toString() {
        return sampleID;
    }

    public ArrayList<AbstractJob> getJobs() {
        return jobs;
    }

}

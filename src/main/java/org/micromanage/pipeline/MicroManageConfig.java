/*
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

package org.micromanage.pipeline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.micromanage.pipeline.job.AbstractJob;
import org.micromanage.pipeline.sample.BacterialSample;
import org.micromanage.pipeline.sample.Genus;
import org.micromanage.pipeline.sample.Gram;
import org.micromanage.pipeline.sample.Species;
import org.micromanage.pipeline.sample.Strain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author minhduc
 *
 */
public class MicroManageConfig {
	private static final Logger LOG = LoggerFactory.getLogger(MicroManageConfig.class);

	//TODO: in this config file, the followings need to be set/checked
	//TODO:  *paths to files of all intermediate steps
	//TODO:  *paths and version of all binaries
	//These can be eithet set by setting path before hand, or absolute path to executive
	public String exeJava = "java";
	public String exeProkka = "prokka";
	public String exeFreeBayes = "freebayes";
	public String exeSpades = "spades.py";
	public String exeBlastn = "blastn";
	public String exePython = "python";
	public String exeBwa = "bwa";
	public String exeSamtools = "samtools";
	public String pathTrimmomatic = "/sw/trimmomatic/Trimmomatic-0.36/";


	public String dbDir = "dbs";
	public String scriptDir = "scripts";
	public String logDir = "logs";
	public String baseDir = "data";
	public String rawDir = "Raw";
	public String trimmedDir = "Trimmed";
	public String assemblyDir = "Assemblies";
	public String annotationDir = "Annotation";
	

	public String genusFile =   dbDir + "/genuses.json";
	public String speciesFile = dbDir + "/species.json";
	public String sampleFile =  dbDir + "/samples.json";

	private PipelineThread pipeline = null;
	private Thread thread = null;

	public PipelineThread getPipeline(){
		return pipeline;
	}
	/* It also acts like a central database and provide methods such as getSpecies/
	 * genus/strain from name
	 */

	/**
	 * The configuration of the system
	 */
	private static MicroManageConfig MM_CONFIG = null;
	//private static String configPath = null;

	private HashMap<String, Genus>   genusesDb = new HashMap<String, Genus> ();
	private HashMap<String, Species> speciesDb = new HashMap<String, Species> ();
	private HashMap<String, Strain>  strainDb = new HashMap<String, Strain> ();

	private ArrayList<BacterialSample> samples = new ArrayList<BacterialSample> ();


	/**
	 * Make constructor private to avoid creating multiple configuration
	 */
	private MicroManageConfig(){
		String [] genusStr = {
				"Acinetobacter",
				"Enterobacter",
				"Enterococcus",
				"Escherichia",
				"Helicobacter",
				"Klebsiella",
				"Listeria",
				"Mycobacterium",
				"Pseudomonas",
				"Salmonella",
				"Staphylococcus",
				"Streptococcus",
				"Vibrio"				
		};
		String [] speciesStr = {
				"Acinetobacter_baumannii",
				"Enterobacter_aerogenes",
				"Enterobacter_cloacae",
				"Enterococcus_faecium",
				"Escherichia_coli",
				"Helicobacter_pylori",
				"Klebsiella_pneumoniae",
				"Klebsiella_quasipneumoniae",
				"Listeria_monocytogenes",
				"Mycobacterium_tuberculosis",
				"Pseudomonas_aeruginosa",
				"Salmonella_bongori",
				"Salmonella_enterica",
				"Staphylococcus_aureus",
				"Streptococcus_pneumoniae",
				"Vibrio_cholerae"
		};

		//Populate common information,
		//TODO: to convert to json format
		for (String st:genusStr){
			genusesDb.put(st, new Genus(st,st));
		}
		
		genusesDb.get("Acinetobacter").setGram(Gram.NEGATIVE);
		genusesDb.get("Enterobacter").setGram(Gram.NEGATIVE);
		genusesDb.get("Enterococcus").setGram(Gram.POSITIVE);
		genusesDb.get("Escherichia").setGram(Gram.NEGATIVE);
		genusesDb.get("Helicobacter").setGram(Gram.NEGATIVE);
		genusesDb.get("Klebsiella").setGram(Gram.NEGATIVE);
		genusesDb.get("Pseudomonas").setGram(Gram.NEGATIVE);
		genusesDb.get("Salmonella").setGram(Gram.NEGATIVE);
		genusesDb.get("Staphylococcus").setGram(Gram.POSITIVE);
		genusesDb.get("Streptococcus").setGram(Gram.POSITIVE);
		genusesDb.get("Vibrio").setGram(Gram.NEGATIVE);
			
		
		//genusesDb.g

		for (String st:speciesStr){
			speciesDb.put(st, new Species(st,st.replaceAll("_", " ")));
		}		 
		//Read the list of samples
	}

	public synchronized boolean addSample(BacterialSample sample){
		return samples.add(sample);
	}

	public boolean startPipeline(){
		if (pipeline != null){
			LOG.error("Pipeline has been set up");
			return false;
		}
		pipeline = new PipelineThread(this);		
		thread = new Thread(pipeline);

		LOG.info("Starting pipeline");
		thread.start();
		LOG.info("Pipeline is up");
		
		return true;
	}
	
	public void interruptPipeline(){
		thread.interrupt();
	}
	
	/**
	 * Write the current list of sample to sample file
	 */
	public void writeSamples(){
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

		for (BacterialSample sample:samples){
			JsonObject jsonObject = Json.createObjectBuilder()					   
					.add("sampleID", sample.getSampleID())
					.add("genus", sample.getGenus().getId())
					.add("species", sample.getSpecies().getId())
					.add("desc", sample.getDesc())
					.build();
			jsonArrayBuilder.add(jsonObject);			
		}


		try{ 
			PrintWriter pw = new PrintWriter(sampleFile);
			JsonWriter jsonWriter = Json.createWriter(pw);
			jsonWriter.writeArray(jsonArrayBuilder.build());
			jsonWriter.close();
		}catch (IOException e){
			throw new RuntimeException(e.getMessage());
		}	
	}

	/**
	 * Read existing samples
	 */
	public void readSamples(){
		try {
			File f = new File(sampleFile);
			if (f.exists()){
				if (f.length() > 10){ 
					InputStream fis = new FileInputStream(sampleFile);
					JsonReader jsonReader =  Json.createReader(fis);	
					JsonArray jsonArray = jsonReader.readArray();

					for (JsonObject jObject : jsonArray.getValuesAs(JsonObject.class)) {
						addSample(new BacterialSample(jObject));
					}
					fis.close();
				}
			}

		} catch (IOException e) {		
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

	}



	/**
	 * @return the samples
	 */
	public ArrayList<BacterialSample> getSamples() {
		return samples;
	}


	public void showSample(){
		System.out.println("======================================================");
		System.out.println("There are " + samples.size() + " samples");
		for (BacterialSample sample:samples){
			System.out.println(sample.getSampleID() + " : (" + sample.getSpecies().getName() + ") " + sample.getDesc());			
		}
		System.out.println("======================================================");
	}
	
	public void status(){
		System.out.println("======================================================");
		System.out.println("There are " + samples.size() + " samples");
		for (BacterialSample sample:samples){
			System.out.println(" Sample " + sample.getSampleID());
			ArrayList<AbstractJob>  jobs = sample.getJobs();
			for (AbstractJob job:jobs){
				System.out.println("  Job " + job.jobName() + ":" + AbstractJob.STATUS_STRING[job.getStatus()]);	
			}
		}
		System.out.println("======================================================");
	}

	public static void init(){
		MM_CONFIG = new MicroManageConfig();
		MM_CONFIG.readSamples();
	}

	public static MicroManageConfig getConfig(){
		return MM_CONFIG;
	}

	/**
	 * To implement later
	 * @param genusName
	 * @return
	 */
	public Genus getGenus(String genusName){
		return genusesDb.get(genusName);		
	}

	/**
	 * To implement later
	 * @param speciesName
	 * @return
	 */
	public Species getSpecies(String speciesName){
		return speciesDb.get(speciesName);		
	}

	/**
	 * To implement later
	 * @param strainName
	 * @return
	 */
	public Strain getStrain(String strainName){
		return null;
	}
}

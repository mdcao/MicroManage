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
package org.micromanage.pipeline.tools;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.micromanage.pipeline.MicroManageConfig;
import org.micromanage.pipeline.sample.BacterialSample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Minh Duc Cao
 * 
 */


public class MicroManageMainCmd{
	private static final Logger LOG = LoggerFactory.getLogger(MicroManageMainCmd.class);
	public MicroManageMainCmd(){
	} 

	static BufferedReader reader = null;
	static MicroManageConfig mmConfig;

	public static void main(String[] args) throws Exception {

			//Initiate the system
		MicroManageConfig.init();
		mmConfig =  MicroManageConfig.getConfig();
		
		//Prepare the menu
		options.add(new CommandOption("list", "List the options", MicroManageMainCmd::list));
		options.add(new CommandOption("status", "Show status of the system", mmConfig::status));
		options.add(new CommandOption("showSample", "Show existing samples", mmConfig::showSample));
		options.add(new CommandOption("sample", "Add new sample", MicroManageMainCmd::addSample));
		options.add(new CommandOption("quit", "Quit the pipeline", MicroManageMainCmd::quit));
	
		//Create and launch thread to handle the pipeline		
		mmConfig.startPipeline();

		//Now I can handle requests about my status
		reader = new BufferedReader(new InputStreamReader(System.in));
		String cmd = "";

		while (mmConfig.getPipeline().isRunning()){
			System.out.print("Print enter a command:\n>");
			cmd = reader.readLine();
			if (cmd == null){//stop every thing
				mmConfig.getPipeline().setRunning(false);
				break;//while
			}			

			cmd = cmd.trim();
			if (cmd.length() == 0) 
				continue;

			boolean found = false;
			for (CommandOption opt:options){
				if (opt.cmd.equals(cmd)){
					opt.function.run();
					found = true;
					break;//for
				}
			}

			if (!found){				
				System.out.println("Command " + cmd + " unknown, type list for a list of commands!");
			}
		}
	}

	static ArrayList<CommandOption> options = new ArrayList<CommandOption>();	

	/**
	 * Define functions to respond to user request
	 */
	public static void list(){
		System.out.println("List of options:");
		for (CommandOption opt:options){
			System.out.printf("%10s: %s.\n", opt.cmd, opt.desc);
		}
	}

	public static void quit(){
		LOG.info("Quitting the system, please wait for shutting the pipeline down!");
		mmConfig.getPipeline().setRunning(false);

		//telling the pipeline I am quitting
		mmConfig.interruptPipeline();
		LOG.info("Notified the pipeline of quitting!");

	}

	public static void addSample(){		 
		try {
			String jSonSample = "";
			while (true){				
				String line = reader.readLine();
				jSonSample += line.trim() + "\n";
				if (line.endsWith(";"))
					break;//while
			}//while			

			JsonReader reader = Json.createReader(new StringReader(jSonSample));
			JsonObject sampleObject = reader.readObject();        
			reader.close();

			if (mmConfig.addSample(new BacterialSample(sampleObject))){
				mmConfig.writeSamples();
				mmConfig.interruptPipeline();
			}
			//if successfully added, wake the pipeline thread up to work

		}catch (Exception e) {
			LOG.error("Error adding sample ####\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static class CommandOption{
		String cmd;
		String desc;
		Runnable function;
		public CommandOption(String cmd, String desc, Runnable function){
			this.cmd = cmd;
			this.desc = desc;
			this.function = function;
		}
	}
}
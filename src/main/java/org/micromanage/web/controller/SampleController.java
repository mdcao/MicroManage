package org.micromanage.web.controller;

import org.micromanage.web.domain.Sample;
import org.micromanage.web.repository.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.inject.Inject;
import java.net.URI;

@RestController
public class SampleController {
	private final Logger LOG = LoggerFactory.getLogger(SampleController.class);

	@Inject
	private SampleRepository sampleRepository;
	
	@RequestMapping(value="/samples", method=RequestMethod.POST)
	public ResponseEntity<?> createSample(@RequestBody Sample sample) {
		LOG.info("receive " + sample);
		sample = sampleRepository.save(sample);
		
		// Set the location header for the newly created resource
		HttpHeaders responseHeaders = new HttpHeaders();
		URI newSampleUri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(sample.getId()).toUri();
		responseHeaders.setLocation(newSampleUri);
		
		return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
	}

	@RequestMapping(value="/samples", method=RequestMethod.GET)
	public ResponseEntity<Iterable<Sample>> getAllSamples() {
		Iterable<Sample> allSamples = sampleRepository.findAll();
		return new ResponseEntity<>(allSamples, HttpStatus.OK);
	}

	@RequestMapping(value="/samples/{sampleId}", method=RequestMethod.GET)
	public ResponseEntity<?> getSample(@PathVariable String sampleId) {
		Sample p = sampleRepository.findOne(sampleId);
		return new ResponseEntity<> (p, HttpStatus.OK);
	}
	
	@RequestMapping(value="/samples/{sampleId}", method=RequestMethod.PUT)
	public ResponseEntity<?> updateSample(@RequestBody Sample sample, @PathVariable String sampleId) {
		// Save the entity
		Sample p = sampleRepository.save(sample);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(value="/samples/{sampleId}", method=RequestMethod.DELETE)
	public ResponseEntity<?> deleteSample(@PathVariable String sampleId) {
		sampleRepository.delete(sampleId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
}
